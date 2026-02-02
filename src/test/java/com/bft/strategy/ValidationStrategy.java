package com.bft.strategy;

import org.testng.asserts.SoftAssert;

/**
 * Стратегия валидации результатов теста
 * Определяет, как проверять результаты выполнения теста
 */
public interface ValidationStrategy<T> {

    /**
     * Выполняет основные проверки
     */
    void validate(T actualResult, T expectedResult, SoftAssert softAssert);

    /**
     * Выполняет дополнительные проверки
     */
    void validateAdditionalConditions(T result, SoftAssert softAssert);

    /**
     * Проверяет бизнес-правила
     */
    void validateBusinessRules(T result, SoftAssert softAssert);

    /**
     * Проверяет производительность
     */
    void validatePerformance(long executionTime, SoftAssert softAssert);

    /**
     * Проверяет корректность данных
     */
    void validateDataIntegrity(T result, SoftAssert softAssert);

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