package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import org.openqa.selenium.By;

/**
 * Компонент для работы с чекбоксами
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с чекбоксами:
 * - Отметка и снятие отметки
 * - Переключение состояния
 * - Проверка состояния (отмечен/не отмечен)
 * - Проверка доступности
 * - Получение значения и текста метки
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Отметить чекбокс по метке
 * CheckboxComponent.createByLabel("Согласен с условиями", "Согласие").check();
 * 
 * // Проверка состояния
 * boolean isChecked = CheckboxComponent.createById("terms", "Условия").isChecked();
 * 
 * // Убедиться что чекбокс отмечен
 * CheckboxComponent.createByName("newsletter", "Рассылка").ensureChecked();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class CheckboxComponent extends BaseComponent {

    private final SmartElement checkboxInput;
    private final SmartElement checkboxLabel;

    /**
     * Конструктор компонента чекбокса
     * @param checkboxSelector селектор чекбокса
     * @param checkboxName имя чекбокса для логирования
     */
    public CheckboxComponent(String checkboxSelector, String checkboxName) {
        super(ElementFactory.css(checkboxSelector).named(checkboxName).waitVisible().build(), checkboxName);
        this.checkboxInput = ElementFactory.xpath(checkboxSelector + "//input")
                .named("Input для " + checkboxName)
                .build();
        this.checkboxLabel = ElementFactory.xpath(checkboxSelector + "//label")
                .named("Label для " + checkboxName)
                .build();
    }

    /**
     * Конструктор компонента чекбокса с By селектором
     * @param checkboxLocator By селектор чекбокса
     * @param checkboxName имя чекбокса для логирования
     */
    public CheckboxComponent(By checkboxLocator, String checkboxName) {
        super(ElementFactory.by(checkboxLocator).named(checkboxName).waitVisible().build(), checkboxName);
        this.checkboxInput = ElementFactory.xpath("//input[@type='checkbox']")
                .named("Input для " + checkboxName)
                .build();
        this.checkboxLabel = ElementFactory.xpath("//label[text()='" + checkboxName + "']")
                .named("Label для " + checkboxName)
                .build();
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible();
    }

    /**
     * Устанавливает состояние чекбокса
     */
    public CheckboxComponent setChecked(boolean checked) {
        boolean currentlyChecked = isChecked();
        if (currentlyChecked != checked) {
            logger.info("{} чекбокс: {}", checked ? "Отмечаем" : "Снимаем отметку с", componentName);
            rootElement.click();
        } else {
            logger.debug("Чекбокс '{}' уже в нужном состоянии: {}", componentName, checked ? "отмечен" : "не отмечен");
        }
        return this;
    }

    /**
     * Отмечает чекбокс
     */
    public CheckboxComponent check() {
        return setChecked(true);
    }

    /**
     * Снимает отметку с чекбокса
     */
    public CheckboxComponent uncheck() {
        return setChecked(false);
    }

    /**
     * Переключает состояние чекбокса
     */
    public CheckboxComponent toggle() {
        logger.info("Переключаем чекбокс: {}", componentName);
        rootElement.click();
        return this;
    }

    /**
     * Проверяет, отмечен ли чекбокс
     */
    public boolean isChecked() {
        try {
            String checkedAttribute = checkboxInput.getAttribute("checked");
            boolean isChecked = "true".equals(checkedAttribute) || checkedAttribute != null;
            logger.debug("Чекбокс '{}' {}", componentName, isChecked ? "отмечен" : "не отмечен");
            return isChecked;
        } catch (Exception e) {
            logger.warn("Не удалось определить состояние чекбокса '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что чекбокс отмечен
     */
    public boolean isSelected() {
        return isChecked();
    }

    /**
     * Проверяет, что чекбокс доступен для взаимодействия
     */
    public boolean isEnabled() {
        try {
            String disabledAttribute = checkboxInput.getAttribute("disabled");
            boolean enabled = disabledAttribute == null || "false".equals(disabledAttribute);
            logger.debug("Чекбокс '{}' {}", componentName, enabled ? "доступен" : "недоступен");
            return enabled;
        } catch (Exception e) {
            logger.warn("Не удалось определить доступность чекбокса '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ ЧЕКБОКСОВ =====

    /**
     * Создает компонент для чекбокса по тексту лейбла
     */
    public static CheckboxComponent createByLabel(String labelText, String checkboxName) {
        return new CheckboxComponent(
            By.xpath("//div[child::label[text() = '" + labelText + "']]"),
            "Чекбокс '" + checkboxName + "'"
        );
    }

    /**
     * Создает компонент для чекбокса по ID
     */
    public static CheckboxComponent createById(String checkboxId, String checkboxName) {
        return new CheckboxComponent(
            By.xpath("//input[@id='" + checkboxId + "']"),
            "Чекбокс ID '" + checkboxName + "'"
        );
    }

    /**
     * Создает компонент для чекбокса по имени
     */
    public static CheckboxComponent createByName(String checkboxName, String displayName) {
        return new CheckboxComponent(
            By.xpath("//input[@name='" + checkboxName + "']"),
            "Чекбокс Name '" + displayName + "'"
        );
    }

    /**
     * Создает компонент для чекбокса по значению
     */
    public static CheckboxComponent createByValue(String value, String checkboxName) {
        return new CheckboxComponent(
            By.xpath("//input[@value='" + value + "']"),
            "Чекбокс Value '" + checkboxName + "'"
        );
    }

    /**
     * Создает компонент для чекбокса по любому XPath
     */
    public static CheckboxComponent createCustom(String xpath, String checkboxName) {
        return new CheckboxComponent(By.xpath(xpath), "Чекбокс '" + checkboxName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Убеждается, что чекбокс отмечен (check if not checked)
     */
    public CheckboxComponent ensureChecked() {
        if (!isChecked()) {
            check();
        }
        return this;
    }

    /**
     * Убеждается, что чекбокс не отмечен (uncheck if checked)
     */
    public CheckboxComponent ensureUnchecked() {
        if (isChecked()) {
            uncheck();
        }
        return this;
    }

    /**
     * Проверяет соответствие ожидаемому состоянию
     */
    public boolean matchesState(boolean expectedChecked) {
        boolean actualChecked = isChecked();
        boolean matches = actualChecked == expectedChecked;
        logger.debug("Проверка состояния чекбокса '{}' - ожидалось {}, получено {}: {}",
                    componentName, expectedChecked, actualChecked, matches);
        return matches;
    }

    /**
     * Получает текст лейбла чекбокса
     */
    public String getLabelText() {
        try {
            return checkboxLabel.getText();
        } catch (Exception e) {
            logger.warn("Не удалось получить текст лейбла для чекбокса '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Получает значение чекбокса
     */
    public String getValue() {
        try {
            return checkboxInput.getAttribute("value");
        } catch (Exception e) {
            logger.warn("Не удалось получить значение чекбокса '{}': {}", componentName, e.getMessage());
            return "";
        }
    }
}