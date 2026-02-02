package com.bft.LK_Insurence.EFS_1;

import com.bft.config.TestConfiguration;
import com.bft.config.TestStrategyType;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для проверки инициализации конфигурации фреймворка
 * 
 * Проверяет корректность работы TestConfiguration с различными стратегиями.
 */
@Epic("Configuration")
@Feature("Test Configuration")
public class TestConfigurationTest {

    @Test(groups = {"config", "smoke"}, 
          description = "Проверка инициализации конфигурации")
    @Story("Configuration Initialization")
    @Description("Тест проверяет инициализацию конфигурации с различными стратегиями: автоматическая, UI, API")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfigurationInitialization() {
        try {
            // Test automatic strategy selection
            TestConfiguration.initialize();
            System.out.println("✓ Automatic configuration initialization successful");

            // Reset for next test
            TestConfiguration.reset();

            // Test explicit UI strategy
            TestConfiguration.initialize(TestStrategyType.UI);
            System.out.println("✓ UI strategy configuration initialization successful");

            // Reset for next test
            TestConfiguration.reset();

            // Test explicit API strategy
            TestConfiguration.initialize(TestStrategyType.API);
            System.out.println("✓ API strategy configuration initialization successful");

        } catch (Exception e) {
            System.err.println("✗ Configuration initialization failed: " + e.getMessage());
            throw e;
        }
    }
}