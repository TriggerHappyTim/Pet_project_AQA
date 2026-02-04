// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.integration;

import com.bft.integration.allure.AllureResultsPublisher;
import com.bft.integration.config.IntegrationConfig;
import com.bft.integration.zephyr.ZephyrClient;
import com.bft.integration.zephyr.ZephyrClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Главный класс для публикации результатов тестов в Zephyr
 * 
 * <p>CLI для запуска публикации из командной строки или Maven exec plugin.
 * 
 * <p><b>Использование:</b>
 * <pre>
 * {@code
 * # Через Maven
 * mvn exec:java -Dexec.mainClass="com.bft.integration.ZephyrPublisherMain" \
 *   -Dexec.args="--mode=allure --results-dir=target/allure-results"
 * 
 * # Напрямую
 * java com.bft.integration.ZephyrPublisherMain --mode=allure --results-dir=target/allure-results
 * }
 * </pre>
 */
// public class ZephyrPublisherMain {
    
    private static final Logger logger = LoggerFactory.getLogger(ZephyrPublisherMain.class);
    
    public static void main(String[] args) {
        logger.info("Zephyr Publisher started");
        
        try {
            // Парсинг аргументов
            Options options = parseArguments(args);
            
            // Загрузить конфигурацию
            IntegrationConfig config = IntegrationConfig.fromCredentials();
            
            if (!config.isZephyrEnabled()) {
                logger.info("Zephyr integration is disabled. Exiting.");
                System.out.println("Zephyr integration disabled");
                return;
            }
            
            if (!config.isValid()) {
                logger.error("Zephyr configuration is invalid. Please check credentials.");
                System.err.println("ERROR: Invalid Zephyr configuration");
                System.exit(1);
            }
            
            // Создать Zephyr client
            logger.info("Creating Zephyr client: type={}", config.getZephyrType());
            ZephyrClient client = ZephyrClientFactory.create(config);
            
            // Проверить доступность
            if (!client.isAvailable()) {
                logger.warn("Zephyr API is not available. Results may not be published.");
            }
            
            // Опубликовать результаты
            if ("allure".equalsIgnoreCase(options.mode) || "both".equalsIgnoreCase(options.mode)) {
                publishAllureResults(client, options.resultsDir);
            }
            
            // Закрыть клиент
            client.close();
            
            logger.info("Zephyr Publisher finished successfully");
            System.out.println("Successfully published results to Zephyr");
            
        } catch (Exception e) {
            logger.error("Failed to publish results to Zephyr: {}", e.getMessage(), e);
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Опубликовать результаты из Allure
     */
    private static void publishAllureResults(ZephyrClient client, String resultsDir) {
        logger.info("Publishing Allure results from: {}", resultsDir);
        
        Path allureResultsPath = Paths.get(resultsDir);
        AllureResultsPublisher publisher = new AllureResultsPublisher(client);
        publisher.publishResults(allureResultsPath);
    }
    
    /**
     * Парсинг аргументов командной строки
     */
    private static Options parseArguments(String[] args) {
        Options options = new Options();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if ("--mode".equals(arg) && i + 1 < args.length) {
                options.mode = args[++i];
            } else if ("--results-dir".equals(arg) && i + 1 < args.length) {
                options.resultsDir = args[++i];
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                System.exit(0);
            }
        }
        
        // Значения по умолчанию
        if (options.mode == null || options.mode.isEmpty()) {
            options.mode = "allure";
        }
        
        if (options.resultsDir == null || options.resultsDir.isEmpty()) {
            options.resultsDir = "target/allure-results";
        }
        
        return options;
    }
    
    /**
     * Вывести справку
     */
    private static void printHelp() {
        System.out.println("Zephyr Publisher - Publish test results to Zephyr");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java com.bft.integration.ZephyrPublisherMain [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --mode <mode>           Publication mode: allure, testng, both (default: allure)");
        System.out.println("  --results-dir <path>    Path to allure-results directory (default: target/allure-results)");
        System.out.println("  --help, -h              Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  mvn exec:java -Dexec.mainClass=\"com.bft.integration.ZephyrPublisherMain\" \\");
        System.out.println("    -Dexec.args=\"--mode=allure --results-dir=target/allure-results\"");
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("  Set credentials in credentials.properties or environment variables:");
        System.out.println("  - jira.url");
        System.out.println("  - jira.username");
        System.out.println("  - jira.api.token");
        System.out.println("  - jira.project.key");
        System.out.println("  - zephyr.enabled=true");
        System.out.println("  - zephyr.type=scale|squad");
    }
    
    /**
     * Класс для хранения опций командной строки
     */
    private static class Options {
        String mode;
        String resultsDir;
    }
// }
