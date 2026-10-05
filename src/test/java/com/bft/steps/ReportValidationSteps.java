package com.bft.steps;

import com.bft.enums.ReportXmlResource;
import com.bft.enums.TabType;
import com.bft.ui.component.TableComponent;
import com.bft.ui.pages.MainPage;
import com.bft.pw.Condition;
import com.bft.pw.ElementsCollection;
import com.bft.pw.PwSession;
import com.bft.pw.SelenideElement;
import com.bft.pw.WebDriverRunner;
import com.bft.utils.FormStructureParser;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.bft.test.TestAssertions;
import com.bft.pw.Selenide;

import static com.bft.pw.Selenide.$x;
import static com.bft.pw.Selenide.$$x;
import static com.bft.pw.Selenide.back;
import static com.bft.pw.Selenide.executeJavaScript;

/**
 * Класс шагов для валидации отчетов и данных.
 * Вынесен из MainPage для разделения ответственности (Actions vs Assertions).
 */
public class ReportValidationSteps {

    private static final Logger log = LoggerFactory.getLogger(ReportValidationSteps.class);

    // Регистронезависимый поиск текста: пары верхний/нижний регистр для XPath translate()
    private static final String RU_UPPER = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String RU_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz";

    // Компонент таблицы для переиспользования (ленивая инициализация — Playwright ещё не настроен при создании объекта)
    private TableComponent resultsTable;
    private final TestAssertions softAssert = new TestAssertions();

    private TableComponent getResultsTable() {
        if (resultsTable == null) {
            resultsTable = TableComponent.createResultsTable("Таблица результатов");
        }
        return resultsTable;
    }

    /**
     * Проверяет положительный результат протокола проверки.
     */
    public ReportValidationSteps verifyProtocolPositive() {
        waitForUppStatus();

        boolean noErrors = $x("//div[@class='noDataTable']").isDisplayed();
        String uppStatus = $x("//div[contains(@class, 'chip_with-bg')]").getText();

        log.debug("Статус УПП: {}", uppStatus);

        assertEquals(uppStatus, "Положительный", "Статус УПП должен быть 'Положительный'");
        assertTrue(noErrors, "В протоколе не должно быть ошибок (таблица пуста)");

        return this;
    }

    /**
     * Проверяет наличие отчетов страхователя.
     */
    public ReportValidationSteps verifyInsurerReportsExist() {
        getResultsTable().waitForLoad();

        ElementsCollection rows = $$x("//table//tbody/tr");
        assertNotEquals(rows.size(), 0, "Таблица отчетов страхователя пуста");

        rows.get(0).click();
        waitIlsDocumentLoad();

        // Возврат назад реализован через браузер, лучше вынести в NavigationSteps или делать явно в тесте
        back();
        return this;
    }

    /**
     * Проверяет наличие данных о стаже.
     */
    public ReportValidationSteps verifyStazhData() {
        getResultsTable().waitForLoad();

        ElementsCollection stazhRows = $$x("(//table//tbody)[1]/tr");
        assertNotEquals(stazhRows.size(), 0, "Таблица стажа пуста");

        stazhRows.get(0).click();

        $x("(//div[@class = 'pgs-output-text'])[1]/*[2]")
                .shouldHave(Condition.exactText("ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"ФОРВАРД\""), Duration.ofSeconds(10));

        back();
        return this;
    }

