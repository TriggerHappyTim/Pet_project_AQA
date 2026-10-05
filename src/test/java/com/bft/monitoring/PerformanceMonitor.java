package com.bft.monitoring;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Мониторинг производительности тестов
 * 
 * <p>Отслеживает время выполнения тестов и предоставляет статистику:
 * <ul>
 *   <li>Время выполнения каждого теста</li>
 *   <li>Среднее время выполнения тестов</li>
 *   <li>Медленные тесты (превышающие пороговое значение)</li>
 *   <li>Статистика по группам тестов</li>
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * @ExtendWith(PerformanceMonitor.class)
 * public class MyTest {
 *     // Тесты автоматически мониторятся
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 3.0
 * @since 3.0
 */
public class PerformanceMonitor implements BeforeEachCallback, AfterTestExecutionCallback {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();

    /**
     * Пороговое значение для определения медленных тестов (в миллисекундах)
     */
    private static final long SLOW_TEST_THRESHOLD_MS = 30_000;

    /**
     * Хранилище времени начала выполнения тестов
     */
    private final Map<String, Long> testStartTimes = new ConcurrentHashMap<>();

    /**
     * Хранилище времени выполнения тестов
     */
    private final Map<String, Long> testDurations = new ConcurrentHashMap<>();

    /**
     * Счетчик общего количества выполненных тестов
     */
    private final AtomicLong totalTests = new AtomicLong(0);

    /**
     * Счетчик медленных тестов
     */
    private final AtomicLong slowTests = new AtomicLong(0);

    private PerformanceMonitor() {
    }

    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        String testId = getTestId(context);
        testStartTimes.put(testId, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        String testId = getTestId(context);
        Long startTime = testStartTimes.remove(testId);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            testDurations.put(testId, duration);
            totalTests.incrementAndGet();

            if (duration > SLOW_TEST_THRESHOLD_MS) {
                slowTests.incrementAndGet();
                logger.warn("Медленный тест обнаружен: {} выполнился за {} мс (порог: {} мс)",
                    testId, duration, SLOW_TEST_THRESHOLD_MS);
            }
        }
    }

    /**
     * Возвращает время выполнения теста в миллисекундах
     * 
     * @param context контекст теста JUnit 5
     * @return время выполнения в миллисекундах, или -1 если тест еще не завершен
     */
    public long getTestDuration(ExtensionContext context) {
        String testId = getTestId(context);
        return testDurations.getOrDefault(testId, -1L);
    }

    /**
     * Возвращает среднее время выполнения всех тестов
     * 
     * @return среднее время выполнения в миллисекундах
     */
    public double getAverageDuration() {
        if (testDurations.isEmpty()) {
            return 0.0;
        }

        long totalDuration = testDurations.values().stream()
            .mapToLong(Long::longValue)
            .sum();

        return (double) totalDuration / testDurations.size();
    }

    /**
     * Возвращает количество медленных тестов
     * 
     * @return количество медленных тестов
     */
    public long getSlowTestsCount() {
        return slowTests.get();
    }

    /**
     * Возвращает общее количество выполненных тестов
     * 
     * @return общее количество тестов
     */
    public long getTotalTestsCount() {
        return totalTests.get();
    }

    /**
     * Выводит статистику производительности в лог
     */
    public void printStatistics() {
        logger.info("=== Статистика производительности тестов ===");
        logger.info("Всего тестов выполнено: {}", totalTests.get());
        logger.info("Медленных тестов: {} (порог: {} мс)", slowTests.get(), SLOW_TEST_THRESHOLD_MS);
        logger.info("Среднее время выполнения: {} мс", String.format("%.2f", getAverageDuration()));

        if (!testDurations.isEmpty()) {
            long maxDuration = testDurations.values().stream().mapToLong(Long::longValue).max().orElse(0);
            long minDuration = testDurations.values().stream().mapToLong(Long::longValue).min().orElse(0);
            logger.info("Максимальное время выполнения: {} мс", maxDuration);
            logger.info("Минимальное время выполнения: {} мс", minDuration);

            logger.info("Топ-5 самых медленных тестов:");
            testDurations.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .forEach(entry -> logger.info("  {}: {} мс", entry.getKey(), entry.getValue()));
        }

        logger.info("===========================================");
    }

    /**
     * Очищает всю статистику
     */
    public void clear() {
        testStartTimes.clear();
        testDurations.clear();
        totalTests.set(0);
        slowTests.set(0);
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
