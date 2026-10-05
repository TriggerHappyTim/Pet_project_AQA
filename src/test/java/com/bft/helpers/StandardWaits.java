package com.bft.helpers;

import com.bft.enums.TimeoutConstants;
import com.bft.pw.Condition;
import com.bft.pw.SelenideElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.bft.enums.TimeoutConstants.AJAX_WAIT;
import static com.bft.enums.TimeoutConstants.DEFAULT_WAIT;
import static com.bft.enums.TimeoutConstants.MODAL_WAIT;
import static com.bft.enums.TimeoutConstants.PAGE_LOAD_WAIT;
import static com.bft.enums.TimeoutConstants.SHORT_WAIT;

/**
 * Типовые ожидания для UI тестов
 * 
 * Предоставляет удобные методы для наиболее часто используемых ожиданий в UI тестах.
 * Использует константы из {@link TimeoutConstants} и Selenide Conditions
 * для умного ожидания элементов.
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>Ожидание видимости элементов</li>
 *   <li>Ожидание кликабельности элементов</li>
 *   <li>Ожидание исчезновения элементов (спиннеры, загрузки)</li>
 *   <li>Ожидание загрузки страницы</li>
 *   <li>Ожидание изменения текста</li>
 *   <li>Ожидание появления/исчезновения модальных окон</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * import static com.bft.helpers.StandardWaits.*;
 * 
 * // Ожидание видимости элемента
 * waitForVisible($("#user-name"));
 * 
 * // Ожидание кликабельности кнопки
 * waitForClickable($("#submit-button"));
 * 
 * // Ожидание исчезновения спиннера
 * waitForSpinnerToDisappear();
 * 
 * // Ожидание загрузки страницы
 * waitForPageLoad();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see TimeoutConstants для констант таймаутов
 * @see com.bft.ui.core.wait.WaitStrategies для более сложных стратегий ожидания
 * @since 2.0
 */
public class StandardWaits {
    
    private static final Logger logger = LoggerFactory.getLogger(StandardWaits.class);
    
    /**
     * Ожидает видимости элемента
     * 
     * Использует стандартный таймаут из {@link TimeoutConstants#DEFAULT_WAIT}.
     * 
     * @param element Selenide элемент для ожидания
     * @throws com.bft.pw.ex.ElementShould если элемент не стал видимым за таймаут
     */
    public static void waitForVisible(SelenideElement element) {
        waitForVisible(element, DEFAULT_WAIT);
    }
    
    /**
     * Ожидает видимости элемента с кастомным таймаутом
     * 
     * @param element Selenide элемент для ожидания
     * @param timeout максимальное время ожидания
     * @throws com.bft.pw.ex.ElementShould если элемент не стал видимым за таймаут
     */
    public static void waitForVisible(SelenideElement element, Duration timeout) {
        logger.debug("Ожидание видимости элемента с таймаутом: {}", timeout);
        element.shouldBe(Condition.visible, timeout);
    }
    
    /**
     * Ожидает кликабельности элемента
     * 
     * Элемент должен быть видимым и enabled.
     * Использует стандартный таймаут.
     * 
     * @param element Selenide элемент для ожидания
     * @throws com.bft.pw.ex.ElementShould если элемент не стал кликабельным за таймаут
     */
    public static void waitForClickable(SelenideElement element) {
        waitForClickable(element, DEFAULT_WAIT);
    }
    
    /**
     * Ожидает кликабельности элемента с кастомным таймаутом
     * 
     * @param element Selenide элемент для ожидания
     * @param timeout максимальное время ожидания
     * @throws com.bft.pw.ex.ElementShould если элемент не стал кликабельным за таймаут
     */
    public static void waitForClickable(SelenideElement element, Duration timeout) {
        logger.debug("Ожидание кликабельности элемента с таймаутом: {}", timeout);
        element.shouldBe(Condition.and("visible and enabled", Condition.visible, Condition.enabled), timeout);
    }
    
    /**
     * Ожидает исчезновения элемента
     * 
     * Используется для ожидания исчезновения спиннеров, модальных окон и других временных элементов.
     * Использует стандартный таймаут.
     * 
     * @param element Selenide элемент для ожидания исчезновения
     * @throws com.bft.pw.ex.ElementShould если элемент не исчез за таймаут
     */
    public static void waitForDisappear(SelenideElement element) {
        waitForDisappear(element, DEFAULT_WAIT);
    }
    
