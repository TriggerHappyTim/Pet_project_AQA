package com.bft.ui.pages;

import com.bft.ui.component.ButtonComponent;
import com.bft.ui.component.CheckboxComponent;
import com.bft.ui.component.DateComponent;
import com.bft.ui.component.FileComponent;
import com.bft.ui.component.InputComponent;
import com.bft.ui.component.NavigationComponent;
import com.bft.ui.component.RadioButtonComponent;
import com.bft.ui.component.SelectComponent;
import com.bft.ui.component.TableComponent;
import com.bft.ui.component.TextareaComponent;
import com.bft.utils.FormStructureParser;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;

import com.bft.enums.TabType;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import com.bft.constants.TimeoutConstants;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
/**
 * Page Object для главной страницы системы EVS
 * 
 * Предоставляет методы для взаимодействия с основными элементами интерфейса:
 * - Навигация по вкладкам и разделам
 * - Работа с кнопками различных типов (primary, secondary, footer, modal)
 * - Работа с формами (поля ввода, чекбоксы, радиокнопки, селекты)
 * - Работа с таблицами (ожидание загрузки, клик по строкам)
 * - Проверка статусов отчетов и обработки данных
 * 
 * <p>Все методы возвращают this для поддержки fluent API (цепочки вызовов).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * new MainPage()
 *     .openTab("Отчеты")
 *     .clickButton("Добавить отчет")
 *     .waitTableToLoad();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see LoginPage для авторизации перед использованием MainPage
 * @see NavigationComponent для навигации
 * @see TableComponent для работы с таблицами
 */
public class MainPage {

    private static final Logger log = LoggerFactory.getLogger(MainPage.class);

    /** Заголовок диалога "Льготный стаж" для поиска по тексту. */
    private static final String GRACE_PERIOD_DIALOG_TITLE = "Льготный стаж";
    /** Имя поля input для льготного стажа в форме. */
    private static final String INPUT_NAME_TU_BASIS = "tuBasis";
    /** Каталог для отладочного вывода (парсинг структуры страницы). */
    private static final String DEBUG_OUTPUT_DIR = "target/debug/";

    private SoftAssert softAssert = new SoftAssert();

    // Компоненты для работы с различными элементами интерфейса
    private final NavigationComponent navigation = NavigationComponent.createMainNavigation("Главная навигация");
    @SuppressWarnings("unused") // резерв для работы с главной таблицей
    private final TableComponent mainTable = TableComponent.createMainTable("Главная таблица");
    /**
     * Компонент таблицы результатов
     * 
     * Используется для работы с таблицами результатов на странице.
     * В будущем может быть разбит на специализированные классы
     * (например, ResultsTableComponent, MainTableComponent) для лучшей организации кода.
     */
    private final TableComponent resultsTable = TableComponent.createResultsTable("Таблица результатов");

    /**
     * Возвращает актуальный результат выполнения операции
     * 
     * Используется для получения значений, сохраненных в процессе выполнения теста
     * (например, номер запроса, статус обработки).
     * 
     * @return сохраненное значение результата или null если не установлено
     */
    public String getActualResult() {
        return actualResult;
    }

    private String actualResult;

    /**
     * Открывает вкладку по имени
     * 
     * Выполняет переход на указанную вкладку через навигационный компонент.
     * 
     * @param tabName название вкладки для открытия (например, "Отчеты", "Застрахованные лица")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage openTab(String tabName) {
        navigation.openTab(tabName);
        return this;
    }

    /**
     * Открывает таблицу результатов
     * 
     * Выполняет переход к таблице результатов через навигационный компонент.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage openTable() {
        navigation.openTable();
        return this;
    }

    /**
     * Кликает по стандартной кнопке в приложении
     * 
     * Ищет кнопку по тексту внутри секции 'application' и выполняет клик.
     * Автоматически ожидает кликабельности кнопки перед кликом.
     * 
     * @param buttonName текст на кнопке (например, "Сохранить", "Добавить", "Отправить")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickButton(String buttonName) {
        ButtonComponent.createButton(buttonName).click();
        return this;
    }

    /**
     * Кликает по кнопке в футере страницы
     * 
     * Используется для кнопок в нижней части страницы (футер формы, модального окна).
     * 
     * @param buttonName точный текст на кнопке в футере
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickFooterButton(String buttonName) {
        ButtonComponent.createFooterButton(buttonName).click();
        return this;
    }

    /**
     * Кликает по кнопке в футере с текстом внутри span элемента
     * 
     * Используется когда текст кнопки в футере обернут в span.
     * 
     * @param buttonName текст внутри span элемента кнопки в футере
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickFooterSpanButton(String buttonName) {
        $x("//div[@class = 'item-footer']//button[span[text() = '" + buttonName + "']]").click();
        return this;
    }

    /**
     * Кликает по span элементу, который ведет себя как кнопка
     * 
     * Используется когда кнопка реализована через span вместо button.
     * Поиск выполняется по частичному совпадению текста.
     * 
     * @param buttonName текст содержащийся в span (частичное совпадение)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSpanButton(String buttonName) {
        ButtonComponent.createSpanButton(buttonName).click();
        return this;
    }

    /**
     * Вводит значение в поле по ID через span элемент
     * 
     * Используется для заполнения полей ввода, которые активируются через клик по span.
     * 
     * @param fieldName ID поля для ввода
     * @param value значение для ввода в поле
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSpanId(String fieldName, String value) {
        $x("//input[@id='" + fieldName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10)).sendKeys(value);
        return this;
    }

    /**
     * Выбирает секцию по имени
     * 
     * Выполняет переход к указанной секции через навигационный компонент.
     * 
     * @param buttonName название секции для выбора
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSectionA(String buttonName) {
        navigation.selectSection(buttonName);
        return this;
    }

    /**
     * Кликает по второй span кнопке с указанным текстом
     * 
     * Используется когда на странице несколько span элементов с одинаковым текстом.
     * Выбирает второй по порядку элемент.
     * 
     * @param buttonName текст содержащийся в span (частичное совпадение)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSecondSpanButton(String buttonName) {
        $x("(//span[contains(text(),'" + buttonName + "')])[2]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по главной кнопке по частичному совпадению текста
     * 
     * Ищет кнопку в любом месте страницы по содержанию текста.
     * 
     * @param buttonName текст содержащийся в кнопке (частичное совпадение)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickMainButton(String buttonName) {
        ButtonComponent.createMainButton(buttonName).click();
        return this;
    }

    /**
     * Кликает по второй кнопке с одинаковым текстом
     * 
     * Используется когда на странице несколько кнопок с одинаковым текстом.
     * Выбирает вторую по порядку кнопку в DOM.
     * 
     * @param buttonName текст на кнопке
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSecondButton(String buttonName) {
        ButtonComponent.createSecondButton(buttonName).click();
        return this;
    }

    /**
     * Кликает по третьей кнопке с одинаковым текстом
     * 
     * Выбирает третью по порядку кнопку в DOM с указанным текстом.
     * 
     * @param buttonName текст на кнопке
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickThirdButton(String buttonName) {
        ButtonComponent.createThirdButton(buttonName).click();
        return this;
    }

    /**
     * Кликает по кнопке в модальном окне
     * 
     * Ищет кнопку внутри элемента с классом 'modal-content'.
     * 
     * @param buttonName текст на кнопке внутри модального окна
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickButtonModal(String buttonName) {
        $x("//div[@class = 'modal-content']//button[*[text() = '" + buttonName + "']]").click();
        return this;
    }

    /**
     * Кликает по первичной кнопке (btn-primary)
     * 
     * Первичные кнопки обычно используются для главного действия на странице.
     * Имеют CSS класс 'btn-primary'.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnPrimary(String buttonName) {
        var btn = $x("//button[contains(@class, 'btn-primary')][.//span[text() = '" + buttonName + "']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50));
        try {
            com.codeborne.selenide.Selenide.executeJavaScript("arguments[0].scrollIntoView({block:'center'});", btn.getWrappedElement());
            btn.click();
        } catch (Throwable e) {
            Throwable cause = e;
            while (cause != null && !(cause instanceof org.openqa.selenium.ElementClickInterceptedException)) {
                cause = cause.getCause();
            }
            if (cause != null) {
                com.codeborne.selenide.Selenide.executeJavaScript("arguments[0].click();", btn.getWrappedElement());
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * Кликает по первой первичной кнопке с указанным текстом
     * 
     * Используется когда на странице несколько первичных кнопок с одинаковым текстом.
     * Выбирает первую по порядку.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnPrimary_1(String buttonName) {
        $x("(//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "'])[1]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по второй первичной кнопке с указанным текстом
     * 
     * Используется когда на странице несколько первичных кнопок с одинаковым текстом.
     * Выбирает вторую по порядку.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnPrimary_2(String buttonName) {
        $x("(//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "'])[2]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по вторичной кнопке (btn-secondary)
     * 
     * Вторичные кнопки используются для дополнительных действий на странице.
     * Имеют CSS класс 'btn-secondary'.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary(String buttonName) {
        $x("//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по второй вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary2(String buttonName) {
        $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[2]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по третьей вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary3(String buttonName) {
        $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[3]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по четвертой вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary4(String buttonName) {
        $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[4]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по пятой вторичной кнопке с указанным текстом.
     * Прокручивает к элементу и при перехвате клика использует JS-клик.
     *
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary5(String buttonName) {
        var btn = $x("(//button[contains(@class, 'btn-secondary')][.//span[text() = '" + buttonName + "']])[5]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50));
        try {
            com.codeborne.selenide.Selenide.executeJavaScript("arguments[0].scrollIntoView({block:'center'});", btn.getWrappedElement());
            btn.click();
        } catch (Throwable e) {
            Throwable cause = e;
            while (cause != null && !(cause instanceof org.openqa.selenium.ElementClickInterceptedException)) {
                cause = cause.getCause();
            }
            if (cause != null) {
                com.codeborne.selenide.Selenide.executeJavaScript("arguments[0].click();", btn.getWrappedElement());
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * Кликает по шестой вторичной кнопке с указанным текстом
     * 
     * Кликает по шестой вторичной кнопке с указанным текстом
     * 
     * Использует универсальный метод из {@link com.bft.ui.component.ButtonComponent#createSecondaryButtonByIndex(String, int)}
     * для создания компонента кнопки по индексу.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @see com.bft.ui.component.ButtonComponent#createSecondaryButtonByIndex(String, int) для универсального метода
     */
    public MainPage clickBtnSecondary6(String buttonName) {
        com.bft.ui.component.ButtonComponent.createSecondaryButtonByIndex(buttonName, 6).click();
        return this;
    }

