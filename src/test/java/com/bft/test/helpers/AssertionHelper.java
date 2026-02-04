package com.bft.test.helpers;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;

/**
 * Вспомогательный класс для улучшения сообщений об ошибках в тестах
 * 
 * <p>Предоставляет методы для создания информативных сообщений об ошибках
 * с контекстом выполнения теста, состоянием элементов и окружения.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * softAssert.assertTrue(
 *     element.isDisplayed(),
 *     AssertionHelper.formatElementError("Кнопка 'Войти'", "должна быть видимой")
 * );
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class AssertionHelper {

    /**
     * Форматирует сообщение об ошибке для проверки элемента
     * 
     * Создает информативное сообщение с контекстом элемента, страницы и состояния.
     * 
     * @param elementName имя элемента для отображения в сообщении
     * @param expectedState ожидаемое состояние элемента
     * @param actualState фактическое состояние элемента (может быть null)
     * @param pageUrl URL страницы, на которой находится элемент
     * @return отформатированное сообщение об ошибке
     */
    public static String formatElementError(String elementName, String expectedState, 
                                           String actualState, String pageUrl) {
        return String.format(
            "Элемент '%s' %s. Фактическое состояние: %s. Страница: %s. Время: %s",
            elementName,
            expectedState,
            actualState != null ? actualState : "не определено",
            pageUrl != null ? pageUrl : getCurrentUrlSafely(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
    }

    /**
     * Упрощенная версия formatElementError без указания фактического состояния
     * 
     * @param elementName имя элемента
     * @param expectedState ожидаемое состояние
     * @return отформатированное сообщение об ошибке
     */
    public static String formatElementError(String elementName, String expectedState) {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            String currentUrl = driver != null ? driver.getCurrentUrl() : null;
            return formatElementError(elementName, expectedState, null, currentUrl);
        } catch (Exception e) {
            return String.format("Элемент '%s' %s", elementName, expectedState);
        }
    }

    /**
     * Форматирует сообщение об ошибке для проверки значения
     * 
     * @param fieldName имя поля/элемента
     * @param expectedValue ожидаемое значение
     * @param actualValue фактическое значение
     * @return отформатированное сообщение об ошибке
     */
    public static String formatValueError(String fieldName, Object expectedValue, Object actualValue) {
        return String.format(
            "Поле '%s' должно содержать значение '%s', но фактически содержит '%s'",
            fieldName,
            expectedValue != null ? expectedValue.toString() : "null",
            actualValue != null ? actualValue.toString() : "null"
        );
    }

    /**
     * Форматирует сообщение об ошибке для проверки состояния страницы
     * 
     * @param pageName имя страницы
     * @param expectedState ожидаемое состояние
     * @param actualState фактическое состояние
     * @return отформатированное сообщение об ошибке
     */
    public static String formatPageStateError(String pageName, String expectedState, String actualState) {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            String currentUrl = driver != null ? driver.getCurrentUrl() : null;
            return String.format(
                "Страница '%s' должна быть в состоянии '%s', но фактически '%s'. URL: %s",
                pageName,
                expectedState,
                actualState,
                currentUrl != null ? currentUrl : "не определен"
            );
        } catch (Exception e) {
            return String.format(
                "Страница '%s' должна быть в состоянии '%s', но фактически '%s'",
                pageName,
                expectedState,
                actualState
            );
        }
    }

    /**
     * Форматирует сообщение об ошибке для проверки API ответа
     * 
     * @param endpoint API endpoint
     * @param expectedStatus ожидаемый статус код
     * @param actualStatus фактический статус код
     * @param responseBody тело ответа (опционально)
     * @return отформатированное сообщение об ошибке
     */
    public static String formatApiError(String endpoint, int expectedStatus, 
                                       int actualStatus, String responseBody) {
        String bodyInfo = responseBody != null && responseBody.length() > 200 
            ? responseBody.substring(0, 200) + "..." 
            : responseBody;
        
        return String.format(
            "API запрос к '%s' вернул статус %d вместо ожидаемого %d. Тело ответа: %s",
            endpoint,
            actualStatus,
            expectedStatus,
            bodyInfo != null ? bodyInfo : "пусто"
        );
    }

    /**
     * Форматирует сообщение об ошибке с контекстом теста
     * 
     * @param testName имя теста
     * @param errorMessage сообщение об ошибке
     * @param additionalContext дополнительный контекст (может быть null)
     * @return отформатированное сообщение об ошибке
     */
    public static String formatTestError(String testName, String errorMessage, String additionalContext) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Тест '%s' провалился: %s", testName, errorMessage));
        
        if (additionalContext != null && !additionalContext.isEmpty()) {
            message.append(String.format(". Контекст: %s", additionalContext));
        }
        
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            if (driver != null) {
                String currentUrl = driver.getCurrentUrl();
                message.append(String.format(". Текущая страница: %s", currentUrl));
            }
        } catch (Exception e) {
            // Игнорируем ошибки получения URL
        }
        
        return message.toString();
    }

    /**
     * Форматирует сообщение об ошибке таймаута
     * 
     * @param elementName имя элемента
     * @param timeout таймаут в секундах
     * @param condition условие, которое не выполнилось
     * @return отформатированное сообщение об ошибке
     */
    public static String formatTimeoutError(String elementName, long timeout, String condition) {
        return String.format(
            "Элемент '%s' не выполнил условие '%s' в течение %d секунд",
            elementName,
            condition,
            timeout
        );
    }

    /**
     * Форматирует сообщение об ошибке валидации данных
     * 
     * @param dataType тип данных
     * @param fieldName имя поля
     * @param reason причина ошибки валидации
     * @return отформатированное сообщение об ошибке
     */
    public static String formatValidationError(String dataType, String fieldName, String reason) {
        return String.format(
            "Валидация данных не прошла. Тип: %s, Поле: %s, Причина: %s",
            dataType,
            fieldName,
            reason
        );
    }
    
    /**
     * Безопасное получение текущего URL страницы
     * 
     * @return текущий URL или null, если не удалось получить
     */
    private static String getCurrentUrlSafely() {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            return driver != null ? driver.getCurrentUrl() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