    /**
     * Ожидает исчезновения элемента с кастомным таймаутом
     * 
     * @param element Selenide элемент для ожидания исчезновения
     * @param timeout максимальное время ожидания
     * @throws com.bft.pw.ex.ElementShould если элемент не исчез за таймаут
     */
    public static void waitForDisappear(SelenideElement element, Duration timeout) {
        logger.debug("Ожидание исчезновения элемента с таймаутом: {}", timeout);
        element.shouldBe(Condition.disappear, timeout);
    }
    
    /**
     * Ожидает исчезновения спиннеров загрузки
     * 
     * Ищет элементы с классами, содержащими "loading", "spinner", "loader"
     * и ожидает их исчезновения. Использует короткий таймаут для быстрых операций.
     * 
     * @throws com.bft.pw.ex.ElementShould если спиннеры не исчезли за таймаут
     */
    public static void waitForSpinnerToDisappear() {
        waitForSpinnerToDisappear(SHORT_WAIT);
    }
    
    /**
     * Ожидает исчезновения спиннеров загрузки с кастомным таймаутом
     * 
     * @param timeout максимальное время ожидания
     * @throws com.bft.pw.ex.ElementShould если спиннеры не исчезли за таймаут
     */
    public static void waitForSpinnerToDisappear(Duration timeout) {
        logger.debug("Ожидание исчезновения спиннеров загрузки с таймаутом: {}", timeout);
        
        try {
            com.bft.pw.Selenide.$x("//div[contains(@class, 'loading') or contains(@class, 'spinner') or contains(@class, 'loader')]")
                    .shouldBe(Condition.disappear, timeout);
        } catch (Exception e) {
            // Если спиннеры не найдены, это нормально - возможно их нет на странице
            logger.debug("Спиннеры загрузки не найдены или уже исчезли");
        }
    }
    
    /**
     * Ожидает загрузки страницы
     * 
     * Проверяет готовность страницы через JavaScript (document.readyState == "complete")
     * и отсутствие спиннеров загрузки. Использует длинный таймаут для медленных страниц.
     * 
     * @throws RuntimeException если страница не загрузилась за таймаут
     */
    public static void waitForPageLoad() {
        waitForPageLoad(PAGE_LOAD_WAIT);
    }
    
    /**
     * Ожидает загрузки страницы с кастомным таймаутом
     * 
     * @param timeout максимальное время ожидания
     * @throws RuntimeException если страница не загрузилась за таймаут
     */
    public static void waitForPageLoad(Duration timeout) {
        logger.debug("Ожидание загрузки страницы с таймаутом: {}", timeout);
        
        // Ожидаем готовности страницы через JavaScript
        com.bft.pw.Selenide.executeJavaScript(
            "return document.readyState === 'complete'"
        );
        
        // Ожидаем исчезновения спиннеров
        waitForSpinnerToDisappear(SHORT_WAIT);
    }
    
    /**
     * Ожидает появления модального окна
     * 
     * Ищет элементы с классом "modal" или атрибутом role="dialog".
     * Использует таймаут для модальных окон.
     * 
     * @return найденный элемент модального окна
     * @throws com.bft.pw.ex.ElementShould если модальное окно не появилось за таймаут
     */
    public static SelenideElement waitForModal() {
        return waitForModal(MODAL_WAIT);
    }
    
    /**
     * Ожидает появления модального окна с кастомным таймаутом
     * 
     * @param timeout максимальное время ожидания
     * @return найденный элемент модального окна
     * @throws com.bft.pw.ex.ElementShould если модальное окно не появилось за таймаут
     */
    public static SelenideElement waitForModal(Duration timeout) {
        logger.debug("Ожидание появления модального окна с таймаутом: {}", timeout);
        
        SelenideElement modal = com.bft.pw.Selenide.$x(
            "//div[contains(@class, 'modal') or @role='dialog']"
        );
        modal.shouldBe(Condition.visible, timeout);
        return modal;
    }
    
    /**
     * Ожидает исчезновения модального окна
     * 
     * Использует таймаут для модальных окон.
     * 
     * @throws com.bft.pw.ex.ElementShould если модальное окно не исчезло за таймаут
     */
    public static void waitForModalToDisappear() {
        waitForModalToDisappear(MODAL_WAIT);
    }
    
