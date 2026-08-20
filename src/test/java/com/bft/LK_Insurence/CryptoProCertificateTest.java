package com.bft.LK_Insurence;

import com.bft.ui.pages.CryptoProDemoPage;
import com.bft.strategy.CryptoProValidationStrategy;
import com.bft.strategy.ExecutionStrategyType;
import com.bft.strategy.TestExecutionStrategy;
import com.bft.strategy.TestStrategyManager;
import com.bft.test.base.UITestBase;
import com.bft.test.retry.Retry;
import com.bft.test.retry.RetryAnalyzer;
import com.bft.utils.CryptoProPluginVerifier;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("КриптоПРО")
@Feature("Электронная подпись")
public class CryptoProCertificateTest extends UITestBase {

    private CryptoProDemoPage cryptoProPage;

    @BeforeMethod
    public void initializePageObject() {
        // Инициализируем страницу через конструктор по умолчанию
        cryptoProPage = new CryptoProDemoPage();

        // ПРОВЕРКА URL ЧЕРЕЗ Selenide (вместо несуществующего getCurrentUrl())
        // Если текущий URL не содержит нужного пути, открываем страницу
        String currentUrl = com.codeborne.selenide.WebDriverRunner.url();
        if (currentUrl == null || !currentUrl.contains("cryptopro")) {
            cryptoProPage.openPageAndVerify("Демо-страница КриптоПРО");
        }
    }

    @Test(groups = {"web", "crypto", "smoke"}, testName = "#6 Проверка загрузки плагина КриптоПРО",
            retryAnalyzer = RetryAnalyzer.class)
    @AllureId("CRYPTO-006")
    @Story("Проверка загрузки плагина")
    @Severity(SeverityLevel.CRITICAL)
    @Retry(maxAttempts = 3, reason = "Нестабильность плагина")
    public void verifyCryptoProPluginLoaded() {
        arrangeActAssert(
                () -> logger.info("Подготовка проверки плагина"),
                () -> {
                    Allure.step("Открытие страницы", () -> cryptoProPage.openPageAndVerify("Демо-страница КриптоПРО"));
                    Allure.step("Проверка статусов", () -> cryptoProPage.verifyAllStatusesLoaded());
                },
                (assertions) -> {
                    assertions.assertTrue(cryptoProPage.verifyExtensionLoaded(), "Расширение должно быть загружено");
                    assertions.assertTrue(cryptoProPage.verifyPluginLoaded(), "Плагин должен быть загружен");
                    assertions.assertTrue(cryptoProPage.verifyCspLoaded(), "CSP должен быть загружен");
                    assertions.assertTrue(cryptoProPage.verifyObjectsLoaded(), "Объекты должны быть загружены");
                },
                "Проверка загрузки плагина КриптоПРО"
        );
    }

    @Test(groups = {"web", "crypto", "signature", "blocker"}, testName = "#2 Проверка создания электронной подписи")
    @AllureId("CRYPTO-002")
    @Story("Проверка создания подписи")
    @Severity(SeverityLevel.BLOCKER)
    public void testCertificateSelectionAndSigning() {
        Allure.step("Открытие страницы", () -> cryptoProPage.openPage("Демо-страница КриптоПРО"));

        Allure.step("Проверка готовности", () -> {
            if (!CryptoProPluginVerifier.verifyAllStatuses(20)) {
                throw new RuntimeException("Плагин не готов");
            }
        });

        Allure.step("Нажатие кнопки Подписать", () -> cryptoProPage.clickSignButton());
        Allure.step("Ожидание диалога", () -> cryptoProPage.waitForCertificateDialog());

        Allure.step("Проверка результата", () -> {
            boolean res = cryptoProPage.verifySignatureResult();
            if (!res) {
                cryptoProPage.takeScreenshot("signature_fail");
                assertions.fail("Подпись не создана");
            }
        });
    }

    @Test(groups = {"web", "crypto", "diagnostics"}, testName = "#4 Полная проверка диагностики")
    @AllureId("CRYPTO-004")
    @Story("Проверка элементов страницы")
    @Severity(SeverityLevel.NORMAL)
    public void testPageDiagnostics() {
        Allure.step("Открытие страницы", () -> cryptoProPage.openPage("Демо-страница КриптоПРО"));

        String[] selectors = {
                "#ExtensionEnabledImg", "#ExtensionEnabledTxt",
                "#PluginEnabledImg", "#PluginEnabledTxt",
                "#CspEnabledImg", "#CspEnabledTxt",
                "#ObjectsLoadedImg", "#ObjectsLoadedTxt"
        };

        boolean allFound = cryptoProPage.verifyStatusElements(selectors);
        assertions.assertTrue(allFound, "Все статусные элементы должны быть найдены");
    }

    @Test(groups = {"web", "crypto", "strategy"}, testName = "#7 Стратегия валидации")
    @AllureId("CRYPTO-007")
    @Story("Стратегия")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyCryptoProPluginWithStrategy() {
        TestStrategyManager manager = TestStrategyManager.createDefault();
        manager.registerExecutionStrategy(new CryptoProValidationStrategy(true, false));

        TestExecutionStrategy.TestContext ctx = manager.executeTestWithStrategy(
                "crypto_strategy", ExecutionStrategyType.UI_CRYPTO_PRO_VALIDATION, null
        );

        assertions.assertTrue((Boolean) ctx.getActualResult(), "Стратегия должна вернуть успех");
    }

    @Test(groups = {"web", "crypto", "quick"}, testName = "#1 Быстрая проверка")
    @AllureId("CRYPTO-001")
    @Story("Быстрая проверка")
    @Severity(SeverityLevel.MINOR)
    public void quickPluginCheck() {
        cryptoProPage.openPage("Демо-страница КриптоПРО");
        assertions.assertTrue(cryptoProPage.verifyExtensionLoaded(), "Расширение должно работать");
    }
}