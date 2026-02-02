package com.bft.test.logging;

import com.bft.config.TestConfiguration;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.screenshot;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

/**
 * Улучшенная интеграция с Allure для автоматического прикрепления данных
 */
public class AllureIntegration {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Прикрепление скриншота с автоматическим именем
     */
    public static void attachScreenshot(String name) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fileName = name + "_" + timestamp;

            // Делаем скриншот через Selenide
            screenshot(fileName);

            // Добавляем в Allure
            Allure.addAttachment("Screenshot: " + name, "image/png",
                Paths.get("build/reports/tests/" + fileName + ".png").toString());

        } catch (Exception e) {
            Allure.addAttachment("Screenshot Error", "text/plain",
                "Failed to capture screenshot: " + e.getMessage());
        }
    }

    /**
     * Прикрепление скриншота при ошибке
     */
    public static void attachScreenshotOnError(String context) {
        attachScreenshot("error_" + context);
    }

    /**
     * Прикрепление информации о странице
     */
    public static void attachPageInfo(String pageName) {
        try {
            WebDriver driver = getWebDriver();
            String url = driver.getCurrentUrl();
            String title = driver.getTitle();

            String pageInfo = String.format(
                "Page Information:\n" +
                "Name: %s\n" +
                "URL: %s\n" +
                "Title: %s\n" +
                "Timestamp: %s",
                pageName, url, title,
                LocalDateTime.now().format(TIMESTAMP_FORMAT)
            );

            Allure.addAttachment("Page Info: " + pageName, "text/plain", pageInfo);

        } catch (Exception e) {
            Allure.addAttachment("Page Info Error", "text/plain",
                "Failed to get page info: " + e.getMessage());
        }
    }

    /**
     * Прикрепление логов браузера
     */
    public static void attachBrowserLogs() {
        try {
            WebDriver driver = getWebDriver();
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);

            StringBuilder logContent = new StringBuilder();
            logContent.append("Browser Console Logs:\n");
            logContent.append("Timestamp: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n\n");

            for (LogEntry entry : logs) {
                logContent.append(String.format("[%s] %s: %s\n",
                    entry.getTimestamp(), entry.getLevel(), entry.getMessage()));
            }

            Allure.addAttachment("Browser Logs", "text/plain", logContent.toString());

        } catch (Exception e) {
            Allure.addAttachment("Browser Logs Error", "text/plain",
                "Failed to get browser logs: " + e.getMessage());
        }
    }

    /**
     * Прикрепление сетевых логов (если доступны)
     */
    public static void attachNetworkLogs() {
        try {
            WebDriver driver = getWebDriver();
            LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);

            StringBuilder logContent = new StringBuilder();
            logContent.append("Network Performance Logs:\n");
            logContent.append("Timestamp: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n\n");

            for (LogEntry entry : logs) {
                logContent.append(String.format("[%s] %s: %s\n",
                    entry.getTimestamp(), entry.getLevel(), entry.getMessage()));
            }

            Allure.addAttachment("Network Logs", "text/plain", logContent.toString());

        } catch (Exception e) {
            Allure.addAttachment("Network Logs Error", "text/plain",
                "Failed to get network logs: " + e.getMessage());
        }
    }

    /**
     * Прикрепление системной информации
     */
    public static void attachSystemInfo() {
        Map<String, String> systemInfo = new HashMap<>();

        // Java информация
        systemInfo.put("Java Version", System.getProperty("java.version"));
        systemInfo.put("Java Vendor", System.getProperty("java.vendor"));
        systemInfo.put("JVM Name", System.getProperty("java.vm.name"));

        // OS информация
        systemInfo.put("OS Name", System.getProperty("os.name"));
        systemInfo.put("OS Version", System.getProperty("os.version"));
        systemInfo.put("OS Architecture", System.getProperty("os.arch"));

        // Системные ресурсы
        systemInfo.put("Available Processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        systemInfo.put("Total Memory", String.valueOf(Runtime.getRuntime().totalMemory() / 1024 / 1024) + " MB");
        systemInfo.put("Free Memory", String.valueOf(Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB");

        // Test configuration
        systemInfo.put("Browser", TestConfiguration.getCurrentConfig().getBrowser());
        systemInfo.put("Environment", TestConfiguration.getCurrentConfig().getEnvironment());
        systemInfo.put("Headless", String.valueOf(TestConfiguration.getCurrentConfig().isHeadless()));

        // Форматирование
        StringBuilder info = new StringBuilder();
        info.append("System Information:\n");
        info.append("Timestamp: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n\n");

        systemInfo.forEach((key, value) ->
            info.append(String.format("%-25s: %s\n", key, value)));

        Allure.addAttachment("System Info", "text/plain", info.toString());
    }

    /**
     * Прикрепление конфигурации теста
     */
    public static void attachTestConfiguration() {
        try {
            var config = TestConfiguration.getCurrentConfig();
            var strategy = TestConfiguration.getCurrentStrategy();

            String configInfo = String.format(
                "Test Configuration:\n" +
                "Timestamp: %s\n\n" +
                "Browser: %s\n" +
                "Browser Version: %s\n" +
                "Headless: %s\n" +
                "Remote: %s\n" +
                "Environment: %s\n" +
                "Test Suite: %s\n" +
                "Strategy: %s\n" +
                "Strategy Type: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                config.getBrowser(),
                config.getBrowserVersion(),
                config.isHeadless(),
                config.isRemote(),
                config.getEnvironment(),
                config.getTestSuite(),
                strategy.getClass().getSimpleName(),
                TestConfiguration.getCurrentStrategy().getType()
            );

            Allure.addAttachment("Test Configuration", "text/plain", configInfo);

        } catch (Exception e) {
            Allure.addAttachment("Test Configuration Error", "text/plain",
                "Failed to get test configuration: " + e.getMessage());
        }
    }

    /**
     * Прикрепление файла
     */
    public static void attachFile(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    /**
     * Прикрепление JSON данных
     */
    public static void attachJson(String name, Object data) {
        try {
            String json = data instanceof String ? (String) data :
                         new com.fasterxml.jackson.databind.ObjectMapper()
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(data);
            Allure.addAttachment(name, "application/json", json);
        } catch (Exception e) {
            Allure.addAttachment(name + " (Error)", "text/plain",
                "Failed to serialize JSON: " + e.getMessage());
        }
    }

    /**
     * Прикрепление CSV данных
     */
    public static void attachCsv(String name, String csvContent) {
        Allure.addAttachment(name, "text/csv", csvContent);
    }

    /**
     * Прикрепление XML данных
     */
    public static void attachXml(String name, String xmlContent) {
        Allure.addAttachment(name, "application/xml", xmlContent);
    }

    /**
     * Прикрепление метрик производительности
     */
    public static void attachPerformanceMetrics(String operation, long duration, long memoryUsage) {
        String metrics = String.format(
            "Performance Metrics:\n" +
            "Operation: %s\n" +
            "Duration: %d ms\n" +
            "Memory Usage: %d KB\n" +
            "Timestamp: %s",
            operation, duration, memoryUsage,
            LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );

        Allure.addAttachment("Performance: " + operation, "text/plain", metrics);
    }

    /**
     * Прикрепление логов выполнения
     */
    public static void attachExecutionLogs(String logs) {
        Allure.addAttachment("Execution Logs", "text/plain", logs);
    }

    /**
     * Комплексное прикрепление данных для отладки
     */
    public static void attachDebugInfo(String context) {
        attachScreenshot("debug_" + context);
        attachPageInfo("Debug Page");
        attachBrowserLogs();
        attachSystemInfo();
        attachTestConfiguration();
    }

    /**
     * Прикрепление данных при ошибке
     */
    public static void attachErrorInfo(String errorContext, Exception exception) {
        attachScreenshotOnError(errorContext);

        String errorInfo = String.format(
            "Error Information:\n" +
            "Context: %s\n" +
            "Exception: %s\n" +
            "Message: %s\n" +
            "Timestamp: %s\n\n" +
            "Stack Trace:\n%s",
            errorContext,
            exception.getClass().getName(),
            exception.getMessage(),
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            getStackTraceAsString(exception)
        );

        Allure.addAttachment("Error Details: " + errorContext, "text/plain", errorInfo);
        attachSystemInfo();
    }

    /**
     * Получение stack trace в виде строки
     */
    private static String getStackTraceAsString(Exception exception) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}