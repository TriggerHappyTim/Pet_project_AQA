package com.bft.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

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
 * public class MyTest extends UITestBase {
 *     private static final FlakyTestDetector detector = FlakyTestDetector.getInstance();
 *     
 *     @AfterMethod
 *     public void trackTest(ITestResult result) {
 *         detector.recordTestResult(result);
 *     }
 *     
 *     @AfterSuite
 *     public void reportFlakyTests() {
 *         detector.reportFlakyTests();
 *     }
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @since 2.0
 */
public class FlakyTestDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(FlakyTestDetector.class);
    private static final FlakyTestDetector INSTANCE = new FlakyTestDetector();
    
    /**
     * Пороговое значение для определения нестабильного теста
     * Тест считается нестабильным, если процент успешных выполнений ниже этого значения
     */
    private static final double FLAKY_THRESHOLD = 0.95; // 95%
    
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
            
            // Извлекаем тип ошибки из сообщения (первые 50 символов)
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
     * Ключ: уникальный идентификатор теста (className#methodName)
     * Значение: статистика выполнения теста
     */
    private final Map<String, TestStatistics> testStatistics = new ConcurrentHashMap<>();
    
    /**
     * Приватный конструктор для Singleton pattern
     */
    private FlakyTestDetector() {
    }
    
    /**
     * Возвращает единственный экземпляр FlakyTestDetector
     * 
     * @return экземпляр FlakyTestDetector
     */
    public static FlakyTestDetector getInstance() {
        return INSTANCE;
    }
    
    /**
     * Записывает результат выполнения теста
     * 
     * @param result результат теста TestNG
     */
    public void recordTestResult(ITestResult result) {
        String testId = getTestId(result);
        TestStatistics stats = testStatistics.computeIfAbsent(testId, k -> new TestStatistics());
        
        if (result.isSuccess()) {
            stats.recordSuccess();
        } else {
            String errorMessage = result.getThrowable() != null
                ? result.getThrowable().getMessage()
                : "Unknown error";
            stats.recordFailure(errorMessage);
            
            // Если тест упал, проверяем, не стал ли он нестабильным
            if (stats.isFlaky()) {
                logger.warn("Обнаружен нестабильный тест: {} (успешность: {:.2f}%)",
                    testId, stats.getSuccessRate() * 100);
            }
        }
    }
    
    /**
     * Проверяет, является ли тест нестабильным
     * 
     * @param result результат теста TestNG
     * @return true если тест считается нестабильным, false в противном случае
     */
    public boolean isFlaky(ITestResult result) {
        String testId = getTestId(result);
        TestStatistics stats = testStatistics.get(testId);
        return stats != null && stats.isFlaky();
    }
    
    /**
     * Возвращает процент успешных выполнений теста
     * 
     * @param result результат теста TestNG
     * @return процент успешных выполнений (0.0 - 1.0)
     */
    public double getSuccessRate(ITestResult result) {
        String testId = getTestId(result);
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
                    logger.warn("  Успешность: {:.2f}% ({}/{})",
                        stats.getSuccessRate() * 100,
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
     * @param result результат теста TestNG
     * @return уникальный идентификатор теста (className#methodName)
     */
    private String getTestId(ITestResult result) {
        return result.getTestClass().getName() + "#" + result.getMethod().getMethodName();
    }
}