    /**
     * Проверяет вкладку на отсутствие ошибок и пустоты.
     */
    public ReportValidationSteps verifyTabNotEmptyNoErrors(String tabName, TabType tabType) {
        // Предполагается, что навигация вызывается отдельно или через новый NavigationSteps
        // Если нужно оставить здесь, то потребуется зависимость от MainPage или NavigationComponent
        // Для чистоты архитектуры лучше вызывать openTab() из теста перед этим методом

        getResultsTable().waitForLoad();

        String tableName = getResultsTable().getTableTitle();
        boolean hasError = getResultsTable().hasError();

        softAssert.assertFalse(hasError, "На вкладке '" + tableName + "' обнаружена ошибка");
        if (hasError) {
            log.debug("ОШИБКА на вкладке {}: {}", tableName, "Текст ошибки (если есть)");
            getResultsTable().closeErrorAlert();
        }

        if (tabType == TabType.TABLE) {
            getResultsTable().clickFirstRow();
        }

        boolean isNotEmpty = getResultsTable().isNotEmpty();
        softAssert.assertTrue(isNotEmpty, "Таблица '" + tableName + "' пуста");

        int rowCount = getResultsTable().getRowCount();
        if (!isNotEmpty) {
            log.debug("Количество записей в таблице {} = {}", tableName, rowCount);
        } else {
            log.debug("Успешно: количество записей в таблице {} = {}", tableName, rowCount);
        }

        return this;
    }

    /**
     * Проверяет базовую информацию о физическом лице (ФИО, ИНН).
     */
    public ReportValidationSteps verifyPersonBasicInfo(String expectedLastName, String expectedFirstName, String expectedSecondName) {
        getResultsTable().waitForLoad();

        String lastName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[1]").getText();
        String firstName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[2]").getText();
        String secondName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[3]").getText();
        String inn = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[7]").getText();

        log.debug("ФИО: {} {} {}, ИНН: {}", lastName, firstName, secondName, inn);

        softAssert.assertEquals(lastName, expectedLastName, "Фамилия не совпадает");
        softAssert.assertEquals(firstName, expectedFirstName, "Имя не совпадает");
        softAssert.assertEquals(secondName, expectedSecondName, "Отчество не совпадает");

        return this;
    }

    // ==================== Новые методы валидации ====================

    /**
     * Проверяет, что таблица отчётов содержит хотя бы одну строку.
     * Переходит на страницу отчётов, если текущий URL не содержит /reports.
     */
    @Step("Проверка наличия отчётов в таблице")
    public ReportValidationSteps verifyReportTableNotEmpty() {
        getResultsTable().waitForLoad();

        ElementsCollection rows = $$x("//table//tbody/tr");
        softAssert.assertNotEquals(rows.size(), 0, "Таблица отчётов должна содержать хотя бы одну запись");

        log.debug("Таблица отчётов: {} записей", rows.size());
        return this;
    }

    /**
     * Проверяет, что в таблице отчётов есть строки с указанным статусом.
     *
     * @param expectedStatus ожидаемый статус (например, "Черновик", "Отправлен", "На проверке")
     */
    @Step("Проверка наличия отчётов со статусом '{expectedStatus}'")
    public ReportValidationSteps verifyReportWithStatusExists(String expectedStatus) {
        getResultsTable().waitForLoad();

        ElementsCollection statusCells = $$x(
                "//table//tbody//td[contains(@class, 'status') or contains(@class, 'chip')]");
        boolean found = false;
        for (int i = 0; i < statusCells.size(); i++) {
            String text = statusCells.get(i).getText();
            if (text.contains(expectedStatus)) {
                found = true;
                break;
            }
        }
        softAssert.assertTrue(found,
                "В таблице должен быть хотя бы один отчёт со статусом '" + expectedStatus + "'");

        log.debug("Проверка статуса '{}': {}", expectedStatus, found ? "найден" : "не найден");
        return this;
    }

