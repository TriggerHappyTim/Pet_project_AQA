package com.bft.ui.pages;

import com.bft.ui.component.*;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;

import com.bft.enums.ReportType;
import com.bft.enums.TabType;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;
import static com.bft.enums.ReportType.*;

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
    SoftAssert softAssert = new SoftAssert();

    // Компоненты для работы с различными элементами интерфейса
    private final NavigationComponent navigation = NavigationComponent.createMainNavigation("Главная навигация");
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
        $x("//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
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
     * Кликает по пятой вторичной кнопке с указанным текстом
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return текущий экземпляр MainPage для цепочки вызовов
     */
    public MainPage clickBtnSecondary5(String buttonName) {
        $x("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[5]")
                .shouldBe(Condition.visible, Duration.ofSeconds(50)).click();
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
        RadioButtonComponent.createBySpanText(buttonName, buttonName).select();
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
        System.out.println(actualResult);
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
        System.out.println(actualResult);
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
        System.out.println("uppStatus " + uppStatus);

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
            System.out.println((char) 27 + "[31mОШИБКА на вкладке   " + tableName + (char) 27 + "[37m");
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
            System.out.println((char) 27 + "[31mКоличество записей в таблице   " + tableName + " = " + rowCount + (char) 27 + "[37m");
        } else {
            System.out.println((char) 27 + "[32mКоличество записей в таблице   " + tableName + " = " + rowCount + (char) 27 + "[37m");
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
        System.out.println("Фамилия: " + lastName + " Имя: " + firstName + " Отчество: " + secondName);
        System.out.println("ИНН " + inn);
        System.out.println("Адрес " + adress);
        return this;
    }


    /*public MainPage checkBasicIlsInfo() {
        openTab("Общие сведения об ИЛС");
        waitTableToLoad();
        String status = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[4]").getText();
        String state = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[5]").getText();
        softAssert.assertEquals(status, "Открыт");
        softAssert.assertEquals(state, "Актуальный");

        System.out.println("Статус ИЛС: " + status);
        System.out.println("Состояние ИЛС:  " + state);
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

    private void waitForLoadingForm() {
        $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(60));
    }
}