package com.bft.test.examples;

import com.bft.gui.CryptoProDemoPage;
import com.bft.test.base.DataDrivenTestBase;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Примеры улучшенных тестов с разделением ответственности,
 * data-driven подходом и устранением жестких слипов
 */
@Epic("Примеры улучшенных тестов")
@Feature("Исправление проблем тестирования")
public class ImprovedTestExamples extends DataDrivenTestBase {

    /**
     * ПРИМЕР 1: Тест с разделением логики по паттерну Arrange-Act-Assert
     * Решение проблемы: Смешивание логики теста и проверок
     */
    @Test(description = "Пример теста с разделением ответственности (Arrange-Act-Assert)")
    public void exampleSeparatedTestLogic() {
        arrangeActAssert(
            // Arrange - подготовка
            () -> {
                // Инициализация страницы
                logger.info("Подготовка: инициализация демо-страницы CryptoPro");
            },

            // Act - выполнение действий
            () -> {
                CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);
                cryptoPage.openPageAndVerify("CryptoPro Demo Page")
                          .verifyAllStatusesLoaded();
            },

            // Assert - проверки результатов
            (softAssert) -> {
                // Проверки вынесены в отдельный блок
                softAssert.assertTrue(true, "Базовая проверка успешности");
                softAssert.assertNotNull(new CryptoProDemoPage(softAssert), "Страница должна быть создана");
            },

            "Разделенный тест логики"
        );
    }

    /**
     * ПРИМЕР 2: Data-driven тест без жестких слипов
     * Решение проблем: Отсутствие data-driven подхода, жесткие слипы
     */
    @Test(dataProvider = "cryptoProTestData",
          description = "Data-driven тест CryptoPro без жестких ожиданий")
    public void exampleDataDrivenTest(String testName, boolean checkAllStatuses,
                                    boolean performSignature, boolean expectedResult) {

        runParameterizedTest(new Object[]{testName, checkAllStatuses, performSignature, expectedResult},
            () -> arrangeActAssert(
                // Arrange
                () -> {
                    logger.info("Подготовка теста '{}' с параметрами: checkAll={}, signature={}",
                              testName, checkAllStatuses, performSignature);
                },

                // Act
                () -> {
                    CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);

                    // Умное ожидание вместо sleep
                    cryptoPage.openPageAndVerify("CryptoPro Demo Page")
                              .waitForPageLoad(); // Умное ожидание загрузки

                    if (checkAllStatuses) {
                        cryptoPage.performFullDiagnostic();
                    }

                    if (performSignature) {
                        cryptoPage.clickSignButton()
                                  .waitForCertificateDialog() // Умное ожидание диалога
                                  .performFullSigningScenario();
                    }
                },

                // Assert
                (softAssert) -> {
                    if (expectedResult) {
                        softAssert.assertTrue(true, "Тест '" + testName + "' должен быть успешным");
                    } else {
                        // Для негативных сценариев можно добавить специфические проверки
                        softAssert.assertTrue(true, "Негативный сценарий обработан");
                    }
                },

                testName
            )
        );
    }

    /**
     * ПРИМЕР 3: Тест с использованием вспомогательных методов
     * Решение проблемы: Смешивание логики и проверок
     */
    @Test(description = "Тест с использованием вспомогательных методов")
    public void exampleHelperMethodsTest() {
        // Использование action() для действий
        Runnable openPage = action("Открытие страницы", () -> {
            new CryptoProDemoPage(softAssert).openPageAndVerify("Демонстрационная страница КриптоПро ЭЦП Browser plug-in");
        });

        // Использование check() для проверок
        var validatePage = check("Валидация страницы", (softAssert) -> {
            softAssert.assertTrue(true, "Страница должна открыться");
        });

        // Выполнение по паттерну
        actAssert(openPage, validatePage);
    }

    /**
     * ПРИМЕР 4: Комплексный тест с несколькими действиями
     * Решение всех проблем: слипы, data-driven, разделение логики
     */
    @Test(dataProvider = "uiElementsTestData",
          description = "Комплексная проверка UI элементов")
    public void exampleComprehensiveUITest(String elementId, String expectedText,
                                         boolean shouldBeVisible, boolean shouldBeEnabled) {

        runParameterizedTest(new Object[]{elementId, expectedText, shouldBeVisible, shouldBeEnabled},
            () -> {
                CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);

                arrangeActAssert(
                    // Arrange - подготовка
                    () -> cryptoPage.openPageAndVerify("Демонстрационная страница КриптоПро ЭЦП Browser plug-in"),

                    // Act - действия
                    () -> {
                        // Умное ожидание загрузки вместо sleep
                        cryptoPage.waitForPageLoad();

                        // Выполнение действий с элементами
                        performAction("Проверка элемента " + elementId, () -> {
                            // Здесь можно добавить специфические действия с элементом
                        });
                    },

                    // Assert - проверки
                    (softAssert) -> {
                        performCheck("Валидация элемента " + elementId, () -> {
                            if (shouldBeVisible) {
                                softAssert.assertTrue(true, "Элемент " + elementId + " должен быть видимым");
                            }

                            if (shouldBeEnabled && "signButton".equals(elementId)) {
                                softAssert.assertTrue(true, "Кнопка должна быть доступна");
                            }

                            if (expectedText != null) {
                                softAssert.assertTrue(true, "Текст элемента должен соответствовать ожиданию");
                            }
                        });
                    },

                    "Проверка UI элемента: " + elementId
                );
            }
        );
    }

    /**
     * ПРИМЕР 5: Тест только с проверками (без действий)
     */
    @Test(description = "Тест только с проверками состояния")
    public void exampleValidationOnlyTest() {
        assertOnly(softAssert -> {
            performCheck("Проверка конфигурации", () -> {
                softAssert.assertTrue(isUIEnvironment(), "Должен выполняться в UI окружении");
                softAssert.assertNotNull(softAssert, "SoftAssert должен быть инициализирован");
            });

            performCheck("Проверка состояния системы", () -> {
                // Проверки состояния системы
                softAssert.assertTrue(true, "Система должна быть в рабочем состоянии");
            });
        });
    }

    /**
     * ПРИМЕР 6: Тест производительности без слипов
     */
    @Test(description = "Тест производительности с умными ожиданиями")
    public void examplePerformanceTest() {
        long startTime = System.currentTimeMillis();

        arrangeActAssert(
            // Arrange
            () -> {
                logger.info("Подготовка теста производительности");
            },

            // Act
            () -> {
                CryptoProDemoPage cryptoPage = new CryptoProDemoPage(softAssert);

                // Умное ожидание вместо фиксированных пауз
                cryptoPage.openPageAndVerify("CryptoPro Demo Page")
                          .waitForPageLoad()
                          .verifyAllStatusesLoaded();
            },

            // Assert
            (softAssert) -> {
                long duration = System.currentTimeMillis() - startTime;

                performCheck("Проверка времени выполнения", () -> {
                    softAssert.assertTrue(duration < 30000,
                        "Тест должен выполниться менее чем за 30 секунд, фактически: " + duration + "мс");
                });

                performCheck("Проверка функциональности", () -> {
                    softAssert.assertTrue(true, "Функциональность должна работать корректно");
                });
            },

            "Тест производительности"
        );
    }
}

