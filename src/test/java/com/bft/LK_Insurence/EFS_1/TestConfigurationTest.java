package com.bft.LK_Insurence.EFS_1;

import com.bft.config.TestConfiguration;
import com.bft.config.TestStrategyType;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Тесты для проверки инициализации конфигурации фреймворка
 * 
 * Проверяет корректность работы TestConfiguration с различными стратегиями.
 */
@Epic("Configuration")
@Feature("Test Configuration")
public class TestConfigurationTest {

    private static final Logger log = LoggerFactory.getLogger(TestConfigurationTest.class);

    @Test(groups = {"config", "smoke"}, 
          testName = "#1 Проверка инициализации конфигурации",
          description = "Проверка инициализации конфигурации")
    @AllureId("CONFIG-001")
    @Story("Configuration Initialization")
    @Description("Тест проверяет инициализацию конфигурации с различными стратегиями: автоматическая, UI, API")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfigurationInitialization() {
        try {
            // Test automatic strategy selection
            TestConfiguration.initialize();
            log.info("✓ Automatic configuration initialization successful");

            // Reset for next test
            TestConfiguration.reset();

            // Test explicit UI strategy
            TestConfiguration.initialize(TestStrategyType.UI);
            log.info("✓ UI strategy configuration initialization successful");

            // Reset for next test
            TestConfiguration.reset();

            // ЗАКОММЕНТИРОВАНО: API тесты не используются
            // Test explicit API strategy
            // TestConfiguration.initialize(TestStrategyType.API);
            // System.out.println("✓ API strategy configuration initialization successful");

        } catch (Exception e) {
            log.error("✗ Configuration initialization failed: {}", e.getMessage());
            throw e;
        }
    }
}