    /**
     * Ожидает исчезновения модального окна с кастомным таймаутом
     * 
     * @param timeout максимальное время ожидания
     * @throws com.bft.pw.ex.ElementShould если модальное окно не исчезло за таймаут
     */
    public static void waitForModalToDisappear(Duration timeout) {
        logger.debug("Ожидание исчезновения модального окна с таймаутом: {}", timeout);
        
        try {
            com.bft.pw.Selenide.$x(
                "//div[contains(@class, 'modal') or @role='dialog']"
            ).shouldBe(Condition.disappear, timeout);
        } catch (Exception e) {
            // Если модальное окно не найдено, это нормально
            logger.debug("Модальное окно не найдено или уже исчезло");
        }
    }
    
    /**
     * Ожидает изменения текста элемента
     * 
     * Ожидает, пока текст элемента не изменится с начального значения.
     * Использует стандартный таймаут.
     * 
     * @param element элемент для проверки текста
     * @param initialText начальный текст элемента
     * @throws RuntimeException если текст не изменился за таймаут
     */
    public static void waitForTextChange(SelenideElement element, String initialText) {
        waitForTextChange(element, initialText, DEFAULT_WAIT);
    }
    
    /**
     * Ожидает изменения текста элемента с кастомным таймаутом
     * 
     * @param element элемент для проверки текста
     * @param initialText начальный текст элемента
     * @param timeout максимальное время ожидания
     * @throws RuntimeException если текст не изменился за таймаут
     */
    public static void waitForTextChange(SelenideElement element, String initialText, Duration timeout) {
        logger.debug("Ожидание изменения текста элемента с таймаутом: {}", timeout);
        
        element.shouldNotHave(Condition.exactText(initialText), timeout);
    }
    
    /**
     * Ожидает появления текста в элементе
     * 
     * Ожидает, пока элемент не будет содержать указанный текст.
     * Использует стандартный таймаут.
     * 
     * @param element элемент для проверки текста
     * @param expectedText ожидаемый текст
     * @throws com.bft.pw.ex.ElementShould если текст не появился за таймаут
     */
    public static void waitForText(SelenideElement element, String expectedText) {
        waitForText(element, expectedText, DEFAULT_WAIT);
    }
    
    /**
     * Ожидает появления текста в элементе с кастомным таймаутом
     * 
     * @param element элемент для проверки текста
     * @param expectedText ожидаемый текст
     * @param timeout максимальное время ожидания
     * @throws com.bft.pw.ex.ElementShould если текст не появился за таймаут
     */
    public static void waitForText(SelenideElement element, String expectedText, Duration timeout) {
        logger.debug("Ожидание появления текста '{}' в элементе с таймаутом: {}", expectedText, timeout);
        element.shouldHave(Condition.text(expectedText), timeout);
    }
    
    /**
     * Ожидает завершения AJAX запросов
     * 
     * Проверяет отсутствие активных AJAX запросов через jQuery (если доступен)
     * и отсутствие спиннеров загрузки. Использует таймаут для AJAX операций.
     * 
     * @throws RuntimeException если AJAX запросы не завершились за таймаут
     */
    public static void waitForAjaxToComplete() {
        waitForAjaxToComplete(AJAX_WAIT);
    }
    
    /**
     * Ожидает завершения AJAX запросов с кастомным таймаутом
     * 
     * @param timeout максимальное время ожидания
     * @throws RuntimeException если AJAX запросы не завершились за таймаут
     */
    public static void waitForAjaxToComplete(Duration timeout) {
        logger.debug("Ожидание завершения AJAX запросов с таймаутом: {}", timeout);
        
        try {
            // Проверяем через jQuery, если доступен
            Boolean ajaxComplete = (Boolean) com.bft.pw.Selenide.executeJavaScript(
                "return typeof jQuery !== 'undefined' ? jQuery.active === 0 : true"
            );
            
            if (!ajaxComplete) {
                // Ждем завершения AJAX запросов
                com.bft.pw.Selenide.executeJavaScript(
                    "return new Promise(function(resolve) { " +
                    "if (typeof jQuery !== 'undefined' && jQuery.active > 0) { " +
                    "var checkInterval = setInterval(function() { " +
                    "if (jQuery.active === 0) { clearInterval(checkInterval); resolve(true); } " +
                    "}, 100); " +
                    "} else { resolve(true); } " +
                    "})"
                );
            }
        } catch (Exception e) {
            logger.debug("jQuery не доступен или произошла ошибка при проверке AJAX: {}", e.getMessage());
        }
        
        // Ожидаем исчезновения спиннеров
        waitForSpinnerToDisappear(SHORT_WAIT);
    }
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров
     */
    private StandardWaits() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
