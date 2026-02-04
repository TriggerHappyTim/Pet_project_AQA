package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import org.openqa.selenium.By;

/**
 * Компонент для работы с полями дат
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с полями дат:
 * - Установка даты в формате DD-MM-YYYY
 * - Установка даты из компонентов (день, месяц, год)
 * - Установка текущей даты
 * - Установка даты относительно текущей (offset)
 * - Валидация дат (валидность, прошлое/будущее)
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Установка даты в формате DD-MM-YYYY
 * DateComponent.createIdDate("birthDate", "Дата рождения").setDate("01-01-2000");
 * 
 * // Установка текущей даты
 * DateComponent.createLabeledDate("Дата начала", "startDate").setCurrentDate();
 * 
 * // Установка даты через 7 дней от текущей
 * DateComponent.createIdDate("endDate", "Дата окончания").setDateRelative(7);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class DateComponent extends BaseComponent {

    /**
     * Создает компонент поля даты с CSS селектором
     * 
     * @param dateSelector CSS селектор поля даты
     * @param dateName имя поля для логирования
     */
    public DateComponent(String dateSelector, String dateName) {
        super(ElementFactory.css(dateSelector).named(dateName).waitVisible().build(), dateName);
    }

    /**
     * Создает компонент поля даты с By селектором
     * 
     * Рекомендуемый способ создания компонента, так как By.xpath
     * более универсален для сложных селекторов.
     * 
     * @param dateLocator By селектор поля даты (обычно XPath)
     * @param dateName имя поля для логирования
     */
    public DateComponent(By dateLocator, String dateName) {
        super(ElementFactory.by(dateLocator).named(dateName).waitVisible().build(), dateName);
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible() && isEnabled();
    }

    /**
     * Устанавливает дату в формате DD-MM-YYYY
     * 
     * Вводит дату в поле в формате день-месяц-год (например, "01-01-2000").
     * 
     * @param date дата в формате DD-MM-YYYY
     * @return текущий экземпляр DateComponent для цепочки вызовов
     */
    public DateComponent setDate(String date) {
        logger.info("Устанавливаем дату '{}' в поле: {}", date, componentName);
        rootElement.type(date);
        return this;
    }

    /**
     * Устанавливает дату из компонентов (день, месяц, год)
     * 
     * Форматирует дату в формат DD-MM-YYYY и вводит в поле.
     * 
     * @param day день месяца (1-31)
     * @param month месяц (1-12)
     * @param year год (например, 2000)
     * @return текущий экземпляр DateComponent для цепочки вызовов
     */
    public DateComponent setDate(int day, int month, int year) {
        String dateString = String.format("%02d-%02d-%04d", day, month, year);
        return setDate(dateString);
    }

    /**
     * Устанавливает текущую дату
     * 
     * Получает текущую дату системы и вводит её в поле в формате DD-MM-YYYY.
     * 
     * @return текущий экземпляр DateComponent для цепочки вызовов
     */
    public DateComponent setCurrentDate() {
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        String dateString = currentDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        logger.info("Устанавливаем текущую дату '{}' в поле: {}", dateString, componentName);
        return setDate(dateString);
    }

    /**
     * Устанавливает дату относительно текущей
     * 
     * Вычисляет дату как текущая дата плюс указанное количество дней.
     * Отрицательные значения для дат в прошлом.
     * 
     * @param daysOffset смещение в днях от текущей даты (положительное - будущее, отрицательное - прошлое)
     * @return текущий экземпляр DateComponent для цепочки вызовов
     */
    public DateComponent setDateRelative(int daysOffset) {
        java.time.LocalDate targetDate = java.time.LocalDate.now().plusDays(daysOffset);
        String dateString = targetDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        logger.info("Устанавливаем дату '{}' (offset {} дней) в поле: {}", dateString, daysOffset, componentName);
        return setDate(dateString);
    }

    /**
     * Получает значение даты из поля
     * 
     * Извлекает значение из атрибута "value" поля даты.
     * 
     * @return значение даты в формате строки или пустая строка, если поле пустое
     */
    public String getDate() {
        String dateValue = rootElement.getAttribute("value");
        logger.debug("Получена дата '{}' из поля: {}", dateValue, componentName);
        return dateValue;
    }

    /**
     * Проверяет значение даты в поле
     * 
     * Сравнивает текущее значение поля с ожидаемым значением.
     * 
     * @param expectedDate ожидаемая дата для сравнения (формат DD-MM-YYYY)
     * @return true если значение совпадает, false в противном случае
     */
    public boolean hasDate(String expectedDate) {
        String actualDate = getDate();
        boolean matches = expectedDate.equals(actualDate);
        logger.debug("Проверка даты в поле '{}' - ожидалось '{}', получено '{}': {}",
                    componentName, expectedDate, actualDate, matches);
        return matches;
    }

    /**
     * Очищает поле даты
     * 
     * Удаляет значение из поля даты.
     * 
     * @return текущий экземпляр DateComponent для цепочки вызовов
     */
    public DateComponent clearDate() {
        logger.info("Очищаем поле даты: {}", componentName);
        rootElement.getElement().clear();
        return this;
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ ПОЛЕЙ ДАТЫ =====

    /**
     * Создает компонент для поля даты с лейблом (точное совпадение text())
     */
    public static DateComponent createLabeledDate(String label, String dateName) {
        return new DateComponent(
            By.xpath("//div[label[text() = '" + label + "']]//input"),
            "Поле даты '" + dateName + "'"
        );
    }

    /**
     * Создает компонент для поля даты по подстроке метки (MUI/React: метка может быть в span, с пробелами).
     * XPath: div, содержащий любой потомок с текстом, содержащим labelSubstring, затем input внутри.
     */
    public static DateComponent createLabeledDateContains(String labelSubstring, String dateName) {
        return new DateComponent(
            By.xpath("//div[.//*[contains(., '" + labelSubstring.replace("'", "''") + "')]]//input"),
            "Поле даты '" + dateName + "'"
        );
    }

    /**
     * Создает компонент для поля даты с ID
     */
    public static DateComponent createIdDate(String fieldId, String dateName) {
        return new DateComponent(
            By.xpath("//div[child::*[@id= '" + fieldId + "']]//input"),
            "Поле даты '" + dateName + "'"
        );
    }

    /**
     * Создает компонент для поля даты по имени лейбла
     */
    public static DateComponent createLabelDate(String labelId, String dateName) {
        return new DateComponent(
            By.xpath("//div[child::*[@id= '" + labelId + "']]//input"),
            "Поле даты '" + dateName + "'"
        );
    }

    /**
     * Создает компонент для поля даты по любому XPath
     */
    public static DateComponent createCustomDate(String xpath, String dateName) {
        return new DateComponent(By.xpath(xpath), "Поле даты '" + dateName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Устанавливает дату начала периода
     */
    public static DateComponent setStartDate(String label, String date) {
        DateComponent dateComponent = createLabeledDate(label, "дата начала");
        return dateComponent.setDate(date);
    }

    /**
     * Устанавливает дату окончания периода
     */
    public static DateComponent setEndDate(String label, String date) {
        DateComponent dateComponent = createLabeledDate(label, "дата окончания");
        return dateComponent.setDate(date);
    }

    /**
     * Устанавливает период дат
     */
    public static void setDatePeriod(String startLabel, String endLabel, String startDate, String endDate) {
        setStartDate(startLabel, startDate);
        setEndDate(endLabel, endDate);
    }

    /**
     * Устанавливает текущий месяц для поля даты
     */
    public DateComponent setCurrentMonth() {
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        String dateString = String.format("01-%02d-%04d", currentDate.getMonthValue(), currentDate.getYear());
        logger.info("Устанавливаем начало текущего месяца '{}' в поле: {}", dateString, componentName);
        return setDate(dateString);
    }

    /**
     * Устанавливает конец текущего месяца для поля даты
     */
    public DateComponent setCurrentMonthEnd() {
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        java.time.LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        String dateString = lastDayOfMonth.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        logger.info("Устанавливаем конец текущего месяца '{}' в поле: {}", dateString, componentName);
        return setDate(dateString);
    }

    /**
     * Проверяет, что дата валидна
     */
    public boolean isValidDate() {
        String dateValue = getDate();
        if (dateValue == null || dateValue.trim().isEmpty()) {
            return false;
        }

        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            java.time.LocalDate.parse(dateValue, formatter);
            logger.debug("Дата '{}' валидна для поля: {}", dateValue, componentName);
            return true;
        } catch (Exception e) {
            logger.warn("Дата '{}' невалидна для поля '{}': {}", dateValue, componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что дата находится в прошлом
     */
    public boolean isPastDate() {
        String dateValue = getDate();
        if (!isValidDate()) {
            return false;
        }

        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            java.time.LocalDate date = java.time.LocalDate.parse(dateValue, formatter);
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean isPast = date.isBefore(today);
            logger.debug("Дата '{}' {} для поля: {}", dateValue, isPast ? "в прошлом" : "не в прошлом", componentName);
            return isPast;
        } catch (Exception e) {
            logger.warn("Ошибка при проверке даты '{}' в поле '{}': {}", dateValue, componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что дата находится в будущем
     */
    public boolean isFutureDate() {
        String dateValue = getDate();
        if (!isValidDate()) {
            return false;
        }

        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            java.time.LocalDate date = java.time.LocalDate.parse(dateValue, formatter);
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean isFuture = date.isAfter(today);
            logger.debug("Дата '{}' {} для поля: {}", dateValue, isFuture ? "в будущем" : "не в будущем", componentName);
            return isFuture;
        } catch (Exception e) {
            logger.warn("Ошибка при проверке даты '{}' в поле '{}': {}", dateValue, componentName, e.getMessage());
            return false;
        }
    }
}