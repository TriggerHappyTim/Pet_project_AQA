package com.bft.test.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Анализатор повторных попыток выполнения тестов для flaky тестов
 * 
 * <p>Позволяет автоматически повторять выполнение тестов при их провале.
 * Используется для обработки нестабильных (flaky) тестов, которые могут
 * падать из-за временных проблем (сеть, таймауты, состояние окружения).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"web", "crypto"})
 * public void verifyCryptoProPluginLoaded() {
 *     // тест
 * }
 * 
 * // Или с кастомным количеством попыток
 * @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"web", "crypto"})
 * @Retry(maxAttempts = 5)
 * public void flakyTest() {
 *     // тест
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @see Retry для аннотации с настройками retry
 * @since 1.0
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    
    /**
     * Максимальное количество попыток по умолчанию
     */
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;
    
    /**
     * Счетчик попыток для текущего теста
     */
    private int retryCount = 0;
    
    /**
     * Максимальное количество попыток для текущего теста
     */
    private int maxRetryCount = DEFAULT_MAX_RETRY_COUNT;

    /**
     * Определяет, нужно ли повторить выполнение теста
     * 
     * Проверяет результат выполнения теста и решает, нужно ли повторить его выполнение.
     * Тест повторяется только если:
     * <ul>
     *   <li>Тест провалился (не был пропущен)</li>
     *   <li>Количество попыток не превысило максимум</li>
     *   <li>Ошибка является retryable (не критическая ошибка конфигурации)</li>
     * </ul>
     * 
     * @param result результат выполнения теста
     * @return true если нужно повторить тест, false в противном случае
     */
    @Override
    public boolean retry(ITestResult result) {
        // Получаем максимальное количество попыток из аннотации @Retry
        Retry retryAnnotation = result.getMethod().getConstructorOrMethod()
            .getMethod().getAnnotation(Retry.class);
        
        if (retryAnnotation != null) {
            maxRetryCount = retryAnnotation.maxAttempts();
        } else {
            maxRetryCount = DEFAULT_MAX_RETRY_COUNT;
        }
        
        // Не повторяем, если тест был пропущен
        if (result.getStatus() == ITestResult.SKIP) {
            logger.debug("Тест {} был пропущен, retry не требуется", result.getName());
            return false;
        }
        
        // Не повторяем, если тест прошел успешно
        if (result.getStatus() == ITestResult.SUCCESS) {
            logger.debug("Тест {} прошел успешно, retry не требуется", result.getName());
            return false;
        }
        
        // Проверяем, является ли ошибка retryable
        if (!isRetryable(result)) {
            logger.warn("Ошибка теста {} не является retryable, повтор не будет выполнен", result.getName());
            return false;
        }
        
        // Проверяем, не превысили ли мы максимальное количество попыток
        if (retryCount < maxRetryCount) {
            retryCount++;
            logger.warn("Попытка {}/{} для теста {} после ошибки: {}",
                retryCount, maxRetryCount, result.getName(), getErrorMessage(result));
            return true;
        }
        
        logger.error("Достигнуто максимальное количество попыток ({}) для теста {}", 
            maxRetryCount, result.getName());
        return false;
    }
    
    /**
     * Проверяет, является ли ошибка retryable (можно ли повторить тест)
     * 
     * Некоторые ошибки не должны приводить к повтору теста, так как они
     * указывают на критические проблемы конфигурации или кода.
     * 
     * @param result результат выполнения теста
     * @return true если ошибка является retryable, false в противном случае
     */
    private boolean isRetryable(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) {
            return false;
        }
        
        String errorMessage = throwable.getMessage();
        if (errorMessage == null) {
            errorMessage = throwable.getClass().getSimpleName();
        }
        
        // Критические ошибки, которые не должны приводить к retry
        String[] nonRetryableErrors = {
            "Test configuration not initialized",
            "IllegalStateException",
            "ClassNotFoundException",
            "NoClassDefFoundError",
            "OutOfMemoryError",
            "StackOverflowError"
        };
        
        for (String nonRetryable : nonRetryableErrors) {
            if (errorMessage.contains(nonRetryable) || 
                throwable.getClass().getSimpleName().contains(nonRetryable)) {
                return false;
            }
        }
        
        // Retryable ошибки (временные проблемы)
        String[] retryableErrors = {
            "TimeoutException",
            "ElementNotFoundException",
            "StaleElementReferenceException",
            "WebDriverException",
            "ConnectionException",
            "SocketTimeoutException",
            "ReadTimeoutException",
            "AssertionError" // Мягкие ошибки проверок можно повторить
        };
        
        for (String retryable : retryableErrors) {
            if (errorMessage.contains(retryable) || 
                throwable.getClass().getSimpleName().contains(retryable)) {
                return true;
            }
        }
        
        // По умолчанию считаем ошибку retryable (можно повторить)
        return true;
    }
    
    /**
     * Получает сообщение об ошибке из результата теста
     * 
     * @param result результат выполнения теста
     * @return сообщение об ошибке или имя класса исключения
     */
    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) {
            return "Unknown error";
        }
        
        String message = throwable.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }
        
        return throwable.getClass().getSimpleName();
    }
    
    /**
     * Сбрасывает счетчик попыток (вызывается перед каждым новым тестом)
     */
    public void reset() {
        retryCount = 0;
        maxRetryCount = DEFAULT_MAX_RETRY_COUNT;
    }
}
