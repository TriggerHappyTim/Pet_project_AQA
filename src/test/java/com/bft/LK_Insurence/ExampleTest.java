// ============================================================================
// ЗАКОММЕНТИРОВАНО: Примеры тестов не используются в проекте
// ============================================================================
// package com.bft.LK_Insurence;
//
// import com.bft.BaseTest;
// import com.bft.config.TestConfiguration;
// import com.bft.pw.Selenide;
// import com.bft.strategy.BaseTestExecutionStrategy;
// import com.bft.strategy.TestExecutionStrategy;
// import com.bft.strategy.TestStrategyManager;
// import com.bft.test.annotations.*;
// import io.qameta.allure.*;
// import org.testng.annotations.BeforeMethod;
// import org.testng.annotations.BeforeSuite;
// import org.testng.annotations.Test;

/**
 * Пример использования паттерна Strategy для тестирования
 * Демонстрирует различные стратегии выполнения тестов
 * ЗАКОММЕНТИРОВАНО: Примеры тестов не используются
 */
// @Epic("Примеры и демонстрации")
// @Feature("Strategy Pattern")
/*@AutomationStatus(AutomationStatus.Status.AUTOMATED)
@TestEnvironment({TestEnvironment.Environment.LOCAL, TestEnvironment.Environment.STAGING})*/
// public class ExampleTest extends BaseTest {
//
//     private TestStrategyManager strategyManager;
//
//     @BeforeSuite
//     public void initializeConfiguration() {
//         // Инициализация конфигурации
//         TestConfiguration.initialize(com.bft.config.TestStrategyType.UI);
//
//         // Создание менеджера стратегий
//         strategyManager = com.bft.strategy.TestStrategyManager.createDefault();
//     }
//
//     @BeforeMethod
//     public void ensureStrategyManager() {
//         if (strategyManager == null) {
//             strategyManager = com.bft.strategy.TestStrategyManager.createDefault();
//         }
//     }
//
//     @Test(groups = {"example", "strategy", "regression"}, 
//           description = "Пример автоматического выбора стратегии")
//     @Story("Автоматический выбор стратегии")
//     @Description("Демонстрация автоматического выбора стратегии на основе имени теста")
//     @Severity(SeverityLevel.NORMAL)
//     public void exampleAutomaticStrategySelection() {
//         // Менеджер автоматически выберет подходящую стратегию
//         // на основе имени теста
//         var context = strategyManager.executeTest("crypto_pro_validation_test", softAssert);
//
//         System.out.println("Тест выполнен за: " + context.getDuration() + " мс");
//         System.out.println("Результат: " + context.getActualResult());
//
//         softAssert.assertAll();
//     }
//
//     @Test(groups = {"example", "strategy", "regression"}, 
//           description = "Пример явного указания стратегии")
//     @Story("Явное указание стратегии")
//     @Description("Демонстрация явного указания стратегии выполнения теста")
//     @Severity(SeverityLevel.NORMAL)
//     public void exampleExplicitStrategySelection() {
//         // Явно указываем стратегию выполнения
//         var context = strategyManager.executeTestWithStrategy(
//             "explicit_crypto_test",
//             com.bft.strategy.ExecutionStrategyType.UI_CRYPTO_PRO_VALIDATION,
//             softAssert
//         );
//
//         System.out.println("Тест с явной стратегией выполнен за: " + context.getDuration() + " мс");
//
//         softAssert.assertAll();
//     }
//
//     /*@Test(description = "Пример API тестирования")
//     @Story("API тестирование")
//     @Description("Демонстрация стратегии для тестирования API")
//     @Severity(SeverityLevel.NORMAL)
//     *//*@Priority(Priority.Level.LOW)*//*
//     public void exampleApiStrategy() {
//         // Создаем стратегию для API тестирования
//         ApiTestExecutionStrategy apiStrategy = new ApiTestExecutionStrategy(
//             "/api/health", "GET", 200
//         );
//
//         strategyManager.registerExecutionStrategy(apiStrategy);
//
//         var context = strategyManager.executeTestWithStrategy(
//             "api_health_check",
//             com.bft.strategy.ExecutionStrategyType.API_REST_CALL,
//             softAssert
//         );
//
//         System.out.println("API тест выполнен за: " + context.getDuration() + " мс");
//
//         softAssert.assertAll();
//     }*/
//
//     @Test(groups = {"example", "strategy", "regression"}, 
//           description = "Пример полной настройки стратегий")
//     @Story("Полная настройка стратегий")
//     @Description("Демонстрация полной настройки и использования различных стратегий")
//     @Severity(SeverityLevel.NORMAL)
//     public void exampleFullStrategySetup() {
//         // Этот тест показывает, как можно настроить все аспекты стратегии
//         // (пока без реализаций DataPreparationStrategy и ValidationStrategy)
//
//         try {
//             var context = strategyManager.executeTestWithFullSetup(
//                 "full_setup_test",
//                 com.bft.strategy.ExecutionStrategyType.UI_CRYPTO_PRO_VALIDATION,
//                 // ПЛАНИРУЕТСЯ: Реализация DataPreparationStrategy для подготовки тестовых данных из БД
//                 null, // DataPreparationStrategy.DataPreparationType.DATABASE,
//                 // ПЛАНИРУЕТСЯ: Реализация ValidationStrategy для строгой валидации результатов
//                 null, // ValidationStrategy.ValidationType.STRICT,
//                 softAssert
//             );
//
//             System.out.println("Тест с полной настройкой выполнен за: " + context.getDuration() + " мс");
//
//         } catch (Exception e) {
//             System.out.println("Полная настройка стратегий пока не реализована: " + e.getMessage());
//             softAssert.assertTrue(true, "Тест пропущен - функциональность в разработке");
//         }
//
//         softAssert.assertAll();
//     }
//
//     @Test(groups = {"example", "config", "smoke"}, 
//           description = "Пример использования конфигурации")
//     @Story("Использование конфигурации")
//     @Description("Демонстрация работы с конфигурацией фреймворка")
//     @Severity(SeverityLevel.MINOR)
//     public void exampleConfigurationUsage() {
//         // Используем helper для инициализации конфигурации
//         com.bft.test.helpers.TestSetupHelper.ensureConfigurationInitialized(com.bft.config.TestStrategyType.UI);
//         
//         // Проверка типа стратегии через helper
//         if (com.bft.test.helpers.TestSetupHelper.isUIEnvironment()) {
//             System.out.println("Это UI тест");
//         } else if (com.bft.test.helpers.TestSetupHelper.isAPIEnvironment()) {
//             System.out.println("Это API тест");
//         }
//
//         // Получение текущей конфигурации через helper (с автоматической проверкой)
//         try {
//             var config = com.bft.test.helpers.TestSetupHelper.getCurrentConfigSafe();
//             System.out.println("Браузер: " + config.getBrowser());
//             System.out.println("Окружение: " + config.getEnvironment());
//
//             // Получение стратегии через helper
//             var strategy = com.bft.test.helpers.TestSetupHelper.getCurrentStrategySafe();
//             System.out.println("Стратегия: " + strategy.getType());
//         } catch (IllegalStateException e) {
//             softAssert.fail("Ошибка при получении конфигурации: " + e.getMessage());
//         }
//
//         // Использование SoftAssert из конфигурации
//         softAssert.assertTrue(true, "Пример мягкой проверки");
//
//         softAssert.assertAll();
//     }
//
//     @Test(groups = {"example", "strategy", "custom", "regression"}, 
//           description = "Пример создания кастомной стратегии")
//     @Story("Кастомные стратегии")
//     @Description("Демонстрация создания и использования кастомной стратегии тестирования")
//     @Severity(SeverityLevel.NORMAL)
//     public void exampleCustomStrategy() {
//         // Создаем кастомную стратегию выполнения
//         TestExecutionStrategy<Boolean> customStrategy = new BaseTestExecutionStrategy<Boolean>("Custom Strategy", 1) {
//
//             @Override
//             public com.bft.strategy.ExecutionStrategyType getType() {
//                 return com.bft.strategy.ExecutionStrategyType.UI_NAVIGATION; // Используем существующий тип для примера
//             }
//
//             @Override
//             public boolean isApplicable(TestContext context) {
//                 return context.getTestName().contains("custom");
//             }
//
//             @Override
//             protected void performPreparation(TestContext context) {
//                 System.out.println("Подготовка кастомной стратегии");
//                 context.setTestData("prepared");
//             }
//
//             @Override
//             protected void performExecution(TestContext context) {
//                 System.out.println("Выполнение кастомной логики");
//                 // Имитация какой-то работы с умным ожиданием
//                 // Вместо sleep используем ожидание условия
//                 long startTime = System.currentTimeMillis();
//                 while (System.currentTimeMillis() - startTime < 100) {
//                     // Имитация работы
//                     Math.random(); // Бесполезная операция для имитации работы
//                 }
//                 context.setActualResult(true);
//             }
//
//             @Override
//             protected void performValidation(TestContext context, org.testng.asserts.SoftAssert softAssert) {
//                 Boolean result = (Boolean) context.getActualResult();
//                 softAssert.assertTrue(result, "Кастомная стратегия должна вернуть true");
//             }
//
//             @Override
//             protected void performCleanup(TestContext context) {
//                 System.out.println("Очистка кастомной стратегии");
//             }
//         };
//
//         // Регистрируем и выполняем
//         strategyManager.registerExecutionStrategy(customStrategy);
//
//         var context = strategyManager.executeTestWithStrategy(
//             "custom_strategy_test",
//             com.bft.strategy.ExecutionStrategyType.UI_NAVIGATION,
//             softAssert
//         );
//
//         System.out.println("Кастомная стратегия выполнена за: " + context.getDuration() + " мс");
//
//         softAssert.assertAll();
//     }
//
//     @Test(groups = {"example", "page-object", "crypto", "regression"}, 
//           description = "Пример улучшенного Page Object Pattern")
//     @Story("Улучшенный Page Object")
//     @Description("Демонстрация улучшенного Page Object Pattern с компонентами")
//     @Severity(SeverityLevel.NORMAL)
//     @TestPriority(TestPriority.Priority.MEDIUM)
//     public void exampleImprovedPageObject() {
//         // Сначала открываем URL (BasePage требует активный webdriver при создании)
//         String demoUrl = "https://cryptopro.ru/sites/default/files/products/cades/demopage/cades_bes_sample.html";
//         Selenide.open(demoUrl);
//
//         // Создаем страницу с улучшенным Page Object
//         com.bft.ui.pages.CryptoProDemoPage cryptoPage = new com.bft.ui.pages.CryptoProDemoPage(softAssert);
//
//         // Используем fluent API с автоматическими ожиданиями
//         cryptoPage.openPageAndVerify("КриптоПРО демо-страница")
//                   .waitForPageLoad()
//                   .performFullDiagnostic();
//         cryptoPage.takeScreenshot("diagnostic_complete");
//
//         // Проверяем статус плагина через компонент
//         var statusInfo = cryptoPage.getPluginStatusInfo();
//         System.out.println("Статус плагина: " + statusInfo);
//
//         softAssert.assertTrue(statusInfo.isFullyReady(), "Плагин должен быть полностью готов");
//         softAssert.assertAll();
//     }
// }
