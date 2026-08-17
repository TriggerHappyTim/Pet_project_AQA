package com.bft.strategy;

import com.bft.gui.CryptoProDemoPage;
import com.bft.utils.CryptoProPluginVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

/**
 * Стратегия валидации CryptoPro плагина через UI.
 * Инкапсулирует логику проверки статусов плагина и выполнения электронной подписи.
 */
public class CryptoProValidationStrategy extends BaseTestExecutionStrategy<Void> {

    private static final Logger log = LoggerFactory.getLogger(CryptoProValidationStrategy.class);

    private CryptoProDemoPage cryptoProPage;
    private final boolean checkAllStatuses;
    private final boolean performSignature;

    /**
     * Конструктор по умолчанию: проверяет все статусы, подпись не выполняет.
     */
    public CryptoProValidationStrategy() {
        this(true, false);
    }

    /**
     * Конструктор с параметрами.
     *
     * @param checkAllStatuses  проверять все статусы (расширение, плагин, CSP, объекты)
     * @param performSignature  выполнять ли сценарий подписи после проверки
     */
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
        // Применима для тестов, содержащих "crypto" или "plugin" в имени
        String testName = context.getTestName() != null ? context.getTestName().toLowerCase() : "";
        return testName.contains("crypto") || testName.contains("plugin");
    }

    @Override
    protected void performPreparation(TestContext context) {
        try {
            // Инициализируем страницу только если она еще не создана
            if (cryptoProPage == null) {
                cryptoProPage = new CryptoProDemoPage();
            }

            // Открываем страницу демо
            cryptoProPage.openPage("CryptoPro Demo Page");

            // ИСПРАВЛЕНИЕ: Убран неверный вызов setTestData(String, boolean).
            // Если нужно сохранить состояние, используйте контекст согласно его API.
            // Обычно достаточно того, что страница открыта. Если критично, можно использовать:
            // context.setTestData(new PageState("opened")); // если метод принимает один объект
            log.debug("CryptoProValidationStrategy: подготовка завершена, страница открыта");

        } catch (Exception e) {
            log.error("Ошибка при подготовке CryptoProValidationStrategy: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось подготовить тест: " + e.getMessage(), e);
        }
    }

    @Override
    protected void performExecution(TestContext context) {
        try {
            // Гарантируем инициализацию страницы
            if (cryptoProPage == null) {
                cryptoProPage = new CryptoProDemoPage();
            }

            boolean result = false;

            // 1. Проверка статусов
            if (checkAllStatuses) {
                log.info("Выполняется полная проверка статусов плагина...");
                result = CryptoProPluginVerifier.verifyAllStatuses(20);
                context.setActualResult(result);
            } else {
                log.info("Выполняется быстрая проверка расширения...");
                result = cryptoProPage.verifyExtensionLoaded();
                context.setActualResult(result);
            }

            // 2. Выполнение подписи (если требуется и предыдущие проверки успешны)
            if (performSignature && result) {
                log.info("Выполняется сценарий электронной подписи...");
                try {
                    cryptoProPage.clickSignButton();
                    cryptoProPage.waitForCertificateDialog();

                    boolean signatureResult = cryptoProPage.verifySignatureResult();
                    context.setActualResult(signatureResult);
                    result = signatureResult; // Обновляем итоговый результат
                } catch (Exception signEx) {
                    log.error("Ошибка при выполнении подписи: {}", signEx.getMessage());
                    context.setActualResult(false);
                    result = false;
                }
            }

            if (!result) {
                log.warn("Проверка CryptoPro завершилась с неудачным результатом");
            }

        } catch (Exception e) {
            log.error("Ошибка при выполнении CryptoProValidationStrategy: {}", e.getMessage(), e);
            context.setActualResult(false);
            throw new RuntimeException("Не удалось выполнить тест: " + e.getMessage(), e);
        }
    }

    @Override
    protected void performValidation(TestContext context, SoftAssert softAssert) {
        try {
            // Инициализируем страницу если нужно (на случай если execution не запускался)
            if (cryptoProPage == null) {
                cryptoProPage = new CryptoProDemoPage();
            }

            Object actualResultObj = context.getActualResult();
            Boolean actualResult = null;

            if (actualResultObj instanceof Boolean) {
                actualResult = (Boolean) actualResultObj;
            } else if (actualResultObj != null) {
                actualResult = Boolean.parseBoolean(actualResultObj.toString());
            }

            if (softAssert != null) {
                softAssert.assertNotNull(actualResult, "Результат проверки не должен быть null");

                if (actualResult != null) {
                    softAssert.assertTrue(actualResult,
                            "CryptoPro плагин должен быть успешно проверен (результат: false)");
                } else {
                    softAssert.fail("Результат проверки имеет некорректное значение");
                }

                // Дополнительные проверки доступности сертификатов
                if (checkAllStatuses && actualResult != null && actualResult) {
                    boolean certificateAvailable = cryptoProPage.verifyCertificateSelectionAvailable();
                    softAssert.assertTrue(certificateAvailable,
                            "Выбор сертификатов должен быть доступен при успешной проверке");
                }

                // Проверка производительности
                long duration = context.getDuration();
                softAssert.assertTrue(duration < 60000,
                        "Проверка не должна занимать более 60 секунд (фактически: " + duration + "мс)");
            }
        } catch (Exception e) {
            log.error("Ошибка при валидации CryptoProValidationStrategy: {}", e.getMessage(), e);
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
                log.debug("Скриншот результата сохранен");
            } catch (Exception e) {
                log.warn("Ошибка при создании скриншота в cleanup: {}", e.getMessage());
            }
        } else {
            log.debug("Страница не инициализирована, скриншот не создается");
        }

        // Сбрасываем ссылку для следующего теста (если стратегия переиспользуется)
        cryptoProPage = null;
    }
}