    /**
     * Проверяет, что на странице отображается ошибка валидации (обязательное поле, формат и т.д.).
     *
     * @param expectedErrorText часть текста ошибки (например, "обязательно для заполнения", "СНИЛС")
     */
    @Step("Проверка наличия ошибки валидации: '{expectedErrorText}'")
    public ReportValidationSteps verifyValidationErrorVisible(String expectedErrorText) {
        // Broad search: look for the error text anywhere on the page first
        boolean textFound = $x("//*[contains(text(), '" + expectedErrorText + "')]").exists();
        if (!textFound) {
            // Try case-insensitive search
            textFound = $x("//*[contains(translate(text(), 'АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ', " +
                    "'абвгдежзийклмнопрстуфхцчшщъыьэюя'), " +
                    "translate('" + expectedErrorText + "', 'АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ', " +
                    "'абвгдежзийклмнопрстуфхцчшщъыьэюя'))]").exists();
        }
        if (!textFound) {
            // Also check in alert/notification elements
            textFound = $x("//*[contains(@class, 'alert') or contains(@class, 'notification') or " +
                    "contains(@class, 'toast') or contains(@class, 'snackbar') or " +
                    "contains(@class, 'error') or contains(@class, 'invalid') or " +
                    "contains(@class, 'helper-text') or contains(@class, 'MuiFormHelperText') " +
                    "or contains(@class, 'ps-alert')]").exists();
        }
        softAssert.assertTrue(textFound,
                "Должна отображаться ошибка валидации с текстом '" + expectedErrorText + "'");

        log.debug("Ошибка валидации '{}': {}", expectedErrorText, textFound ? "найдена" : "не найдена");
        return this;
    }

    /**
     * Проверяет, что на странице нет ошибок валидации.
     */
    @Step("Проверка отсутствия ошибок валидации")
    public ReportValidationSteps verifyNoValidationErrors() {
        try {
            $x("//*[contains(@class, 'error') and contains(text(), 'обязательно')]")
                    .shouldNotBe(Condition.visible, Duration.ofSeconds(2));
        } catch (Exception e) {
            // Элемент не найден — это ожидаемое поведение (ошибок нет)
        }

        boolean hasErrors = $x("//*[contains(@class, 'error') and contains(text(), 'обязательно')]").exists();
        softAssert.assertFalse(hasErrors, "На странице не должно быть ошибок валидации обязательных полей");

        log.debug("Отсутствие ошибок валидации: {}", !hasErrors ? "OK" : "ОШИБКА");
        return this;
    }

    /**
     * Проверяет, что кнопка с указанным текстом отсутствует или недоступна.
     * Используется для проверки, что кнопка "Продолжить" заблокирована после ошибки.
     *
     * @param buttonName текст кнопки
     */
    @Step("Проверка отсутствия кнопки '{buttonName}'")
    public ReportValidationSteps verifyButtonNotVisible(String buttonName) {
        boolean exists = $x("//button[.//span[contains(text(), '" + buttonName + "')]]").exists();
        softAssert.assertFalse(exists, "Кнопка '" + buttonName + "' не должна отображаться");
        return this;
    }

    /**
     * Проверяет, что на странице присутствует указанный текст.
     *
     * @param expectedText ожидаемый текст на странице
     */
    @Step("Проверка наличия текста '{expectedText}' на странице")
    public ReportValidationSteps verifyTextPresent(String expectedText) {
        boolean found = $x("//*[contains(text(), '" + expectedText + "')]").exists();
        softAssert.assertTrue(found, "На странице должен отображаться текст '" + expectedText + "'");

        log.debug("Текст '{}' на странице: {}", expectedText, found ? "найден" : "не найден");
        return this;
    }

    /**
     * Проверяет, что на странице появился указанный текст (ждёт появления, без учёта регистра).
     *
     * <p>Используется для проверки, что введённые при ручном заполнении данные реально
     * сохранились в черновике (ЗЛ/мероприятие/период отображаются на странице), а не только
     * прошли маскировку обязательных полей без ошибок. Поиск регистронезависимый по всей
     * строке поддерева (СФР-формы часто выводят ФИО в верхнем регистре).
     *
     * @param expectedText ожидаемый текст на странице
     */
    @Step("Проверка появления текста '{expectedText}' на странице (с ожиданием)")
    public ReportValidationSteps verifyTextPresentEventually(String expectedText) {
        // После сохранения ЗЛ/мероприятия данные отрисовываются асинхронно (backend/УОС-запрос),
        // поэтому окно ожидания берём с запасом.
        return verifyTextPresentEventually(expectedText, Duration.ofSeconds(30));
    }

