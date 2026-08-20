package com.bft.ui.pages;

import com.bft.enums.ReportFormType;
import com.bft.ui.component.*;
import com.bft.ui.core.ClickHelper;
import com.bft.pw.Condition;
import com.bft.pw.SelenideElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import java.time.Duration;

import static com.bft.pw.Selenide.*;
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

    // Компонент работы с полями дат (DatePicker, masked input, JS-ввод)
    private final DatePickerComponent datePickerComponent = new DatePickerComponent();

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
        ClickHelper.click(
                $x("//div[@class = 'item-footer']//button[span[text() = '" + buttonName + "']]"),
                "Кнопка футера span '" + buttonName + "'");
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
        ClickHelper.click(
                $x("(//span[contains(text(),'" + buttonName + "')])[2]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Вторая span кнопка '" + buttonName + "'");
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
        ClickHelper.click(
                $x("//div[@class = 'modal-content']//button[*[text() = '" + buttonName + "']]"),
                "Кнопка модального окна '" + buttonName + "'");
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
        ClickHelper.click(
                $x("//button[contains(@class, 'btn-primary')][.//span[text() = '" + buttonName + "']]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Первичная кнопка '" + buttonName + "'");
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
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "'])[1]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Первичная кнопка '" + buttonName + "' #1");
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
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "'])[2]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Первичная кнопка '" + buttonName + "' #2");
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
        ClickHelper.click(
                $x("//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Вторичная кнопка '" + buttonName + "'");
        return this;
    }

    /**
     * Кликает по второй вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary2(String buttonName) {
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[2]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Вторичная кнопка '" + buttonName + "' #2");
        return this;
    }

    /**
     * Кликает по третьей вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary3(String buttonName) {
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[3]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Вторичная кнопка '" + buttonName + "' #3");
        return this;
    }

    /**
     * Кликает по четвертой вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary4(String buttonName) {
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[4]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Вторичная кнопка '" + buttonName + "' #4");
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
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-secondary')][.//span[text() = '" + buttonName + "']])[5]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Вторичная кнопка '" + buttonName + "' #5");
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
        ClickHelper.click(
                $x("(//button[contains(@class, 'btn-secondary')]//span)[1]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(10)),
                "Вторичная кнопка '" + buttonName + "' в новом окне");
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
        ClickHelper.click(
                $x("//button[text() = '" + buttonName + "']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(50)),
                "Кнопка '" + buttonName + "'");
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
        ClickHelper.click(
                $x("//div[@class = 'modal-dialog']//button[text() = '" + buttonName + "']"),
                "Кнопка модального диалога '" + buttonName + "'");
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
        ClickHelper.click(
                $x("//div[@class = 'close-dialog']//button[text() = '" + buttonName + "']"),
                "Кнопка закрытия диалога '" + buttonName + "'");
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
        ClickHelper.click(
                $x("//div[@class = 'close-dialog']//span[text() = '" + buttonName + "']"),
                "Span кнопка закрытия диалога '" + buttonName + "'");
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
        ClickHelper.click(
                $x("//div[@class = 'provider-dialog']//button[text() = '" + buttonName + "']"),
                "Кнопка диалога провайдера '" + buttonName + "'");
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
     * Выбирает радиокнопку по атрибуту name и значению value.
     * 
     * Кликает по label, оборачивающему input, что надёжнее при скрытом нативном input.
     * 
     * @param name  значение атрибута name радиогруппы
     * @param value значение атрибута value нужной опции
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickRadioByNameValue(String name, String value) {
        SelenideElement label = $x("//input[@name='" + name + "' and @value='" + value + "']/ancestor::label")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        ClickHelper.click(label, "Радио " + name + "=" + value);
        return this;
    }

    /**
     * Сохраняет сведения о периоде работы / страховом стаже: клик по «Сохранить»
     * в модалке отчётного периода (диалог с полем {@code reportPeriod}).
     * 
     * Вызывается после сохранения льготного периода, когда модалка отчётного
     * периода остаётся открытой поверх формы ЗЛ.
     * 
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickSaveReportPeriodModal() {
        SelenideElement modal = $x("//div[@role='dialog'][.//input[@name='reportPeriod']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        SelenideElement saveBtn = modal.$x(".//button[contains(@class, 'btn-primary')][.//span[text() = 'Сохранить']]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        ClickHelper.click(saveBtn, "Сохранить (сведения о периоде работы)");
        sleep(500);
        return this;
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
     * Использует DatePickerComponent для установки даты в указанном формате.
     * 
     * @param value ID поля даты
     * @param content дата в формате строки (например, "01-01-2000")
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage inputDateLabel(String value, String content) {
        datePickerComponent.inputDateLabel(value, content);
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
        datePickerComponent.inputDateByLabel(label, date);
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
        datePickerComponent.inputDateEventDateViaCalendar(dateDDMMYYYY);
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
        datePickerComponent.inputDateEventDateViaJs(dateDDMMYYYY);
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
        datePickerComponent.inputDateByIdViaJs(inputId, dateDDMMYYYY);
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
        datePickerComponent.inputDateByLabelTextViaJs(labelText, dateDDMMYYYY);
        return this;
    }

    /**
     * Отправляет клавишу Escape (закрывает открытый календарь/попап).
     *
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage pressEscape() {
        datePickerComponent.pressEscape();
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

    // В класс MainPage.java
    public MainPage selectReportType(ReportFormType type) {
        // Адаптируйте селектор под вашу верстку
        // Например, клик по выпадающему списку и выбор значения
        clickMuiInputLabel("Тип отчета", type.toString());
        return this;
    }

}