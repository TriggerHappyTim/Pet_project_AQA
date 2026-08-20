package com.bft.pw;

import java.time.Duration;

/**
 * Конфигурация адаптера, совместимая с Selenide {@code Configuration}.
 */
public final class Configuration {

    private Configuration() {
    }

    /** Таймаут ожидания по умолчанию для shouldBe/shouldHave без явного указания. */
    public static Duration timeout = Duration.ofSeconds(4);

    public static Duration pollingInterval = Duration.ofMillis(100);
}