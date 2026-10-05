package com.bft.monitoring;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Детектор нестабильных (flaky) тестов
 * 
 * <p>Отслеживает результаты выполнения тестов и выявляет нестабильные тесты,
 * которые периодически падают без видимых причин.
 * 
 * <p>Тест считается нестабильным, если:
 * <ul>
 *   <li>Он падал хотя бы один раз за последние N выполнений</li>
 *   <li>Процент успешных выполнений ниже порогового значения</li>
 *   <li>Тест падает с разными ошибками при одинаковых условиях</li>
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * @ExtendWith(FlakyTestDetector.class)
 * public class MyTest {
 *     // Результаты автоматически отслеживаются
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 3.0
 * @since 3.0
 */
public class FlakyTestDetector implements AfterTestExecutionCallback {

    private static final Logger logger = LoggerFactory.getLogger(FlakyTestDetector.class);
    private static final FlakyTestDetector INSTANCE = new FlakyTestDetector();

    /**
     * Пороговое значение для определения нестабильного теста
     */
    private static final double FLAKY_THRESHOLD = 0.95;

    /**
     * Минимальное количество выполнений для определения нестабильности
     */
    private static final int MIN_EXECUTIONS = 5;

    /**
     * Класс для хранения статистики выполнения теста
     */
    private static class TestStatistics {
        private final AtomicInteger totalExecutions = new AtomicInteger(0);
        private final AtomicInteger successfulExecutions = new AtomicInteger(0);
        private final AtomicInteger failedExecutions = new AtomicInteger(0);
        private final Map<String, Integer> errorTypes = new ConcurrentHashMap<>();

        public void recordSuccess() {
            totalExecutions.incrementAndGet();
            successfulExecutions.incrementAndGet();
        }

        public void recordFailure(String errorMessage) {
            totalExecutions.incrementAndGet();
            failedExecutions.incrementAndGet();

            String errorType = errorMessage != null && errorMessage.length() > 50
                ? errorMessage.substring(0, 50)
                : errorMessage != null ? errorMessage : "Unknown error";

            errorTypes.put(errorType, errorTypes.getOrDefault(errorType, 0) + 1);
        }

        public double getSuccessRate() {
            int total = totalExecutions.get();
            if (total == 0) {
                return 1.0;
            }
            return (double) successfulExecutions.get() / total;
        }

        public boolean isFlaky() {
            int total = totalExecutions.get();
            if (total < MIN_EXECUTIONS) {
                return false;
            }
            return getSuccessRate() < FLAKY_THRESHOLD;
        }

        public int getTotalExecutions() {
            return totalExecutions.get();
        }

        public int getSuccessfulExecutions() {
            return successfulExecutions.get();
        }

        public int getFailedExecutions() {
            return failedExecutions.get();
        }

        public Map<String, Integer> getErrorTypes() {
            return errorTypes;
        }
    }

    /**
     * Хранилище статистики выполнения тестов
     */
    private final Map<String, TestStatistics> testStatistics = new ConcurrentHashMap<>();

    private FlakyTestDetector() {
    }

    public static FlakyTestDetector getInstance() {
        return INSTANCE;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        String testId = getTestId(context);
        TestStatistics stats = testStatistics.computeIfAbsent(testId, k -> new TestStatistics());

        context.getExecutionException().ifPresentOrElse(
            throwable -> {
                String errorMessage = throwable.getMessage();
                stats.recordFailure(errorMessage);

                if (stats.isFlaky()) {
                    logger.warn("Обнаружен нестабильный тест: {} (успешность: {}%)",
                        testId, String.format("%.2f", stats.getSuccessRate() * 100));
                }
            },
            stats::recordSuccess
        );
    }

    /**
     * Записывает результат выполнения теста напрямую
     * 
     * @param testId уникальный идентификатор теста
     * @param success прошел ли тест успешно
     * @param errorMessage сообщение об ошибке (null если тест прошел успешно)
     */
    public void recordTestResult(String testId, boolean success, String errorMessage) {
        TestStatistics stats = testStatistics.computeIfAbsent(testId, k -> new TestStatistics());

        if (success) {
            stats.recordSuccess();
        } else {
            stats.recordFailure(errorMessage);

            if (stats.isFlaky()) {
                logger.warn("Обнаружен нестабильный тест: {} (успешность: {}%)",
                    testId, String.format("%.2f", stats.getSuccessRate() * 100));
            }
        }
    }

    /**
     * Проверяет, является ли тест нестабильным
     * 
     * @param context контекст теста JUnit 5
     * @return true если тест считается нестабильным
     */
    public boolean isFlaky(ExtensionContext context) {
        String testId = getTestId(context);
        TestStatistics stats = testStatistics.get(testId);
        return stats != null && stats.isFlaky();
    }

    /**
     * Возвращает процент успешных выполнений теста
     * 
     * @param context контекст теста JUnit 5
     * @return процент успешных выполнений (0.0 - 1.0)
     */
    public double getSuccessRate(ExtensionContext context) {
        String testId = getTestId(context);
        TestStatistics stats = testStatistics.get(testId);
        return stats != null ? stats.getSuccessRate() : 1.0;
    }

    /**
     * Выводит отчет о нестабильных тестах в лог
     */
    public void reportFlakyTests() {
        logger.info("=== Отчет о нестабильных тестах ===");

        long flakyCount = testStatistics.values().stream()
            .filter(TestStatistics::isFlaky)
            .count();

        if (flakyCount == 0) {
            logger.info("Нестабильных тестов не обнаружено");
        } else {
            logger.warn("Обнаружено нестабильных тестов: {}", flakyCount);
            logger.warn("Порог успешности: {}% (минимум выполнений: {})",
                FLAKY_THRESHOLD * 100, MIN_EXECUTIONS);

            testStatistics.entrySet().stream()
                .filter(entry -> entry.getValue().isFlaky())
                .sorted((e1, e2) -> Double.compare(
                    e1.getValue().getSuccessRate(),
                    e2.getValue().getSuccessRate()))
                .forEach(entry -> {
                    TestStatistics stats = entry.getValue();
                    logger.warn("Нестабильный тест: {}", entry.getKey());
                    logger.warn("  Успешность: {}% ({}/{})",
                        String.format("%.2f", stats.getSuccessRate() * 100),
                        stats.getSuccessfulExecutions(),
                        stats.getTotalExecutions());

                    if (!stats.getErrorTypes().isEmpty()) {
                        logger.warn("  Типы ошибок:");
                        stats.getErrorTypes().entrySet().stream()
                            .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                            .forEach(error -> logger.warn("    {}: {} раз(а)",
                                error.getKey(), error.getValue()));
                    }
                });
        }

        logger.info("=====================================");
    }

    /**
     * Очищает всю статистику
     */
    public void clear() {
        testStatistics.clear();
    }

    /**
     * Генерирует уникальный идентификатор теста
     * 
     * @param context контекст теста JUnit 5
     * @return уникальный идентификатор теста (className#methodName)
     */
    private String getTestId(ExtensionContext context) {
        return context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
    }
}
