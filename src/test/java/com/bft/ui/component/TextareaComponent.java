package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import org.openqa.selenium.By;

/**
 * Компонент для работы с текстовыми областями (textarea)
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с textarea элементами:
 * - Ввод и редактирование многострочного текста
 * - Добавление текста к существующему содержимому
 * - Проверка содержимого (точное совпадение, частичное совпадение)
 * - Работа с комментариями и длинными текстами
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Ввод текста в textarea с меткой
 * TextareaComponent.createLabeledTextarea("comment-label", "comment", "Комментарий")
 *     .setText("Тестовый комментарий");
 * 
 * // Добавление текста к существующему
 * TextareaComponent.createIdTextarea("description", "Описание")
 *     .appendText("\nДополнительная информация");
 * 
 * // Проверка содержимого
 * boolean contains = TextareaComponent.createNamedTextarea("notes", "Заметки")
 *     .containsText("важная информация");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class TextareaComponent extends BaseComponent {

    /**
     * Создает компонент textarea с CSS селектором
     * 
     * @param textareaSelector CSS селектор textarea элемента
     * @param textareaName имя textarea для логирования
     */
    public TextareaComponent(String textareaSelector, String textareaName) {
        super(ElementFactory.css(textareaSelector).named(textareaName).waitVisible().build(), textareaName);
    }

    /**
     * Создает компонент textarea с By селектором
     * 
     * Рекомендуемый способ создания компонента, так как By.xpath
     * более универсален для сложных селекторов.
     * 
     * @param textareaLocator By селектор textarea элемента (обычно XPath)
     * @param textareaName имя textarea для логирования
     */
    public TextareaComponent(By textareaLocator, String textareaName) {
        super(ElementFactory.by(textareaLocator).named(textareaName).waitVisible().build(), textareaName);
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible() && isEnabled();
    }

    /**
     * Устанавливает текст в textarea
     */
    public TextareaComponent setText(String text) {
        logger.info("Устанавливаем текст '{}' в textarea: {}", text.length() > 50 ? text.substring(0, 50) + "..." : text, componentName);
        rootElement.clear();
        rootElement.type(text);
        return this;
    }

    /**
     * Добавляет текст к существующему в textarea
     */
    public TextareaComponent appendText(String text) {
        logger.info("Добавляем текст '{}' в textarea: {}", text.length() > 50 ? text.substring(0, 50) + "..." : text, componentName);
        String currentText = getText();
        String newText = currentText + text;
        rootElement.type(newText);
        return this;
    }

    /**
     * Получает текст из textarea
     */
    public String getText() {
        String text = rootElement.getText();
        logger.debug("Получен текст из textarea '{}' (длина: {}): {}",
                    componentName, text.length(), text.length() > 100 ? text.substring(0, 100) + "..." : text);
        return text;
    }

    /**
     * Очищает textarea
     */
    public TextareaComponent clear() {
        logger.info("Очищаем textarea: {}", componentName);
        rootElement.clear();
        return this;
    }

    /**
     * Проверяет текст в textarea
     */
    public boolean hasText(String expectedText) {
        String actualText = getText();
        boolean matches = expectedText.equals(actualText);
        logger.debug("Проверка текста в textarea '{}' - ожидалось '{}', получено '{}': {}",
                    componentName, expectedText, actualText, matches);
        return matches;
    }

    /**
     * Проверяет, содержит ли textarea указанный текст
     */
    public boolean containsText(String text) {
        String actualText = getText();
        boolean contains = actualText.contains(text);
        logger.debug("Проверка содержания текста '{}' в textarea '{}': {}",
                    text, componentName, contains);
        return contains;
    }

    /**
     * Проверяет, что textarea пустая
     */
    public boolean isEmpty() {
        String text = getText();
        boolean empty = text == null || text.trim().isEmpty();
        logger.debug("Проверка textarea '{}' на пустоту: {}", componentName, empty);
        return empty;
    }

    /**
     * Проверяет, что textarea не пустая
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ TEXTAREA =====

    /**
     * Создает компонент для textarea по лейблу
     */
    public static TextareaComponent createLabeledTextarea(String labelId, String textareaId, String textareaName) {
        return new TextareaComponent(
            By.xpath("//label[@id= '" + labelId + "']//following-sibling::textarea[@id= '" + textareaId + "']"),
            "Textarea '" + textareaName + "'"
        );
    }

    /**
     * Создает компонент для textarea по ID
     */
    public static TextareaComponent createIdTextarea(String textareaId, String textareaName) {
        return new TextareaComponent(
            By.xpath("//textarea[@id= '" + textareaId + "']"),
            "Textarea '" + textareaName + "'"
        );
    }

    /**
     * Создает компонент для textarea по имени
     */
    public static TextareaComponent createNamedTextarea(String textareaName, String displayName) {
        return new TextareaComponent(
            By.xpath("//textarea[@name='" + textareaName + "']"),
            "Textarea '" + displayName + "'"
        );
    }

    /**
     * Создает компонент для textarea по любому XPath
     */
    public static TextareaComponent createCustomTextarea(String xpath, String textareaName) {
        return new TextareaComponent(By.xpath(xpath), "Textarea '" + textareaName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Устанавливает длинный текст (например, комментарий)
     */
    public TextareaComponent setComment(String comment) {
        logger.info("Устанавливаем комментарий в textarea: {}", componentName);
        return setText(comment);
    }

    /**
     * Устанавливает текст с переносами строк
     */
    public TextareaComponent setMultilineText(String... lines) {
        String multilineText = String.join("\n", lines);
        logger.info("Устанавливаем многострочный текст ({} строк) в textarea: {}", lines.length, componentName);
        return setText(multilineText);
    }

    /**
     * Получает количество строк в textarea
     */
    public int getLineCount() {
        String text = getText();
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\n").length;
    }

    /**
     * Получает количество символов в textarea
     */
    public int getCharacterCount() {
        String text = getText();
        return text != null ? text.length() : 0;
    }

    /**
     * Проверяет максимальную длину текста
     */
    public boolean checkMaxLength(int maxLength) {
        int currentLength = getCharacterCount();
        boolean withinLimit = currentLength <= maxLength;
        if (!withinLimit) {
            logger.warn("Текст в textarea '{}' превышает максимальную длину: {} > {}", componentName, currentLength, maxLength);
        }
        return withinLimit;
    }

    /**
     * Устанавливает placeholder текст
     */
    public String getPlaceholder() {
        try {
            return rootElement.getAttribute("placeholder");
        } catch (Exception e) {
            logger.warn("Не удалось получить placeholder для textarea '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Проверяет readonly состояние
     */
    public boolean isReadonly() {
        try {
            String readonly = rootElement.getAttribute("readonly");
            return readonly != null;
        } catch (Exception e) {
            logger.warn("Не удалось проверить readonly состояние textarea '{}': {}", componentName, e.getMessage());
            return false;
        }
    }
}