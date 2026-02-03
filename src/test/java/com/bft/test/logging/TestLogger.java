package com.bft.test.logging;

import com.bft.security.masking.SecureLogger;
import io.qameta.allure.Allure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Улучшенная система логирования для тестов с интеграцией Allure
 * Предоставляет структурированное логирование с автоматическим прикреплением к отчетам
 */
public class TestLogger {

    private static final SecureLogger logger = SecureLogger.getLogger(TestLogger.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Хранение контекста тестов
    private static final Map<String, TestContext> testContexts = new ConcurrentHashMap<>();

    private final String testName;
    private final String testId;

    public TestLogger(String testName) {
        this.testName = testName;
        this.testId = generateTestId();
        testContexts.put(testId, new TestContext(testName));
    }

    /**
     * Создание логгера для теста
     */
    public static TestLogger forTest(String testName) {
        return new TestLogger(testName);
    }

    /**
     * Логирование информационных сообщений
     */
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    /**
     * Логирование с параметрами
     */
    public void info(String message, Object... params) {
        log(LogLevel.INFO, formatMessage(message, params), null);
    }

    /**
     * Логирование отладочных сообщений
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    /**
     * Логирование предупреждений
     */
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    /**
     * Логирование предупреждений с исключением
     */
    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    /**
     * Логирование ошибок
     */
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    /**
     * Логирование ошибок с исключением
     */
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    /**
     * Логирование шагов теста с Allure
     */
    public void step(String stepName, Runnable stepAction) {
        try {
            Allure.step(stepName, () -> {
                log(LogLevel.INFO, "Шаг: " + stepName, null);
                stepAction.run();
            });
        } catch (Exception e) {
            error("Ошибка выполнения шага: " + stepName, e);
            throw e;
        }
    }

    /**
     * Логирование действия с результатом
     */
    public void action(String actionName, Runnable action, boolean success) {
        String status = success ? "✅" : "❌";
        log(LogLevel.INFO, status + " Действие: " + actionName, null);

        if (!success) {
            attachScreenshot("action_failed_" + actionName.replaceAll("[^a-zA-Z0-9]", "_"));
        }
    }

    /**
     * Логирование проверки с результатом
     */
    public void check(String checkName, boolean result) {
        String status = result ? "✅" : "❌";
        log(result ? LogLevel.INFO : LogLevel.ERROR,
            status + " Проверка: " + checkName, null);
    }

    /**
     * Логирование начала теста
     */
    public void testStarted() {
        log(LogLevel.INFO, "🚀 НАЧАЛО ТЕСТА: " + testName, null);
        attachEnvironmentInfo();
    }

    /**
     * Логирование завершения теста
     */
    public void testFinished(boolean success, long duration) {
        String status = success ? "✅" : "❌";
        log(success ? LogLevel.INFO : LogLevel.ERROR,
            status + " ЗАВЕРШЕНИЕ ТЕСТА: " + testName + " (время: " + duration + "мс)", null);

        TestContext context = testContexts.get(testId);
        if (context != null) {
            context.setDuration(duration);
            context.setSuccess(success);
            attachTestSummary(context);
        }
    }

    /**
     * Прикрепление скриншота к отчету (с автоматическим снятием)
     */
    public void attachScreenshot(String name) {
        try {
            // Предполагаем, что у нас есть доступ к WebDriver через Selenide
            byte[] screenshot = ((org.openqa.selenium.TakesScreenshot)
                com.codeborne.selenide.WebDriverRunner.getWebDriver())
                .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            attachScreenshot(name, screenshot);
        } catch (Exception e) {
            warn("Не удалось сделать скриншот: " + e.getMessage());
        }
    }

    /**
     * Прикрепление скриншота к отчету с предоставленными данными
     */
    public void attachScreenshot(String name, byte[] screenshotData) {
        try {
            Allure.addAttachment(name, new java.io.ByteArrayInputStream(screenshotData));
        } catch (Exception e) {
            warn("Не удалось прикрепить скриншот: " + e.getMessage());
        }
    }

    /**
     * Прикрепление файла к отчету
     */
    public void attachFile(String name, String content, String type) {
        Allure.addAttachment(name, type, content, ".txt");
    }

    /**
     * Прикрепление JSON данных
     */
    public void attachJson(String name, Object data) {
        try {
            String json = data instanceof String ? (String) data :
                         com.fasterxml.jackson.databind.ObjectMapper.class
                            .getDeclaredConstructor().newInstance()
                            .writeValueAsString(data);
            attachFile(name, json, "application/json");
        } catch (Exception e) {
            warn("Не удалось прикрепить JSON: " + e.getMessage());
        }
    }

    /**
     * Прикрепление метрик производительности
     */
    public void attachPerformanceMetrics(String operation, long duration, long memoryUsage) {
        String metrics = String.format(
            "Operation: %s\nDuration: %d ms\nMemory: %d KB\nTimestamp: %s",
            operation, duration, memoryUsage, LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
        attachFile("performance_" + operation, metrics, "text/plain");
    }

    /**
     * Логирование сетевого запроса
     */
    public void logHttpRequest(String method, String url, int statusCode, long responseTime) {
        String logMessage = String.format("HTTP %s %s -> %d (%d ms)",
                                        method, url, statusCode, responseTime);
        log(statusCode >= 400 ? LogLevel.WARN : LogLevel.INFO, logMessage, null);
    }

    // Приватные методы

    private void log(LogLevel level, String message, Throwable throwable) {
        String timestampedMessage = String.format("[%s] %s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT), message);

        // Логирование в SLF4J
        switch (level) {
            case TRACE:
                logger.trace(timestampedMessage, throwable);
                break;
            case DEBUG:
                logger.debug(timestampedMessage, throwable);
                break;
            case INFO:
                logger.info(timestampedMessage, throwable);
                break;
            case WARN:
                logger.warn(timestampedMessage, throwable);
                break;
            case ERROR:
                logger.error(timestampedMessage, throwable);
                break;
        }

        // Добавление в Allure отчет
        addToAllureReport(level, timestampedMessage, throwable);
    }

    private void addToAllureReport(LogLevel level, String message, Throwable throwable) {
        String allureMessage = String.format("%s: %s", level.name(), message);

        if (throwable != null) {
            Allure.addAttachment("Exception Details",
                "text/plain",
                throwable.toString(),
                ".txt");
        }

        // Добавляем в описание шага Allure
        Allure.description(allureMessage);
    }

    private void attachEnvironmentInfo() {
        String envInfo = String.format(
            "Test Environment Info:\n" +
            "Java Version: %s\n" +
            "OS: %s %s\n" +
            "User: %s\n" +
            "Timestamp: %s\n" +
            "Test ID: %s",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("user.name"),
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            testId
        );

        attachFile("environment_info", envInfo, "text/plain");
    }

    private void attachTestSummary(TestContext context) {
        if (context == null) {
            return;
        }
        
        try {
            String summary = String.format(
                "Test Summary:\n" +
                "Name: %s\n" +
                "Success: %s\n" +
                "Duration: %d ms\n" +
                "Started: %s\n" +
                "Finished: %s",
                context.getTestName() != null ? context.getTestName() : "Unknown",
                context.isSuccess(),
                context.getDuration(),
                context.getStartTime() != null ? context.getStartTime().format(TIMESTAMP_FORMAT) : "N/A",
                context.getEndTime() != null ? context.getEndTime().format(TIMESTAMP_FORMAT) : "N/A"
            );

            attachFile("test_summary", summary, "text/plain");
        } catch (Exception e) {
            // Игнорируем ошибки при создании summary
        }
    }

    private String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }

        try {
            return String.format(message, params);
        } catch (Exception e) {
            return message + " [ошибка форматирования параметров]";
        }
    }

    private String generateTestId() {
        return String.format("test_%d_%s",
            System.currentTimeMillis(),
            Integer.toHexString(hashCode()));
    }

    /**
     * Уровни логирования
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    /**
     * Контекст выполнения теста
     */
    public static class TestContext {
        private final String testName;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private long duration;
        private boolean success;

        public TestContext(String testName) {
            this.testName = testName;
            this.startTime = LocalDateTime.now();
        }

        // Getters and setters
        public String getTestName() { return testName; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    // Статические методы для удобства

    /**
     * Получение логгера для текущего теста
     */
    public static TestLogger getCurrentTestLogger() {
        // В реальном приложении здесь логика получения логгера для текущего теста
        return new TestLogger("current_test");
    }

    /**
     * Быстрое логирование информационного сообщения
     */
    public static void logInfo(String message) {
        getCurrentTestLogger().info(message);
    }

    /**
     * Быстрое логирование ошибки
     */
    public static void logError(String message, Throwable throwable) {
        getCurrentTestLogger().error(message, throwable);
    }
}