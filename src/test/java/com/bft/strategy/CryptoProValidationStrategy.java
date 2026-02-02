package com.bft.strategy;

import com.bft.gui.CryptoProDemoPage;
import com.bft.utils.CryptoProPluginVerifier;
import org.testng.asserts.SoftAssert;

/**
 * Стратегия валидации CryptoPro плагина через UI
 */
public class CryptoProValidationStrategy extends BaseTestExecutionStrategy<Void> {

    private CryptoProDemoPage cryptoProPage;
    private final boolean checkAllStatuses;
    private final boolean performSignature;

    public CryptoProValidationStrategy() {
        this(true, false);
    }

    public CryptoProValidationStrategy(boolean checkAllStatuses, boolean performSignature) {
        super("CryptoPro UI Validation", 10);
        this.checkAllStatuses = checkAllStatuses;
        this.performSignature = performSignature;
    }

    @Override
    public ExecutionStrategyType getType() {
        return ExecutionStrategyType.UI_CRYPTO_PRO_VALIDATION;
    }

    @Override
    public boolean isApplicable(TestContext context) {
        // Применима для UI тестов с CryptoPro
        return context.getTestName().toLowerCase().contains("crypto") ||
               context.getTestName().toLowerCase().contains("plugin");
    }

    @Override
    protected void performPreparation(TestContext context) {
        // Инициализируем страницу если нужно
        if (cryptoProPage == null) {
            cryptoProPage = new CryptoProDemoPage(new org.testng.asserts.SoftAssert());
        }

        // Открываем страницу демо
        cryptoProPage.openPage("CryptoPro Demo Page");
        context.setTestData("demo_page_opened");
    }

    @Override
    protected void performExecution(TestContext context) {
        // Инициализируем страницу без SoftAssert для выполнения действий
        if (cryptoProPage == null) {
            cryptoProPage = new CryptoProDemoPage(new org.testng.asserts.SoftAssert());
        }

        boolean result = false;

        if (checkAllStatuses) {
            // Проверяем все статусы плагина
            result = CryptoProPluginVerifier.verifyAllStatuses(20);
            context.setActualResult(result);
        } else {
            // Быстрая проверка
            result = cryptoProPage.verifyExtensionLoaded();
            context.setActualResult(result);
        }

        if (performSignature && result) {
            // Выполняем подпись
            cryptoProPage.clickSignButton();
            cryptoProPage.waitForCertificateDialog();

            boolean signatureResult = cryptoProPage.verifySignatureResult();
            context.setActualResult(signatureResult);
        }
    }

    @Override
    protected void performValidation(TestContext context, SoftAssert softAssert) {
        // Инициализируем страницу с SoftAssert
        if (cryptoProPage == null) {
            cryptoProPage = new CryptoProDemoPage(softAssert);
        }

        Boolean actualResult = (Boolean) context.getActualResult();

        softAssert.assertNotNull(actualResult, "Результат проверки не должен быть null");
        softAssert.assertTrue(actualResult, "CryptoPro плагин должен быть успешно проверен");

        // Дополнительные проверки
        if (checkAllStatuses) {
            // Проверяем, что все статусы отображаются корректно
            softAssert.assertTrue(cryptoProPage.verifyCertificateSelectionAvailable(),
                "Выбор сертификатов должен быть доступен");
        }

        // Проверка производительности
        long duration = context.getDuration();
        softAssert.assertTrue(duration < 60000, "Проверка не должна занимать более 60 секунд, фактически: " + duration + "мс");
    }

    @Override
    protected void performCleanup(TestContext context) {
        // Сохраняем скриншот результата
        cryptoProPage.takeScreenshot("cryptopro_validation_result");
    }
}