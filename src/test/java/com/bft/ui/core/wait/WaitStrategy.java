package com.bft.ui.core.wait;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Function;

/**
 * Стратегия ожидания элементов и условий
 */
public interface WaitStrategy {

    Logger logger = LoggerFactory.getLogger(WaitStrategy.class);

    /**
     * Выполняет ожидание с описанием
     */
    void wait(String description);

    /**
     * Ожидает элемент с указанным именем
     */
    void waitFor(SelenideElement element, String elementName);

    /**
     * Возвращает таймаут стратегии
     */
    Duration getTimeout();

    /**
     * Возвращает интервал опроса
     */
    Duration getPollingInterval();

    /**
     * Базовая реализация стратегии ожидания
     */
    abstract class BaseWaitStrategy implements WaitStrategy {

        protected final Duration timeout;
        protected final Duration pollingInterval;

        protected BaseWaitStrategy(Duration timeout, Duration pollingInterval) {
            this.timeout = timeout;
            this.pollingInterval = pollingInterval;
        }

        @Override
        public Duration getTimeout() {
            return timeout;
        }

        @Override
        public Duration getPollingInterval() {
            return pollingInterval;
        }

        protected WebDriver getDriver() {
            return WebDriverRunner.getWebDriver();
        }

        protected WebDriverWait createWebDriverWait() {
            WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
            wait.withTimeout(timeout);
            wait.pollingEvery(pollingInterval);
            return wait;
        }

        protected void logWait(String description) {
            logger.debug("Ожидаем: {}", description);
        }
    }
}