package com.bft.strategy;

import com.bft.test.TestAssertions;

/**
 * Стратегия валидации результатов теста
 * Определяет, как проверять результаты выполнения теста
 */
public interface ValidationStrategy<T> {

    /**
     * Выполняет основные проверки
     */
    void validate(T actualResult, T expectedResult, TestAssertions softAssert);

    /**
     * Выполняет дополнительные проверки
     */
    void validateAdditionalConditions(T result, TestAssertions softAssert);

    /**
     * Проверяет бизнес-правила
     */
    void validateBusinessRules(T result, TestAssertions softAssert);

    /**
     * Проверяет производительность
     */
    void validatePerformance(long executionTime, TestAssertions softAssert);

    /**
     * Проверяет корректность данных
     */
    void validateDataIntegrity(T result, TestAssertions softAssert);

    /**
     * Возвращает тип стратегии валидации
     */
    ValidationType getType();

    /**
     * Возвращает приоритет стратегии
     */
    int getPriority();

    /**
     * Проверяет, является ли результат успешным
     */
    boolean isResultSuccessful(T result);

    /**
     * Типы стратегий валидации
     */
    enum ValidationType {
        STRICT("Strict"),
        SOFT("Soft"),
        PERFORMANCE("Performance"),
        SECURITY("Security"),
        BUSINESS_RULES("Business Rules");

        private final String description;

        ValidationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}