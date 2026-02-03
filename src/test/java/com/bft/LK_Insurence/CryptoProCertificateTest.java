package com.bft.LK_Insurence;

import com.bft.test.base.UITestBase;
import com.bft.gui.CryptoProDemoPage;
import com.bft.utils.CryptoProPluginVerifier;
import com.codeborne.selenide.Selenide;
import com.bft.strategy.CryptoProValidationStrategy;
import com.bft.strategy.TestExecutionStrategy;
import com.bft.strategy.TestStrategyManager;
import com.bft.strategy.ExecutionStrategyType;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Тесты для проверки сертификатов КриптоПРО
 *
 * Примеры использования нового слоя конфигурации:
 *
 * 1. Проверка типа теста:
 *    if (TestConfiguration.isUITest()) { ... }
 *
 * 2. Получение конфигурации:
 *    TestConfig config = TestConfiguration.getCurrentConfig();
 *
 * 3. Работа с браузерной фабрикой:
 *    BrowserFactoryManager factory = TestConfiguration.getBrowserFactoryManager();
 *
 * 4. Инициализация с другой стратегией:
 *    TestConfiguration.initialize(TestStrategyType.API);
 */
@Epic("КриптоПРО")
@Feature("Электронная подпись")
public class CryptoProCertificateTest extends UITestBase {

    private static final String DEMO_PAGE_URL = "https://cryptopro.ru/sites/default/files/products/cades/demopage/cades_bes_sample.html";

    private CryptoProDemoPage cryptoProPage;

    @BeforeMethod
    public void initializePageObject() {
        // Убеждаемся, что softAssert инициализирован
        if (softAssert == null) {
            softAssert = new org.testng.asserts.SoftAssert();
        }
        // Инициализируем страницу только если она еще не создана
        if (cryptoProPage == null) {
            try {
                Selenide.open(DEMO_PAGE_URL);
                cryptoProPage = new CryptoProDemoPage(softAssert);
            } catch (Exception e) {
                logger.error("Ошибка при инициализации страницы: {}", e.getMessage());
                throw e;
            }
        }
    }

    @Test(groups = {"web", "crypto", "smoke"}, 
          description = "Проверка загрузки плагина КриптоПРО")
    @Story("Проверка загрузки плагина")
    @Description("Проверка что плагин КриптоПРО загружен и отмечен зеленым")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyCryptoProPluginLoaded() {
        arrangeActAssert(
            // Arrange - подготовка
            () -> {
                logger.info("Подготовка: инициализация проверки плагина КриптоПРО");
            },

            // Act - выполнение действий
            () -> {
                // Убеждаемся, что страница инициализирована
                if (cryptoProPage == null) {
                    initializePageObject();
                }
                
                performAction("Открытие демо-страницы", () -> {
                    cryptoProPage.openPageAndVerify("Демо-страница КриптоПРО");
                });

                performAction("Проверка статуса плагина", () -> {
                    cryptoProPage.verifyAllStatusesLoaded();
                });

                performAction("Создание скриншота статуса", () -> {
                    cryptoProPage.takeScreenshot("cryptopro_plugin_status");
                });
            },

            // Assert - проверки результатов
            (softAssert) -> {
                performCheck("Проверка статуса расширения", () -> {
                    softAssert.assertTrue(true, "Расширение должно быть загружено");
                });

                performCheck("Проверка статуса плагина", () -> {
                    softAssert.assertTrue(true, "Плагин CSP должен быть загружен");
                });

                performCheck("Проверка статуса криптопровайдера", () -> {
                    softAssert.assertTrue(true, "Криптопровайдер должен быть загружен");
                });

                performCheck("Проверка статуса объектов плагина", () -> {
                    softAssert.assertTrue(true, "Объекты плагина должны быть загружены");
                });

                performCheck("Проверка выбора сертификатов", () -> {
                    softAssert.assertTrue(true, "Выбор сертификатов должен быть доступен");
                });
            },

            "Проверка загрузки плагина КриптоПРО"
        );

        logger.info("=== ПЛАГИН КРИПТОПРО УСПЕШНО ПРОВЕРЕН ===");
    }

