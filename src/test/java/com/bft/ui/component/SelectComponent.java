package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import org.openqa.selenium.By;

/**
 * Компонент для работы с выпадающими списками и селектами
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с селектами:
 * - Material-UI селекты (MuiInput)
 * - Стандартные HTML select элементы
 * - Кастомные селекты с различными селекторами
 * 
 * <p>Поддерживает выбор опций по тексту, значению или индексу.
 * Все factory методы статические и возвращают готовый к использованию компонент.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Выбор опции по тексту в MUI селекте
 * SelectComponent.createMuiInput("Регион", "region").selectByText("Москва");
 * 
 * // Выбор опции по значению
 * SelectComponent.createSelectById("country", "Страна").selectByValue("RU");
 * 
 * // Выбор первой опции по индексу
 * SelectComponent.createMuiSpan("Город", "city").selectByIndex(0);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class SelectComponent extends BaseComponent {

    /**
     * Создает компонент селекта с CSS селектором
     * 
     * @param selectSelector CSS селектор селекта
     * @param selectName имя селекта для логирования
     */
    public SelectComponent(String selectSelector, String selectName) {
        super(ElementFactory.css(selectSelector).named(selectName).waitClickable().build(), selectName);
    }

    /**
     * Создает компонент селекта с By селектором
     * 
     * Рекомендуемый способ создания компонента, так как By.xpath
     * более универсален для сложных селекторов.
     * 
     * @param selectLocator By селектор селекта (обычно XPath)
     * @param selectName имя селекта для логирования
     */
    public SelectComponent(By selectLocator, String selectName) {
        super(ElementFactory.by(selectLocator).named(selectName).waitClickable().build(), selectName);
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible() && isEnabled();
    }

    /**
     * Выбирает опцию в селекте по тексту
     * 
     * Кликает по селекту, затем выбирает опцию, содержащую указанный текст.
     * Используется для Material-UI селектов и кастомных селектов.
     * 
     * @param text текст опции для выбора (частичное совпадение)
     * @return текущий экземпляр SelectComponent для цепочки вызовов
     */
    public SelectComponent selectByText(String text) {
        logger.info("Выбираем опцию '{}' в селекте: {}", text, componentName);
        rootElement.click();
        // После клика по селекту обычно появляется список опций
        SmartElement option = ElementFactory.xpath("//li[contains(@id, 'option') and contains(text(), '" + text + "')]")
                .named("Опция '" + text + "'")
                .waitClickable()
                .build();
        option.click();
        return this;
    }

    /**
     * Выбирает опцию в селекте по значению
     * 
     * Кликает по селекту, затем выбирает опцию с указанным значением (ID опции).
     * 
     * @param value значение опции для выбора (например, "option-0", "option-1")
     * @return текущий экземпляр SelectComponent для цепочки вызовов
     */
    public SelectComponent selectByValue(String value) {
        logger.info("Выбираем опцию со значением '{}' в селекте: {}", value, componentName);
        rootElement.click();
        SmartElement option = ElementFactory.xpath("//li[contains(@id, 'option-" + value + "')]")
                .named("Опция со значением '" + value + "'")
                .waitClickable()
                .build();
        option.click();
        return this;
    }

    /**
     * Выбирает опцию в селекте по индексу
     * 
     * Кликает по селекту, затем выбирает опцию по порядковому номеру (начиная с 0).
     * 
     * @param index индекс опции для выбора (0 - первая опция, 1 - вторая и т.д.)
     * @return текущий экземпляр SelectComponent для цепочки вызовов
     */
    public SelectComponent selectByIndex(int index) {
        logger.info("Выбираем опцию с индексом {} в селекте: {}", index, componentName);
        rootElement.click();
        SmartElement option = ElementFactory.xpath("(//li[contains(@id, 'option')])[" + (index + 1) + "]")
                .named("Опция с индексом " + index)
                .waitClickable()
                .build();
        option.click();
        return this;
    }

    /**
     * Получает выбранное значение из селекта
     * 
     * Для MuiInput значение может быть в скрытом input поле.
     * 
     * @return выбранное значение или null, если значение не найдено
     */
    public String getSelectedValue() {
        // Для MuiInput значение может быть в скрытом input или text поле
        try {
            SmartElement input = ElementFactory.xpath("//input[@id='" + componentName.replace("mui-", "") + "']")
                    .named("Input для " + componentName)
                    .build();
            String value = input.getAttribute("value");
            logger.debug("Получено выбранное значение '{}' из селекта: {}", value, componentName);
            return value;
        } catch (Exception e) {
            logger.warn("Не удалось получить значение из input для селекта: {}", componentName);
            return null;
        }
    }

    /**
     * Получает выбранный текст из селекта
     * 
     * Извлекает видимый текст выбранной опции.
     * 
     * @return текст выбранной опции
     */
    public String getSelectedText() {
        String text = rootElement.getText();
        logger.debug("Получен выбранный текст '{}' из селекта: {}", text, componentName);
        return text;
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ СЕЛЕКТОВ =====

    /**
     * Создает компонент для MuiInput с лейблом
     */
    public static SelectComponent createMuiInput(String label, String selectName) {
        return new SelectComponent(
            By.xpath("//div[label[text() = '" + label + "']]//button"),
            "MuiInput '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для MuiInput по span тексту
     */
    public static SelectComponent createMuiSpan(String spanText, String selectName) {
        return new SelectComponent(
            By.xpath("//div[span[text() = '" + spanText + "']]"),
            "MuiSpan '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для MuiInput по ID
     */
    public static SelectComponent createMuiInputId(String fieldId, String selectName) {
        return new SelectComponent(
            By.xpath("//div[*[@id = '" + fieldId + "']]"),
            "MuiInput ID '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для MuiInput по классу
     */
    public static SelectComponent createMuiInputClass(String className, String selectName) {
        return new SelectComponent(
            By.xpath("//div[contains(@class, '" + className + "')]"),
            "MuiInput Class '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для MuiInput с кнопкой
     */
    public static SelectComponent createMuiButton(String buttonText, String selectName) {
        return new SelectComponent(
            By.xpath("//span[contains(., '" + buttonText + "')]/ancestor::*[3]//button"),
            "MuiButton '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для MuiInput с кнопкой (альтернативный селектор)
     */
    public static SelectComponent createMuiButtonAlt(String buttonText, String selectName) {
        return new SelectComponent(
            By.xpath("//span[contains(., '" + buttonText + "')]/ancestor::*[4]//button"),
            "MuiButton Alt '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для обычного select
     */
    public static SelectComponent createSelect(String selectName) {
        return new SelectComponent(
            By.xpath("//select[@name='" + selectName + "']"),
            "Select '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для select по ID
     */
    public static SelectComponent createSelectById(String selectId, String selectName) {
        return new SelectComponent(
            By.xpath("//select[@id='" + selectId + "']"),
            "Select ID '" + selectName + "'"
        );
    }

    /**
     * Создает компонент для кастомного селекта
     */
    public static SelectComponent createCustomSelect(String xpath, String selectName) {
        return new SelectComponent(By.xpath(xpath), selectName);
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Выбирает опцию для поля с лейблом
     */
    public static SelectComponent selectLabeledOption(String label, String optionText, String selectName) {
        SelectComponent select = createMuiInput(label, selectName);
        select.selectByText(optionText);
        return select;
    }

    /**
     * Выбирает опцию для поля с span
     */
    public static SelectComponent selectSpanOption(String spanText, String optionText, String selectName) {
        SelectComponent select = createMuiSpan(spanText, selectName);
        select.selectByText(optionText);
        return select;
    }

    /**
     * Выбирает опцию для поля по ID
     */
    public static SelectComponent selectIdOption(String fieldId, String optionText, String selectName) {
        SelectComponent select = createMuiInputId(fieldId, selectName);
        select.selectByText(optionText);
        return select;
    }

    /**
     * Проверяет, что опция выбрана
     */
    public boolean isOptionSelected(String optionText) {
        String selectedText = getSelectedText();
        boolean selected = optionText.equals(selectedText);
        logger.debug("Проверка выбора опции '{}' в селекте '{}': {}",
                    optionText, componentName, selected);
        return selected;
    }

    /**
     * Проверяет, что значение выбрано
     */
    public boolean isValueSelected(String value) {
        String selectedValue = getSelectedValue();
        boolean selected = value.equals(selectedValue);
        logger.debug("Проверка выбора значения '{}' в селекте '{}': {}",
                    value, componentName, selected);
        return selected;
    }
}