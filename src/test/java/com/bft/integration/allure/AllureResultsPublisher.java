package com.bft.integration.allure;

import com.bft.integration.mapper.TestResult;
import com.bft.integration.mapper.TestResultMapper;
import com.bft.integration.mapper.TestStatus;
// import com.bft.integration.zephyr.ZephyrClient;  // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Publisher для отправки результатов из Allure в Zephyr
 * 
 * <p>Парсит JSON файлы из директории allure-results и отправляет результаты в Zephyr.
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * {@code
 * ZephyrClient client = ZephyrClientFactory.create(config);
 * AllureResultsPublisher publisher = new AllureResultsPublisher(client);
 * publisher.publishResults(Paths.get("target/allure-results"));
 * }
 * </pre>
 */
public class AllureResultsPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(AllureResultsPublisher.class);
    
    // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
    // private final ZephyrClient zephyrClient;
    private final Gson gson;
    
    /**
     * Конструктор AllureResultsPublisher
     * 
     * @param zephyrClient клиент для Zephyr
     */
    // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
    // public AllureResultsPublisher(ZephyrClient zephyrClient) {
    //     this.zephyrClient = zephyrClient;
    //     this.gson = new Gson();
    // }
    public AllureResultsPublisher(Object zephyrClient) { // ЗАКОММЕНТИРОВАНО: Zephyr интеграция отключена
        // this.zephyrClient = zephyrClient;
        this.gson = new Gson();
    }
    
    /**
     * Опубликовать результаты из директории allure-results
     * 
     * @param allureResultsDir путь к директории с результатами
     */
    public void publishResults(Path allureResultsDir) {
        logger.info("Publishing Allure results from: {}", allureResultsDir);
        
        if (!Files.exists(allureResultsDir)) {
            logger.error("Allure results directory does not exist: {}", allureResultsDir);
            return;
        }
        
        try {
            // Парсить все JSON файлы с результатами
            List<AllureTestResult> allureResults = parseAllureResults(allureResultsDir);
            logger.info("Parsed {} Allure test results", allureResults.size());
            
            // Конвертировать в TestResult
            List<TestResult> testResults = allureResults.stream()
                .map(this::fromAllureResult)
                .filter(r -> r != null && r.getTestKey() != null)
                .collect(Collectors.toList());
            
            logger.info("Converted {} results with Zephyr test keys", testResults.size());
            
            if (testResults.isEmpty()) {
                logger.warn("No test results with @ZephyrTest annotation found");
                return;
            }
            
            // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
            // Отправить в Zephyr
            // zephyrClient.publishTestResults(testResults);
            // logger.info("Successfully published {} test results to Zephyr", testResults.size());
            
        } catch (Exception e) {
            logger.error("Failed to publish Allure results: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Парсить все JSON файлы с результатами из директории
     * 
     * @param dir директория с allure-results
     * @return список результатов
     */
    private List<AllureTestResult> parseAllureResults(Path dir) throws IOException {
        List<AllureTestResult> results = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> jsonFiles = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith("-result.json"))
                .collect(Collectors.toList());
            
            logger.debug("Found {} JSON result files", jsonFiles.size());
            
            for (Path jsonFile : jsonFiles) {
                try {
                    String json = new String(Files.readAllBytes(jsonFile));
                    AllureTestResult result = gson.fromJson(json, AllureTestResult.class);
                    results.add(result);
                } catch (Exception e) {
                    logger.warn("Failed to parse {}: {}", jsonFile.getFileName(), e.getMessage());
                }
            }
        }
        
        return results;
    }
    
    /**
     * Конвертировать из Allure результата в TestResult
     * 
     * @param allureResult результат из Allure
     * @return TestResult или null
     */
    private TestResult fromAllureResult(AllureTestResult allureResult) {
        try {
            // Извлечь Zephyr test key из labels
            String testKey = allureResult.getLabelValue("testKey");
            if (testKey == null || testKey.isEmpty()) {
                logger.debug("Test {} does not have testKey label", allureResult.getName());
                return null;
            }
            
            // Конвертировать статус
            TestStatus status = mapAllureStatus(allureResult.getStatus());
            
            // Вычислить длительность
            Duration duration = Duration.ofMillis(
                allureResult.getStop() - allureResult.getStart()
            );
            
            // Время выполнения
            LocalDateTime executedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(allureResult.getStart()),
                ZoneId.systemDefault()
            );
            
            // Построить результат
            TestResult.Builder builder = TestResult.builder()
                .testKey(testKey)
                .testName(allureResult.getName())
                .status(status)
                .duration(duration)
                .executedAt(executedAt);
            
            // Добавить test cycle если есть
            String testCycle = allureResult.getLabelValue("testCycle");
            if (testCycle != null && !testCycle.isEmpty()) {
                builder.testCycle(testCycle);
            }
            
            // Добавить комментарий
            builder.comment("Automated test execution from Allure");
            
            // Добавить информацию об ошибке если тест провален
            if (allureResult.getStatusDetails() != null) {
                builder.errorMessage(allureResult.getStatusDetails());
            }
            
            return builder.build();
            
        } catch (Exception e) {
            logger.error("Failed to convert Allure result {}: {}", 
                allureResult.getName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Маппинг статуса из Allure
     * 
     * @param allureStatus статус из Allure
     * @return TestStatus
     */
    private TestStatus mapAllureStatus(String allureStatus) {
        if (allureStatus == null) {
            return TestStatus.NOT_EXECUTED;
        }
        
        switch (allureStatus.toLowerCase()) {
            case "passed":
                return TestStatus.PASS;
            case "failed":
            case "broken":
                return TestStatus.FAIL;
            case "skipped":
                return TestStatus.SKIP;
            default:
                return TestStatus.NOT_EXECUTED;
        }
    }
}