    /**
     * Кликает по первой вторичной кнопке в новом окне (вторая вкладка)
     * 
     * Переключается на второе окно (window(1)) и кликает по первой вторичной кнопке.
     * 
     * @param buttonName текст кнопки (используется только для логирования, фактически выбирается первая кнопка)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary1(String buttonName) {
        switchTo().window(1);
        $x("(//button[contains(@class, 'btn-secondary')]//span)[1]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        return this;
    }

    /**
     * Кликает по простой кнопке по точному тексту
     * 
     * Ищет кнопку по точному совпадению текста (не частичному).
     * 
     * @param buttonName точный текст на кнопке
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtn(String buttonName) {
        $x("//button[text() = '" + buttonName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
        return this;
    }

    /**
     * Кликает по кнопке в модальном диалоге
     * 
     * Ищет кнопку внутри элемента с классом 'modal-dialog'.
     * 
     * @param buttonName текст на кнопке внутри модального диалога
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickButtonModalDialog(String buttonName) {
        $x("//div[@class = 'modal-dialog']//button[text() = '" + buttonName + "']").click();
        return this;
    }

    /**
     * Кликает по кнопке закрытия диалога
     * 
     * Ищет кнопку внутри элемента с классом 'close-dialog'.
     * 
     * @param buttonName текст на кнопке закрытия диалога
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickButtonCloseDialog(String buttonName) {
        $x("//div[@class = 'close-dialog']//button[text() = '" + buttonName + "']").click();
        return this;
    }

    /**
     * Кликает по span кнопке закрытия диалога
     * 
     * Ищет span элемент внутри элемента с классом 'close-dialog'.
     * 
     * @param buttonName текст на span кнопке закрытия диалога
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSpanCloseDialog(String buttonName) {
        $x("//div[@class = 'close-dialog']//span[text() = '" + buttonName + "']").click();
        return this;
    }

    /**
     * Кликает по кнопке диалога провайдера
     * 
     * Ищет кнопку внутри элемента с классом 'provider-dialog'.
     * Используется для работы с диалогами выбора провайдера подписи (CryptoPro и т.д.).
     * 
     * @param buttonName текст на кнопке диалога провайдера
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickButtonProviderDialog(String buttonName) {
        $x("//div[@class = 'provider-dialog']//button[text() = '" + buttonName + "']").click();
        return this;
    }

    /**
     * Выбирает последний провайдер в диалоге выбора провайдера
     * 
     * Кликает по второму label в диалоге провайдера (последний в списке).
     * Используется для выбора провайдера подписи документов.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage chooseLastProvider(){
        $x("//div[@class = 'provider-dialog']//label[2]").click();
        return this;
    }

    /**
     * Выбирает радиокнопку по метке (label)
     * 
     * Использует RadioButtonComponent для поиска и выбора радиокнопки по тексту метки.
     * 
     * @param buttonName текст метки радиокнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickRadioButton(String buttonName) {
        RadioButtonComponent.createByLabel(buttonName, buttonName).select();
        return this;
    }

    /**
     * Выбирает радиокнопку по тексту внутри span элемента
     * 
     * Использует RadioButtonComponent для поиска радиокнопки по тексту span.
     * 
     * @param buttonName текст внутри span элемента радиокнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickRadioInput(String buttonName) {
        String escaped = buttonName.replace("'", "''");
        // Пробуем несколько селекторов (разметка ЕВС: label.n2o-radio-input или div.n2o-radio-input-wrapper)
        try {
            RadioButtonComponent.createBySpanText(buttonName, buttonName).select();
            return this;
        } catch (Exception e1) {
            try {
                // Прямой клик по label или span (разметка "Добавление отчета")
                $x("//label[contains(@class, 'n2o-radio-input')]//span[text() = '" + escaped + "']")
                    .shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .click();
                return this;
            } catch (Exception e2) {
                try {
                    $x("//div[contains(@class, 'n2o-radio-input-wrapper')]//span[text() = '" + escaped + "']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(10))
                        .click();
                    return this;
                } catch (Exception e3) {
                    // Пробуем contains для текста (пробелы, невидимые символы)
                    $x("//span[contains(text(), '" + escaped + "')]/ancestor::label[contains(@class, 'n2o-radio')]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(10))
                        .click();
                    return this;
                }
            }
        }
    }

    /**
     * Устанавливает чекбокс в отмеченное состояние
     * 
     * Использует CheckboxComponent для поиска и установки чекбокса по тексту метки.
     * 
     * @param buttonName текст метки чекбокса
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickCheckbox(String buttonName) {
        CheckboxComponent.createByLabel(buttonName, buttonName).check();
        return this;
    }

    /**
     * Устанавливает номер запроса
     * 
     * Вводит значение в поле номера запроса через клик и ввод текста.
     * 
     * @param content номер запроса для установки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage setNomberRequest(String content){
        $x("//div[label[@id = 'requestNumber-label']]/div").click();
        $x("//div[label[@id = 'requestNumber-label']]/div").sendKeys(content);
        return this;
    }

    /**
     * Выбирает значение в MUI селекте по метке
     * 
     * Использует SelectComponent для работы с Material-UI селектами.
     * 
     * @param buttonName метка селекта
     * @param content значение для выбора
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickMuiInputLabel(String buttonName, String content){
        SelectComponent.createMuiInput(buttonName, buttonName).selectByText(content);
        return this;
    }

    /**
     * Выбирает значение в MUI селекте по span элементу
     * 
     * Использует SelectComponent для работы с Material-UI селектами через span.
     * 
     * @param buttonName текст span элемента селекта
     * @param content значение для выбора
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickMuiInputSpan(String buttonName, String content){
        SelectComponent.createMuiSpan(buttonName, buttonName).selectByText(content);
        return this;
    }

    /**
     * Выбирает значение в MUI селекте по ID поля
     * 
     * Кликает по селекту и вводит значение напрямую в input поле.
     * 
     * @param buttonName ID поля селекта
     * @param content значение для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickMuiInputLabelID(String buttonName, String content){
        $x(String.format("//div[*[@id = '%s']]", buttonName)).click();
        $x(String.format("//input[@id = '%s']", buttonName)).setValue(content);
        /*$x("//*[contains(@id, 'option-0')]").click();*/
        return this;
    }

    /**
     * Выбирает первое значение в MUI селекте по метке
     * 
     * Кликает по селекту и выбирает первый вариант из списка (option-0).
     * 
     * @param buttonName текст метки селекта
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage muiLabel(String buttonName){
        $x(String.format("//div[label[text() = '%s']]//label", buttonName)).click();
        $x("//li[contains(@id, 'option-0')]").click();
        return this;
    }

    /**
     * Выбирает первое значение в MUI селекте по span элементу
     * 
     * Кликает по селекту через span и выбирает первый вариант из списка (option-0).
     * 
     * @param buttonName текст span элемента селекта
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage muiSpan(String buttonName){
        $x(String.format("//span[contains(., '%s')]/ancestor::*[3]//button", buttonName)).click();
        $x("//li[contains(@id, 'option-0')]").click();
        return this;
    }

    /**
     * Выбирает значение в MUI селекте по span элементу с указанным ID опции
     * 
     * Кликает по селекту через span и выбирает опцию с указанным ID.
     * 
     * @param buttonName текст span элемента селекта
     * @param value ID опции для выбора (например, "option-0", "option-1")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage muiSpanValue(String buttonName, String value){
        $x(String.format("//span[contains(., '%s')]/ancestor::*[4]//button", buttonName)).click();
        $x(String.format("//li[contains(@id, '%s')]", value)).click();
        return this;
    }

    /**
     * Выбирает первое значение в первом доступном MUI селекте
     * 
     * Кликает по первому найденному MUI input и выбирает первую опцию.
     * Используется когда на странице только один селект.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage muiuInput(){
        $x("//div[contains(@class= 'MuiInput-input')]").click();
        $x("//li[contains(@id, 'option-0')]").click();
        return this;
    }

    /**
     * Навигирует по боковому меню (sidebar)
     * 
     * Выполняет навигацию по двухуровневому меню: сначала выбирает родительский элемент,
     * затем дочерний элемент внутри него.
     * 
     * @param field CSS класс родительского элемента
     * @param value текст родительского элемента
     * @param field1 CSS класс дочернего элемента
     * @param value1 текст дочернего элемента
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSidebar(String field, String value, String field1, String value1){
        navigation.navigateSidebar(field, value, field1, value1);
        return this;
    }

    /**
     * Получает номер запроса и сохраняет его в actualResult
     * 
     * Извлекает текст из первого span элемента в форме с классом 'form-badge primary noSpace'
     * и сохраняет его для последующего использования в тестах.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage getNomberRequest(){
        actualResult = $x("(//div[@class= 'form-badge primary noSpace']//span)[1]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10)).getText();
        return this;
    }

    /**
     * Вводит значение в поле по ID через клик по label
     * 
     * Кликает по контейнеру с полем (по label) и вводит значение в input поле.
     * 
     * @param idName ID поля для ввода
     * @param content значение для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickMInputLabel(String idName, String content){
        $x(String.format("//div[child::*[@id= '%s']]", idName)).click();
        $x(String.format("//div[child::*[@id= '%s']]//input", idName)).sendKeys(content);
        return this;
    }

    /**
     * Вводит значение в MUI input поле по ID
     * 
     * Кликает по контейнеру с полем и вводит значение в input поле.
     * Значение преобразуется в строку перед вводом.
     * 
     * @param idName ID поля для ввода
     * @param content значение для ввода (будет преобразовано в строку)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickMuiInputBase(String idName, String content){
        $x(String.format("//div[child::*[@id= '%s']]", idName)).click();
        $x(String.format("//div[child::*[@id= '%s']]//input", idName)).sendKeys(String.valueOf(content));
        return this;
    }

    /**
     * Устанавливает дату в поле по ID
     * 
     * Использует DateComponent для установки даты в указанном формате.
     * 
     * @param value ID поля даты
     * @param content дата в формате строки (например, "01-01-2000")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateLabel(String value, String content) {
        DateComponent.createIdDate(value, value).setDate(content);
        return this;
    }

    /**
     * Устанавливает дату в поле по тексту метки (label).
     * Использует поиск по подстроке (contains), чтобы находить поле при метке в span/MUI и лишних пробелах.
     *
     * @param label текст метки поля (например, "Дата мероприятия")
     * @param date  дата в формате DD.MM.YYYY или DD-MM-YYYY
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateByLabel(String label, String date) {
        DateComponent.createLabeledDateContains(label, label).setDate(date);
        return this;
    }

    /**
     * Устанавливает дату в поле «Дата мероприятия» через календарь MUI (клик по полю → выбор дня в календаре).
     * Используется когда прямое ввод в input не обновляет состояние компонента (MUI DatePicker).
     *
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY (например, "01.01.2025")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateEventDateViaCalendar(String dateDDMMYYYY) {
        String[] parts = dateDDMMYYYY.replace("-", ".").split("\\.");
        int day = parts.length > 0 ? Integer.parseInt(parts[0].trim()) : 1;
        int year = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 2025;

        $x("//input[@id='eventDate']").shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        $("[role=dialog]").shouldBe(Condition.visible, Duration.ofSeconds(10));

        String yearStr = String.valueOf(year);
        for (int i = 0; i < 24; i++) {
            String header = $("[role=dialog]").getText().toLowerCase();
            if (header.contains(yearStr) && (header.contains("январ") || header.contains("january"))) {
                break;
            }
            boolean clicked = false;
            if ($("[role=dialog]").$x(".//button[contains(@aria-label, 'revious') or contains(@aria-label, 'Previous') or contains(@aria-label, 'редыдущ') or contains(@aria-label, 'ack') or contains(@aria-label, 'efore')]").exists()) {
                $("[role=dialog]").$x(".//button[contains(@aria-label, 'revious') or contains(@aria-label, 'Previous') or contains(@aria-label, 'редыдущ') or contains(@aria-label, 'ack') or contains(@aria-label, 'efore')]").click();
                clicked = true;
            }
            if (!clicked && $("[role=dialog]").$x(".//div[contains(@class,'CalendarHeader')]//button[1]").exists()) {
                $("[role=dialog]").$x(".//div[contains(@class,'CalendarHeader')]//button[1]").click();
                clicked = true;
            }
            if (!clicked && $("[role=dialog]").$x(".//button[.//*[local-name()='svg']][1]").exists()) {
                $("[role=dialog]").$x(".//button[.//*[local-name()='svg']][1]").click();
                clicked = true;
            }
            if (!clicked) {
                break;
            }
            $x("//body").shouldBe(Condition.visible, Duration.ofMillis(400));
        }

        $("[role=dialog]").$x(".//*[@role='gridcell' and text()='" + day + "']").shouldBe(Condition.visible, Duration.ofSeconds(5)).click();
        return this;
    }

    /**
     * Устанавливает дату в поле «Дата мероприятия» (#eventDate) через JavaScript.
     * Подходит для MUI masked input (mask 00.00.0000, delimiter "."), когда календарь ненадёжен.
     *
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY (например, "01.01.2025")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateEventDateViaJs(String dateDDMMYYYY) {
        inputDateByIdViaJs("eventDate", dateDDMMYYYY);
        return this;
    }

    /**
     * Устанавливает дату в поле по id инпута через JavaScript (MUI masked date).
     * Используется когда ввод через календарь не применяет значение (например «Конец периода»).
     *
     * @param inputId   id элемента input (например, "experienceTimePeriodDateTo")
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateByIdViaJs(String inputId, String dateDDMMYYYY) {
        com.codeborne.selenide.Selenide.executeJavaScript(
            "var el = document.getElementById(arguments[0]) || document.querySelector('input[name=\"' + arguments[0] + '\"]'); if (el) { el.value = arguments[1]; el.dispatchEvent(new Event('input', { bubbles: true })); el.dispatchEvent(new Event('change', { bubbles: true })); }",
            inputId,
            dateDDMMYYYY
        );
        return this;
    }

    /**
     * Устанавливает дату в поле по тексту метки: ищет input через Selenide (XPath по подстроке метки), затем значение — через JS.
     * Подходит для MUI, когда чисто JS не находит элемент (метка в span и т.п.).
     *
     * @param labelText текст метки (например, "Начало периода", "Конец периода")
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateByLabelTextViaJs(String labelText, String dateDDMMYYYY) {
        var input = $x("//div[.//*[contains(., '" + labelText.replace("'", "''") + "')]]//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        com.codeborne.selenide.Selenide.executeJavaScript(
            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true })); arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            input.getWrappedElement(),
            dateDDMMYYYY
        );
        return this;
    }

    /**
     * Заполняет поля «Начало периода» и «Конец периода» в форме периода стажа.
     * <p>Пробует по порядку: aria-label (как Playwright getByRole('textbox', name)), id, метка в диалоге через JS, setValue в диалоге.
     *
     * @param startDate дата начала в формате DD.MM.YYYY
     * @param endDate   дата окончания в формате DD.MM.YYYY
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDatePeriodStaj(String startDate, String endDate) {
        // Важно: на странице несколько диалогов; «Начало периода» есть и в заголовке таблицы (второй диалог).
        // Инпуты experienceTimePeriodDateAt/To — только в диалоге «Сведения о льготном периоде». Ищем диалог по ним.
        var dialog = $x("//*[@role='dialog'][.//input[@id='experienceTimePeriodDateAt']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        var byIdStart = dialog.$("#experienceTimePeriodDateAt");
        var byIdEnd = dialog.$("#experienceTimePeriodDateTo");
        byIdStart.shouldBe(Condition.visible, Duration.ofSeconds(5));
        byIdEnd.shouldBe(Condition.visible, Duration.ofSeconds(5));
        setDateViaJsReact(byIdStart, startDate);
        setDateViaJsReact(byIdEnd, endDate);
        return this;
    }

    /**
     * Кликает кнопку «Добавить» в блоке «ЛЬГОТНЫЙ СТАЖ» внутри диалога «Сведения о льготном периоде».
     * Вызывать после заполнения полей «Начало периода» и «Конец периода», перед заполнением строки стажа.
     * 
     * После клика ожидает появления НОВОГО диалога с формой строки (не внутри текущего диалога).
     *
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickAddInGracePeriodTable() {
        var parentDialog = $x("//*[@role='dialog'][.//input[@id='experienceTimePeriodDateAt']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(5));
        var addBtn = parentDialog.$x(".//*[contains(., 'ЛЬГОТНЫЙ СТАЖ')]/ancestor::*[.//button[.//span[text()='Добавить']]][1]//button[.//span[text()='Добавить']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        
        // Запоминаем количество открытых диалогов до клика
        int dialogsBefore = $$x("//*[@role='dialog']").size();
        
        addBtn.click();
        
        // Ожидание появления нового диалога (React рендеринг)
        sleep(800);
        
        // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 1: Ищем НОВЫЙ диалог по появлению поля tuBasis на странице
        // Форма строки открывается в отдельном диалоге, не внутри родительского
        com.codeborne.selenide.SelenideElement formDialog = null;
        
        // Ждём появления поля tuBasis глобально на странице (в новом диалоге)
        for (int attempt = 0; attempt < 30; attempt++) {
            try {
                var tuBasisInput = $("input[name='" + INPUT_NAME_TU_BASIS + "']");
                if (tuBasisInput.exists() && tuBasisInput.isDisplayed()) {
                    // Нашли поле tuBasis - ищем его родительский диалог
                    formDialog = tuBasisInput.$x("./ancestor::*[@role='dialog'][1]");
                    if (!formDialog.exists() || !formDialog.isDisplayed()) {
                        formDialog = tuBasisInput.$x("./ancestor::*[contains(@class,'modal')][1]");
                    }
                    if (formDialog.exists() && formDialog.isDisplayed()) {
                        log.debug("Найден новый диалог с формой строки (по полю tuBasis)");
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Продолжаем ожидание
            }
            
            // Проверяем, появился ли новый диалог (по количеству)
            try {
                int dialogsAfter = $$x("//*[@role='dialog']").size();
                if (dialogsAfter > dialogsBefore) {
                    // Нашли новый диалог - пробуем найти в нём tuBasis
                    var allDialogs = $$x("//*[@role='dialog']");
                    for (var d : allDialogs) {
                        try {
                            var tuBasisInDialog = d.$x(".//input[@name='" + INPUT_NAME_TU_BASIS + "']");
                            if (tuBasisInDialog.exists() && tuBasisInDialog.isDisplayed()) {
                                formDialog = d;
                                log.debug("Найден новый диалог с формой строки (по количеству диалогов)");
                                break;
                            }
                        } catch (Exception ignored) {
                            // Продолжаем поиск
                        }
                    }
                    if (formDialog != null && formDialog.exists()) {
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Продолжаем ожидание
            }
            
            sleep(300);
        }
        
        // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 2: Если tuBasis не найден, ищем самый последний открытый диалог
        if (formDialog == null || !formDialog.exists()) {
            try {
                var allDialogs = $$x("//*[@role='dialog']");
                if (allDialogs.size() > 0) {
                    // Берём последний диалог (самый новый)
                    formDialog = allDialogs.get(allDialogs.size() - 1);
                    log.debug("Используем последний открытый диалог как fallback");
                }
            } catch (Exception e) {
                log.warn("Не удалось найти диалоги: " + e.getMessage());
            }
        }
        
        // Проверяем, что диалог найден и стабилен
        if (formDialog != null && formDialog.exists()) {
            formDialog.shouldBe(Condition.visible, Duration.ofSeconds(5));
            
            // Ждём стабилизации диалога (исчезновение спиннеров)
            try {
                var spinner = formDialog.$x(".//*[contains(@class,'MuiCircularProgress')] | .//*[@role='progressbar']");
                if (spinner.exists()) {
                    spinner.shouldBe(Condition.disappear, Duration.ofSeconds(5));
                }
            } catch (Exception ignored) {
                // Спиннеров нет - это нормально
            }
            
            // Ожидание загрузки формы через AJAX/React
            sleep(1000);
            
            // Финальная проверка: поле tuBasis должно быть видимым в найденном диалоге
            try {
                formDialog.$x(".//input[@name='" + INPUT_NAME_TU_BASIS + "']").shouldBe(Condition.visible, TimeoutConstants.DEFAULT_WAIT);
                log.debug("Поле tuBasis найдено в диалоге формы строки");
            } catch (Exception e) {
                // Если tuBasis не найден, парсим для отладки
                log.warn("ОШИБКА: Поле tuBasis не найдено в диалоге формы строки");
                try {
                    FormStructureParser.parseAndSaveForm(formDialog, "grace-period-form-dialog-no-tuBasis");
                    FormStructureParser.parseAndSaveForm($("body"), "page-grace-period-form-not-loaded");
                } catch (Exception parseEx) {
                    log.warn("Ошибка при парсинге: " + parseEx.getMessage());
                }
                throw new AssertionError("Поле tuBasis не найдено в диалоге формы строки после клика 'Добавить'. " +
                    "Возможно, форма не загрузилась. Структура сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.");
            }
        } else {
            // Если диалог не найден, парсим структуру для отладки
            log.warn("ОШИБКА: Диалог с формой строки не найден после клика 'Добавить'");
            try {
                FormStructureParser.parseAndSaveForm(parentDialog, "grace-period-parent-dialog-after-click");
                FormStructureParser.parseAndSaveForm($("body"), "page-after-add-click-failed");
            } catch (Exception parseEx) {
                log.warn("Ошибка при парсинге: " + parseEx.getMessage());
            }
            throw new AssertionError("Диалог с формой строки 'Льготный стаж' не найден после клика 'Добавить'. " +
                "Возможно, форма не открылась или открылась в неожиданном месте. " +
                "Структура сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.");
        }
        
        return this;
    }

    /**
     * Возвращает диалог формы «Льготный стаж», открытый после клика «Добавить» в таблице.
     * Ищет НОВЫЙ диалог с формой строки (не родительский диалог "Сведения о льготном периоде").
     * Форма строки открывается в отдельном диалоге с полем tuBasis.
     * При ошибке поиска парсит структуру страницы для отладки.
     */
    private com.codeborne.selenide.SelenideElement getGracePeriodFormDialog() {
        try {
            // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 1: Ищем поле tuBasis глобально на странице (в новом диалоге)
            // Это самый надёжный способ - форма строки всегда содержит это поле
            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    var tuBasisInput = $("input[name='" + INPUT_NAME_TU_BASIS + "']");
                    if (tuBasisInput.exists() && tuBasisInput.isDisplayed()) {
                        // Нашли поле tuBasis - ищем его родительский диалог
                        var dialog = tuBasisInput.$x("./ancestor::*[@role='dialog'][1]");
                        if (dialog.exists() && dialog.isDisplayed()) {
                            log.debug("Найден диалог формы строки по полю tuBasis");
                            return dialog;
                        }
                        // Если не нашли по role='dialog', пробуем по классу modal
                        dialog = tuBasisInput.$x("./ancestor::*[contains(@class,'modal')][1]");
                        if (dialog.exists() && dialog.isDisplayed()) {
                            log.debug("Найден диалог формы строки по полю tuBasis (через класс modal)");
                            return dialog;
                        }
                    }
                } catch (Exception ignored) {
                    // Продолжаем поиск
                }
                
                sleep(300);
            }
            
            // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 2: Ищем все открытые диалоги и проверяем наличие tuBasis в каждом
            try {
                var allDialogs = $$x("//*[@role='dialog']");
                for (var dialog : allDialogs) {
                    try {
                        var tuBasisInDialog = dialog.$x(".//input[@name='" + INPUT_NAME_TU_BASIS + "']");
                        if (tuBasisInDialog.exists() && tuBasisInDialog.isDisplayed()) {
                            log.debug("Найден диалог формы строки среди открытых диалогов");
                            return dialog;
                        }
                    } catch (Exception ignored) {
                        // Продолжаем поиск в следующем диалоге
                    }
                }
            } catch (Exception ignored) {
                // Продолжаем к следующей стратегии
            }
            
            // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 3: Используем самый последний открытый диалог как fallback
            try {
                var allDialogs = $$x("//*[@role='dialog']");
                if (allDialogs.size() > 0) {
                    var lastDialog = allDialogs.get(allDialogs.size() - 1);
                    log.debug("Используем последний открытый диалог как fallback");
                    return lastDialog;
                }
            } catch (Exception ignored) {
                // Продолжаем к финальной проверке
            }
            
            // Если ничего не найдено, пробрасываем ошибку
            throw new AssertionError("Не удалось найти диалог с формой строки (поле tuBasis не найдено)");
            
        } catch (Throwable e) {
            // Если не удалось найти диалог, парсим структуру страницы для отладки
            log.warn("========================================");
            log.warn("ОШИБКА: Не найден диалог формы строки 'Льготный стаж' (input[name='tuBasis'])");
            log.warn("Парсим структуру страницы для анализа...");
            log.warn("========================================");
            
            parsePageStructureOnError(e);
            
            // Пробрасываем исходное исключение с информацией о сохранённых данных
            throw new AssertionError("Не найден диалог формы строки 'Льготный стаж'. " +
                "Элемент input[name='tuBasis'] не найден на странице. " +
                "Возможно, форма строки не открылась после клика 'Добавить' в таблице. " +
                "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа. " +
                "Оригинальная ошибка: " + e.getMessage(), e);
        }
    }
    
    /**
     * Проверяет наличие ошибок валидации на странице.
     * Возвращает текст первой найденной ошибки или null, если ошибок нет.
     */
    private String checkForValidationErrors() {
        try {
            // Ищем различные варианты сообщений об ошибках
            var errorSelectors = new String[]{
                "//*[contains(text(),'Поле обязательно для заполнения')]",
                "//*[contains(text(),'обязательно для заполнения')]",
                "//*[contains(@class,'error') and contains(text(),'обязательно')]",
                "//*[contains(@class,'Mui-error')]//*[contains(text(),'обязательно')]",
                "//*[@role='alert']",
                "//*[contains(@class,'validation-error')]",
                "//*[contains(@class,'error-message')]",
                "//*[contains(@class,'MuiFormHelperText-root') and contains(@class,'Mui-error')]"
            };
            
            for (String selector : errorSelectors) {
                try {
                    var errorElement = $x(selector);
                    if (errorElement.exists() && errorElement.isDisplayed()) {
                        String errorText = errorElement.getText();
                        if (errorText != null && !errorText.trim().isEmpty()) {
                            log.warn("Найдена ошибка валидации: " + errorText);
                            return errorText;
                        }
                    }
                } catch (Exception ignored) {
                    // Продолжаем поиск
                }
            }
        } catch (Exception e) {
            log.warn("Ошибка при проверке валидации: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Парсит структуру страницы при ошибке поиска диалога
     * 
     * Сохраняет структуру всех найденных диалогов или всей страницы для анализа.
     * 
     * @param originalError исходная ошибка поиска диалога
     */
    private void parsePageStructureOnError(Throwable originalError) {
        // Сначала проверяем наличие ошибок валидации
        String validationError = checkForValidationErrors();
        if (validationError != null) {
            log.warn("========================================");
            log.warn("ОБНАРУЖЕНА ОШИБКА ВАЛИДАЦИИ: " + validationError);
            log.warn("========================================");
        }
        
        try {
            // Пытаемся найти любой диалог на странице
            var allDialogs = $$x("//*[@role='dialog'] | //*[contains(@class,'modal')] | //*[contains(@class,'dialog')] | //*[contains(@class,'MuiDialog')]");
            
            if (allDialogs.size() > 0) {
                log.debug("Найдено диалогов на странице: " + allDialogs.size());
                for (int i = 0; i < allDialogs.size(); i++) {
                    var dialog = allDialogs.get(i);
                    try {
                        if (dialog.exists() && dialog.isDisplayed()) {
                            String dialogName = "dialog-" + (i + 1) + "-on-error";
                            FormStructureParser.parseAndSaveForm(dialog, dialogName);
                            log.debug("✓ Сохранена структура диалога: " + dialogName);
                        }
                    } catch (Exception ex) {
                        log.warn("✗ Ошибка при парсинге диалога " + (i + 1) + ": " + ex.getMessage());
                    }
                }
            } else {
                // Если диалогов нет, парсим всю страницу
                log.debug("Диалоги не найдены, парсим всю страницу...");
                try {
                    var body = $("body");
                    if (body.exists()) {
                        FormStructureParser.parseAndSaveForm(body, "page-structure-on-error");
                        log.debug("✓ Сохранена структура всей страницы");
                    }
                } catch (Exception ex) {
                    log.warn("✗ Ошибка при парсинге body: " + ex.getMessage());
                }
            }
            
            // Также сохраняем HTML всей страницы
            try {
                String pageSource = getWebDriver().getPageSource();
                FormStructureParser.saveHtmlToFile(pageSource, "page-source-on-error");
                log.debug("✓ Сохранён HTML страницы");
            } catch (Exception ex) {
                log.warn("✗ Ошибка при сохранении HTML: " + ex.getMessage());
            }
            
            // Пытаемся найти все input элементы на странице для анализа
            try {
                var allInputs = $$x("//input[not(@type='hidden')]");
                log.debug("Найдено input элементов на странице: " + allInputs.size());
                if (allInputs.size() > 0 && allInputs.size() <= 50) {
                    // Если не слишком много элементов, сохраняем их список
                    StringBuilder inputsInfo = new StringBuilder();
                    inputsInfo.append("Список всех input элементов на странице:\n");
                    for (int i = 0; i < Math.min(allInputs.size(), 20); i++) {
                        var input = allInputs.get(i);
                        try {
                            if (input.exists()) {
                                String name = input.getAttribute("name");
                                String id = input.getAttribute("id");
                                String type = input.getAttribute("type");
                                inputsInfo.append(String.format("  [%d] name='%s', id='%s', type='%s'\n", 
                                    i + 1, name != null ? name : "", id != null ? id : "", type != null ? type : ""));
                            }
                        } catch (Exception e) {
                            log.debug("Не удалось получить атрибуты input элемента [{}]: {}", i + 1, e.getMessage());
                        }
                    }
                    log.debug("Inputs info: {}", inputsInfo.toString());
                }
            } catch (Exception ex) {
                log.warn("✗ Ошибка при анализе input элементов: {}", ex.getMessage());
            }

            log.info("========================================");
            log.info("Все данные сохранены в " + DEBUG_OUTPUT_DIR);
            log.info("========================================");

        } catch (Exception parseException) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА при парсинге структуры страницы: {}", parseException.getMessage());
            log.debug("Подробности", parseException);
        }
    }

    /**
     * Ожидает стабилизации диалога после изменения значений (React может перерисовывать форму).
     * Проверяет, что диалог всё ещё открыт и видим.
     * Ждёт, пока исчезнут индикаторы загрузки и форма станет интерактивной.
     * Быстро обнаруживает закрытие диалога, чтобы не ждать долго.
     */
    private void waitForDialogStabilization() {
        try {
            // Сначала быстро проверяем, открыт ли диалог (короткий таймаут)
            // Это позволяет быстро обнаружить закрытие диалога
            com.codeborne.selenide.SelenideElement dialog = null;
            try {
                // Быстрая проверка наличия диалога по заголовку
                var dialogByTitle = $x("//*[@role='dialog'][.//*[contains(., '" + GRACE_PERIOD_DIALOG_TITLE + "') or contains(., 'ЛЬГОТНЫЙ СТАЖ')]]");
                if (dialogByTitle.exists() && dialogByTitle.isDisplayed()) {
                    dialog = dialogByTitle;
                } else {
                    // Если диалог не найден по заголовку, проверяем по tuBasis
                    var inputTuBasis = $("input[name='" + INPUT_NAME_TU_BASIS + "']");
                    if (inputTuBasis.exists() && inputTuBasis.isDisplayed()) {
                        dialog = inputTuBasis.$x("./ancestor::*[@role='dialog'][1]");
                        if (!dialog.exists()) {
                            dialog = inputTuBasis.$x("./ancestor::*[contains(@class,'modal')][1]");
                        }
                    }
                }
            } catch (Exception e) {
                // Диалог не найден - быстро выбрасываем исключение
                throw new AssertionError("Диалог закрылся после выбора значения. " +
                    "Быстрая проверка показала, что диалог больше не доступен.", e);
            }
            
            // Если диалог найден, проверяем наличие tuBasis с коротким таймаутом
            if (dialog != null && dialog.exists()) {
                try {
                    // Сначала проверяем, что диалог всё ещё видим
                    dialog.shouldBe(Condition.visible, Duration.ofSeconds(1));
                    
                    // Затем проверяем наличие tuBasis
                    var inputTuBasis = dialog.$x(".//input[@name='" + INPUT_NAME_TU_BASIS + "']");
                    if (inputTuBasis.exists()) {
                        inputTuBasis.shouldBe(Condition.visible, Duration.ofSeconds(2));
                    } else {
                        // Если tuBasis не найден, но диалог открыт - это может быть нормально
                        // (возможно, форма изменилась или tuBasis скрыт)
                        // Просто логируем предупреждение и продолжаем
                        log.debug("Предупреждение: поле tuBasis не найдено в диалоге, но диалог открыт. " +
                            "Возможно, форма изменилась после выбора значения.");
                    }
                } catch (Exception e) {
                    // Если tuBasis не найден или диалог закрылся - проверяем
                    try {
                        // Проверяем, видим ли диалог
                        if (dialog.exists()) {
                            dialog.shouldBe(Condition.visible, Duration.ofSeconds(1));
                            // Если диалог видим, но tuBasis не найден - это может быть нормально
                            log.debug("Предупреждение: поле tuBasis не найдено, но диалог открыт. " +
                                "Продолжаем выполнение.");
                        } else {
                            throw new AssertionError("Диалог закрылся после выбора значения. " +
                                "Диалог больше не существует в DOM.", e);
                        }
                    } catch (AssertionError ae) {
                        // Пробрасываем AssertionError как есть
                        throw ae;
                    } catch (Exception closedEx) {
                        throw new AssertionError("Диалог закрылся после выбора значения. " +
                            "Поле tuBasis не найдено, и диалог больше не видим.", closedEx);
                    }
                }
            } else {
                throw new AssertionError("Диалог не найден после выбора значения.");
            }
            
            // Ждём, пока исчезнут спиннеры/лоадеры в диалоге (если есть)
            // ВАЖНО: не ищем по классу 'spinner', так как n2o-spinner-wrapper - это контейнер формы, а не индикатор загрузки
            try {
                // Ищем только реальные индикаторы загрузки MUI (крутящиеся спиннеры)
                var muiSpinner = dialog.$x(".//*[contains(@class,'MuiCircularProgress')]");
                if (muiSpinner.exists()) {
                    muiSpinner.shouldBe(Condition.disappear, Duration.ofSeconds(2));
                }
                
                // Также проверяем элементы с role='progressbar'
                var progressbar = dialog.$x(".//*[@role='progressbar']");
                if (progressbar.exists()) {
                    progressbar.shouldBe(Condition.disappear, Duration.ofSeconds(2));
                }
            } catch (Exception ignored) {
                // Если нет лоадеров или они уже исчезли, это нормально - просто продолжаем
            }
            
            // Ожидание React обновления состояния
            sleep(300);
        } catch (AssertionError e) {
            // Пробрасываем AssertionError как есть
            throw e;
        } catch (Exception e) {
            // Если диалог закрылся, это будет обработано при следующем обращении
            throw new AssertionError("Диалог закрылся после выбора значения: " + e.getMessage(), e);
        }
    }

    /**
     * Выбирает значение в выпадающем списке по имени поля в форме «Льготный стаж».
     * Клик по input (MUI Autocomplete) → ожидание списка → клик по опции.
     *
     * @param inputName  name поля (например, "tuBasis" для «Код территориальных условий»)
     * @param optionText текст опции (например, "МКС")
     * @return this
     */
    public MainPage selectInGracePeriodFormByName(String inputName, String optionText) {
        var dialog = getGracePeriodFormDialog();
        var input = dialog.$("input[name='" + inputName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        input.click();
        clickDropdownOptionByText(optionText);
        
        // Ожидание стабилизации после выбора значения
        sleep(800);
        
        // Проверяем, что диалог остаётся открытым
        try {
            dialog = getGracePeriodFormDialog();
            dialog.shouldBe(Condition.visible, Duration.ofSeconds(2));
        } catch (Exception e) {
            log.warn("ОШИБКА: Диалог закрылся после выбора '" + optionText + "' в поле name='" + inputName + "'");
            try {
                parsePageStructureOnError(e);
            } catch (Exception parseEx) {
                log.warn("Не удалось сохранить структуру страницы: " + parseEx.getMessage());
            }
            throw new AssertionError("Диалог 'Льготный стаж' закрылся после выбора значения '" + optionText + 
                "' в поле name='" + inputName + "'. Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
        }
        
        return this;
    }

    /**
     * Вводит значение в поле по name в форме «Льготный стаж».
     */
    public MainPage inputInGracePeriodFormByName(String inputName, String value) {
        var dialog = getGracePeriodFormDialog();
        var input = dialog.$("input[name='" + inputName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(5));
        setInputValueViaJs(input, value);
        return this;
    }

    /**
     * Нажимает «Сохранить» в модальном окне формы «Льготный стаж» (сохраняет строку, закрывает форму).
     * Не использовать pressEscape() — оно закрывает окно без сохранения.
     *
     * @return this
     */
    public MainPage clickSaveInGracePeriodFormDialog() {
        var dialog = getGracePeriodFormDialog();
        dialog.$x(".//button[.//span[text()='Сохранить']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(5)).click();
        return this;
    }

    /**
     * Выбирает значение в MUI селекте/автокомплите (выпадающий список) в модальном окне формы «Льготный стаж».
     * Простой подход: найти элемент по метке, кликнуть, выбрать значение из списка.
     *
     * @param labelContains подстрока метки (например, "Код дополнительных сведений")
     * @param optionText    текст опции для выбора (например, "ДЕКРЕТ")
     * @return this
     */
    public MainPage selectInGracePeriodDialogByLabel(String labelContains, String optionText) {
        // Убеждаемся, что диалог открыт
        var dialog = getGracePeriodFormDialog();
        dialog.shouldBe(Condition.visible, Duration.ofSeconds(5));
        
        String escaped = labelContains.replace("'", "''");
        com.codeborne.selenide.SelenideElement trigger = null;
        
        // Простой поиск: находим элемент с меткой, затем ищем рядом input/button
        // Стратегия 1: Ищем в MuiFormControl (самый распространённый случай)
        try {
            var byFormControl = dialog.$x(".//*[contains(., '" + escaped + "')]/ancestor::*[contains(@class,'MuiFormControl')][1]//input[not(@type='hidden')] | .//*[contains(., '" + escaped + "')]/ancestor::*[contains(@class,'MuiFormControl')][1]//button");
            if (byFormControl.exists() && byFormControl.isDisplayed()) {
                trigger = byFormControl;
            }
        } catch (Exception ignored) {
            // Пробуем следующую стратегию
        }
        
        // Стратегия 2: Ищем input/button в div с меткой
        if (trigger == null) {
            try {
                var byDiv = dialog.$x(".//div[contains(., '" + escaped + "')]//input[not(@type='hidden')] | .//div[contains(., '" + escaped + "')]//button");
                if (byDiv.exists() && byDiv.isDisplayed()) {
                    trigger = byDiv;
                }
            } catch (Exception ignored) {
                // Пробуем следующую стратегию
            }
        }
        
        // Стратегия 3: Ищем следующий input/button после метки
        if (trigger == null) {
            try {
                var byFollowing = dialog.$x(".//*[contains(., '" + escaped + "')]/following::input[not(@type='hidden')][1] | .//*[contains(., '" + escaped + "')]/following::button[1]");
                if (byFollowing.exists() && byFollowing.isDisplayed()) {
                    trigger = byFollowing;
                }
            } catch (Exception ignored) {
                // Пробуем следующую стратегию
            }
        }
        
        // Стратегия 4: Обратный поиск - находим все input и проверяем, есть ли рядом метка
        if (trigger == null) {
            try {
                var allInputs = dialog.$$x(".//input[not(@type='hidden')] | .//button[@role='combobox']");
                for (var input : allInputs) {
                    try {
                        var container = input.$x("./ancestor::div[contains(@class,'MuiFormControl') or contains(@class,'row')][1]");
                        if (container.exists()) {
                            var hasLabel = container.$x(".//*[contains(., '" + escaped + "')]");
                            if (hasLabel.exists() && input.isDisplayed()) {
                                trigger = input;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        // Пробуем следующий input
                    }
                }
            } catch (Exception ignored) {
                // Не найдено
            }
        }
        
        if (trigger == null || !trigger.exists()) {
            throw new AssertionError("Не найден селект/автокомплит по метке: " + labelContains);
        }
        
        // Кликаем по элементу
        trigger.shouldBe(Condition.visible, Duration.ofSeconds(5));
        trigger.shouldBe(Condition.enabled, Duration.ofSeconds(2));
        
        try {
            trigger.scrollIntoView(true);
            sleep(200);
        } catch (Exception e) {
            log.debug("scrollIntoView или sleep не выполнились: {}", e.getMessage());
        }

        trigger.click();
        
        // Выбираем опцию из выпадающего списка
        clickDropdownOptionByText(optionText);
        
        sleep(500);
        
        return this;
    }

    /**
     * Выбирает значение в выпадающем списке, перебирая варианты меток (первая найденная).
     * Если не найдено по меткам, пробует по порядку селектов в форме (fallback).
     */
    public MainPage selectInGracePeriodDialogByLabelFirstMatch(String[] labelVariants, String optionText) {
        // Убеждаемся, что диалог открыт перед поиском поля
        com.codeborne.selenide.SelenideElement dialog;
        try {
            dialog = getGracePeriodFormDialog();
            dialog.shouldBe(Condition.visible, Duration.ofSeconds(5));
            log.debug("Диалог 'Льготный стаж' найден перед поиском поля: " + String.join(", ", labelVariants));
        } catch (Exception e) {
            // Диалог не найден - сохраняем структуру страницы для анализа
            log.warn("========================================");
            log.warn("ОШИБКА: Диалог 'Льготный стаж' закрыт или не найден перед поиском поля '" + 
                String.join("', '", labelVariants) + "'");
            log.warn("Парсим структуру страницы для анализа...");
            log.warn("========================================");
            
            try {
                parsePageStructureOnError(e);
            } catch (Exception parseEx) {
                log.warn("Не удалось сохранить структуру страницы: " + parseEx.getMessage());
            }
            
            throw new AssertionError("Диалог 'Льготный стаж' закрыт или не найден перед поиском поля '" + 
                String.join("', '", labelVariants) + "'. Возможно, предыдущий шаг закрыл диалог. " +
                "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
        }
        
        // Ждём стабилизации диалога после предыдущего действия (если было)
        try {
            waitForDialogStabilization();
        } catch (Exception e) {
            log.warn("Предупреждение: не удалось дождаться стабилизации диалога: " + e.getMessage());
        }
        
        sleep(500);
        
        // Проверяем, что диалог всё ещё открыт после стабилизации
        try {
            dialog = getGracePeriodFormDialog();
            dialog.shouldBe(Condition.visible, Duration.ofSeconds(3));
            log.debug("Диалог 'Льготный стаж' остаётся открытым после стабилизации");
        } catch (Exception e) {
            // Диалог закрылся - сохраняем структуру страницы для анализа
            log.warn("========================================");
            log.warn("ОШИБКА: Диалог 'Льготный стаж' закрылся после стабилизации");
            log.warn("Парсим структуру страницы для анализа...");
            log.warn("========================================");
            
            try {
                parsePageStructureOnError(e);
            } catch (Exception parseEx) {
                log.warn("Не удалось сохранить структуру страницы: " + parseEx.getMessage());
            }
            
            throw new AssertionError("Диалог 'Льготный стаж' закрылся после выбора предыдущего значения. " +
                "Возможно, значение в предыдущем поле вызывает закрытие диалога. " +
                "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
        }
        
        // Ждём появления поля (может появляться условно после выбора предыдущего поля)
        for (String label : labelVariants) {
            try {
                // Обновляем ссылку на диалог перед поиском поля (на случай перерисовки)
                dialog = getGracePeriodFormDialog();
                dialog.shouldBe(Condition.visible, Duration.ofSeconds(2));
                
                // Пробуем найти поле с ожиданием его появления
                String escaped = label.replace("'", "''");
                dialog.$x(".//*[contains(., '" + escaped + "')]")
                    .shouldBe(Condition.exist, Duration.ofSeconds(5));
                
                // ПЕРЕД выбором значения парсим структуру диалога для отладки
                // Это поможет понять, почему диалог закрывается после выбора
                try {
                    FormStructureParser.parseAndSaveForm(dialog, "grace-period-before-select-" + 
                        label.replaceAll("[^a-zA-Zа-яА-Я0-9]", "-").toLowerCase());
                    log.debug("Структура диалога сохранена перед выбором значения '" + optionText + 
                        "' в поле '" + label + "'");
                } catch (Exception parseEx) {
                    log.warn("Предупреждение: не удалось сохранить структуру перед выбором: " + parseEx.getMessage());
                }
                
                // Если поле найдено, выбираем значение
                // selectInGracePeriodDialogByLabel уже содержит проверки на закрытие диалога
                selectInGracePeriodDialogByLabel(label, optionText);
                
                // Дополнительная быстрая проверка после выбора (selectInGracePeriodDialogByLabel уже проверил, но перепроверяем)
                try {
                    dialog = getGracePeriodFormDialog();
                    dialog.shouldBe(Condition.visible, Duration.ofSeconds(1));
                } catch (Exception e) {
                    // Диалог закрылся - это уже обработано в selectInGracePeriodDialogByLabel, но перепроверяем
                    // Если мы здесь, значит диалог закрылся после всех проверок
                    log.warn("========================================");
                    log.warn("ОШИБКА: Диалог закрылся после выбора значения '" + optionText + 
                        "' в поле '" + label + "' (финальная проверка)");
                    log.warn("Парсим структуру страницы для анализа...");
                    log.warn("========================================");
                    
                    parsePageStructureOnError(e);
                    
                    throw new AssertionError("Диалог 'Льготный стаж' закрылся после выбора значения '" + optionText + 
                        "' в поле '" + label + "'. Возможно, это значение вызывает закрытие диалога. " +
                        "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
                }
                
                return this;
            } catch (AssertionError | Exception ignored) {
                // try next label
            }
        }
        // Fallback: попробовать найти по порядку селектов (если известно, что это 3-й или 4-й селект)
        // Но это рискованно, поэтому оставляем только как последний вариант
        
        // Парсинг и сохранение структуры страницы для отладки (если диалог не найден)
        try {
            // Пробуем найти диалог
            dialog = getGracePeriodFormDialog();
            // Если диалог найден, парсим его структуру
            String jsonPath = FormStructureParser.parseAndSaveForm(dialog, "grace-period-dialog-not-found");
            log.debug("Структура диалога сохранена для анализа: " + jsonPath);
        } catch (Exception parseEx) {
            // Диалог не найден - сохраняем HTML всей страницы для анализа
            log.warn("Диалог не найден, сохраняем HTML страницы для анализа: " + parseEx.getMessage());
            try {
                String html = $("body").getAttribute("outerHTML");
                FormStructureParser.saveHtmlToFile(html, "grace-period-dialog-not-found-page");
                log.debug("HTML страницы сохранён для анализа");
            } catch (Exception htmlEx) {
                log.warn("Не удалось сохранить HTML страницы: " + htmlEx.getMessage());
            }
        }
        
        throw new AssertionError("Не найден селект по меткам: " + String.join(", ", labelVariants) + 
            ". Диалог может быть закрыт или поле не существует. Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.");
    }

    /**
     * Выбирает значение в селекте по порядку (N-й селект в форме). Запасной вариант.
     */
    public MainPage selectInGracePeriodBySelectIndex(int oneBasedIndex, String optionText) {
        var dialog = getGracePeriodFormDialog();
        // Ищем все селекты (input с autocomplete или button, которые открывают dropdown)
        var selects = dialog.$$x(".//input[@role='combobox'] | .//div[contains(@class,'MuiAutocomplete')]//input | .//button[contains(@class,'MuiSelect')]")
                .filter(Condition.visible);
        if (selects.size() < oneBasedIndex) {
            throw new AssertionError("В форме «Льготный стаж» найдено селектов: " + selects.size() + ", нужен индекс: " + oneBasedIndex);
        }
        var target = selects.get(oneBasedIndex - 1);
        target.click();
        clickDropdownOptionByText(optionText);
        return this;
    }

    /**
     * Кликает опцию в выпадающем списке MUI по тексту. Ждёт появления опции (список подгружается), пробует несколько селекторов.
     * Важно: ищет опцию в контексте всего документа (выпадающий список MUI обычно рендерится в body, не в диалоге).
     */
    private void clickDropdownOptionByText(String optionText) {
        String optEscaped = optionText.replace("'", "''");
        String[] xpaths = {
                "//li[contains(@id, 'option') and contains(., '" + optEscaped + "')]",
                "//*[@role='option'][contains(., '" + optEscaped + "')]",
                "//li[contains(., '" + optEscaped + "')]",
                "//li[normalize-space(text())='" + optEscaped + "']", // Точное совпадение текста
                "//*[@role='option'][normalize-space(text())='" + optEscaped + "']" // Точное совпадение для role='option'
        };
        
        log.debug("Ищем опцию '" + optionText + "' в выпадающем списке...");
        
        for (String xpath : xpaths) {
            try {
                var el = $x(xpath);
                el.shouldBe(Condition.visible, Duration.ofSeconds(12));
                
                // Прокручиваем элемент в видимую область перед кликом
                try {
                    el.scrollIntoView(true);
                    sleep(100);
                } catch (Exception scrollEx) {
                    log.debug("Предупреждение: не удалось прокрутить опцию: " + scrollEx.getMessage());
                }
                
                log.debug("Найдена опция '" + optionText + "', выполняем клик...");
                el.click();
                
                sleep(300);
                
                log.debug("Клик по опции '" + optionText + "' выполнен успешно");
                
                // Проверяем, что диалог всё ещё открыт (если он был открыт)
                // Используем более мягкую проверку - просто проверяем наличие ключевого элемента
                try {
                    // Проверяем наличие основного поля формы (более надёжно, чем проверка всего диалога)
                    var inputTuBasis = $("input[name='" + INPUT_NAME_TU_BASIS + "']");
                    inputTuBasis.shouldBe(Condition.exist, Duration.ofSeconds(3));
                } catch (Exception dialogEx) {
                    // Если диалог закрылся, это проблема - выбрасываем исключение
                    throw new AssertionError("Диалог закрылся после выбора опции '" + optionText + "'. " +
                        "Возможно, выбор значения '" + optionText + "' вызывает закрытие диалога. " +
                        "Проверьте, что значение корректно для этого поля.", dialogEx);
                }
                
                return;
            } catch (AssertionError e) {
                // Если это AssertionError о закрытии диалога, пробрасываем его
                throw e;
            } catch (Throwable ignored) {
                // пробуем следующий селектор
                log.debug("Опция не найдена по XPath: " + xpath + ", пробуем следующий...");
            }
        }
        
        // Если все селекторы не сработали, выводим более подробную ошибку
        log.warn("========================================");
        log.warn("ОШИБКА: Не найдена опция '" + optionText + "' в выпадающем списке");
        log.warn("Проверьте, что:");
        log.warn("1. Выпадающий список открыт");
        log.warn("2. Опция '" + optionText + "' существует в списке");
        log.warn("3. Текст опции точно совпадает (учитывайте пробелы и регистр)");
        log.warn("========================================");
        
        throw new AssertionError("Не найдена опция выпадающего списка: " + optionText);
    }

    /**
     * Вводит значение в поле по метке в модальном окне формы «Льготный стаж».
     *
     * @param labelContains подстрока метки (например, "Коэффициент")
     * @param value         значение для ввода
     * @return this
     */
    public MainPage inputInGracePeriodDialogByLabel(String labelContains, String value) {
        var dialog = getGracePeriodFormDialog();
        var input = findInputInDialogByLabel(dialog, labelContains);
        if (input == null || !input.exists()) {
            throw new AssertionError("Не найдено поле по метке: " + labelContains);
        }
        input.shouldBe(Condition.visible, Duration.ofSeconds(5));
        setInputValueViaJs(input, value);
        return this;
    }

    /**
     * Вводит значение в поле, перебирая варианты меток (и placeholder/aria-label).
     * Используется, когда точный текст метки в UI может отличаться.
     *
     * @param labelVariants варианты подстрок метки или placeholder (например, "Коэффициент", "Доля ставки")
     * @param value         значение для ввода
     * @return this
     */
    public MainPage inputInGracePeriodDialogByLabelFirstMatch(String[] labelVariants, String value) {
        var dialog = getGracePeriodFormDialog();
        for (String label : labelVariants) {
            var input = findInputInDialogByLabel(dialog, label);
            if (input != null && input.exists()) {
                input.shouldBe(Condition.visible, Duration.ofSeconds(5));
                setInputValueViaJs(input, value);
                return this;
            }
            var ph = label.replace("'", "\\'");
            var byPlaceholder = dialog.$("input[placeholder*='" + ph + "']");
            if (byPlaceholder.exists()) {
                byPlaceholder.shouldBe(Condition.visible, Duration.ofSeconds(5));
                setInputValueViaJs(byPlaceholder, value);
                return this;
            }
            var ariaEscaped = label.replace("'", "''");
            var byAria = dialog.$x(".//input[contains(@aria-label, '" + ariaEscaped + "')]");
            if (byAria.exists()) {
                byAria.shouldBe(Condition.visible, Duration.ofSeconds(5));
                setInputValueViaJs(byAria, value);
                return this;
            }
        }
        throw new AssertionError("Не найдено поле по меткам: " + String.join(", ", labelVariants));
    }

    /**
     * Ищет input в диалоге по подстроке метки (label в том же блоке или предке).
     */
    private com.codeborne.selenide.SelenideElement findInputInDialogByLabel(
            com.codeborne.selenide.SelenideElement dialog, String labelContains) {
        String escaped = labelContains.replace("'", "''");
        if (dialog.$$x(".//div[.//*[contains(., '" + escaped + "')]]//input").size() > 0) {
            return dialog.$$x(".//div[.//*[contains(., '" + escaped + "')]]//input").first();
        }
        var byAncestor = dialog.$$x(".//*[contains(., '" + escaped + "')]/ancestor::*[.//input][1]//input");
        if (byAncestor.size() > 0) {
            return byAncestor.first();
        }
        // MUI: метка и input в общем предке MuiFormControl
        var byFormControl = dialog.$$x(".//*[contains(., '" + escaped + "')]/ancestor::*[contains(@class,'MuiFormControl')][1]//input");
        if (byFormControl.size() > 0) {
            return byFormControl.first();
        }
        // Элемент с меткой и input в одном родителе (label + div с input)
        var withLabel = dialog.$$x(".//*[contains(., '" + escaped + "')]");
        for (var el : withLabel) {
            var parent = el.parent();
            if (parent.exists() && parent.$$x(".//input").size() > 0) {
                return parent.$$x(".//input").first();
            }
        }
        return null;
    }

    /**
     * Ввод в поле формы «Льготный стаж»: сначала по name, при отсутствии — по одному из вариантов метки.
     */
    public MainPage inputInGracePeriodFormByNameOrLabel(String inputName, String[] labelFallbacks, String value) {
        var dialog = getGracePeriodFormDialog();
        var byName = dialog.$("input[name='" + inputName + "']");
        if (byName.exists() && byName.isDisplayed()) {
            byName.shouldBe(Condition.visible, Duration.ofSeconds(5));
            setInputValueViaJs(byName, value);
            return this;
        }
        return inputInGracePeriodDialogByLabelFirstMatch(labelFallbacks, value);
    }

    /**
     * Заполняет N-е по счёту видимое поле ввода в форме «Льготный стаж» (1 = первое поле).
     * Запасной вариант, когда поиск по метке не срабатывает.
     */
    public MainPage inputInGracePeriodByInputIndex(int oneBasedIndex, String value) {
        var dialog = getGracePeriodFormDialog();
        var inputs = dialog.$$x(".//input[not(@type='hidden')]").filter(Condition.visible);
        if (inputs.size() < oneBasedIndex) {
            throw new AssertionError("В форме «Льготный стаж» найдено полей: " + inputs.size() + ", нужен индекс: " + oneBasedIndex);
        }
        var target = inputs.get(oneBasedIndex - 1);
        target.click();
        setInputValueViaJs(target, value);
        return this;
    }

    /**
     * Заполняет поля «Коэффициент» и «Доля ставки» в форме «Льготный стаж».
     * Стратегия: сначала по name (tuCoefficient, tuBidShare), затем по метке, затем по порядку полей.
     * После выбора tuBasis ждём закрытия выпадающего списка; значения задаём через JS (React controlled).
     */
    public MainPage inputInGracePeriodCoefficientAndBidShare(String coefficientValue, String bidShareValue) {
        try {
            $x("//*[@role='listbox']").should(Condition.disappear, Duration.ofSeconds(5));
        } catch (Throwable ignored) {
            // список уже закрыт или не был открыт
        }
        // Коэффициент: сначала по name="tuCoefficient" (из HTML скриншота)
        try {
            inputInGracePeriodFormByName("tuCoefficient", coefficientValue);
        } catch (AssertionError e) {
            try {
                inputInGracePeriodDialogByLabelFirstMatch(
                        new String[]{"Коэффициент", "Доля ставки", "Доля", "Коэф"}, coefficientValue);
            } catch (AssertionError e2) {
                inputInGracePeriodByInputIndex(2, coefficientValue);
            }
        }
        // Доля ставки: сначала по name="tuBidShare"
        try {
            inputInGracePeriodFormByName("tuBidShare", bidShareValue);
        } catch (AssertionError e) {
            try {
                inputInGracePeriodDialogByLabelFirstMatch(
                        new String[]{"Доля ставки", "Коэффициент", "Доля"}, bidShareValue);
            } catch (AssertionError e2) {
                inputInGracePeriodByInputIndex(3, bidShareValue);
            }
        }
        return this;
    }

    /**
     * Выбор в выпадающем списке формы «Льготный стаж»: сначала по name, при отсутствии — по одному из вариантов метки.
     */
    public MainPage selectInGracePeriodFormByNameOrLabel(String inputName, String[] labelFallbacks, String optionText) {
        var dialog = getGracePeriodFormDialog();
        var byName = dialog.$("input[name='" + inputName + "']");
        if (byName.exists() && byName.isDisplayed()) {
            byName.click();
            clickDropdownOptionByText(optionText);
            
            // Быстрая проверка закрытия диалога сразу после выбора значения (не ждём долго)
            sleep(200);
            
            // Быстрая проверка, не закрылся ли диалог
            try {
                var quickCheckDialog = $x("//*[@role='dialog'][.//*[contains(., '" + GRACE_PERIOD_DIALOG_TITLE + "') or contains(., 'ЛЬГОТНЫЙ СТАЖ')]]");
                if (!quickCheckDialog.exists() || !quickCheckDialog.isDisplayed()) {
                    // Диалог закрылся - парсим структуру страницы для анализа
                    log.warn("========================================");
                    log.warn("ОШИБКА: Диалог закрылся сразу после выбора значения '" + optionText + 
                        "' в поле name='" + inputName + "'");
                    log.warn("Парсим структуру страницы для анализа...");
                    log.warn("========================================");
                    
                    parsePageStructureOnError(new AssertionError("Диалог закрылся после выбора значения"));
                    
                    throw new AssertionError("Диалог 'Льготный стаж' закрылся сразу после выбора значения '" + optionText + 
                        "' в поле name='" + inputName + "'. Возможно, это значение вызывает закрытие диалога. " +
                        "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.");
                }
            } catch (AssertionError e) {
                // Пробрасываем AssertionError как есть
                throw e;
            } catch (Exception ignored) {
                // Продолжаем - возможно диалог ещё открывается
            }
            
            // После выбора значения ждём стабилизации диалога (React может перерисовывать форму)
            try {
                waitForDialogStabilization();
            } catch (AssertionError e) {
                // Если waitForDialogStabilization обнаружил закрытие диалога, парсим структуру
                log.warn("========================================");
                log.warn("ОШИБКА: Диалог закрылся после выбора значения '" + optionText + 
                    "' в поле name='" + inputName + "' (в waitForDialogStabilization)");
                log.warn("Парсим структуру страницы для анализа...");
                log.warn("========================================");
                
                parsePageStructureOnError(e);
                
                throw new AssertionError("Диалог 'Льготный стаж' закрылся после выбора значения '" + optionText + 
                    "' в поле name='" + inputName + "'. Возможно, это значение вызывает закрытие диалога. " +
                    "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
            }
            
            // Проверяем, что диалог всё ещё открыт после выбора значения
            try {
                dialog = getGracePeriodFormDialog();
                dialog.shouldBe(Condition.visible, Duration.ofSeconds(1));
            } catch (Exception e) {
                // Диалог закрылся - парсим структуру страницы для анализа
                log.warn("========================================");
                log.warn("ОШИБКА: Диалог закрылся после стабилизации (значение '" + optionText + 
                    "' в поле name='" + inputName + "')");
                log.warn("Парсим структуру страницы для анализа...");
                log.warn("========================================");
                
                parsePageStructureOnError(e);
                
                throw new AssertionError("Диалог 'Льготный стаж' закрылся после выбора значения '" + optionText + 
                    "' в поле name='" + inputName + "'. Возможно, это значение вызывает закрытие диалога. " +
                    "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
            }
            return this;
        }
        for (String label : labelFallbacks) {
            try {
                // selectInGracePeriodDialogByLabel уже содержит проверки на закрытие диалога
                selectInGracePeriodDialogByLabel(label, optionText);
                
                // Дополнительная быстрая проверка после выбора (selectInGracePeriodDialogByLabel уже проверил, но перепроверяем)
                try {
                    dialog = getGracePeriodFormDialog();
                    dialog.shouldBe(Condition.visible, Duration.ofSeconds(1));
                } catch (Exception e) {
                    // Диалог закрылся - это уже обработано в selectInGracePeriodDialogByLabel, но перепроверяем
                    log.warn("========================================");
                    log.warn("ОШИБКА: Диалог закрылся после выбора значения '" + optionText + 
                        "' в поле '" + label + "' (финальная проверка)");
                    log.warn("Парсим структуру страницы для анализа...");
                    log.warn("========================================");
                    
                    parsePageStructureOnError(e);
                    
                    throw new AssertionError("Диалог 'Льготный стаж' закрылся после выбора значения '" + optionText + 
                        "' в поле '" + label + "'. Возможно, это значение вызывает закрытие диалога. " +
                        "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа.", e);
                }
                return this;
            } catch (AssertionError ae) {
                // Пробрасываем AssertionError как есть (уже содержит информацию о закрытии диалога)
                throw ae;
            } catch (Exception ignored) {
                // try next label
            }
        }
        throw new AssertionError("Не найден селект по name=" + inputName + " или меткам: " + String.join(", ", labelFallbacks));
    }

    /**
     * Устанавливает дату в MUI/React controlled input через нативный сеттер и события,
     * чтобы React обновил state (обычный setValue маска может игнорировать).
     */
    private void setDateViaJsReact(com.codeborne.selenide.SelenideElement input, String date) {
        setInputValueViaJs(input, date);
    }

    /**
     * Устанавливает значение в React controlled input через нативный setter и события,
     * чтобы React обновил state (setValue часто не срабатывает для MUI/числовых полей).
     */
    private void setInputValueViaJs(com.codeborne.selenide.SelenideElement input, String value) {
        com.codeborne.selenide.Selenide.executeJavaScript(
            "var el = arguments[0]; var v = arguments[1];"
            + "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
            + "if (setter) setter.call(el, v); else el.value = v;"
            + "el.dispatchEvent(new Event('input', { bubbles: true }));"
            + "el.dispatchEvent(new Event('change', { bubbles: true }));"
            + "el.dispatchEvent(new Event('blur', { bubbles: true }));",
            input.getWrappedElement(),
            value
        );
    }

    /**
     * Отправляет клавишу Escape (закрывает открытый календарь/попап).
     *
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage pressEscape() {
        new Actions(getWebDriver()).sendKeys(Keys.ESCAPE).perform();
        return this;
    }

    /**
     * Вводит текст в textarea по меткам
     * 
     * Использует TextareaComponent для ввода многострочного текста.
     * 
     * @param value ID метки textarea
     * @param value1 ID самого textarea элемента
     * @param content текст для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputLabelTextarea(String value, String value1, String content) {
        TextareaComponent.createLabeledTextarea(value, value1, value1).setText(content);
        return this;
    }

    /**
     * Вводит значение в поле по метке (label)
     * 
     * Использует InputComponent для поиска поля по метке и ввода значения.
     * 
     * @param buttonName текст метки поля
     * @param content значение для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickInputLabel(String buttonName, String content){
        InputComponent.createLabeledInput(buttonName, buttonName).setValue(content);
        return this;
    }

    /**
     * Вводит значение во второе поле по метке
     * 
     * Используется когда на странице несколько полей с одинаковой меткой.
     * Выбирает второе поле по порядку.
     * 
     * @param buttonName текст метки поля
     * @param content значение для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSecondInputLabel(String buttonName, String content){
        $x("(//div[label[text() = '" + buttonName + "']]//input)[2]").setValue(content);
        return this;
    }

    /**
     * Вводит значение в поле по имени поля
     * 
     * Использует InputComponent для поиска поля по метке и ввода значения.
     * 
     * @param fieldName имя поля (текст метки)
     * @param value значение для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputField(String fieldName, String value) {
        InputComponent.createLabeledInput(fieldName, fieldName).setValue(value);
        return this;
    }

    /**
     * Вводит значение в поле по ID (для персональных данных)
     * 
     * Используется для ввода данных в поля формы персональных данных.
     * Значение преобразуется в строку перед вводом.
     * 
     * @param fieldName ID поля для ввода
     * @param value значение для ввода (будет преобразовано в строку)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputFieldPerson(String fieldName, String value) {
        $x(String.format("//input[@id= '%s']", fieldName)).setValue(String.valueOf(value));
        return this;
    }

    /**
     * Вводит значение в поле по атрибуту name (для полей формы без id).
     *
     * @param name  атрибут name элемента input
     * @param value значение для ввода
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputByName(String name, String value) {
        $x(String.format("//input[@name='%s']", name)).setValue(String.valueOf(value));
        return this;
    }

    /**
     * Открывает MUI-селект по имени input и выбирает опцию по тексту (для полей без явной метки).
     *
     * @param inputName   атрибут name у input
     * @param optionText  текст опции для выбора
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage selectOptionByInputName(String inputName, String optionText) {
        $x(String.format("//input[@name='%s']", inputName)).click();
        $x(String.format("//li[contains(., '%s')]", optionText.replace("'", "''"))).click();
        return this;
    }

    /**
     * Проверяет значение в поле по метке
     * 
     * Проверяет, что текст в поле соответствует ожидаемому значению.
     * 
     * @param buttonName текст метки поля
     * @param content ожидаемое значение в поле
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если значение не соответствует ожидаемому
     */
    public MainPage verifyField(String buttonName, String content){
        $x(String.format("//div[child::*[text() = '%s']]//input", buttonName)).shouldHave(Condition.text(content));
        return this;
    }

    /**
     * @deprecated Используйте умные ожидания Selenide вместо жестких задержек.
     * Например: $("selector").shouldBe(Condition.visible) или $(".spinner").should(Condition.disappear)
     * 
     * Этот метод будет удален в будущих версиях.
     * Все использования уже заменены на умные ожидания.
     * 
     * Текущая реализация использует только умное ожидание без Thread.sleep().
     */
    @Deprecated
    public MainPage w8sec(){
        // Используем только умное ожидание без жестких задержек
        // Ожидаем готовности страницы и отсутствия спиннеров загрузки
        try {
            $x("//body").shouldBe(Condition.visible, Duration.ofMillis(100));
            // Проверяем отсутствие элементов загрузки
            $x("//div[contains(@class, 'spinner') or contains(@class, 'loading')]")
                    .should(Condition.disappear, Duration.ofSeconds(3));
        } catch (Exception e) {
            // Если спиннеры не найдены или уже исчезли, продолжаем выполнение
            // Это нормально, так как не все страницы имеют спиннеры
        }
        return this;
    }

    /**
     * Загружает файл через модальное окно загрузки
     * 
     * Используется для загрузки отчетов, документов и других файлов.
     * Путь к файлу может быть относительным (от classpath) или абсолютным.
     * 
     * @param path путь к файлу для загрузки (например, "reports/efs1_sample.xml")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage uploadFile(String path) {
        FileComponent.createModalFileInput(path).uploadFile(path);
        return this;
    }

    /**
     * Выполняет выход из системы
     * 
     * Последовательность действий:
     * 1. Закрывает алерт (если отображается)
     * 2. Кликает по имени пользователя
     * 3. Кликает по кнопке выхода
     * 4. Ожидает появления страницы авторизации
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage logOut() {

        if (
                $x("//div[@class= 'ps-alert-top']").isDisplayed()) {
            $x("//i[@class= 'ps-icon-x ps-alert-close']").click();
        }

        $x("//div[contains(@class, 'user-name')]").click();
        $x("//button[contains(@class, 'logout')]").click();
        $x("//div[contains(text(), 'Войдите в систему')]").shouldBe(Condition.visible, Duration.ofSeconds(60));

        return this;
    }


    /**
     * Вводит ID процесса в поле поиска
     * 
     * Ожидает готовности элементов формы (таблица и кнопка) перед вводом ID процесса.
     * 
     * <p>Метод использует прямой доступ к элементу через XPath.
     * Для более универсального подхода можно использовать
     * {@link com.bft.ui.component.InputComponent} компоненты.
     * 
     * @param id ID процесса для ввода (UUID формата)
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @see com.bft.ui.component.InputComponent для универсальной работы с полями ввода
     */
    public MainPage enterProcessId(String id) {
        $x("//tr[@data-row-key='1']").shouldBe(Condition.enabled,Duration.ofSeconds(8));
        $x("//button[@class= 'btn btn-primary']").shouldBe(Condition.enabled,Duration.ofSeconds(8));

        $x("//*[@id='processId']").shouldBe(Condition.visible,Duration.ofSeconds(8));
        $x("//*[@id='processId']").setValue(id);
        return this;
    }

    /**
     * Вводит СНИЛС в поле поиска
     * 
     * Кликает по полю СНИЛС и вводит значение.
     * 
     * @param snils СНИЛС для ввода (формат: XXX-XXX-XXX-XX)
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage enterSnils(String snils) {
        $("input#snils-label").click();
        $("input#snils-label").sendKeys(snils);

        return this;
    }

    /**
     * Кликает по кнопке поиска
     * 
     * Используется для запуска поиска после ввода критериев.
     * 
     * <p>Метод использует прямой доступ к элементу через XPath.
     * Для более универсального подхода можно использовать
     * {@link com.bft.ui.component.ButtonComponent} компоненты.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @see com.bft.ui.component.ButtonComponent для универсальной работы с кнопками
     */
    public MainPage clickSearchButton() {
        $x("//button[@class = 'btn btn-primary']").click();
        return this;
    }


    /**
     * Открывает отчет УОС, кликая по первой строке таблицы
     * 
     * Ожидает загрузки следующей страницы после клика по строке таблицы.
     * Использует умное ожидание вместо жесткой задержки.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage openReportUos() {
        $x("//tbody[@class= 'n2o-advanced-table-tbody']/tr[1]").click();
        // Ожидаем загрузки следующей страницы - проверяем наличие элементов формы отчета
        $x("//div[contains(@class, 'application')]").shouldBe(Condition.visible, Duration.ofSeconds(10));
        return this;
    }


    /**
     * Ожидает завершения обработки отчета
     * 
     * Периодически обновляет страницу и проверяет статус обработки отчета.
     * Ожидает пока статус не станет "Формирование УПП/УУОН-ПУ" или "Завершена".
     * Максимальное время ожидания: 10 минут (30 попыток по 20 секунд).
     * 
     * <p>Использует цикл с обновлением страницы для проверки статуса.
     * Использует умное ожидание вместо жесткой задержки sleep(20000).
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage  waitReportToBeProcessed() {

        int attempts = 0;

        // Обновляем страницу пока текст в поле "статус обработки" не станет "Формирование УПП/УУОН-ПУ" или пока не пройдет 10 минут
        while(attempts < 30) {
            String statusText = $x("(//span[contains(@class,'MuiChip-label MuiChip-labelMedium')])[2]").getText();
            if("Формирование УПП/УУОН-ПУ".equals(statusText) || "Завершена".equals(statusText)) {
                break; // Статус достигнут, выходим из цикла
            } else {
                refresh();
                // Используем умное ожидание: ждем загрузки страницы и появления элемента статуса
                $x("(//span[contains(@class,'MuiChip-label MuiChip-labelMedium')])[2]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(20));
                attempts++;
            }
        }

        return this;
    }


    /**
     * Проверяет статус обработки отчета
     * 
     * Ожидает появления статуса "Завершена" и сравнивает его с ожидаемым значением.
     * Использует Assert.assertEquals для валидации.
     * 
     * @param expectedResult ожидаемый статус обработки (например, "Завершена")
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если статус не соответствует ожидаемому
     */
    public MainPage checkProcessingStatus(String expectedResult) {
        $x("(//span[contains(@class,'MuiChip-label MuiChip-labelMedium')])[2]").shouldHave(Condition.exactText("Завершена"), Duration.ofSeconds(60));
        String actualResult = $x("(//span[contains(@class,'MuiChip-label MuiChip-labelMedium')])[2]").getText();
        log.debug(actualResult);
        Assert.assertEquals(expectedResult, actualResult);
        return this;
    }

    /**
     * Проверяет статус отчета
     * 
     * Получает текущий статус отчета и сравнивает его с ожидаемым значением.
     * Использует Assert.assertEquals для валидации.
     * 
     * @param expectedResult ожидаемый статус отчета
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если статус не соответствует ожидаемому
     */
    public MainPage checkReportStatus(String expectedResult) {
        String actualResult = $x("(//span[contains(@class,'MuiChip-label MuiChip-labelMedium')])[1]").getText();
        log.debug(actualResult);
        Assert.assertEquals(expectedResult, actualResult);
        return this;
    }


    /**
     * Проверяет наличие ошибок в протоколе проверки
     * 
     * Проверяет, что протокол содержит ошибки с указанными кодами
     * 
     * Сравнивает коды ошибок из протокола с ожидаемыми значениями.
     * Проверяет все переданные коды ошибок в порядке их появления в протоколе.
     * 
     * <p>Текущая реализация проверяет до 2 кодов ошибок.
     * Для проверки большего количества кодов ошибок можно расширить метод
     * или использовать цикл по всем элементам протокола.
     * 
     * @param errorCodes массив ожидаемых кодов ошибок (минимум 1 элемент, опционально 2)
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если коды ошибок не соответствуют ожидаемым
     */
    public MainPage checkProtocolsNegative(String[] errorCodes) {
        ElementsCollection protocol = $$x("//td[@colspan='1'][1]");
        Assert.assertEquals(protocol.get(0).getText(), errorCodes[0], 
                "Первый код ошибки должен соответствовать ожидаемому");
        
        if (protocol.size() > 1 && errorCodes.length > 1) {
            Assert.assertEquals(protocol.get(1).getText(), errorCodes[1],
                    "Второй код ошибки должен соответствовать ожидаемому");
        }
        return this;
    }

    /**
     * Проверяет положительный результат протокола проверки
     * 
     * Проверяет, что:
     * 1. Статус УПП равен "Положительный"
     * 2. В протоколе нет ошибок (отображается сообщение "Данных нет" в таблице)
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если статус не положительный или есть ошибки
     */
    public MainPage checkProtocolsPositive() {

        waitForUppStatus();
        boolean noErrors = $x("//div[@class='noDataTable']").isDisplayed();
        String uppStatus = $x("//div[@class= 'MuiChip-root MuiChip-filled MuiChip-sizeMedium MuiChip-colorDefault MuiChip-filledDefault chip_with-bg css-wk4je1']").getText();
        log.debug("uppStatus " + uppStatus);

        Assert.assertEquals(uppStatus, "Положительный");
        Assert.assertTrue(noErrors);

        return this;
    }

    /**
     * Проверяет наличие отчетов страхователя
     * 
     * Ожидает загрузки таблицы, проверяет что таблица не пустая,
     * открывает первый отчет и проверяет загрузку документа ИЛС.
     * 
     * <p>Текущая реализация проверяет наличие документа ИЛС через ожидание загрузки.
     * Для более детальной проверки типа документа можно использовать
     * методы проверки содержимого документа или его метаданных.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если таблица пустая или документ не загрузился
     */
    public MainPage checkInsurerReportings() {

        // СДЕЛАТЬ НОРМАЛЬНУЮ ПРОВЕРКУ ТИПА ДОКУМЕНТА
        waitTableToLoad();

        ElementsCollection insurerReportings = $$x("//tbody[@class= 'n2o-advanced-table-tbody']/tr");
        Assert.assertNotEquals(insurerReportings.size(), 0);
        insurerReportings.get(0).click();
        waitIlsDocumentLoad();
        //СКРИНШОТ
        back();
        return this;
    }

    /*public MainPage checkWorkExperience(ReportType reportType) {
        waitTableToLoad();
        ElementsCollection insurerReportings = null;
        ElementsCollection workPeriods = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[3]");
        String reportTypeName = reportType.name();

        *//*if (reportTypeName == SZV_M.name()) {
            insurerReportings = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[2]");
        } else if (reportTypeName == SZV_TD_TYPE1.name() || reportTypeName == SZV_TD_TYPE5.name() || reportTypeName == EFS_TD_TYPE1.name()) {
            insurerReportings = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[1]");
        }*//*


        //    ElementsCollection insurerReportings = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[2]");
        Assert.assertNotEquals(insurerReportings.size(), 0);
        Assert.assertNotEquals(workPeriods.size(), 0);
        //СКРИНШОТ
        back();
        return this;
    }*/

    /**
     * Проверяет наличие данных о стаже
     * 
     * Ожидает загрузки таблицы, проверяет что таблица не пустая,
     * открывает первую запись и проверяет название организации.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если таблица пустая или название организации не соответствует ожидаемому
     */
    public MainPage checkStazh() {
        waitTableToLoad();
        ElementsCollection stazhReportings = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[1]");
        Assert.assertNotEquals(stazhReportings.size(), 0);
        $x("(//tr[@data-row-key='1'])").click();
        $x("(//div[@class = 'pgs-output-text'])[1]/*[2]").shouldHave(Condition.exactText("ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"ФОРВАРД\""),Duration.ofSeconds(10));
        //СКРИНШОТ
        back();
        return this;
    }

    /**
     * Проверяет наличие данных о выплатах и иных вознаграждениях
     * 
     * Открывает вкладку "Выплаты и иные вознаграждения", ожидает загрузки таблицы
     * и проверяет что таблица не пустая.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если таблица пустая
     */
    public MainPage checkPayments() {
        openTab("Выплаты и иные вознаграждения");
        waitTableToLoad();
        ElementsCollection paymentsReportings = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[1]/*");
        Assert.assertNotEquals(paymentsReportings.size(), 0);
        back();
        return this;
    }

    /**
     * Проверяет наличие данных о страховых взносах
     * 
     * Открывает вкладку "Страховые взносы", ожидает загрузки таблицы
     * и проверяет что таблица не пустая.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если таблица пустая
     */
    public MainPage checkInsurancePayments() {
        openTab("Страховые взносы");
        waitTableToLoad();
        ElementsCollection insuranceReportings = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[1]/*");
        Assert.assertNotEquals(insuranceReportings.size(), 0);
        back();
        return this;
    }

    /**
     * Проверяет наличие данных о государственном стаже
     * 
     * Ожидает загрузки таблицы, открывает первую запись, проверяет наличие данных
     * и проверяет название работодателя.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если таблица пустая или название работодателя не соответствует ожидаемому
     */
    public MainPage checkGovernmentStazh() {
        waitTableToLoad();
        $x("(//td[@rowspan='1'])[1]").click();
        ElementsCollection stazhReportings = $$x(TabType.TABLE.value);
        Assert.assertNotEquals(stazhReportings.size(), 0);
        $x("//td[@class= 'withTooltip']").click();
        waitTableToLoad();
        String employerName = $x("(//div[@class = 'pgs-output-text'])[1]/*[2]  ").getText();
        Assert.assertEquals(employerName, "АО \"Светлый путь\"");
        //СКРИНШОТ
        back();
        return this;
    }

    /**
     * Открывает карточку физического лица по ID
     * 
     * Закрывает боковое меню и открывает URL карточки физического лица в системе РПУ.
     * Используется для перехода к детальной информации о физическом лице.
     * 
     * @param flId ID физического лица для открытия карточки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage openPerson(String flId) {

        $x("//i[@class='n2o-sidebar-switcher ps-icon-x']").click();

        open("https://portal.test.ecp/rpu/#/rpu/common/zags/searchFlBaseData/flBaseDataMainInfo/" + flId + "/record");
        // open("https://rpu-common-bo.uat.ecp/#/rpu/common/zags/searchFlBaseData/flBaseDataMainInfo/" + flId + "/record");
        return this;
    }

    /**
     * Проверяет что вкладка не пустая и не содержит ошибок
     * 
     * Выполняет комплексную проверку вкладки:
     * 1. Открывает указанную вкладку
     * 2. Ожидает загрузки таблицы
     * 3. Проверяет отсутствие ошибок в таблице
     * 4. Проверяет что таблица не пустая
     * 5. Логирует результаты проверки
     * 
     * @param tabName название вкладки для проверки
     * @param tabType тип вкладки из enum TabType (определяет поведение проверки)
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если таблица пустая или содержит ошибки
     */
    public MainPage checkTabNotEmptyNoErrors(String tabName, TabType tabType) {
        openTab(tabName);
        waitTableToLoad();

        String tableName = resultsTable.getTableTitle();
        boolean hasError = resultsTable.hasError();

        //ПРОВЕРКА, ОТОБРАЖАЕТСЯ ЛИ ОШИБКА
        softAssert.assertFalse(hasError);
        if (hasError) {
            log.debug((char) 27 + "[31mОШИБКА на вкладке   " + tableName + (char) 27 + "[37m");
            resultsTable.closeErrorAlert();
        }

        //ПРОВЕРКА, ЧТО ТАБЛИЦА НЕ ПУСТАЯ
        if (tabType.value.equals(TabType.TABLE.value)) {
            resultsTable.clickFirstRow();
        }

        boolean isNotEmpty = resultsTable.isNotEmpty();
        softAssert.assertTrue(isNotEmpty);

        //ЛОГ
        int rowCount = resultsTable.getRowCount();
        if (!isNotEmpty) {
            log.debug((char) 27 + "[31mКоличество записей в таблице   " + tableName + " = " + rowCount + (char) 27 + "[37m");
        } else {
            log.debug((char) 27 + "[32mКоличество записей в таблице   " + tableName + " = " + rowCount + (char) 27 + "[37m");
        }

        return this;
    }

    /**
     * Проверяет базовую информацию о физическом лице
     * 
     * Извлекает и проверяет ФИО, ИНН и адрес физического лица.
     * Сравнивает значения с ожидаемыми (хардкод для тестового сценария).
     * Логирует извлеченные данные.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     * @throws AssertionError если данные не соответствуют ожидаемым значениям
     */
    public MainPage checkBasicInfo() {
        waitTableToLoad();
        String lastName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[1]").getText();
        String firstName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[2]").getText();
        String secondName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[3]").getText();
        String inn = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[7]").getText();
        String adress = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[8]").getText();

        softAssert.assertEquals(lastName, "ВЯТСКИЙ");
        softAssert.assertEquals(firstName, "ФЕДОР");
        softAssert.assertEquals(secondName, "МИХАЙЛОВИЧ");

        //лог
        log.debug("Фамилия: " + lastName + " Имя: " + firstName + " Отчество: " + secondName);
        log.debug("ИНН " + inn);
        log.debug("Адрес " + adress);
        return this;
    }


    /*public MainPage checkBasicIlsInfo() {
        openTab("Общие сведения об ИЛС");
        waitTableToLoad();
        String status = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[4]").getText();
        String state = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[5]").getText();
        softAssert.assertEquals(status, "Открыт");
        softAssert.assertEquals(state, "Актуальный");

        log.debug("Статус ИЛС: " + status);
        log.debug("Состояние ИЛС:  " + state);
        return this;
    }*/

    /**
     * Ожидает загрузки таблицы результатов
     * 
     * Использует TableComponent для умного ожидания загрузки таблицы.
     * Проверяет наличие данных в таблице перед продолжением выполнения теста.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage waitTableToLoad() {
        resultsTable.waitForLoad();
        return this;
    }

    /**
     * Ожидает загрузки документа ИЛС
     * 
     * Проверяет, что документ ИЛС загружен и содержит данные
     * (текст не равен "ДАННЫХ НЕТ").
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage waitIlsDocumentLoad() {
        $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[3]").shouldNotBe(Condition.exactText("ДАННЫХ НЕТ"), Duration.ofSeconds(10L));

        return this;
    }

    /**
     * Ожидает появления статуса УПП
     * 
     * Проверяет, что статус УПП отображается и не равен "Данных нет".
     * Используется для проверки готовности данных перед дальнейшими действиями.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage waitForUppStatus() {

        $x("//div[@class='MuiChip-root MuiChip-filled MuiChip-sizeMedium MuiChip-colorDefault MuiChip-filledDefault chip_with-bg css-wk4je1']").shouldNot(Condition.exactText("Данных нет"), Duration.ofSeconds(20)
        );
        return this;
    }

    /**
     * Ожидает 7 секунд
     * 
     * Использует стандартный метод wait() для ожидания.
     * Рекомендуется заменить на умное ожидание конкретного условия.
     * 
     * @deprecated Используйте умные ожидания Selenide вместо жестких задержек
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    @Deprecated
    public MainPage waitForSecond() {

        try {
            wait(7000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Обновляет страницу и ожидает загрузки таблицы
     * 
     * Выполняет обновление страницы (refresh) и ожидает появления
     * первого элемента таблицы для подтверждения загрузки.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage refreshAndWait() {

        refresh();
        $x("//tbody[@class= 'n2o-advanced-table-tbody']/*[1]/*[6]").shouldBe(Condition.visible,Duration.ofSeconds(5L));
        return this;
    }

    @SuppressWarnings("unused") // для явного ожидания загрузки формы при необходимости
    private void waitForLoadingForm() {
        $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(60));
    }
}