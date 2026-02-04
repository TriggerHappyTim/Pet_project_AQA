package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import org.openqa.selenium.By;

/**
 * Компонент для работы с радиокнопками
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с радиокнопками:
 * - Выбор радиокнопки
 * - Проверка выбранного состояния
 * - Проверка доступности
 * - Получение значения и текста метки
 * - Работа с группами радиокнопок
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Выбор радиокнопки по метке
 * RadioButtonComponent.createByLabel("Положительный", "Ответ").select();
 * 
 * // Проверка выбранного состояния
 * boolean isSelected = RadioButtonComponent.createBySpanText("Исходная", "Тип отчета").isSelected();
 * 
 * // Убедиться что радиокнопка выбрана
 * RadioButtonComponent.createByValue("option1", "Вариант 1").ensureSelected();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class RadioButtonComponent extends BaseComponent {

    private final SmartElement radioInput;
    private final SmartElement radioLabel;

    /**
     * Конструктор компонента радиокнопки
     * @param radioSelector селектор радиокнопки
     * @param radioName имя радиокнопки для логирования
     */
    public RadioButtonComponent(String radioSelector, String radioName) {
        super(ElementFactory.css(radioSelector).named(radioName).waitVisible().build(), radioName);
        this.radioInput = ElementFactory.xpath(radioSelector + "//input[@type='radio']")
                .named("Input для " + radioName)
                .build();
        this.radioLabel = ElementFactory.xpath(radioSelector + "//span")
                .named("Label для " + radioName)
                .build();
    }

    /**
     * Конструктор компонента радиокнопки с By селектором
     * @param radioLocator By селектор радиокнопки
     * @param radioName имя радиокнопки для логирования
     */
    public RadioButtonComponent(By radioLocator, String radioName) {
        super(ElementFactory.by(radioLocator).named(radioName).waitVisible().build(), radioName);
        this.radioInput = ElementFactory.xpath("//input[@type='radio']")
                .named("Input для " + radioName)
                .build();
        this.radioLabel = ElementFactory.xpath("//span[text()='" + radioName + "']")
                .named("Label для " + radioName)
                .build();
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible();
    }

    /**
     * Выбирает радиокнопку
     */
    public RadioButtonComponent select() {
        if (!isSelected()) {
            logger.info("Выбираем радиокнопку: {}", componentName);
            rootElement.click();
        } else {
            logger.debug("Радиокнопка '{}' уже выбрана", componentName);
        }
        return this;
    }

    /**
     * Проверяет, выбрана ли радиокнопка
     */
    public boolean isSelected() {
        try {
            String checkedAttribute = radioInput.getAttribute("checked");
            boolean isSelected = "true".equals(checkedAttribute) || checkedAttribute != null;
            logger.debug("Радиокнопка '{}' {}", componentName, isSelected ? "выбрана" : "не выбрана");
            return isSelected;
        } catch (Exception e) {
            logger.warn("Не удалось определить состояние радиокнопки '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что радиокнопка доступна для взаимодействия
     */
    public boolean isEnabled() {
        try {
            String disabledAttribute = radioInput.getAttribute("disabled");
            boolean enabled = disabledAttribute == null || "false".equals(disabledAttribute);
            logger.debug("Радиокнопка '{}' {}", componentName, enabled ? "доступна" : "недоступна");
            return enabled;
        } catch (Exception e) {
            logger.warn("Не удалось определить доступность радиокнопки '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Получает значение радиокнопки
     */
    public String getValue() {
        try {
            return radioInput.getAttribute("value");
        } catch (Exception e) {
            logger.warn("Не удалось получить значение радиокнопки '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Получает текст лейбла радиокнопки
     */
    public String getLabelText() {
        try {
            return radioLabel.getText();
        } catch (Exception e) {
            logger.warn("Не удалось получить текст лейбла для радиокнопки '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ РАДИОКНОПОК =====

    /**
     * Создает компонент для радиокнопки по тексту лейбла
     */
    public static RadioButtonComponent createByLabel(String labelText, String radioName) {
        return new RadioButtonComponent(
            By.xpath("//div[contains(@class, 'radio')][label[span[text() = '" + labelText + "']]]"),
            "Радиокнопка '" + radioName + "'"
        );
    }

    /**
     * Создает компонент для радиокнопки по тексту span
     * 
     * Поддерживает несколько вариантов разметки:
     * - label с классом n2o-radio-input (текущая разметка ЕВС)
     * - div с классом n2o-radio-input-wrapper
     * - div с классом n2o-radio-input
     */
    public static RadioButtonComponent createBySpanText(String spanText, String radioName) {
        String escaped = spanText.replace("'", "''");
        // Приоритет 1: label с классом n2o-radio-input (разметка ЕВС "Добавление отчета")
        String xpathLabel = "//label[contains(@class, 'n2o-radio-input')]//span[text() = '" + escaped + "']";
        // Приоритет 2: обертка n2o-radio-input-wrapper
        String xpathWrapper = "//div[contains(@class, 'n2o-radio-input-wrapper')]//span[text() = '" + escaped + "']";
        // Приоритет 3: div с n2o-radio-input (обратная совместимость)
        String xpathDiv = "//div[contains(@class, 'n2o-radio-input')]//span[text() = '" + escaped + "']";
        // Объединяем через union: первый найденный
        String xpath = xpathLabel + " | " + xpathWrapper + " | " + xpathDiv;
        return new RadioButtonComponent(By.xpath(xpath), "Радиокнопка '" + radioName + "'");
    }

    /**
     * Создает компонент для радиокнопки по значению
     */
    public static RadioButtonComponent createByValue(String value, String radioName) {
        return new RadioButtonComponent(
            By.xpath("//input[@type='radio'][@value='" + value + "']"),
            "Радиокнопка '" + radioName + "'"
        );
    }

    /**
     * Создает компонент для радиокнопки по имени группы
     */
    public static RadioButtonComponent createByName(String name, String value, String radioName) {
        return new RadioButtonComponent(
            By.xpath("//input[@type='radio'][@name='" + name + "'][@value='" + value + "']"),
            "Радиокнопка '" + radioName + "'"
        );
    }

    /**
     * Создает компонент для радиокнопки по ID
     */
    public static RadioButtonComponent createById(String radioId, String radioName) {
        return new RadioButtonComponent(
            By.xpath("//input[@id='" + radioId + "']"),
            "Радиокнопка ID '" + radioName + "'"
        );
    }

    /**
     * Создает компонент для радиокнопки по любому XPath
     */
    public static RadioButtonComponent createCustom(String xpath, String radioName) {
        return new RadioButtonComponent(By.xpath(xpath), "Радиокнопка '" + radioName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Выбирает радиокнопку если она еще не выбрана
     */
    public RadioButtonComponent ensureSelected() {
        if (!isSelected()) {
            select();
        }
        return this;
    }

    /**
     * Проверяет соответствие ожидаемому состоянию
     */
    public boolean matchesState(boolean expectedSelected) {
        boolean actualSelected = isSelected();
        boolean matches = actualSelected == expectedSelected;
        logger.debug("Проверка состояния радиокнопки '{}' - ожидалось {}, получено {}: {}",
                    componentName, expectedSelected, actualSelected, matches);
        return matches;
    }

    /**
     * Проверяет, что радиокнопка выбрана в группе
     */
    public boolean isSelectedInGroup(String groupName) {
        // Для проверки выбора в группе можно использовать имя группы
        boolean selected = isSelected();
        if (selected) {
            logger.debug("Радиокнопка '{}' выбрана в группе '{}'", componentName, groupName);
        }
        return selected;
    }

    /**
     * Получает имя группы радиокнопки
     */
    public String getGroupName() {
        try {
            return radioInput.getAttribute("name");
        } catch (Exception e) {
            logger.warn("Не удалось получить имя группы для радиокнопки '{}': {}", componentName, e.getMessage());
            return "";
        }
    }
}