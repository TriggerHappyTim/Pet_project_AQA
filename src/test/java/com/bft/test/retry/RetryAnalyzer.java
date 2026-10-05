package com.bft.test.retry;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Анализатор повторных попыток выполнения тестов для flaky тестов
 * 
 * <p>JUnit 5 Extension, которая автоматически повторяет выполнение тестов при их провале.
 * Используется для обработки нестабильных (flaky) тестов, которые могут
 * падать из-за временных проблем (сеть, таймауты, состояние окружения).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * @ExtendWith(RetryAnalyzer.class)
 * @Test
 * public void verifyCryptoProPluginLoaded() {
 *     // тест
 * }
 * 
 * // Или с кастомным количеством попыток
 * @ExtendWith(RetryAnalyzer.class)
 * @Retry(maxAttempts = 5)
 * @Test
 * public void flakyTest() {
 *     // тест
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see Retry для аннотации с настройками retry
 * @since 2.0
 */
public class RetryAnalyzer implements TestExecutionExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);

    /**
     * Максимальное количество попыток по умолчанию
     */
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    /**
     * Счетчик попыток для каждого теста
     */
    private final Map<String, Integer> retryCounters = new ConcurrentHashMap<>();

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Method testMethod = context.getRequiredTestMethod();
        String testId = getTestId(context);

        Retry retryAnnotation = testMethod.getAnnotation(Retry.class);
        int maxRetryCount = retryAnnotation != null ? retryAnnotation.maxAttempts() : DEFAULT_MAX_RETRY_COUNT;

        if (!isRetryable(throwable)) {
            logger.warn("Ошибка теста {} не является retryable, повтор не будет выполнен", testMethod.getName());
            retryCounters.remove(testId);
            throw throwable;
        }

        int currentRetry = retryCounters.getOrDefault(testId, 0);

        if (currentRetry < maxRetryCount) {
            retryCounters.put(testId, currentRetry + 1);
            logger.warn("Попытка {}/{} для теста {} после ошибки: {}",
                currentRetry + 1, maxRetryCount, testMethod.getName(), getErrorMessage(throwable));
            return;
        }

        logger.error("Достигнуто максимальное количество попыток ({}) для теста {}",
            maxRetryCount, testMethod.getName());
        retryCounters.remove(testId);
        throw throwable;
    }

    /**
     * Проверяет, является ли ошибка retryable (можно ли повторить тест)
     */
    private boolean isRetryable(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        String errorMessage = throwable.getMessage();
        if (errorMessage == null) {
            errorMessage = throwable.getClass().getSimpleName();
        }

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

        String[] retryableErrors = {
            "TimeoutException",
            "ElementNotFoundException",
            "StaleElementReferenceException",
            "WebDriverException",
            "ConnectionException",
            "SocketTimeoutException",
            "ReadTimeoutException",
            "AssertionError"
        };

        for (String retryable : retryableErrors) {
            if (errorMessage.contains(retryable) ||
                throwable.getClass().getSimpleName().contains(retryable)) {
                return true;
            }
        }

        return true;
    }

    /**
     * Получает сообщение об ошибке из исключения
     */
    private String getErrorMessage(Throwable throwable) {
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
     * Сбрасывает счетчик попыток для теста
     */
    public void reset(ExtensionContext context) {
        retryCounters.remove(getTestId(context));
    }

    private String getTestId(ExtensionContext context) {
        return context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
    }
}
