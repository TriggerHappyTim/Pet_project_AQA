package com.bft.ui.core.wait;

import com.bft.pw.Condition;
import com.bft.pw.PwDriver;
import com.bft.pw.PwWait;
import com.bft.pw.SelenideElement;
import com.bft.pw.TimeoutException;

import java.time.Duration;
import java.util.function.Function;

/**
 * Фабрика и реализации стратегий ожидания
 */
public class WaitStrategies {

    // Стандартные таймауты
    public static final Duration SHORT_TIMEOUT = Duration.ofSeconds(3);
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration LONG_TIMEOUT = Duration.ofSeconds(30);
    public static final Duration POLLING_INTERVAL = Duration.ofMillis(500);

    /**
     * Быстрое ожидание видимости элемента
     */
    public static WaitStrategy visible() {
        return new VisibleWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL);
    }

    /**
     * Быстрое ожидание видимости элемента с кастомным таймаутом
     */
    public static WaitStrategy visible(Duration timeout) {
        return new VisibleWaitStrategy(timeout, POLLING_INTERVAL);
    }

    /**
     * Ожидание кликабельности элемента
     */
    public static WaitStrategy clickable() {
        return new ClickableWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL);
    }

    /**
     * Ожидание кликабельности элемента с кастомным таймаутом
     */
    public static WaitStrategy clickable(Duration timeout) {
        return new ClickableWaitStrategy(timeout, POLLING_INTERVAL);
    }

    /**
     * Ожидание присутствия элемента в DOM
     */
    public static WaitStrategy present() {
        return new PresentWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL);
    }

    /**
     * Ожидание исчезновения элемента
     */
    public static WaitStrategy disappear() {
        return new DisappearWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL);
    }

    /**
     * Ожидание изменения текста элемента
     */
    public static WaitStrategy textChange(String initialText) {
        return new TextChangeWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL, initialText);
    }

    /**
     * Ожидание выполнения JavaScript условия
     */
    public static WaitStrategy jsCondition(String condition) {
        return new JsConditionWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL, condition);
    }

    /**
     * Ожидание загрузки страницы
     */
    public static WaitStrategy pageLoad() {
        return new PageLoadWaitStrategy(LONG_TIMEOUT, POLLING_INTERVAL);
    }

    /**
     * Кастомное ожидание с функцией условия
     */
    public static WaitStrategy custom(Function<PwDriver, Boolean> condition) {
        return new CustomWaitStrategy(DEFAULT_TIMEOUT, POLLING_INTERVAL, condition);
    }

    // Реализации стратегий

    private static class VisibleWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        public VisibleWaitStrategy(Duration timeout, Duration pollingInterval) {
            super(timeout, pollingInterval);
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем условие: " + description);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        return "complete".equals(driver.executeScript("return document.readyState"));
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                logWait("Таймаут ожидания условия: " + description);
            }
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            logWait("Ожидаем видимости элемента: " + elementName);
            try {
                element.shouldBe(Condition.visible, timeout);
            } catch (Exception e) {
                throw new TimeoutException("Элемент '" + elementName + "' не стал видимым за " + timeout);
            }
        }
    }

    private static class ClickableWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        public ClickableWaitStrategy(Duration timeout, Duration pollingInterval) {
            super(timeout, pollingInterval);
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем кликабельности: " + description);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        Object readyState = driver.executeScript("return document.readyState");
                        return "complete".equals(readyState);
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                logWait("Таймаут ожидания кликабельности: " + description);
            }
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            logWait("Ожидаем кликабельности элемента: " + elementName);
            try {
                element.shouldBe(Condition.and("visible and enabled", Condition.visible, Condition.enabled), timeout);
            } catch (Exception e) {
                throw new TimeoutException("Элемент '" + elementName + "' не стал кликабельным за " + timeout);
            }
        }
    }

    private static class PresentWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        public PresentWaitStrategy(Duration timeout, Duration pollingInterval) {
            super(timeout, pollingInterval);
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем присутствия: " + description);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        return "complete".equals(driver.executeScript("return document.readyState"));
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                logWait("Таймаут ожидания присутствия: " + description);
            }
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            logWait("Ожидаем присутствия элемента: " + elementName);
            try {
                element.shouldBe(Condition.exist, timeout);
            } catch (Exception e) {
                throw new TimeoutException("Элемент '" + elementName + "' не появился в DOM за " + timeout);
            }
        }
    }

    private static class DisappearWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        public DisappearWaitStrategy(Duration timeout, Duration pollingInterval) {
            super(timeout, pollingInterval);
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем исчезновения: " + description);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        Object readyState = driver.executeScript("return document.readyState");
                        Object noLoaders = driver.executeScript(
                                "return document.querySelectorAll('[class*=\"loading\"], [class*=\"spinner\"], [class*=\"loader\"]').length === 0");
                        return "complete".equals(readyState) && Boolean.TRUE.equals(noLoaders);
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                logWait("Таймаут ожидания исчезновения: " + description);
            }
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            logWait("Ожидаем исчезновения элемента: " + elementName);
            try {
                element.shouldBe(Condition.disappear, timeout);
            } catch (Exception e) {
                throw new TimeoutException("Элемент '" + elementName + "' не исчез за " + timeout);
            }
        }
    }

    private static class TextChangeWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        private final String initialText;

        public TextChangeWaitStrategy(Duration timeout, Duration pollingInterval, String initialText) {
            super(timeout, pollingInterval);
            this.initialText = initialText;
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем изменения текста: " + description);
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            logWait("Ожидаем изменения текста элемента: " + elementName);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        String currentText = element.getText();
                        return !initialText.equals(currentText);
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                throw new TimeoutException("Текст элемента '" + elementName + "' не изменился за " + timeout);
            }
        }
    }

    private static class JsConditionWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        private final String condition;

        public JsConditionWaitStrategy(Duration timeout, Duration pollingInterval, String condition) {
            super(timeout, pollingInterval);
            this.condition = condition;
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем JS условия: " + description);
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            logWait("Ожидаем JS условия для элемента: " + elementName);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        Object result = driver.executeScript("return " + condition);
                        return Boolean.TRUE.equals(result);
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                throw new TimeoutException("JS условие не выполнено за " + timeout + ": " + condition);
            }
        }
    }

    private static class PageLoadWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        public PageLoadWaitStrategy(Duration timeout, Duration pollingInterval) {
            super(timeout, pollingInterval);
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем загрузки страницы: " + description);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(driver -> {
                    try {
                        return "complete".equals(driver.executeScript("return document.readyState"));
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (TimeoutException e) {
                throw new TimeoutException("Страница не загрузилась за " + timeout);
            }
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            wait("Загрузка страницы");
        }
    }

    private static class CustomWaitStrategy extends WaitStrategy.BaseWaitStrategy {
        private final Function<PwDriver, Boolean> condition;

        public CustomWaitStrategy(Duration timeout, Duration pollingInterval,
                                  Function<PwDriver, Boolean> condition) {
            super(timeout, pollingInterval);
            this.condition = condition;
        }

        @Override
        public void wait(String description) {
            logWait("Ожидаем кастомного условия: " + description);
            PwWait wait = createWebDriverWait();
            try {
                wait.until(condition);
            } catch (TimeoutException e) {
                throw new TimeoutException("Кастомное условие не выполнено за " + timeout + ": " + description);
            }
        }

        @Override
        public void waitFor(SelenideElement element, String elementName) {
            wait("Кастомное условие для элемента: " + elementName);
        }
    }
}