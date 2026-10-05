package com.bft.integration.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Маппер для конвертации результатов тестов из различных источников
 *
 * <p>Поддерживает конвертацию из:
 * <ul>
 *   <li>Простых параметров теста (testClass, testMethod, success, errorMessage, durationMs)</li>
 *   <li>Allure JSON результатов (через AllureTestResult DTO)</li>
 * </ul>
 */
public class TestResultMapper {

    private static final Logger logger = LoggerFactory.getLogger(TestResultMapper.class);

    /**
     * Создать TestResult из простых параметров теста
     *
     * @param testClass    полное имя класса теста
     * @param testMethod   имя тестового метода
     * @param success      true если тест прошёл успешно
     * @param errorMessage сообщение об ошибке (null если тест прошёл успешно)
     * @param durationMs   длительность выполнения теста в миллисекундах
     * @return TestResult DTO
     */
    public TestResult mapTestResult(String testClass, String testMethod, boolean success,
                                    String errorMessage, long durationMs) {
        TestStatus status = success ? TestStatus.PASS : TestStatus.FAIL;

        Duration duration = Duration.ofMillis(durationMs);

        LocalDateTime executedAt = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(System.currentTimeMillis() - durationMs),
                ZoneId.systemDefault()
        );

        String comment = buildComment(testClass, testMethod, success, errorMessage);

        TestResult.Builder builder = TestResult.builder()
                .testName(testMethod)
                .status(status)
                .duration(duration)
                .executedAt(executedAt)
                .comment(comment);

        if (!success && errorMessage != null) {
            builder.errorMessage(errorMessage);
        }

        return builder.build();
    }

    /**
     * Построить комментарий для test execution
     */
    private String buildComment(String testClass, String testMethod, boolean success, String errorMessage) {
        StringBuilder comment = new StringBuilder();

        comment.append("Automated test execution\n");
        comment.append("Test: ").append(testMethod).append("\n");
        comment.append("Class: ").append(testClass).append("\n");
        comment.append("Status: ").append(success ? "PASSED" : "FAILED").append("\n");

        if (!success && errorMessage != null) {
            comment.append("Error: ").append(errorMessage).append("\n");
        }

        return comment.toString();
    }
}
