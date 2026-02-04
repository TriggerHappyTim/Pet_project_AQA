// ============================================================================
// ЗАКОММЕНТИРОВАНО: Расширенные примеры отчетности не используются в проекте
// ============================================================================
// package com.bft.test.examples;
//
// import com.bft.test.annotations.*;
// import com.bft.test.base.UITestBase;
// import com.bft.test.logging.AllureIntegration;
// import com.bft.test.logging.TestLogger;
// import com.bft.gui.CryptoProDemoPage;
// import io.qameta.allure.*;
// import org.testng.annotations.Test;
//
// import java.util.Map;

/**
 * Примеры использования улучшенной отчетности и логирования
 * Демонстрирует все возможности новых аннотаций и интеграции с Allure
 * ЗАКОММЕНТИРОВАНО: Расширенные примеры отчетности не используются
 */
// @TestType(TestType.Type.UI)
// @TestPriority(TestPriority.Priority.HIGH)
// @Requirement(
//     id = "REQ-UI-001",
//     name = "Пользовательский интерфейс должен корректно отображаться",
//     source = "UI Specification v2.1",
//     version = "2.1",
//     author = "UI Team",
//     status = Requirement.Status.ACTIVE
// )
// @TestEnvironment(
//     browsers = {TestEnvironment.Browser.CHROME, TestEnvironment.Browser.FIREFOX},
//     operatingSystems = {TestEnvironment.OS.WINDOWS, TestEnvironment.OS.LINUX},
//     minJavaVersion = "11"
// )
// @Epic("Продвинутая отчетность")
// @Feature("Примеры использования")
// public class AdvancedReportingExample extends UITestBase {
//
//     private final TestLogger logger = TestLogger.forTest("AdvancedReportingExample");
//
//     /**
//      * ПРИМЕР 1: Тест с полным набором аннотаций и логирования
//      */
//     @Test(description = "Комплексный тест с улучшенной отчетностью")
//     @TestPriority(TestPriority.Priority.CRITICAL)
//     @AutomationAction(value = "Проверка полной функциональности", type = AutomationAction.ActionType.ACTION)
//     @Requirement(
//         id = "REQ-FUNC-001",
//         name = "Все функции должны работать корректно",
//         version = "1.0"
//     )
//     public void comprehensiveReportingTest() {
//         logger.testStarted();
//
//         try {
//             arrangeActAssert(
//                 // Arrange - подготовка с логированием
//                 () -> {
//                     logger.info("Подготовка тестовых данных для комплексного тестирования");
//                     logger.attachJson("test_config", Map.of(
//                         "browser", "chrome",
//                         "environment", "test",
//                         "timeout", 30000
//                     ));
//                 },
//
//                 // Act - выполнение с детальным логированием
//                 () -> performAction("Открытие и настройка страницы", () -> {
//                     CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);
//
//                     logger.step("Открытие страницы", () -> {
//                         cryptoPage.openPageAndVerify("Демонстрационная страница КриптоПро ЭЦП Browser plug-in");
//                         AllureIntegration.attachPageInfo("КриптоПРО демо");
//                     });
//
//                     logger.step("Выполнение диагностики", () -> {
//                         cryptoPage.performFullDiagnostic();
//                         AllureIntegration.attachScreenshot("diagnostic_complete");
//                     });
//
//                     logger.action("Получение статуса", () -> {
//                         var statusInfo = cryptoPage.getPluginStatusInfo();
//                         AllureIntegration.attachJson("plugin_status", statusInfo);
//                     }, true);
//                 }),
//
//                 // Assert - проверки с детальным логированием
//                 (softAssert) -> {
//                     performCheck("Проверка готовности плагина", () -> {
//                         softAssert.assertTrue(true, "Плагин должен быть готов");
//                         logger.check("Готовность плагина", true);
//                     });
//
//                     performCheck("Проверка производительности", () -> {
//                         softAssert.assertTrue(true, "Производительность в норме");
//                         logger.check("Производительность", true);
//                     });
//
//                     // Прикрепление дополнительных данных
//                     AllureIntegration.attachSystemInfo();
//                     AllureIntegration.attachTestConfiguration();
//                     AllureIntegration.attachBrowserLogs();
//                 },
//
//                 "Комплексный тест с полной отчетностью"
//             );
//
//             logger.testFinished(true, 15000);
//
//         } catch (Exception e) {
//             logger.testFinished(false, 15000);
//             AllureIntegration.attachErrorInfo("comprehensive_test", e);
//             throw e;
//         }
//     }
//
//     /**
//      * ПРИМЕР 2: Data-driven тест с улучшенной отчетностью
//      */
//     @Test(dataProvider = "comprehensiveTestData")
//     @TestPriority(TestPriority.Priority.MEDIUM)
//     @AutomationAction(value = "Параметризованная проверка", type = AutomationAction.ActionType.VERIFICATION)
//     public void parameterizedReportingTest(String testName, String browser, boolean expectedSuccess) {
//
//         logger.info("Запуск параметризованного теста: {} с браузером {}", testName, browser);
//
//         // Добавление параметров в отчет
//         Allure.parameter("Test Name", testName);
//         Allure.parameter("Browser", browser);
//         Allure.parameter("Expected Success", expectedSuccess);
//
//         try {
//             arrangeActAssert(
//                 () -> logger.info("Подготовка параметризованного теста"),
//
//                 () -> {
//                     CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);
//                     cryptoPage.openPageAndVerify("Демонстрационная страница КриптоПро ЭЦП Browser plug-in");
//
//                     if ("full".equals(testName)) {
//                         cryptoPage.performFullDiagnostic();
//                     }
//
//                     AllureIntegration.attachScreenshot("parameterized_" + testName);
//                 },
//
//                 (softAssert) -> {
//                     performCheck("Проверка результата", () -> {
//                         softAssert.assertEquals(true, expectedSuccess,
//                             "Результат должен соответствовать ожиданию");
//                     });
//
//                     // Прикрепление данных о параметрах
//                     AllureIntegration.attachJson("test_parameters", Map.of(
//                         "name", testName,
//                         "browser", browser,
//                         "success", expectedSuccess
//                     ));
//                 },
//
//                 "Параметризованный тест: " + testName
//             );
//
//         } catch (Exception e) {
//             AllureIntegration.attachErrorInfo("parameterized_" + testName, e);
//             throw e;
//         }
//     }
//
//     @org.testng.annotations.DataProvider(name = "comprehensiveTestData")
//     public Object[][] comprehensiveTestData() {
//         return new Object[][] {
//             {"basic", "chrome", true},
//             {"full", "firefox", true},
//             {"minimal", "chrome", false}
//         };
//     }
//
//     /**
//      * ПРИМЕР 3: Тест производительности с метриками
//      */
//     @Test(description = "Тест производительности с детальными метриками")
//     @TestPriority(TestPriority.Priority.HIGH)
//     @AutomationAction(value = "Измерение производительности", type = AutomationAction.ActionType.ASSERTION, timeout = 60)
//     public void performanceReportingTest() {
//         logger.info("Запуск теста производительности");
//
//         long startTime = System.currentTimeMillis();
//         long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//         try {
//             arrangeActAssert(
//                 () -> {
//                     logger.info("Подготовка теста производительности");
//                     AllureIntegration.attachSystemInfo();
//                 },
//
//                 () -> {
//                     logger.step("Выполнение операций", () -> {
//                         CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);
//
//                         // Измеряем время открытия страницы
//                         long pageOpenStart = System.currentTimeMillis();
//                         cryptoPage.openPageAndVerify("Performance Test");
//                         long pageOpenTime = System.currentTimeMillis() - pageOpenStart;
//
//                         logger.attachPerformanceMetrics("page_open", pageOpenTime, 0);
//
//                         // Измеряем время диагностики
//                         long diagnosticStart = System.currentTimeMillis();
//                         cryptoPage.performFullDiagnostic();
//                         long diagnosticTime = System.currentTimeMillis() - diagnosticStart;
//
//                         logger.attachPerformanceMetrics("diagnostic", diagnosticTime, 0);
//                     });
//                 },
//
//                 (softAssert) -> {
//                     long endTime = System.currentTimeMillis();
//                     long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//                     long totalDuration = endTime - startTime;
//                     long memoryUsage = endMemory - startMemory;
//
//                     performCheck("Проверка времени выполнения", () -> {
//                         softAssert.assertTrue(totalDuration < 45000,
//                             "Тест должен выполниться менее чем за 45 секунд, фактически: " + totalDuration + "мс");
//                     });
//
//                     performCheck("Проверка потребления памяти", () -> {
//                         softAssert.assertTrue(memoryUsage < 50 * 1024 * 1024, // 50 MB
//                             "Потребление памяти должно быть менее 50MB, фактически: " + memoryUsage / 1024 / 1024 + "MB");
//                     });
//
//                     // Прикрепление метрик производительности
//                     logger.attachPerformanceMetrics("total_test", totalDuration, memoryUsage / 1024);
//                     AllureIntegration.attachPerformanceMetrics("full_test_execution", totalDuration, memoryUsage / 1024);
//                 },
//
//                 "Тест производительности с метриками"
//             );
//
//         } catch (Exception e) {
//             AllureIntegration.attachErrorInfo("performance_test", e);
//             throw e;
//         }
//     }
//
//     /**
//      * ПРИМЕР 4: Тест с автоматическим логированием HTTP запросов
//      */
//     @Test(description = "Тест с логированием сетевых взаимодействий")
//     @AutomationAction(value = "API взаимодействие", type = AutomationAction.ActionType.ACTION)
//     public void networkLoggingTest() {
//         logger.info("Тест с логированием сетевых запросов");
//
//         arrangeActAssert(
//             () -> {
//                 logger.info("Подготовка API теста");
//                 AllureIntegration.attachTestConfiguration();
//             },
//
//             () -> {
//                 // Имитация HTTP запросов с логированием
//                 logger.logHttpRequest("GET", "/api/health", 200, 150);
//                 logger.logHttpRequest("POST", "/api/users", 201, 300);
//                 logger.logHttpRequest("GET", "/api/users/123", 200, 120);
//
//                 // Прикрепление логов сетевых запросов
//                 AllureIntegration.attachNetworkLogs();
//             },
//
//             (softAssert) -> {
//                 performCheck("Проверка HTTP ответов", () -> {
//                     softAssert.assertTrue(true, "Все HTTP запросы должны быть успешными");
//                 });
//
//                 // Прикрепление статистики запросов
//                 String requestStats = "HTTP Requests Summary:\n" +
//                     "- GET /api/health: 200 (150ms)\n" +
//                     "- POST /api/users: 201 (300ms)\n" +
//                     "- GET /api/users/123: 200 (120ms)\n" +
//                     "- Total: 3 requests, 0 errors";
//
//                 AllureIntegration.attachFile("HTTP Statistics", requestStats);
//             },
//
//             "Тест с логированием сетевых взаимодействий"
//         );
//     }
//
//     /**
//      * ПРИМЕР 5: Тест с комплексной отладочной информацией
//      */
//     @Test(description = "Тест с полной отладочной информацией")
//     @TestPriority(TestPriority.Priority.LOW)
//     @AutomationAction(value = "Комплексная отладка", type = AutomationAction.ActionType.VERIFICATION, takeScreenshot = true)
//     public void debugInformationTest() {
//         logger.info("Тест с комплексной отладочной информацией");
//
//         try {
//             arrangeActAssert(
//                 () -> {
//                     logger.info("Подготовка отладочного теста");
//                     // Прикрепление начальной отладочной информации
//                     AllureIntegration.attachDebugInfo("test_start");
//                 },
//
//                 () -> {
//                     CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);
//                     cryptoPage.openPageAndVerify("Демонстрационная страница КриптоПро ЭЦП Browser plug-in");
//
//                     // Прикрепление промежуточной информации
//                     AllureIntegration.attachPageInfo("After page load");
//                     AllureIntegration.attachScreenshot("after_page_load");
//                 },
//
//                 (softAssert) -> {
//                     performCheck("Проверка отладочной информации", () -> {
//                         softAssert.assertTrue(true, "Отладочная информация должна быть собрана");
//                     });
//
//                     // Финальная отладочная информация
//                     AllureIntegration.attachDebugInfo("test_completion");
//                     AllureIntegration.attachBrowserLogs();
//                 },
//
//                 "Тест с полной отладочной информацией"
//             );
//
//         } catch (Exception e) {
//             // При ошибке прикрепляем полную отладочную информацию
//             AllureIntegration.attachDebugInfo("test_error");
//             AllureIntegration.attachErrorInfo("debug_test_error", e);
//             throw e;
//         }
//     }
//
//     /**
//      * ПРИМЕР 6: Тест с кастомными метками и категориями
//      */
//     @Test(description = "Тест с расширенными метками Allure")
//     @TestPriority(TestPriority.Priority.MEDIUM)
//     @Requirement(
//         id = "REQ-REG-001",
//         name = "Регрессионные тесты должны выполняться регулярно",
//         source = "Regression Test Plan",
//         status = Requirement.Status.ACTIVE
//     )
//     public void customLabelsTest() {
//         logger.info("Тест с кастомными метками и категориями");
//
//         // Добавление кастомных меток в Allure
//         Allure.label("custom.tag", "regression");
//         Allure.label("test.category", "ui_validation");
//         Allure.label("business.area", "cryptography");
//         Allure.label("test.level", "integration");
//
//         // Добавление описания
//         Allure.description("Этот тест демонстрирует использование кастомных меток и категорий в Allure отчетах.\n\n" +
//                           "**Особенности:**\n" +
//                           "- Кастомные метки для фильтрации\n" +
//                           "- Категоризация по бизнес-областям\n" +
//                           "- Уровни тестирования\n" +
//                           "- Подробное описание");
//
//         arrangeActAssert(
//             () -> {
//                 logger.info("Подготовка теста с метками");
//                 AllureIntegration.attachTestConfiguration();
//             },
//
//             () -> {
//                 CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);
//                 cryptoPage.openPageAndVerify("Демонстрационная страница КриптоПро ЭЦП Browser plug-in");
//
//                 // Добавление дополнительных меток во время выполнения
//                 Allure.label("execution.step", "page_loaded");
//                 cryptoPage.verifyAllStatusesLoaded();
//                 Allure.label("execution.step", "diagnostic_completed");
//             },
//
//             (softAssert) -> {
//                 performCheck("Проверка работы с метками", () -> {
//                     softAssert.assertTrue(true, "Метки должны быть успешно добавлены");
//                 });
//
//                 // Добавление финальной метки
//                 Allure.label("execution.result", "success");
//             },
//
//             "Тест с расширенными метками Allure"
//         );
//     }
// }
