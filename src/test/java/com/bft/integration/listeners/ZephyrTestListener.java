package com.bft.integration.listeners;

import com.bft.integration.config.IntegrationConfig;
import com.bft.integration.mapper.TestResult;
import com.bft.integration.mapper.TestResultMapper;
import com.bft.integration.zephyr.ZephyrClient;
import com.bft.integration.zephyr.ZephyrClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * TestNG Listener для автоматической публикации результатов тестов в Zephyr
 * 
 * <p>Перехватывает события выполнения тестов и отправляет результаты в Zephyr в real-time.
 * 
 * <p><b>Регистрация в testng.xml:</b>
 * <pre>
 * {@code
 * <listeners>
 *     <listener class-name="com.bft.integration.listeners.ZephyrTestListener"/>
 * </listeners>
 * }
 * </pre>
 * 
 * <p><b>Использование с аннотацией:</b>
 * <pre>
 * {@code
 * @Test(groups = {"web", "smoke"})
 * @ZephyrTest(testKey = "EVS-T-123")
 * public void loginTest() {
 *     // Test автоматически отправится в Zephyr после выполнения
 * }
 * }
 * </pre>
 */
public class ZephyrTestListener implements ITestListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ZephyrTestListener.class);
    
    private ZephyrClient zephyrClient;
    private TestResultMapper mapper;
    private boolean enabled;
    private List<TestResult> pendingResults;
    
    public ZephyrTestListener() {
        this.mapper = new TestResultMapper();
        this.pendingResults = new ArrayList<>();
        initialize();
    }
    
    /**
     * Инициализация listener
     */
    private void initialize() {
        try {
            IntegrationConfig config = IntegrationConfig.fromCredentials();
            this.enabled = config.isZephyrEnabled();
            
            if (enabled) {
                this.zephyrClient = ZephyrClientFactory.create(config);
                logger.info("Zephyr Test Listener initialized and enabled");
            } else {
                logger.info("Zephyr Test Listener initialized but disabled");
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize Zephyr Test Listener: {}", e.getMessage());
            this.enabled = false;
        }
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        if (!enabled) return;
        
        logger.debug("Test started: {}", result.getName());
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        if (!enabled) return;
        
        logger.info("Test passed: {}", result.getName());
        publishTestResult(result);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        if (!enabled) return;
        
        logger.warn("Test failed: {}", result.getName());
        publishTestResult(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        if (!enabled) return;
        
        logger.info("Test skipped: {}", result.getName());
        publishTestResult(result);
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        if (!enabled) return;
        
        logger.info("Test failed within success percentage: {}", result.getName());
        publishTestResult(result);
    }
    
    @Override
    public void onStart(ITestContext context) {
        if (!enabled) return;
        
        logger.info("Test suite started: {}", context.getName());
    }
    
    @Override
    public void onFinish(ITestContext context) {
        if (!enabled) return;
        
        logger.info("Test suite finished: {}", context.getName());
        
        // Опубликовать все накопленные результаты
        if (!pendingResults.isEmpty()) {
            try {
                logger.info("Publishing {} pending test results", pendingResults.size());
                zephyrClient.publishTestResults(pendingResults);
                pendingResults.clear();
            } catch (Exception e) {
                logger.error("Failed to publish pending results: {}", e.getMessage());
            }
        }
        
        // Закрыть клиент
        if (zephyrClient != null) {
            try {
                zephyrClient.close();
            } catch (Exception e) {
                logger.warn("Error closing Zephyr client: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Опубликовать результат теста в Zephyr
     * 
     * @param testNGResult результат из TestNG
     */
    private void publishTestResult(ITestResult testNGResult) {
        try {
            // Конвертировать результат
            TestResult result = mapper.fromTestNGResult(testNGResult);
            
            if (result == null) {
                logger.debug("Test {} does not have @ZephyrTest annotation, skipping", 
                    testNGResult.getName());
                return;
            }
            
            // Отправить результат в Zephyr
            logger.info("Publishing test result to Zephyr: {} -> {}", 
                result.getTestKey(), result.getStatus());
            
            zephyrClient.updateTestExecution(
                result.getTestKey(),
                result.getStatus(),
                result.getComment()
            );
            
        } catch (Exception e) {
            logger.error("Failed to publish test result for {}: {}", 
                testNGResult.getName(), e.getMessage());
            
            // Добавить в pending для повторной попытки
            try {
                TestResult result = mapper.fromTestNGResult(testNGResult);
                if (result != null) {
                    pendingResults.add(result);
                }
            } catch (Exception ex) {
                logger.error("Failed to add result to pending queue: {}", ex.getMessage());
            }
        }
    }
}
