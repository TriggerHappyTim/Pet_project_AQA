package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import com.bft.ui.core.element.SmartElementList;
import com.bft.pw.By;

/**
 * Компонент для работы с таблицами
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с таблицами:
 * - Получение количества строк и колонок
 * - Клик по строкам и ячейкам
 * - Поиск строк по содержимому
 * - Проверка наличия данных
 * - Работа с заголовками таблицы
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * Автоматическое ожидание загрузки таблицы перед взаимодействием.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Клик по первой строке таблицы
 * TableComponent.createTable("results", "Таблица результатов").clickFirstRow();
 * 
 * // Проверка количества строк
 * int rowCount = TableComponent.createTable("data", "Таблица данных").getRowCount();
 * 
 * // Поиск строки по тексту
 * TableComponent.createTable("users", "Таблица пользователей").findRowByText("Иванов");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class TableComponent extends BaseComponent {

    private final SmartElementList tableRows;

    /**
     * Конструктор компонента таблицы
     * @param tableSelector селектор таблицы
     * @param tableName имя таблицы для логирования
     */
    public TableComponent(String tableSelector, String tableName) {
        super(ElementFactory.css(tableSelector).named(tableName).waitVisible().build(), tableName);
        this.tableRows = ElementFactory.listBy(By.xpath(tableSelector + "//tbody//tr"))
                .named("Строки таблицы " + tableName)
                .build();
    }

    /**
     * Конструктор компонента таблицы с By селектором
     * @param tableLocator By селектор таблицы
     * @param tableName имя таблицы для логирования
     */
    public TableComponent(By tableLocator, String tableName) {
        super(ElementFactory.by(tableLocator).named(tableName).waitVisible().build(), tableName);
        this.tableRows = ElementFactory.listBy(By.xpath("//tbody//tr"))
                .named("Строки таблицы " + tableName)
                .build();
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible();
    }

    /**
     * Получает количество строк в таблице
     * 
     * Подсчитывает количество строк в tbody таблицы.
     * 
     * @return количество строк в таблице (0 если таблица пустая)
     */
    public int getRowCount() {
        int count = tableRows.size();
        logger.debug("Таблица '{}' содержит {} строк", componentName, count);
        return count;
    }

    /**
     * Получает количество колонок в таблице
     * 
     * Определяет количество колонок по первой строке таблицы.
     * Предполагается, что все строки имеют одинаковое количество колонок.
     * 
     * @return количество колонок в таблице (0 если таблица пустая)
     */
    public int getColumnCount() {
        if (getRowCount() == 0) {
            return 0;
        }
        // Предполагаем, что все строки имеют одинаковое количество колонок
        SmartElement firstRow = tableRows.get(0);
        int columnCount = firstRow.getElement().findElements(By.xpath(".//td")).size();
        logger.debug("Таблица '{}' содержит {} колонок", componentName, columnCount);
        return columnCount;
    }

    /**
     * Проверяет, что таблица не пустая
     * 
     * Проверяет наличие хотя бы одной строки данных в таблице.
     * 
     * @return true если таблица содержит данные, false если таблица пустая
     */
    public boolean isNotEmpty() {
        boolean notEmpty = getRowCount() > 0;
        logger.debug("Таблица '{}' {} пустая", componentName, notEmpty ? "не" : "");
        return notEmpty;
    }

    /**
     * Проверяет, что таблица пустая
     * 
     * Проверяет отсутствие строк данных в таблице.
     * 
     * @return true если таблица пустая, false если таблица содержит данные
     */
    public boolean isEmpty() {
        return !isNotEmpty();
    }

    /**
     * Кликает по строке с указанным индексом
     * 
     * Выполняет клик по строке таблицы по её порядковому номеру (начиная с 0).
     * 
     * @param rowIndex индекс строки для клика (0 - первая строка, 1 - вторая и т.д.)
     * @return текущий экземпляр TableComponent для цепочки вызовов
     * @throws IndexOutOfBoundsException если индекс выходит за границы таблицы
     */
    public TableComponent clickRow(int rowIndex) {
        logger.info("Кликаем по строке {} в таблице: {}", rowIndex, componentName);
        SmartElement row = tableRows.get(rowIndex);
        row.click();
        return this;
    }

    /**
     * Кликает по первой строке таблицы
     * 
     * Удобный метод для клика по первой строке (индекс 0).
     * 
     * @return текущий экземпляр TableComponent для цепочки вызовов
     * @throws IndexOutOfBoundsException если таблица пустая
     */
    public TableComponent clickFirstRow() {
        return clickRow(0);
    }

    /**
     * Кликает по ячейке с указанными координатами
     */
    public TableComponent clickCell(int rowIndex, int columnIndex) {
        logger.info("Кликаем по ячейке [{},{}] в таблице: {}", rowIndex, columnIndex, componentName);
        SmartElement cell = ElementFactory.xpath(".//td[" + (columnIndex + 1) + "]")
                .named("Ячейка [" + rowIndex + "," + columnIndex + "]")
                .build();
        cell.click();
        return this;
    }

    /**
     * Получает текст из ячейки
     */
    public String getCellText(int rowIndex, int columnIndex) {
        SmartElement cell = ElementFactory.xpath(".//td[" + (columnIndex + 1) + "]")
                .named("Ячейка [" + rowIndex + "," + columnIndex + "]")
                .build();
        String text = cell.getText();
        logger.debug("Получен текст '{}' из ячейки [{},{}] таблицы: {}", text, rowIndex, columnIndex, componentName);
        return text;
    }

    /**
     * Получает текст из всей строки
     */
    public String[] getRowText(int rowIndex) {
        int columnCount = getColumnCount();
        String[] rowData = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            rowData[i] = getCellText(rowIndex, i);
        }

        logger.debug("Получена строка {} из таблицы '{}': {}", rowIndex, componentName, String.join(" | ", rowData));
        return rowData;
    }

    /**
     * Ищет строку по тексту в указанной колонке
     */
    public int findRowByCellText(int columnIndex, String text) {
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String cellText = getCellText(i, columnIndex);
            if (text.equals(cellText)) {
                logger.debug("Найдена строка {} по тексту '{}' в колонке {} таблицы: {}",
                            i, text, columnIndex, componentName);
                return i;
            }
        }
        logger.warn("Строка с текстом '{}' в колонке {} не найдена в таблице: {}", text, columnIndex, componentName);
        return -1;
    }

    /**
     * Кликает по строке, содержащей указанный текст
     */
    public TableComponent clickRowByText(String text) {
        return clickRowByText(text, 0); // Поиск в первой колонке
    }

    /**
     * Кликает по строке, содержащей указанный текст в указанной колонке
     */
    public TableComponent clickRowByText(String text, int columnIndex) {
        int rowIndex = findRowByCellText(columnIndex, text);
        if (rowIndex >= 0) {
            return clickRow(rowIndex);
        }
        throw new IllegalArgumentException("Строка с текстом '" + text + "' не найдена в таблице: " + componentName);
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ ТАБЛИЦ =====

    /**
     * Создает компонент для основной таблицы
     */
    public static TableComponent createMainTable(String tableName) {
        return new TableComponent(
            By.xpath("//table[@class='']"),
            "Главная таблица '" + tableName + "'"
        );
    }

    /**
     * Создает компонент для таблицы с указанным классом
     */
    public static TableComponent createClassTable(String className, String tableName) {
        return new TableComponent(
            By.xpath("//table[@class='" + className + "']"),
            "Таблица класса '" + tableName + "'"
        );
    }

    /**
     * Создает компонент для таблицы с указанным ID
     */
    public static TableComponent createIdTable(String tableId, String tableName) {
        return new TableComponent(
            By.xpath("//table[@id='" + tableId + "']"),
            "Таблица ID '" + tableName + "'"
        );
    }

    /**
     * Создает компонент для таблицы результатов
     */
    public static TableComponent createResultsTable(String tableName) {
        return new TableComponent(
            By.xpath("//table//tbody"),
            "Таблица результатов '" + tableName + "'"
        );
    }

    /**
     * Создает компонент для кастомной таблицы
     */
    public static TableComponent createCustomTable(String xpath, String tableName) {
        return new TableComponent(By.xpath(xpath), "Таблица '" + tableName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Ожидает загрузки таблицы
     */
    public TableComponent waitForLoad() {
        logger.info("Ожидаем загрузки таблицы: {}", componentName);
        ElementFactory.xpath("//table//tbody/*[1]/*[6]")
                .named("Первый элемент таблицы")
                .waitVisible()
                .build();
        logger.info("Таблица '{}' загружена", componentName);
        return this;
    }

    /**
     * Проверяет, что таблица содержит ожидаемое количество строк
     */
    public boolean hasExpectedRowCount(int expectedCount) {
        int actualCount = getRowCount();
        boolean matches = actualCount == expectedCount;
        logger.debug("Проверка количества строк в таблице '{}' - ожидалось {}, получено {}: {}",
                    componentName, expectedCount, actualCount, matches);
        return matches;
    }

    /**
     * Проверяет, что таблица содержит не менее указанного количества строк
     */
    public boolean hasAtLeastRows(int minRowCount) {
        int actualCount = getRowCount();
        boolean hasEnough = actualCount >= minRowCount;
        logger.debug("Проверка минимального количества строк в таблице '{}' - минимум {}, получено {}: {}",
                    componentName, minRowCount, actualCount, hasEnough);
        return hasEnough;
    }

    /**
     * Получает заголовок таблицы (если есть)
     */
    public String getTableTitle() {
        try {
            SmartElement title = ElementFactory.xpath("//div[@class='formToolbarTitle'][1]")
                    .named("Заголовок таблицы")
                    .build();
            String titleText = title.getText();
            logger.debug("Получен заголовок таблицы '{}': {}", componentName, titleText);
            return titleText;
        } catch (Exception e) {
            logger.warn("Не удалось получить заголовок таблицы '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Проверяет наличие ошибки на странице таблицы
     */
    public boolean hasError() {
        try {
            SmartElement error = ElementFactory.xpath("//i[@class = 'ps-icon-x ps-alert-close']")
                    .named("Индикатор ошибки")
                    .build();
            boolean hasError = error.isVisible();
            if (hasError) {
                logger.warn("Обнаружена ошибка в таблице '{}'", componentName);
            }
            return hasError;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Закрывает алерт с ошибкой (если есть)
     */
    public TableComponent closeErrorAlert() {
        if (hasError()) {
            logger.info("Закрываем алерт с ошибкой в таблице: {}", componentName);
            SmartElement closeButton = ElementFactory.xpath("//i[@class= 'ps-icon-x ps-alert-close']")
                    .named("Кнопка закрытия ошибки")
                    .build();
            closeButton.click();
        }
        return this;
    }
}