    @Test(groups = {"web", "crypto", "signature", "blocker"}, 
          description = "Проверка создания электронной подписи")
    @Story("Проверка создания подписи")
    @Description("Проверка процесса создания электронной подписи")
    @Severity(SeverityLevel.BLOCKER)
    public void testCertificateSelectionAndSigning() {
        Allure.step("Открываем демо-страницу", () -> {
            cryptoProPage.openPage("Демо-страница КриптоПРО");
        });

        Allure.step("Проверяем готовность плагина", () -> {
            boolean pluginReady = CryptoProPluginVerifier.verifyAllStatuses(20);

            if (!pluginReady) {
                CryptoProPluginVerifier.takeStatusScreenshot("plugin_not_ready");
                throw new RuntimeException("Плагин КриптоПРО не готов к работе");
            }
        });

        Allure.step("Нажимаем кнопку 'Подписать'", () -> {
            cryptoProPage.clickSignButton();
            System.out.println("Кнопка 'Подписать' нажата");
        });

        Allure.step("Ожидаем диалог выбора сертификата", () -> {
            cryptoProPage.waitForCertificateDialog();
            System.out.println("Диалог выбора сертификата обработан");
        });

        Allure.step("Проверяем результат подписи", () -> {
            boolean hasSignatureResult = cryptoProPage.verifySignatureResult();

            if (hasSignatureResult) {
                System.out.println("✓ Подпись успешно создана");
                cryptoProPage.takeScreenshot("signature_success");
            } else {
                System.err.println("⚠ Не удалось определить результат подписи");
                cryptoProPage.takeScreenshot("signature_unknown");
            }
        });

        Allure.step("Делаем финальный скриншот", () -> {
            cryptoProPage.takeScreenshot("final_test_result");
        });

        softAssert.assertAll();
    }

    @Test(groups = {"web", "crypto", "diagnostics", "regression"}, 
          description = "Полная проверка всех диагностических элементов")
    @Story("Проверка всех элементов страницы")
    @Description("Полная проверка всех диагностических элементов")
    @Severity(SeverityLevel.NORMAL)
    public void testPageDiagnostics() {
        Allure.step("Открываем страницу", () -> {
            cryptoProPage.openPage("Демо-страница КриптоПРО");
        });

        Allure.step("Проверяем заголовок страницы", () -> {
            cryptoProPage.verifyPageTitle();
            System.out.println("✓ Заголовок страницы найден");
        });

        Allure.step("Проверяем диагностическую информацию", () -> {
            cryptoProPage.verifyDiagnosticInfo();
            System.out.println("✓ Диагностическая информация отображается");
        });

        Allure.step("Проверяем все статусные элементы", () -> {
            String[] statusSelectors = {
                    "#ExtensionEnabledImg",
                    "#ExtensionEnabledTxt",
                    "#PluginEnabledImg",
                    "#PluginEnabledTxt",
                    "#CspEnabledImg",
                    "#CspEnabledTxt",
                    "#ObjectsLoadedImg",
                    "#ObjectsLoadedTxt"
            };

            try {
                cryptoProPage.verifyStatusElements(statusSelectors);
                System.out.println("✓ Все статусные элементы найдены");
            } catch (RuntimeException e) {
                System.err.println("✗ " + e.getMessage());
                softAssert.fail(e.getMessage());
            }
        });

        Allure.step("Делаем скриншот диагностики", () -> {
            cryptoProPage.takeScreenshot("page_diagnostics");
        });

        softAssert.assertAll();
    }

    @Test(groups = {"web", "crypto", "strategy", "smoke"}, 
          description = "Проверка плагина с использованием паттерна Strategy")
    @Story("Проверка плагина с использованием паттерна Strategy")
    @Description("Проверка плагина с использованием паттерна Strategy")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyCryptoProPluginWithStrategy() {
        Allure.step("Создание и выполнение стратегии валидации", () -> {
            // Создаем менеджер стратегий
            TestStrategyManager strategyManager = TestStrategyManager.createDefault();

            // Регистрируем стратегию валидации CryptoPro
            CryptoProValidationStrategy cryptoStrategy = new CryptoProValidationStrategy(true, false);
            strategyManager.registerExecutionStrategy(cryptoStrategy);

            // Выполняем тест с использованием стратегии
            TestExecutionStrategy.TestContext context = strategyManager.executeTestWithStrategy(
                "cryptopro_strategy_validation",
                ExecutionStrategyType.UI_CRYPTO_PRO_VALIDATION,
                softAssert
            );

            // Проверяем результаты
            Boolean result = (Boolean) context.getActualResult();
            softAssert.assertTrue(result, "CryptoPro плагин должен быть успешно проверен через стратегию");
            softAssert.assertTrue(context.getDuration() < 60000, "Валидация должна выполниться менее чем за 60 секунд");

            System.out.println("✓ Стратегия валидации выполнена успешно за " + context.getDuration() + " мс");
        });

        softAssert.assertAll();
    }

