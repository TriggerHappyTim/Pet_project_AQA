package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;

/**
 * Фабрика для создания конфигурации браузеров
 */
public interface BrowserConfigFactory {

    /**
     * Возвращает capabilities для браузера
     */
    Capabilities getCapabilities();

    /**
     * Возвращает имя браузера
     */
    String getBrowserName();

    /**
     * Проверяет, доступен ли браузер в текущей среде
     */
    boolean isAvailable();

    /**
     * Настраивает специфичные для браузера опции
     */
    void configureSpecificOptions();
}