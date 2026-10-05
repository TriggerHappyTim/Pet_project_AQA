package com.bft.steps;

import com.bft.config.EnvironmentUtils;
import com.bft.service.graphql.SignServiceImpl;
import com.bft.service.sign.DaPortalClient;
import com.bft.service.sign.SigningHelper;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.Allure;
import com.bft.pw.Condition;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bft.enums.TimeoutConstants.DEFAULT_WAIT;
import static com.bft.enums.TimeoutConstants.LONG_WAIT;
import static com.bft.enums.TimeoutConstants.SHORT_WAIT;
import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$x;

/**
 * Шаги для работы с запросами в архивные организации.
 *
 * <p>Режимы подписания (свойство {@code evs.sign.mode}):
 * <ul>
 *   <li>{@code ui} (по умолчанию) — полное E2E через диалог провайдера и КриптоПРО;</li>
 *   <li>{@code api} — реальное подписание архивного ответа через бэкенд
 *       (ArchivalResponseSignatureService). Фронт при клике «Подписать» выполняет
 *       prepare (initPrepareDoc), а тест по перехваченным из /syncPrepare данным
 *       (itemId/xmlGuid/requestId) скачивает XML из DA, подписывает его (XMLDSIG,
 *       см. {@code SigningHelper}), загружает в DA и завершает process
 *       (processSignatureDone). КриптоПРО-плагин не требуется. ТРЕБУЕТ сертификат
 *       (evs.sign.pem-dir / evs.sign.pfx-path); без него шаг упадёт с понятной ошибкой.</li>
 * </ul>
 */
public class ArchiveSteps {

    private static final Logger log = LoggerFactory.getLogger(ArchiveSteps.class);

    private final SignServiceImpl signService = new SignServiceImpl();

    /** true, если в этом экземпляре шагов подпись уже выполнена через API */
    private boolean apiSignPerformed;

    String getActualResult;

    @Step("Добавление исполнителя")
    public void addPerformer() {
        openArchiveCabinetIfNeeded();
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр исполнителей")
                .waitTableToLoad()
                .clickButton("Добавить");
    }

    /**
     * После авторизации через ЕПГУ открывается посадочная страница «е-услуги»
     * с единственным пунктом навигации «Открыть кабинет ПФ РФ» (ссылка на /archive).
     * Если мы ещё не вошли в кабинет архивной организации — кликаем эту ссылку.
     */
    private void openArchiveCabinetIfNeeded() {
        try {
            var link = $x("//a[contains(@class,'nav-link')][contains(normalize-space(.), 'Открыть кабинет ПФ РФ')]");
            if (link.exists() && link.isDisplayed()) {
                String href = link.getAttribute("href");
                log.info("Открываем кабинет ПФ РФ по ссылке: {}", href);
                com.bft.pw.Selenide.open(href != null && !href.isBlank() ? href : archiveBaseUrl());
                com.bft.test.helpers.SmartWaits.waitForPageLoad(LONG_WAIT);
            } else {
                log.debug("Ссылка 'Открыть кабинет ПФ РФ' не найдена — переходим на кабинет напрямую");
                com.bft.pw.Selenide.open(archiveBaseUrl());
                com.bft.test.helpers.SmartWaits.waitForPageLoad(LONG_WAIT);
            }
        } catch (Exception e) {
            log.debug("Кабинет уже открыт или переход не удался: {}", e.getMessage());
        }
    }

    /** Базовый URL кабинета архивной организации (…/archive/#/). */
    private String archiveBaseUrl() {
        String cur = com.bft.pw.PwSession.url();
        int slash = cur.indexOf("/#/");
        String base = slash > 0 ? cur.substring(0, slash) : cur;
        return base.replaceFirst("/insurer", "") + "/archive/#/";
    }

    @Step("Заполнение исполнителя")
    public void fillPerformer() {
        $("[name=surname]").setValue("Котова");
        $("[name=name]").setValue("Екатерина");
        $("[name=patronymic]").setValue("Олеговна");
        $("[name=phoneNumber]").setValue("+79772684510");
        $("[name=division]").setValue("Тестовое государственное казенное учреждение Тестовой области");
        new MainPage().clickButtonModal("Подтвердить");
    }

    @Step("Переход во вкладку Реестр запросов")
    public void reestrZaprosov() {
        openArchiveCabinetIfNeeded();
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов")
                .openTab("Управление запросами");
    }

    @Step("Поиск исполнителя по имени: {performerName}")
    public void searchPerformer(String performerName) {
        $("#executorFio").setValue(performerName);
        new MainPage().clickMainButton("Применить");
    }

    @Step("Поиск запроса в фильтре и открытие карточки")
    public void searchRequest1(String numberRequest) {
        new MainPage()
                .clickMInputLabel("requestNumber", numberRequest)
                .clickMainButton("Применить")
                .openTable();
    }

    @Step("Назначение исполнителя")
    public void assignPerformer() {
        new MainPage()
                .clickBtnSecondary1("Назначить исполнителя")
                .clickMuiInputLabelID("mui-38", "Слатова Марина Николаевна")
                .clickSpanButton("Подтвердить");
    }

