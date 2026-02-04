package com.bft.integration.mapper;

// import com.bft.test.annotations.ZephyrTest;  // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Маппер для конвертации результатов тестов из различных источников
 * 
 * <p>Поддерживает конвертацию из:
 * <ul>
 *   <li>TestNG ITestResult</li>
 *   <li>Allure JSON results (через AllureTestResult DTO)</li>
 * </ul>
 */
public class TestResultMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(TestResultMapper.class);
    
    /**
     * Конвертировать из TestNG ITestResult
     * 
     * @param testNGResult результат из TestNG
     * @return TestResult DTO
     */
    public TestResult fromTestNGResult(ITestResult testNGResult) {
        Method method = testNGResult.getMethod().getConstructorOrMethod().getMethod();
        
        // Извлечь Zephyr test key из аннотации
        // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
        // Optional<String> testKey = extractZephyrTestKey(method);
        // Optional<String> testCycle = extractZephyrTestCycle(method);
        // 
        // if (!testKey.isPresent()) {
        //     logger.debug("Test method {} does not have @ZephyrTest annotation", method.getName());
        //     return null;
        // }
        return null; // Zephyr интеграция отключена
        
        // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
        // Конвертировать статус
        // TestStatus status = TestStatus.fromTestNGStatus(testNGResult.getStatus());
        // 
        // Вычислить длительность
        // long durationMs = testNGResult.getEndMillis() - testNGResult.getStartMillis();
        // Duration duration = Duration.ofMillis(durationMs);
        // 
        // Время выполнения
        // LocalDateTime executedAt = LocalDateTime.ofInstant(
        //     Instant.ofEpochMilli(testNGResult.getStartMillis()),
        //     ZoneId.systemDefault()
        // );
        // 
        // Построить комментарий
        // String comment = buildComment(testNGResult);
        // 
        // Создать TestResult
        // TestResult.Builder builder = TestResult.builder()
        //     .testKey(testKey.get())
        //     .testName(method.getName())
        //     .status(status)
        //     .duration(duration)
        //     .executedAt(executedAt)
        //     .comment(comment);
        // 
        // Добавить test cycle если указан
        // testCycle.ifPresent(builder::testCycle);
        // 
        // Добавить информацию об ошибке если тест провален
        // if (testNGResult.getThrowable() != null) {
        //     Throwable throwable = testNGResult.getThrowable();
        //     builder.errorMessage(throwable.getMessage());
        //     builder.stackTrace(getStackTraceAsString(throwable));
        // }
        // 
        // return builder.build();
    }
    
    /**
     * Извлечь Zephyr test key из аннотации метода или класса
     * 
     * @param testMethod тестовый метод
     * @return test key если найден
     */
    // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
    // public Optional<String> extractZephyrTestKey(Method testMethod) {
    //     // Проверить аннотацию на методе
    //     ZephyrTest methodAnnotation = testMethod.getAnnotation(ZephyrTest.class);
    //     if (methodAnnotation != null && !methodAnnotation.testKey().isEmpty()) {
    //         return Optional.of(methodAnnotation.testKey());
    //     }
    //     
    //     // Проверить аннотацию на классе
    //     ZephyrTest classAnnotation = testMethod.getDeclaringClass().getAnnotation(ZephyrTest.class);
    //     if (classAnnotation != null && !classAnnotation.testKey().isEmpty()) {
    //         return Optional.of(classAnnotation.testKey());
    //     }
    //     
    //     return Optional.empty();
    // }
    
    /**
     * Извлечь Zephyr test cycle из аннотации
     * 
     * @param testMethod тестовый метод
     * @return test cycle если указан
     */
    // ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется
    // public Optional<String> extractZephyrTestCycle(Method testMethod) {
    //     // Проверить аннотацию на методе
    //     ZephyrTest methodAnnotation = testMethod.getAnnotation(ZephyrTest.class);
    //     if (methodAnnotation != null && !methodAnnotation.testCycle().isEmpty()) {
    //         return Optional.of(methodAnnotation.testCycle());
    //     }
    //     
    //     // Проверить аннотацию на классе
    //     ZephyrTest classAnnotation = testMethod.getDeclaringClass().getAnnotation(ZephyrTest.class);
    //     if (classAnnotation != null && !classAnnotation.testCycle().isEmpty()) {
    //         return Optional.of(classAnnotation.testCycle());
    //     }
    //     
    //     return Optional.empty();
    // }
    
    /**
     * Построить комментарий для test execution
     * 
     * @param testNGResult результат теста
     * @return комментарий
     */
    private String buildComment(ITestResult testNGResult) {
        StringBuilder comment = new StringBuilder();
        
        comment.append("Automated test execution\n");
        comment.append("Test: ").append(testNGResult.getName()).append("\n");
        comment.append("Class: ").append(testNGResult.getTestClass().getName()).append("\n");
        
        // Добавить параметры если есть
        Object[] parameters = testNGResult.getParameters();
        if (parameters != null && parameters.length > 0) {
            comment.append("Parameters: ");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) comment.append(", ");
                comment.append(parameters[i]);
            }
            comment.append("\n");
        }
        
        return comment.toString();
    }
    
    /**
     * Получить stack trace как строку
     * 
     * @param throwable исключение
     * @return stack trace
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            // Ограничить длину stack trace
            if (sb.length() > 5000) {
                sb.append("\t... (truncated)\n");
                break;
            }
        }
        
        return sb.toString();
    }
}
