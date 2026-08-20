package com.bft.ui.core.wait;

import com.bft.pw.PwDriver;
import com.bft.pw.PwWait;
import com.bft.pw.SelenideElement;
import com.bft.pw.WebDriverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

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

        protected PwDriver getDriver() {
            return WebDriverRunner.getWebDriver();
        }

        protected PwWait createWebDriverWait() {
            return new PwWait(timeout, pollingInterval);
        }

        protected void logWait(String description) {
            logger.debug("Ожидаем: {}", description);
        }
    }
}