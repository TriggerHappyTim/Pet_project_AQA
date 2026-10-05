package com.bft.strategy;

/**
 * Типы стратегий выполнения тестов
 * 
 * Используется для определения конкретных стратегий выполнения тестов.
 * Отличается от {@link com.bft.config.TestStrategyType}, который определяет общие типы тестов (UI, MOBILE). // API - ЗАКОММЕНТИРОВАНО
 * 
 * @see com.bft.config.TestStrategyType для общих типов тестов
 */
public enum ExecutionStrategyType {
    // UI стратегии
    UI_FORM_SUBMISSION("UI Form Submission"),
    UI_NAVIGATION("UI Navigation"),

    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // API стратегии
    // API_REST_CALL("API REST Call"),
    // API_SOAP_CALL("API SOAP Call"),
    // API_GRAPHQL("API GraphQL"),

    // Data стратегии
    DATA_SQL_VALIDATION("Data SQL Validation"),
    DATA_XML_VALIDATION("Data XML Validation"),
    DATA_JSON_VALIDATION("Data JSON Validation"),

    // Integration стратегии
    INTEGRATION_DATABASE("Integration Database"),
    INTEGRATION_FILE_SYSTEM("Integration File System"),
    INTEGRATION_EXTERNAL_SERVICE("Integration External Service"),

    // Performance стратегии
    PERFORMANCE_LOAD("Performance Load"),
    PERFORMANCE_STRESS("Performance Stress"),
    PERFORMANCE_SPIKE("Performance Spike"),

    // Security стратегии
    SECURITY_AUTHENTICATION("Security Authentication"),
    SECURITY_AUTHORIZATION("Security Authorization"),
    SECURITY_ENCRYPTION("Security Encryption");

    private final String description;

    ExecutionStrategyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