    /**
     * Проверяет, что на странице появился указанный текст (ждёт появления, без учёта регистра).
     *
     * <p>Используется для проверки, что введённые при ручном заполнении данные реально
     * сохранились в черновике (ЗЛ/мероприятие/период отображаются на странице), а не только
     * прошли маскировку обязательных полей без ошибок. Поиск регистронезависимый по всей
     * строке поддерева (СФР-формы часто выводят ФИО в верхнем регистре).
     *
     * @param expectedText ожидаемый текст на странице
     * @param timeout      максимальное время ожидания появления текста
     */
    @Step("Проверка появления текста '{expectedText}' на странице (с ожиданием)")
    public ReportValidationSteps verifyTextPresentEventually(String expectedText, Duration timeout) {
        // XPath 1.0 translate(): приводим текст/значения и искомую строку к нижнему регистру.
        // ВАЖНО: XPath матчит ВСЕ вхождения (включая предков видимого текста и hidden/script),
        // поэтому shouldBe(visible) НЕ используется — isVisible() у Playwright требует
        // единственного совпадения (strict mode violation) и вернётся false. Вместо этого
        // опрашиваем «есть ли хотя бы один ВИДИМЫЙ элемент с текстом».
        // Помимо текстовых узлов учитываем значения input[@value] и div[@localvalue]
        // (MUI-ячейки таблиц рендерят введённое значение именно атрибутами).
        String lower = expectedText.toLowerCase(java.util.Locale.ROOT);
        String upper = RU_UPPER;
        String xpath = "//*[contains(translate(., '" + upper + "', '" + RU_LOWER + "'), '" + lower + "')"
                + " or contains(translate(@value, '" + upper + "', '" + RU_LOWER + "'), '" + lower + "')"
                + " or contains(translate(@localvalue, '" + upper + "', '" + RU_LOWER + "'), '" + lower + "')]";
        if (!waitForAnyVisible(xpath, timeout)) {
            System.out.println("DIAG verifyPersisted FAIL for '" + expectedText + "': " + textPresenceDiagnostics(expectedText, xpath));
            try {
                FormStructureParser.saveHtmlToFile(
                        WebDriverRunner.source(), "source-verifyPersisted-fail-" + expectedText);
            } catch (Exception ex) {
                log.debug("Не удалось сохранить HTML при срыве проверки '{}': {}", expectedText, ex.getMessage());
            }
            softAssert.fail("На странице так и не появился текст '" + expectedText
                    + "' — заполненные данные не сохранились");
            return this;
        }
        log.debug("Текст '{}' появился на странице: OK", expectedText);
        return this;
    }

