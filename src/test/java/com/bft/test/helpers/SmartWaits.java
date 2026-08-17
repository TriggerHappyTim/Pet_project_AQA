package com.bft.test.helpers;

import com.bft.enums.TimeoutConstants;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Вспомогательный класс для умных ожиданий в UI тестах
 * 
 * <p>Предоставляет методы для ожидания различных состояний элементов и страниц
 * с использованием констант таймаутов вместо магических чисел.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * SmartWaits.waitForElementVisible($(".button"), "Кнопка 'Войти'");
 * SmartWaits.waitForPageLoad();
 * SmartWaits.waitForAjaxComplete();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class SmartWaits {

    private static final Logger logger = LoggerFactory.getLogger(SmartWaits.class);

    /** Интервал опроса (мс) в циклах ожидания (readyState, AJAX, спиннеры). */
    private static final long POLL_INTERVAL_MS = 100;

    /**
     * Ожидает видимости элемента с использованием стандартного таймаута
     * 
     * @param element элемент для ожидания
     * @param elementName имя элемента для логирования
     */
    public static void waitForElementVisible(SelenideElement element, String elementName) {
        waitForElementVisible(element, elementName, TimeoutConstants.DEFAULT_WAIT);
    }

    /**
     * Ожидает видимости элемента с кастомным таймаутом
     * 
     * @param element элемент для ожидания
     * @param elementName имя элемента для логирования
     * @param timeout таймаут ожидания
     */
    public static void waitForElementVisible(SelenideElement element, String elementName, Duration timeout) {
        logger.debug("Ожидание видимости элемента: {} (таймаут: {}s)", elementName, timeout.getSeconds());
        try {
            element.shouldBe(Condition.visible, timeout);
            logger.debug("Элемент '{}' стал видимым", elementName);
        } catch (Exception e) {
            logger.error("Элемент '{}' не стал видимым за {} секунд", elementName, timeout.getSeconds());
            throw new AssertionError(
                AssertionHelper.formatTimeoutError(elementName, timeout.getSeconds(), "видимость"),
                e
            );
        }
    }

    /**
     * Ожидает исчезновения элемента с использованием стандартного таймаута
     * 
     * @param element элемент для ожидания
     * @param elementName имя элемента для логирования
     */
    public static void waitForElementDisappear(SelenideElement element, String elementName) {
        waitForElementDisappear(element, elementName, TimeoutConstants.DEFAULT_WAIT);
    }

    /**
     * Ожидает исчезновения элемента с кастомным таймаутом
     * 
     * @param element элемент для ожидания
     * @param elementName имя элемента для логирования
     * @param timeout таймаут ожидания
     */
    public static void waitForElementDisappear(SelenideElement element, String elementName, Duration timeout) {
        logger.debug("Ожидание исчезновения элемента: {} (таймаут: {}s)", elementName, timeout.getSeconds());
        try {
            element.should(Condition.disappear, timeout);
            logger.debug("Элемент '{}' исчез", elementName);
        } catch (Exception e) {
            logger.error("Элемент '{}' не исчез за {} секунд", elementName, timeout.getSeconds());
            throw new AssertionError(
                AssertionHelper.formatTimeoutError(elementName, timeout.getSeconds(), "исчезновение"),
                e
            );
        }
    }

    /**
     * Ожидает кликабельности элемента с использованием стандартного таймаута
     * 
     * @param element элемент для ожидания
     * @param elementName имя элемента для логирования
     */
    public static void waitForElementClickable(SelenideElement element, String elementName) {
        waitForElementClickable(element, elementName, TimeoutConstants.DEFAULT_WAIT);
    }

    /**
     * Ожидает кликабельности элемента с кастомным таймаутом
     * 
     * @param element элемент для ожидания
     * @param elementName имя элемента для логирования
     * @param timeout таймаут ожидания
     */
    public static void waitForElementClickable(SelenideElement element, String elementName, Duration timeout) {
        logger.debug("Ожидание кликабельности элемента: {} (таймаут: {}s)", elementName, timeout.getSeconds());
        try {
            element.shouldBe(Condition.enabled, timeout);
            element.shouldBe(Condition.visible, timeout);
            logger.debug("Элемент '{}' стал кликабельным", elementName);
        } catch (Exception e) {
            logger.error("Элемент '{}' не стал кликабельным за {} секунд", elementName, timeout.getSeconds());
            throw new AssertionError(
                AssertionHelper.formatTimeoutError(elementName, timeout.getSeconds(), "кликабельность"),
                e
            );
        }
    }

    /**
     * Ожидает загрузки страницы через JavaScript readyState
     * 
     * Использует стандартный таймаут загрузки страницы.
     */
    public static void waitForPageLoad() {
        waitForPageLoad(TimeoutConstants.PAGE_LOAD_WAIT);
    }

    /**
     * Ожидает загрузки страницы с кастомным таймаутом
     * 
     * @param timeout таймаут ожидания
     */
    public static void waitForPageLoad(Duration timeout) {
        logger.debug("Ожидание загрузки страницы (таймаут: {}s)", timeout.getSeconds());
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeout.toMillis()) {
                String readyState = (String) js.executeScript("return document.readyState");
                if ("complete".equals(readyState)) {
                    logger.debug("Страница загружена");
                    return;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }

            throw new AssertionError(
                AssertionHelper.formatTimeoutError("Страница", timeout.getSeconds(), "загрузка (readyState != complete)")
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Ожидание загрузки страницы прервано", e);
        } catch (Exception e) {
            logger.error("Ошибка при ожидании загрузки страницы: {}", e.getMessage());
            throw new AssertionError("Не удалось дождаться загрузки страницы", e);
        }
    }

    /**
     * Ожидает завершения всех AJAX запросов через jQuery
     * 
     * Проверяет, что jQuery.active == 0 и нет активных AJAX запросов.
     * Использует стандартный таймаут для AJAX операций.
     */
    public static void waitForAjaxComplete() {
        waitForAjaxComplete(TimeoutConstants.AJAX_WAIT);
    }

    /**
     * Ожидает завершения всех AJAX запросов с кастомным таймаутом
     * 
     * @param timeout таймаут ожидания
     */
    public static void waitForAjaxComplete(Duration timeout) {
        logger.debug("Ожидание завершения AJAX запросов (таймаут: {}s)", timeout.getSeconds());
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeout.toMillis()) {
                // Проверяем jQuery.active (если jQuery доступен)
                Boolean jQueryActive = (Boolean) js.executeScript(
                    "return (typeof jQuery !== 'undefined' && jQuery.active === 0) || typeof jQuery === 'undefined'"
                );
                
                // Проверяем, что нет активных XMLHttpRequest
                Long activeRequests = (Long) js.executeScript(
                    "return (typeof XMLHttpRequest !== 'undefined') ? " +
                    "(window.XMLHttpRequest.activeRequests || 0) : 0"
                );
                
                if (Boolean.TRUE.equals(jQueryActive) && activeRequests != null && activeRequests == 0) {
                    logger.debug("AJAX запросы завершены");
                    return;
                }

                Thread.sleep(POLL_INTERVAL_MS);
            }

            logger.warn("AJAX запросы не завершились за {} секунд", timeout.getSeconds());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Ожидание AJAX запросов прервано");
        } catch (Exception e) {
            logger.warn("Ошибка при проверке AJAX запросов: {}", e.getMessage());
        }
    }

    /**
     * Ожидает исчезновения спиннера загрузки
     * 
     * Ищет элементы с классами, содержащими "spinner", "loading", "loader".
     * Использует стандартный таймаут.
     * 
     * @param spinnerSelectors селекторы спиннеров для проверки
     */
    public static void waitForSpinnerDisappear(String... spinnerSelectors) {
        waitForSpinnerDisappear(TimeoutConstants.DEFAULT_WAIT, spinnerSelectors);
    }

    /**
     * Ожидает исчезновения спиннера загрузки с кастомным таймаутом
     * 
     * @param timeout таймаут ожидания
     * @param spinnerSelectors селекторы спиннеров для проверки
     */
    public static void waitForSpinnerDisappear(Duration timeout, String... spinnerSelectors) {
        logger.debug("Ожидание исчезновения спиннеров загрузки (таймаут: {}s)", timeout.getSeconds());
        
        if (spinnerSelectors == null || spinnerSelectors.length == 0) {
            // Используем стандартные селекторы спиннеров
            String[] defaultSelectors = {
                ".spinner",
                ".loading",
                ".loader",
                "[class*='spinner']",
                "[class*='loading']",
                "[class*='loader']"
            };
            waitForSpinnerDisappear(timeout, defaultSelectors);
            return;
        }
        
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeout.toMillis()) {
                boolean allSpinnersHidden = true;
                
                for (String selector : spinnerSelectors) {
                    Long count = (Long) js.executeScript(
                        String.format(
                            "return document.querySelectorAll('%s').length",
                            selector.replace("'", "\\'")
                        )
                    );
                    
                    if (count != null && count > 0) {
                        // Проверяем, что элементы скрыты (display: none или visibility: hidden)
                        Long visibleCount = (Long) js.executeScript(
                            String.format(
                                "return Array.from(document.querySelectorAll('%s')).filter(el => " +
                                "window.getComputedStyle(el).display !== 'none' && " +
                                "window.getComputedStyle(el).visibility !== 'hidden').length",
                                selector.replace("'", "\\'")
                            )
                        );
                        
                        if (visibleCount != null && visibleCount > 0) {
                            allSpinnersHidden = false;
                            break;
                        }
                    }
                }
                
                if (allSpinnersHidden) {
                    logger.debug("Все спиннеры загрузки исчезли");
                    return;
                }

                Thread.sleep(POLL_INTERVAL_MS);
            }

            logger.warn("Спиннеры загрузки не исчезли за {} секунд", timeout.getSeconds());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Ожидание спиннеров прервано");
        } catch (Exception e) {
            logger.warn("Ошибка при проверке спиннеров: {}", e.getMessage());
        }
    }

    /**
     * Ожидает завершения анимации с использованием стандартного таймаута анимации
     */
    public static void waitForAnimation() {
        waitForAnimation(TimeoutConstants.ANIMATION_NORMAL);
    }

    /**
     * Ожидает завершения анимации с кастомным таймаутом
     * 
     * @param timeout таймаут ожидания
     */
    public static void waitForAnimation(Duration timeout) {
        logger.debug("Ожидание завершения анимации (таймаут: {}s)", timeout.getSeconds());
        try {
            Thread.sleep(timeout.toMillis());
            logger.debug("Анимация завершена");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Ожидание анимации прервано");
        }
    }
}
