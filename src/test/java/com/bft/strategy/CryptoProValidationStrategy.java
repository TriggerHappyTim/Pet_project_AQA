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
        try {
            // Инициализируем страницу если нужно
            if (cryptoProPage == null) {
                cryptoProPage = new CryptoProDemoPage(new org.testng.asserts.SoftAssert());
            }

            // Открываем страницу демо
            cryptoProPage.openPage("CryptoPro Demo Page");
            context.setTestData("demo_page_opened");
        } catch (Exception e) {
            System.err.println("Ошибка при подготовке CryptoProValidationStrategy: " + e.getMessage());
            throw new RuntimeException("Не удалось подготовить тест: " + e.getMessage(), e);
        }
    }

    @Override
    protected void performExecution(TestContext context) {
        try {
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
                if (cryptoProPage != null) {
                    result = cryptoProPage.verifyExtensionLoaded();
                    context.setActualResult(result);
                } else {
                    context.setActualResult(false);
                    throw new IllegalStateException("CryptoProDemoPage не инициализирован");
                }
            }

            if (performSignature && result) {
                // Выполняем подпись
                if (cryptoProPage != null) {
                    cryptoProPage.clickSignButton();
                    cryptoProPage.waitForCertificateDialog();

                    boolean signatureResult = cryptoProPage.verifySignatureResult();
                    context.setActualResult(signatureResult);
                } else {
                    throw new IllegalStateException("CryptoProDemoPage не инициализирован для подписи");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении CryptoProValidationStrategy: " + e.getMessage());
            context.setActualResult(false);
            throw new RuntimeException("Не удалось выполнить тест: " + e.getMessage(), e);
        }
    }

    @Override
    protected void performValidation(TestContext context, SoftAssert softAssert) {
        try {
            // Инициализируем страницу с SoftAssert
            if (cryptoProPage == null) {
                cryptoProPage = new CryptoProDemoPage(softAssert);
            }

            Object actualResultObj = context.getActualResult();
            Boolean actualResult = null;

            if (actualResultObj instanceof Boolean) {
                actualResult = (Boolean) actualResultObj;
            } else if (actualResultObj != null) {
                // Пытаемся преобразовать в boolean
                actualResult = Boolean.parseBoolean(actualResultObj.toString());
            }

            if (softAssert != null) {
                softAssert.assertNotNull(actualResult, "Результат проверки не должен быть null");
                softAssert.assertTrue(actualResult != null && actualResult, 
                    "CryptoPro плагин должен быть успешно проверен");

                // Дополнительные проверки
                if (checkAllStatuses && cryptoProPage != null) {
                    // Проверяем, что все статусы отображаются корректно
                    boolean certificateAvailable = cryptoProPage.verifyCertificateSelectionAvailable();
                    softAssert.assertTrue(certificateAvailable,
                        "Выбор сертификатов должен быть доступен");
                }

                // Проверка производительности
                long duration = context.getDuration();
                softAssert.assertTrue(duration < 60000, 
                    "Проверка не должна занимать более 60 секунд, фактически: " + duration + "мс");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при валидации CryptoProValidationStrategy: " + e.getMessage());
            if (softAssert != null) {
                softAssert.fail("Ошибка валидации: " + e.getMessage());
            }
            throw new RuntimeException("Не удалось валидировать результаты: " + e.getMessage(), e);
        }
    }

    @Override
    protected void performCleanup(TestContext context) {
        // Сохраняем скриншот результата, если страница была инициализирована
        if (cryptoProPage != null) {
            try {
                cryptoProPage.takeScreenshot("cryptopro_validation_result");
            } catch (Exception e) {
                // Логируем ошибки при cleanup, но не прерываем выполнение
                System.err.println("Ошибка при cleanup CryptoProValidationStrategy: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Логируем предупреждение, если страница не была инициализирована
            System.out.println("Предупреждение: cryptoProPage равен null при cleanup, скриншот не будет создан");
        }
    }
}