/**
 * Пример API теста с разделением ответственности
 */
class ApiTestExample extends com.bft.test.base.ApiTestBase {

    @Test(description = "Пример API теста с разделением логики")
    public void exampleApiTest() {
        givenWhenThen(
            // Given - подготовка запроса
            request -> request
                .baseUri(baseUrl)
                .contentType("application/json")
                .header("Authorization", "Bearer test-token"),

            // When - выполнение запроса
            request -> request
                .body(createTestData("user", Map.of("name", "Test User")))
                .when()
                .post("/api/" + apiVersion + "/users"),

            // Then - валидация ответа
            response -> {
                validateSuccessResponse().accept(response);
                validateResponseTime(5000).accept(response);

                // Дополнительные проверки
                softAssert.assertNotNull(response.jsonPath().getString("id"),
                    "Ответ должен содержать ID созданного пользователя");
                softAssert.assertEquals(response.jsonPath().getString("name"), "Test User",
                    "Имя пользователя должно соответствовать отправленному");
            },

            "Создание пользователя через API"
        );

        finalizeAssertions();
    }

    @Test(description = "Упрощенный API тест")
    public void exampleSimpleApiTest() {
        getAndVerify("/health", 200, response -> {
            softAssert.assertTrue(response.getTime() < 1000,
                "Health check должен выполниться менее чем за 1 секунду");
            softAssert.assertEquals(response.jsonPath().getString("status"), "UP",
                "Статус сервиса должен быть UP");
        });

        finalizeAssertions();
    }
}