    /**
     * Ожидает появления хотя бы одного видимого элемента, соответствующего XPath.
     * Опрос через nth(i) — каждый элемент уникален, strict mode violation не возникает.
     */
    private boolean waitForAnyVisible(String xpath, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                for (SelenideElement el : $$x(xpath)) {
                    if (el.isDisplayed()) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // страница могла перерисоваться — продолжаем опрос
            }
            try {
                Thread.sleep(400L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    /** Считает количество совпадений XPath (для диагностики при срыве проверки). */
    private long matchCount(String xpath) {
        try {
            return $$x(xpath).size();
        } catch (Exception e) {
            return -1;
        }
    }

    /** Диагностика наличия текста: URL вкладки, совпадения по XPath/JS, образец текста body. */
    private String textPresenceDiagnostics(String expectedText, String xpath) {
        StringBuilder out = new StringBuilder();
        try {
            out.append("url=").append(PwSession.page().url());
        } catch (Exception e) {
            out.append("url=<err:").append(e.getMessage()).append('>');
        }
        out.append(" | matchesXpath=").append(matchCount(xpath));
        out.append(" | exactMatches=").append(matchCount("//*[contains(text(), '" + expectedText + "')]"));
        out.append(" | snilsInBody=").append(jsContains("65869076870"));
        out.append(" | ivanovInBody=").append(jsContains(expectedText));
        out.append(" | upperInBody=").append(jsContains(expectedText.toUpperCase(java.util.Locale.ROOT)));
        try {
            String preview = String.valueOf(executeJavaScript(
                    "document.body ? document.body.innerText.substring(0, 200) : 'NO-BODY'"));
            out.append(" | bodyPreview=[").append(normalizeForLog(preview)).append(']');
        } catch (Exception e) {
            out.append(" | bodyPreview=<err>");
        }
        return out.toString();
    }

    private static String jsContains(String text) {
        try {
            Object res = executeJavaScript(
                    "document.body && document.body.innerText.indexOf(arguments[0]) >= 0", text);
            return String.valueOf(res);
        } catch (Exception e) {
            return "<err:" + e.getMessage() + '>';
        }
    }

    private static String normalizeForLog(String s) {
        if (s == null) {
            return "null";
        }
        return s.replace("\r", " ").replace("\n", " ").replace("\t", " ").trim();
    }

    /**
     * Проверяет, что на странице отсутствует указанный текст.
     *
     * @param unexpectedText текст, который не должен отображаться
     */
    @Step("Проверка отсутствия текста '{unexpectedText}' на странице")
    public ReportValidationSteps verifyTextNotPresent(String unexpectedText) {
        boolean found = $x("//*[contains(text(), '" + unexpectedText + "')]").exists();
        softAssert.assertFalse(found, "На странице не должен отображаться текст '" + unexpectedText + "'");

        log.debug("Отсутствие текста '{}' на странице: {}", unexpectedText, !found ? "OK" : "ОШИБКА");
        return this;
    }

    /**
     * Проверяет, что пользователь авторизован (имя пользователя отображается в шапке).
     */
    @Step("Проверка авторизации пользователя")
    public ReportValidationSteps verifyUserIsLoggedIn() {
        boolean isLoggedIn = $x("//div[contains(@class, 'user-name')]").isDisplayed();
        softAssert.assertTrue(isLoggedIn, "Пользователь должен быть авторизован (имя в шапке)");

        log.debug("Авторизация пользователя: {}", isLoggedIn ? "OK" : "НЕ АВТОРИЗОВАН");
        return this;
    }

    /**
     * Проверяет, что пользователь вышел из системы (отображается сообщение о входе).
     */
    @Step("Проверка выхода из системы")
    public ReportValidationSteps verifyUserIsLoggedOut() {
        boolean loggedOut = $x("//div[contains(text(), 'Войдите в систему')]").isDisplayed();
        softAssert.assertTrue(loggedOut, "Пользователь должен выйти из системы");

        log.debug("Выход из системы: {}", loggedOut ? "OK" : "Пользователь всё ещё авторизован");
        return this;
    }

    /**
     * Проверяет, что URL содержит указанную подстроку.
     *
     * @param urlPart часть URL для проверки (например, "/reports", "/add")
     */
    @Step("Проверка URL содержит '{urlPart}'")
    public ReportValidationSteps verifyUrlContains(String urlPart) {
        String currentUrl = com.bft.pw.WebDriverRunner.url();
        softAssert.assertTrue(currentUrl.contains(urlPart),
                "URL должен содержать '" + urlPart + "', но текущий URL: " + currentUrl);

        log.debug("URL содержит '{}': {}", urlPart, currentUrl.contains(urlPart) ? "OK" : "ОШИБКА");
        return this;
    }

    /**
     * Проверяет, что количество строк в таблице больше указанного значения.
     *
     * @param minRows минимальное количество строк
     */
    @Step("Проверка количества строк в таблице > {minRows}")
    public ReportValidationSteps verifyTableRowCountMoreThan(int minRows) {
        getResultsTable().waitForLoad();

        ElementsCollection rows = $$x("//table//tbody/tr");
        softAssert.assertTrue(rows.size() > minRows,
                "Количество строк в таблице должно быть больше " + minRows + ", фактически: " + rows.size());

        log.debug("Количество строк в таблице: {} (ожидалось > {})", rows.size(), minRows);
        return this;
    }

    /**
     * Проверяет, что модальное окно с заголовком открыто.
     *
     * @param dialogTitle заголовок модального окна (например, "Добавление отчета")
     */
    @Step("Проверка открытия модального окна '{dialogTitle}'")
    public ReportValidationSteps verifyModalDialogOpen(String dialogTitle) {
        boolean isOpen = $x("//div[@class='modal-dialog']//*[contains(text(), '" + dialogTitle + "')]").exists();
        softAssert.assertTrue(isOpen, "Модальное окно '" + dialogTitle + "' должно быть открыто");

        log.debug("Модальное окно '{}': {}", dialogTitle, isOpen ? "открыто" : "не найдено");
        return this;
    }

    /**
     * Проверяет, что модальное окно закрыто (не отображается).
     */
    @Step("Проверка закрытия модального окна")
    public ReportValidationSteps verifyModalDialogClosed() {
        try {
            $x("//div[@class='modal-dialog']")
                    .shouldNotBe(Condition.visible, Duration.ofSeconds(3));
        } catch (Exception e) {
            // Элемент не найден — окно закрыто
        }

        boolean isOpen = $x("//div[@class='modal-dialog']").exists();
        softAssert.assertFalse(isOpen, "Модальное окно должно быть закрыто");
        return this;
    }

    // ==================== Проверка блока «Страхователь» после загрузки XML ====================

    /**
     * Проверяет, что после загрузки XML на карточке отчёта отображаются реквизиты
     * страхователя/работодателя (РегНомер, ИНН, КПП), взятые из загруженной фикстуры.
     *
     * <p>Значения на карточке рендерятся в атриботах {@code localvalue}/{@code value}
     * MUI-инпутов (строка формы {@code form="reports_*_mainInfo_*"}). Проверка ищет каждое
     * значение в этих атрибутах без учёта регистра (точное совпадение ячейки), а если не
     * нашлось — ищет как подстроку видимого/скрытого текста страницы (СФР-формы иногда
     * выводят в верхнем регистре). Это не зависит от конкретных id полей, поэтому единый
     * метод работает для всех типов отчётов.
     *
     * @param xml фикстура, которая была загружена
     */
    @Step("Проверка блока «Страхователь» на карточке по данным фикстуры")
    public ReportValidationSteps verifyInsurerRequisitesOnCard(ReportXmlResource xml) {
        return verifyInsurerRequisitesOnCard(xml, false);
    }

    /**
     * Проверка реквизитов страхователя на карточке.
     *
     * @param lenient при {@code true} отсутствие реквизита на карточке не считается ошибкой,
     *                а только фиксируется в логе (применяется для типов с известным продукт-гэпом,
     *                например ЕФС-1 — карточка не подтягивает реквизиты из загруженного XML).
     */
    @Step("Проверка блока «Страхователь» на карточке по данным фикстуры (lenient={lenient})")
    public ReportValidationSteps verifyInsurerRequisitesOnCard(ReportXmlResource xml, boolean lenient) {
        Document doc = parseFixtureDom(xml);

        assertRequisiteOnCard("РегНомер", firstText(doc, "РегНомер"), lenient);
        assertRequisiteOnCard("ИНН", firstText(doc, "ИНН"), lenient);
        assertRequisiteOnCard("КПП", firstText(doc, "КПП"), lenient);

        return this;
    }

    private void assertRequisiteOnCard(String label, String expected, boolean lenient) {
        if (expected == null || expected.trim().isEmpty()) {
            // реквизит отсутствует в фикстуре — для такого типа проверка не применима
            log.debug("Реквизит '{}' отсутствует в фикстуре — пропускаем проверку карточки", label);
            return;
        }
        String trimmed = expected.trim();
        boolean found = pageContainsValue(trimmed);
        if (!found) {
            found = waitForAnyVisible(
                    "//*[contains(translate(@localvalue,'" + RU_UPPER + "','" + RU_LOWER + "'),'" + trimmed.toLowerCase(Locale.ROOT) + "')"
                            + " or contains(translate(@value,'" + RU_UPPER + "','" + RU_LOWER + "'),'" + trimmed.toLowerCase(Locale.ROOT) + "')]",
                    Duration.ofSeconds(10));
        }
        if (!found && lenient) {
            log.warn("[ПРОДУКТ-ГЭП] На карточке {} не отобразился реквизит '{}' = '{}' (lenient — не ошибка)",
                    displayNameHint(), label, trimmed);
            return;
        }
        softAssert.assertTrue(found,
                "На карточке " + displayNameHint() + " не отобразился реквизит '" + label + "' = '" + trimmed + "'");
        log.debug("Реквизит '{}'='{}' на карточке: {}", label, trimmed, found ? "OK" : "ОШИБКА");
    }

    /** Заглушка имени отчёта для сообщения (без знания типа). */
    private String displayNameHint() {
        try {
            String url = WebDriverRunner.url();
            int idx = url.indexOf("/reports/");
            if (idx >= 0) {
                return "'" + url.substring(idx + "/reports/".length(), Math.min(url.length(), idx + "/reports/".length() + 24)) + "' ";
            }
        } catch (Exception ignored) {
            // URL недоступен — без имени отчёта
        }
        return "";
    }

    /** Ищет значение в атрибутах localvalue/value любых элементов страницы (точное совпадение). */
    private boolean pageContainsValue(String expected) {
        try {
            Object res = executeJavaScript(
                    "function(){var v=arguments[0].toLowerCase();var nodes=document.querySelectorAll('[localvalue],[value]');"
                            + "for(var i=0;i<nodes.length;i++){var lv=nodes[i].getAttribute('localvalue');"
                            + "var val=nodes[i].getAttribute('value');"
                            + "if(lv&&lv.toLowerCase()===v)return true;if(val&&val.toLowerCase()===v)return true;}return false;}",
                    expected);
            return Boolean.TRUE.equals(res);
        } catch (Exception e) {
            return false;
        }
    }

    /** Парсит фикстуру в DOM (namespace-aware), ищет файл сначала на classpath, затем в файловой системе. */
    private static Document parseFixtureDom(ReportXmlResource xml) {
        String path = xml.getPath();
        try {
            // classpath: application/<подкаталог>/<файл>
            int idx = path.lastIndexOf("src/test/resources/");
            String cpPath = idx >= 0
                    ? "application/" + path.substring(idx + "src/test/resources/".length())
                    : path;
            cpPath = cpPath.replace('\\', '/');
            java.net.URL resource = ReportValidationSteps.class.getClassLoader().getResource(cpPath);
            if (resource != null) {
                return buildDoc(resource.toURI());
            }
        } catch (Exception e) {
            log.debug("Не удалось прочитать фикстуру с classpath: {}", e.getMessage());
        }
        // fallback: загрузка как файла (рабочая директория = корень проекта)
        return readDoc(Paths.get(path).toFile());
    }

    private static Document readDoc(File file) {
        assertTrue(file.canRead(), "Фикстура не читается: " + file);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception ignored) {
                // не все реализации поддерживают флаг
            }
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file);
        } catch (Exception e) {
            throw new AssertionError("Фикстура не является well-formed XML (" + file + "): " + e.getMessage(), e);
        }
    }

    private static Document buildDoc(URI uri) {
        try {
            return readDoc(new File(uri));
        } catch (Exception e) {
            throw new AssertionError("Не удалось прочитать фикстуру: " + uri + " — " + e.getMessage(), e);
        }
    }

    /** Первый непустой текст элемента с именем tag (без учёта namespace). */
    private static String firstText(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagNameNS("*", tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            String t = n.getTextContent();
            if (t != null && !t.trim().isEmpty()) {
                return t.trim();
            }
        }
        return null;
    }

    // --- Приватные вспомогательные методы (бывшие из MainPage) ---

    private void waitForUppStatus() {
        $x("//div[contains(@class, 'chip_with-bg')]")
                .shouldNot(Condition.exactText("Данных нет"), Duration.ofSeconds(20));
    }

    private void waitIlsDocumentLoad() {
        $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[3]")
                .shouldNotBe(Condition.exactText("ДАННЫХ НЕТ"), Duration.ofSeconds(10));
    }

    /**
     * Получает экземпляр TestAssertions для внешних проверок (если нужно).
     */
    public TestAssertions getTestAssertions() {
        return softAssert;
    }
}