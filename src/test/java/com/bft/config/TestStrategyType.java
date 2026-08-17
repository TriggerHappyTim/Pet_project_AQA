package com.bft.config;

/**
 * Типы стратегий тестирования, поддерживаемые фреймворком.
 *
 * <p>Используется для явного указания стратегии при инициализации {@link TestConfiguration}.
 * Автоматический выбор стратегии также опирается на эти типы.
 *
 * <p>Текущая реализация поддерживает только UI тестирование.
 * Остальные типы зарезервированы для будущего расширения.
 *
 * @author QA Automation Team
 * @version 2.0
 */
public enum TestStrategyType {

    /**
     * Стратегия для UI тестирования через браузер (Selenide).
     * Единственная полностью реализованная стратегия на текущий момент.
     */
    UI("UI Testing");

    // --- Зарезервированные типы для будущего расширения ---
    // API("API Testing"),
    // MOBILE("Mobile Testing"),
    // PERFORMANCE("Performance Testing"),
    // SECURITY("Security Testing");

    private final String description;

    TestStrategyType(String description) {
        this.description = description;
    }

    /**
     * Возвращает текстовое описание типа стратегии.
     *
     * @return описание (например, "UI Testing")
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * Проверяет, реализована ли данная стратегия в текущей версии фреймворка.
     *
     * @return true если стратегия полностью поддерживается, false если зарезервирована
     */
    public boolean isImplemented() {
        return this == UI;
    }
}