    @Step("Выбор ответа на запрос")
    public void answerRequest() {
        new MainPage()
                .clickRadioButton("Положительный");
    }

    @Step("Заполнение таблицы Период работы, иной деятельности")
    public void addPeriodWork() {
        new MainPage()
                .clickButton("Добавить")
                .inputField("Вид деятельности", "Основной")
                .inputDateLabel("workPeriodStartDate", "01-01-2000")
                .inputDateLabel("workPeriodEndDate", "01-01-2000")
                .clickMuiInputLabel("Кадровое мероприятие", "ПРИЕМ")
                .inputField("Структурное подразделение", "Менеджмент")
                .inputField("Должность", "Менеджер")
                .clickMuiInputLabel("Особый период", "Время простоя по вине работодателя")
                .inputField("Дополнительные сведения", "Отпуск")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("administrativeDocumentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 11")
                .clickButtonModal("Подтвердить");
    }

    @Step("Заполнение таблицы сведений о загранкомандировке")
    public void addBusinessTrip() {
        new MainPage()
                .clickSecondButton("Добавить")
                .inputDateLabel("tripPeriodStartDate", "01-01-2000")
                .inputDateLabel("tripPeriodEndDate", "01-01-2000")
                .clickInputLabel("Наименование организации", "ООО Котовасия")
                .clickMuiInputLabel("Государство пребывания", "АНТАРКТИДА")
                .clickRadioButton("На работу")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("administrativeDocumentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 21")
                .clickButtonModal("Подтвердить");
    }

    @Step("Заполнение полей ответа")
    public void fillAnswer() {
        new MainPage()
                .inputLabelTextarea("responseComment-label", "responseComment", "Тестовый длинный текст тут должен быть")
                .inputLabelTextarea("documentRequisites-label", "documentRequisites", "124")
                .clickInputLabel("Номер ответа", "7-45");
    }

