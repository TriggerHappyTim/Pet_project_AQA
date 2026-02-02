package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import org.openqa.selenium.By;

/**
 * Компонент для работы с полями ввода различных типов
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с input полями:
 * - Текстовые поля
 * - Поля с метками (labels)
 * - Поля с плейсхолдерами
 * - Поля с масками (дата, телефон, СНИЛС)
 * - Числовые поля
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * Автоматическое ожидание видимости поля перед взаимодействием.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Ввод текста в поле с меткой
 * InputComponent.createLabeledInput("Email", "email").setValue("test@example.com");
 * 
 * // Ввод числа
 * InputComponent.createIdInput("age", "Возраст").setNumber(25);
 * 
 * // Ввод значения с маской
 * InputComponent.createIdInput("phone", "Телефон").setMaskedValue("+7 (999) 123-45-67");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class InputComponent extends BaseComponent {

    /**
     * Создает компонент поля ввода с CSS селектором
     * 
     * @param inputSelector CSS селектор поля ввода
     * @param inputName имя поля для логирования
     */
    public InputComponent(String inputSelector, String inputName) {
        super(ElementFactory.css(inputSelector).named(inputName).waitVisible().build(), inputName);
    }

    /**
     * Создает компонент поля ввода с By селектором
     * 
     * Рекомендуемый способ создания компонента, так как By.xpath
     * более универсален для сложных селекторов.
     * 
     * @param inputLocator By селектор поля ввода (обычно XPath)
     * @param inputName имя поля для логирования
     */
    public InputComponent(By inputLocator, String inputName) {
        super(ElementFactory.by(inputLocator).named(inputName).waitVisible().build(), inputName);
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible() && isEnabled();
    }

    /**
     * Устанавливает значение в поле ввода
     * 
     * Вводит текст в поле ввода. Если поле уже содержит значение,
     * оно будет заменено новым.
     * 
     * @param value текст для ввода в поле
     * @return текущий экземпляр InputComponent для цепочки вызовов
     */
    public InputComponent setValue(String value) {
        logger.info("Устанавливаем значение '{}' в поле: {}", value, componentName);
        rootElement.type(value);
        return this;
    }

    /**
     * Очищает поле ввода и устанавливает новое значение
     * 
     * Сначала очищает поле (удаляет существующее значение),
     * затем вводит новое значение.
     * 
     * @param value текст для ввода в поле после очистки
     * @return текущий экземпляр InputComponent для цепочки вызовов
     */
    public InputComponent clearAndSetValue(String value) {
        logger.info("Очищаем и устанавливаем значение '{}' в поле: {}", value, componentName);
        rootElement.clear();
        rootElement.type(value);
        return this;
    }

    /**
     * Получает значение из поля ввода
     * 
     * Извлекает значение из атрибута "value" поля ввода.
     * 
     * @return текущее значение поля ввода или пустая строка, если поле пустое
     */
    public String getValue() {
        String value = rootElement.getAttribute("value");
        logger.debug("Получено значение '{}' из поля: {}", value, componentName);
        return value;
    }

    /**
     * Проверяет значение в поле ввода
     * 
     * Сравнивает текущее значение поля с ожидаемым значением.
     * 
     * @param expectedValue ожидаемое значение для сравнения
     * @return true если значение совпадает, false в противном случае
     */
    public boolean hasValue(String expectedValue) {
        String actualValue = getValue();
        boolean matches = expectedValue.equals(actualValue);
        logger.debug("Проверка значения в поле '{}' - ожидалось '{}', получено '{}': {}",
                    componentName, expectedValue, actualValue, matches);
        return matches;
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ ПОЛЕЙ ВВОДА =====

    /**
     * Создает компонент для поля ввода с меткой (label)
     * 
     * Ищет поле ввода внутри div элемента, содержащего label с указанным текстом.
     * 
     * @param label текст метки поля ввода
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createLabeledInput(String label, String inputName) {
        return new InputComponent(
            By.xpath("//div[label[text() = '" + label + "']]//input"),
            "Поле ввода '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для второго поля ввода с таким же лейблом
     * 
     * Используется когда на странице несколько полей с одинаковой меткой.
     * Выбирает второе поле по порядку.
     * 
     * @param label текст метки поля ввода
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent для второго поля
     */
    public static InputComponent createSecondLabeledInput(String label, String inputName) {
        return new InputComponent(
            By.xpath("(//div[label[text() = '" + label + "']]//input)[2]"),
            "Второе поле ввода '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для поля ввода по ID
     * 
     * Ищет поле ввода по атрибуту id.
     * 
     * @param fieldId ID поля ввода
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createIdInput(String fieldId, String inputName) {
        return new InputComponent(
            By.xpath("//input[@id='" + fieldId + "']"),
            "Поле ввода '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для поля ввода с плейсхолдером
     * 
     * Ищет поле ввода по атрибуту placeholder.
     * 
     * @param placeholder текст плейсхолдера поля ввода
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createPlaceholderInput(String placeholder, String inputName) {
        return new InputComponent(
            By.xpath("//input[@placeholder='" + placeholder + "']"),
            "Поле ввода '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для поля ввода по имени (name атрибут)
     * 
     * Ищет поле ввода по атрибуту name.
     * 
     * @param fieldName имя поля ввода (атрибут name)
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createNamedInput(String fieldName, String inputName) {
        return new InputComponent(
            By.xpath("//input[@name='" + fieldName + "']"),
            "Поле ввода '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для поля ввода по CSS классу
     * 
     * Ищет поле ввода по атрибуту class (точное совпадение).
     * 
     * @param className CSS класс поля ввода
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createClassInput(String className, String inputName) {
        return new InputComponent(
            By.xpath("//input[@class='" + className + "']"),
            "Поле ввода '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для поля ввода по типу
     * 
     * Ищет поле ввода по атрибуту type (например, "text", "email", "password").
     * 
     * @param inputType тип поля ввода (атрибут type)
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createTypeInput(String inputType, String inputName) {
        return new InputComponent(
            By.xpath("//input[@type='" + inputType + "']"),
            "Поле ввода типа '" + inputType + "'"
        );
    }

    /**
     * Создает компонент для поля ввода в модальном окне
     * 
     * Ищет первое поле ввода внутри элемента с классом 'modal-content'.
     * 
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createModalInput(String inputName) {
        return new InputComponent(
            By.xpath("//div[@class = 'modal-content']//input"),
            "Поле ввода в модальном окне '" + inputName + "'"
        );
    }

    /**
     * Создает компонент для поля ввода по произвольному XPath селектору
     * 
     * Используйте когда стандартные factory методы не подходят.
     * Позволяет указать любой XPath для поиска поля ввода.
     * 
     * @param xpath XPath селектор для поиска поля ввода
     * @param inputName имя поля для логирования
     * @return новый экземпляр InputComponent
     */
    public static InputComponent createCustomInput(String xpath, String inputName) {
        return new InputComponent(By.xpath(xpath), inputName);
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Устанавливает целочисленное значение в поле ввода
     * 
     * Преобразует число в строку и вводит в поле.
     * 
     * @param number целое число для ввода
     * @return текущий экземпляр InputComponent для цепочки вызовов
     */
    public InputComponent setNumber(long number) {
        return setValue(String.valueOf(number));
    }

    /**
     * Устанавливает дробное числовое значение в поле ввода
     * 
     * Преобразует число в строку и вводит в поле.
     * 
     * @param number дробное число для ввода
     * @return текущий экземпляр InputComponent для цепочки вызовов
     */
    public InputComponent setNumber(double number) {
        return setValue(String.valueOf(number));
    }

    /**
     * Устанавливает значение для поля с маской (например, дата, телефон)
     * 
     * Использует JavaScript для установки значения и триггера событий.
     * Это более надежный способ, чем посимвольный ввод с задержками.
     * 
     * @param value значение для ввода с учетом маски
     * @return текущий экземпляр InputComponent для цепочки вызовов
     */
    public InputComponent setMaskedValue(String value) {
        logger.info("Устанавливаем замаскированное значение '{}' в поле: {}", value, componentName);
        
        // Очищаем поле и вводим значение
        rootElement.clear();
        rootElement.sendKeys(value);
        
        // Триггерим события input и change для корректной работы маски
        // Используем executeJavaScript вместо execute
        rootElement.executeJavaScript(
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            rootElement
        );
        
        return this;
    }

    /**
     * Проверяет, что поле ввода не пустое
     * 
     * Проверяет наличие значения в поле (не null и не пустая строка после trim).
     * 
     * @return true если поле содержит значение, false если поле пустое
     */
    public boolean isNotEmpty() {
        String value = getValue();
        boolean notEmpty = value != null && !value.trim().isEmpty();
        logger.debug("Проверка поля '{}' на непустоту: {}", componentName, notEmpty);
        return notEmpty;
    }

    /**
     * Проверяет, что поле ввода пустое
     * 
     * Проверяет отсутствие значения в поле (null или пустая строка после trim).
     * 
     * @return true если поле пустое, false если поле содержит значение
     */
    public boolean isEmpty() {
        String value = getValue();
        boolean empty = value == null || value.trim().isEmpty();
        logger.debug("Проверка поля '{}' на пустоту: {}", componentName, empty);
        return empty;
    }
}