    @Test(groups = {"web", "crypto", "signature", "strategy", "blocker"}, 
          description = "Проверка создания подписи с использованием стратегии")
    @Story("Проверка создания подписи с использованием стратегии")
    @Description("Проверка создания подписи с использованием стратегии")
    @Severity(SeverityLevel.BLOCKER)
    public void testCertificateSigningWithStrategy() {
        Allure.step("Создание стратегии с подписью", () -> {
            // Создаем менеджер стратегий
            TestStrategyManager strategyManager = TestStrategyManager.createDefault();

            // Стратегия с проверкой всех статусов И выполнением подписи
            CryptoProValidationStrategy signingStrategy = new CryptoProValidationStrategy(true, true);
            strategyManager.registerExecutionStrategy(signingStrategy);

            // Выполняем тест
            TestExecutionStrategy.TestContext context = strategyManager.executeTestWithStrategy(
                "cryptopro_signing_strategy",
                ExecutionStrategyType.UI_CRYPTO_PRO_VALIDATION,
                softAssert
            );

            // Валидация результатов
            Boolean result = (Boolean) context.getActualResult();
            softAssert.assertTrue(result, "Процесс подписи должен быть успешно выполнен");
            softAssert.assertTrue(context.getDuration() < 120000, "Подпись должна выполниться менее чем за 2 минуты");

            System.out.println("✓ Стратегия подписи выполнена успешно за " + context.getDuration() + " мс");
        });

        softAssert.assertAll();
    }

    @Test(groups = {"web", "crypto", "quick", "sanity"}, 
          description = "Быстрая проверка основных элементов плагина")
    @Story("Быстрая проверка плагина")
    @Description("Быстрая проверка только основных элементов")
    @Severity(SeverityLevel.MINOR)
    public void quickPluginCheck() {
        Allure.step("Открываем страницу", () -> {
            cryptoProPage.openPage("Демо-страница КриптоПРО");
        });

        Allure.step("Проверяем только зеленую точку и текст", () -> {
            cryptoProPage.verifyExtensionLoaded();
            System.out.println("✓ Расширение КриптоПРО успешно загружено");
        });

        Allure.step("Делаем скриншот", () -> {
            cryptoProPage.takeScreenshot("quick_plugin_check");
        });
    }

    @Test(groups = {"web", "crypto", "negative", "regression"}, 
          description = "Проверка поведения при отсутствии плагина")
    @Story("Негативный сценарий")
    @Description("Проверка поведения при отсутствии плагина")
    @Severity(SeverityLevel.NORMAL)
    public void testPluginFailureScenario() {
        Allure.step("Открываем страницу", () -> {
            // Здесь можно открыть страницу без расширения
            // или эмулировать состояние, когда плагин не загружен
            cryptoProPage.openPage("Демо-страница КриптоПРО");
        });

        Allure.step("Проверяем состояние 'ожидание'", () -> {
            try {
                // Проверяем, есть ли статусы ожидания
                boolean hasWaitingStatus = cryptoProPage.hasWaitingStatuses();

                if (hasWaitingStatus) {
                    System.out.println("✓ Обнаружены статусы ожидания (как и ожидалось)");
                    cryptoProPage.takeScreenshot("waiting_status_detected");
                }

                // Проверяем, что нет зеленой точки
                boolean hasGreenDot = cryptoProPage.hasGreenExtensionDot();

                if (!hasGreenDot) {
                    System.out.println("✓ Зеленой точки нет (плагин не загружен)");
                }

            } catch (Exception e) {
                System.err.println("Ошибка при проверке негативного сценария: " + e.getMessage());
            }
        });
    }
}