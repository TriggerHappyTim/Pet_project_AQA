package com.bft.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

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
 * public class MyTest extends UITestBase {
 *     private static final PerformanceMonitor monitor = PerformanceMonitor.getInstance();
 *     
 *     @BeforeMethod
 *     public void setup(ITestResult result) {
 *         monitor.startTest(result);
 *     }
 *     
 *     @AfterMethod
 *     public void teardown(ITestResult result) {
 *         monitor.endTest(result);
 *     }
 *     
 *     @AfterSuite
 *     public void printStatistics() {
 *         monitor.printStatistics();
 *     }
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @since 2.0
 */
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();
    
    /**
     * Пороговое значение для определения медленных тестов (в миллисекундах)
     * Тесты, выполняющиеся дольше этого значения, считаются медленными
     */
    private static final long SLOW_TEST_THRESHOLD_MS = 30_000; // 30 секунд
    
    /**
     * Хранилище времени начала выполнения тестов
     * Ключ: уникальный идентификатор теста (className#methodName)
     * Значение: время начала выполнения в миллисекундах
     */
    private final Map<String, Long> testStartTimes = new ConcurrentHashMap<>();
    
    /**
     * Хранилище времени выполнения тестов
     * Ключ: уникальный идентификатор теста
     * Значение: время выполнения в миллисекундах
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
    
    /**
     * Приватный конструктор для Singleton pattern
     */
    private PerformanceMonitor() {
    }
    
    /**
     * Возвращает единственный экземпляр PerformanceMonitor
     * 
     * @return экземпляр PerformanceMonitor
     */
    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }
    
    /**
     * Записывает время начала выполнения теста
     * 
     * @param result результат теста TestNG
     */
    public void startTest(ITestResult result) {
        String testId = getTestId(result);
        testStartTimes.put(testId, System.currentTimeMillis());
    }
    
    /**
     * Записывает время окончания выполнения теста и вычисляет длительность
     * 
     * @param result результат теста TestNG
     */
    public void endTest(ITestResult result) {
        String testId = getTestId(result);
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
     * @param result результат теста TestNG
     * @return время выполнения в миллисекундах, или -1 если тест еще не завершен
     */
    public long getTestDuration(ITestResult result) {
        String testId = getTestId(result);
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
        logger.info("Среднее время выполнения: {:.2f} мс", getAverageDuration());
        
        if (!testDurations.isEmpty()) {
            long maxDuration = testDurations.values().stream().mapToLong(Long::longValue).max().orElse(0);
            long minDuration = testDurations.values().stream().mapToLong(Long::longValue).min().orElse(0);
            logger.info("Максимальное время выполнения: {} мс", maxDuration);
            logger.info("Минимальное время выполнения: {} мс", minDuration);
            
            // Выводим топ-5 самых медленных тестов
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
     * @param result результат теста TestNG
     * @return уникальный идентификатор теста (className#methodName)
     */
    private String getTestId(ITestResult result) {
        return result.getTestClass().getName() + "#" + result.getMethod().getMethodName();
    }
}