    @Step("Передать запрос на подписание")
    public void submitSigning() {
        // UI-часть выполняется в обоих режимах: клики триггерят /syncPrepare,
        // который перехватывается для API-завершения подписи.
        new MainPage()
                .clickSpanButton("Передать на подписание");
        $x("//span[contains(text(), 'Подписать и отправить')]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickSpanButton("Подписать и отправить");
        $x("//div[@class = 'item-footer']//button[span[text() = 'Подписать']]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickFooterSpanButton("Подписать");

        if (isApiSignMode()) {
            apiSign("submitSigning");
        }
    }

    @Step("Создание запроса")
    public void createZaprosRPU() {
        // На портале portal.test.ecp/rpu сайдбар свёрнут по умолчанию —
        // раскрываем его, иначе пункты реестров недоступны для клика.
        expandSidebarIfNeeded();
        new MainPage()
                .openTab("Реестр получателей услуг")
                .openTab("Реестр запросов в архивы")
                .clickButton("Создать запрос");
    }

    /**
     * Раскрывает боковое меню РПУ, если оно свёрнуто.
     * Кнопка-гамбургер: {@code .header__sidebar-switcher}.
     */
    private void expandSidebarIfNeeded() {
        try {
            var burger = $(".header__sidebar-switcher");
            if (burger.exists()) {
                burger.click();
                $x("//div[contains(@class,'sidebar') and not(contains(@class,'collapsed'))]"
                        + "//*[starts-with(@class,'sidebar__item')]")
                        .shouldBe(Condition.visible, SHORT_WAIT);
                log.info("Сайдбар РПУ раскрыт");
            }
        } catch (Exception e) {
            log.debug("Сайдбар уже раскрыт или кнопка не найдена: {}", e.getMessage());
        }
    }

    @Step("Заполнение блока ОСНОВНЫЕ СВЕДЕНИЯ О ЗАПРОСЕ")
    public void genegalInfo() {
        $x("//input[@type='checkbox']").shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickCheckbox("Муниципальные")
                .clickCheckbox("Региональные")
                .clickMuiInputLabel("Регион", "Ярославская область")
                .clickMuiInputLabel("Архивная организация",
                        "Государственное казенное учреждение Ярославской области «Государственный архив Ярославской области»");
    }

    @Step("Заполнение блока СВЕДЕНИЯ О ГРАЖДАНИНЕ")
    public void infoCitizen() {
        new MainPage()
                .clickInputLabel("ФИО гражданина", "Котов Олег Олегович")
                .clickMInputLabel("snils-label", "351-818-056-74")
                .inputDateLabel("birthDate-label", "01-01-2000");
    }

    @Step("Заполнение блока ДАННЫЕ ОРГАНИЗАЦИИ")
    public void organizationData() {
        new MainPage()
                .clickInputLabel("Наименование организации", "ООО Котовасия");
    }

    @Step("Заполнение блока ОБЩИЙ ПЕРИОД РАБОТЫ")
    public void totalPeriod() {
        new MainPage()
                .inputDateLabel("workStartDate-label", "01-01-2000")
                .inputDateLabel("workEndDate-label", "01-01-2024")
                .inputDateLabel("dismissalDate-label", "01-01-2024");
    }

    @Step("Заполнение блока ВИД ФОРМЫ ЗАНЯТОСТИ")
    public void typeFormEmployment() {
        new MainPage()
                .clickInputLabel("Вид формы занятости", "Основной");
    }

    @Step("Заполнение блока ТЕМА ЗАПРОСА Период работы, иной деятельности")
    public void requestSubjectPeriodWork() {
        new MainPage()
                .clickMuiInputLabel("Тема запроса", "Период работы, иной деятельности");
    }

    @Step("Сохранить запрос")
    public void saveRequest() {
        new MainPage()
                .clickButton("Сохранить");
    }

    @Step("Подписать запрос")
    public void signRequest() {
        // UI-часть до точки подписания — в обоих режимах.
        new MainPage()
                .clickButton("Подписать");
        $x("(//span[contains(text(), 'Подписать')])[2]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickSecondSpanButton("Подписать");

        if (isApiSignMode()) {
            apiSign("signRequest");
        }
    }

    @Step("Выбор сертификата")
    public void selectCertificate() {
        // В API-режиме подпись уже завершена через бэкенд ArchivalResponseSignatureService:
        // диалог провайдера КриптоПРО не открывается — выбирать нечего.
        if (apiSignPerformed) {
            log.info("API-режим: подпись выполнена через SignService, "
                    + "выбор сертификата пропущен");
            return;
        }
        if (isApiSignMode()) {
            // signRequest/submitSigning не вызывались — подписываем сейчас,
            // чтобы последовательность оставалась рабочей в любом порядке.
            apiSign("selectCertificate");
            return;
        }
        new MainPage()
                .chooseLastProvider()
                .clickButtonProviderDialog("Подписать");
        $x("//div[@class = 'provider-dialog']").should(Condition.disappear, LONG_WAIT);
        $x("//div[@class = 'item-footer']//button[text() = 'Готово']")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickFooterButton("Готово");
    }

    // ==================== Подписание: выбор режима ====================

    private boolean isApiSignMode() {
        return "api".equalsIgnoreCase(
                EnvironmentUtils.getPropertyOrEnv("evs.sign.mode", "ui"));
    }

    /**
     * Реальное подписание архивного ответа (режим {@code evs.sign.mode=api}):
     * <ol>
     *   <li>UI доходит до точки подписания (кнопка «Подписать» нажата вызывающим шагом);</li>
     *   <li>фронт выполняет prepare (initPrepareDoc) и вызывает {@code /syncPrepare} —
     *       PwSession перехватывает itemId/xmlGuid/requestId;</li>
     *   <li>тест скачивает XML (xmlGuid) из DA, подписывает его (XMLDSIG, {@link SigningHelper}),
     *       загружает в DA и завершает process через {@link SignServiceImpl}
     *       (ArchivalResponseSignatureService, processSignatureDone) — без КриптоПРО-плагина.</li>
     * </ol>
     * Если данные подготовки не перехвачены — громкое падение с диагностикой.
     */
    private void apiSign(String stepName) {
        com.bft.pw.PwSession.SigningData data =
                com.bft.pw.PwSession.awaitSigningData(java.time.Duration.ofSeconds(20));

        if (data == null || data.itemId == null || data.xmlGuid == null) {
            throw new IllegalStateException(
                    "Не удалось перехватить данные подписания (/syncPrepare) за 20 сек. "
                            + "Убедитесь, что перед этим шагом в UI была нажата кнопка подписания. Шаг: "
                            + stepName);
        }

        // prepare (initPrepareDoc) уже выполнен фронтом. Используем перехваченное:
        //   itemId    — id архивного ответа
        //   xmlGuid   — guid XML-документа для подписи (скачивается из DA)
        //   requestId — из initPrepareDoc (передаётся в processSignatureDone)
        DaPortalClient da = new DaPortalClient();
        byte[] content = da.loadFile(data.xmlGuid);
        String signedRef = SigningHelper.signXmlAndUpload(da, content);
        Allure.addAttachment("signXmlGuid (архив)", "text/plain", signedRef, ".txt");

        String requestId = data.requestId != null ? data.requestId : data.itemId;
        boolean ok = signService.processSignatureArchive(data.itemId, signedRef, requestId);
        if (!ok) {
            throw new IllegalStateException("API-подписание архива не удалось (см. GraphQL "
                    + "Request/Response в отчёте Allure). Шаг: " + stepName);
        }
        apiSignPerformed = true;
    }

    @Step("Копируем номер запроса и отправляем запрос")
    public void get_nomber_send_request() {
        MainPage mainPage = new MainPage();
        mainPage.getNomberRequest();
        getActualResult = mainPage.getActualResult();
        $x("//span[contains(text(), 'Отправить запрос')]")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        mainPage.clickSpanButton("Отправить запрос");
        $x("//body").shouldBe(Condition.visible, SHORT_WAIT);
    }
}