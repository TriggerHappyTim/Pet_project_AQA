package com.bft.config;

/**
 * Типы стратегий тестирования
 */
public enum TestStrategyType {
    UI("UI Testing"),
    API("API Testing"),
    MOBILE("Mobile Testing"),
    PERFORMANCE("Performance Testing"),
    SECURITY("Security Testing");

    private final String description;

    TestStrategyType(String description) {
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