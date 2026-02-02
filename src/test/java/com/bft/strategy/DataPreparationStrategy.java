package com.bft.strategy;

/**
 * Стратегия подготовки тестовых данных
 * Определяет, как подготавливать данные для теста
 */
public interface DataPreparationStrategy<T> {

    /**
     * Создает тестовые данные
     */
    T createTestData();

    /**
     * Подготавливает тестовые данные (сохранение в БД, файлы и т.д.)
     */
    void prepareTestData(T data);

    /**
     * Очищает тестовые данные после теста
     */
    void cleanupTestData(T data);

    /**
     * Генерирует случайные тестовые данные
     */
    T generateRandomData();

    /**
     * Валидирует подготовленные данные
     */
    boolean validateTestData(T data);

    /**
     * Возвращает тип стратегии подготовки данных
     */
    DataPreparationType getType();

    /**
     * Возвращает приоритет стратегии
     */
    int getPriority();

    /**
     * Типы стратегий подготовки данных
     */
    enum DataPreparationType {
        DATABASE("Database"),
        FILE_SYSTEM("File System"),
        MEMORY("Memory"),
        API("API"),
        MOCK("Mock");

        private final String description;

        DataPreparationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}