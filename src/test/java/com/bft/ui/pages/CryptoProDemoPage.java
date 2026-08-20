package com.bft.ui.pages;

import com.bft.ui.core.BasePage;
import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import com.bft.ui.core.wait.WaitStrategies;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bft.enums.TimeoutConstants.CRYPTO_PLUGIN_WAIT;
import static com.bft.enums.TimeoutConstants.SHORT_WAIT;
import static com.bft.enums.UrlConstants.CRYPTOPRO_DEMO_PAGE_URL;
import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Улучшенный Page Object для демо-страницы КриптоПРО.
 */
public class CryptoProDemoPage extends BasePage<CryptoProDemoPage> {

    private static final Logger logger = LoggerFactory.getLogger(CryptoProDemoPage.class);

    // --- Умные элементы диагностики расширения ---
    private final SmartElement extensionEnabledImg = ElementFactory.id("ExtensionEnabledImg")
            .named("Иконка статуса расширения")
            .waitVisible()
            .build();

    private final SmartElement extensionEnabledTxt = ElementFactory.id("ExtensionEnabledTxt")
            .named("Текст статуса расширения")
            .waitVisible()
            .build();

    // --- Умные элементы диагностики плагина ---
    private final SmartElement pluginEnabledImg = ElementFactory.id("PluginEnabledImg")
            .named("Иконка статуса плагина")
            .waitVisible()
            .build();

    private final SmartElement pluginEnabledTxt = ElementFactory.id("PluginEnabledTxt")
            .named("Текст статуса плагина")
            .waitVisible()
            .build();

    // --- Умные элементы диагностики криптопровайдера ---
    private final SmartElement cspEnabledImg = ElementFactory.id("CspEnabledImg")
            .named("Иконка статуса криптопровайдера")
            .waitVisible()
            .build();

    private final SmartElement cspEnabledTxt = ElementFactory.id("CspEnabledTxt")
            .named("Текст статуса криптопровайдера")
            .waitVisible()
            .build();

    // --- Умные элементы диагностики объектов плагина ---
    private final SmartElement objectsLoadedImg = ElementFactory.id("ObjectsLoadedImg")
            .named("Иконка статуса объектов плагина")
            .waitVisible()
            .build();

    private final SmartElement objectsLoadedTxt = ElementFactory.id("ObjectsLoadedTxt")
            .named("Текст статуса объектов плагина")
            .waitVisible()
            .build();

    // --- Умные элементы управления подписью ---
    private final SmartElement signButton = ElementFactory.xpath("//button[contains(text(), 'Подписать')]")
            .named("Кнопка 'Подписать'")
            .waitClickable()
            .build();

    private final SmartElement certificateSelection = ElementFactory.xpath("//*[contains(text(), 'Выберите сертификат')]")
            .named("Текст выбора сертификата")
            .waitVisible()
            .build();

    // --- Умные элементы диагностической информации ---
    private final SmartElement pageTitle = ElementFactory.xpath("//h1[contains(text(), 'Проверка создания электронной подписи')]")
            .named("Заголовок страницы")
            .waitVisible()
            .build();

    private final SmartElement platformInfo = ElementFactory.xpath("//*[contains(text(), 'Платформа:')]")
            .named("Информация о платформе")
            .waitVisible()
            .build();

    private final SmartElement userAgentInfo = ElementFactory.xpath("//*[contains(text(), 'UserAgent:')]")
            .named("Информация о UserAgent")
            .waitVisible()
            .build();

    @Override
    protected String getPageUrl() {
        return CRYPTOPRO_DEMO_PAGE_URL;
    }

    @Override
    protected String getPageTitle() {
        return "Проверка создания электронной подписи";
    }

    @Override
    protected boolean isPageLoaded() {
        try {
            pageTitle.shouldBeVisible();

            try {
                extensionEnabledImg.shouldBeVisible();
                return true;
            } catch (Exception e) {
                // расширение не найдено
            }

            try {
                signButton.shouldBeVisible();
                return true;
            } catch (Exception e) {
                // кнопка не найдена
            }

            return false;
        } catch (Exception e) {
            logger.debug("Страница еще не готова: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Step("Ожидаем полной загрузки демо-страницы КриптоПРО")
    public CryptoProDemoPage waitForPageLoad() {
        super.waitForPageLoad();

        waitForJsCondition("typeof cadesplugin !== 'undefined' || typeof window.CryptoPro !== 'undefined'",
                CRYPTO_PLUGIN_WAIT, "КриптоПРО плагин должен загрузиться");

        logger.info("Демо-страница КриптоПРО полностью загружена");
        return this;
    }

    @Step("Проверяем, что расширение КриптоПРО загружено")
    public boolean verifyExtensionLoaded() {
        logger.info("Проверяем статус расширения КриптоПРО");

        try {
            extensionEnabledImg.shouldBeVisible();
            extensionEnabledImg.shouldHaveClass("green");
            extensionEnabledTxt.shouldBeVisible();
            extensionEnabledTxt.shouldContainText("Расширение загружено");

            logger.info("✓ Расширение КриптоПРО успешно загружено");
            return true;
        } catch (Exception e) {
            logger.error("✗ Проверка расширения не пройдена: {}", e.getMessage());
            return false;
        }
    }

    @Step("Проверяем, что плагин КриптоПРО загружен")
    public boolean verifyPluginLoaded() {
        logger.info("Проверяем статус плагина КриптоПРО");

        try {
            pluginEnabledImg.shouldBeVisible();
            pluginEnabledImg.shouldHaveClass("green");
            pluginEnabledTxt.shouldBeVisible();

            String text = pluginEnabledTxt.getText();
            if (text.contains("ожидание")) {
                throw new AssertionError("Плагин находится в состоянии ожидания");
            }

            logger.info("✓ Плагин КриптоПРО успешно загружен");
            return true;
        } catch (Exception e) {
            logger.error("✗ Проверка плагина не пройдена: {}", e.getMessage());
            return false;
        }
    }

    @Step("Проверяем, что криптопровайдер КриптоПРО загружен")
    public boolean verifyCspLoaded() {
        logger.info("Проверяем статус криптопровайдера КриптоПРО");

        try {
            cspEnabledImg.shouldBeVisible();
            cspEnabledImg.shouldHaveClass("green");
            cspEnabledTxt.shouldBeVisible();

            String text = cspEnabledTxt.getText();
            if (text.contains("ожидание")) {
                throw new AssertionError("Криптопровайдер находится в состоянии ожидания");
            }

            logger.info("✓ Криптопровайдер КриптоПРО успешно загружен");
            return true;
        } catch (Exception e) {
            logger.error("✗ Проверка криптопровайдера не пройдена: {}", e.getMessage());
            return false;
        }
    }

    @Step("Проверяем, что объекты плагина КриптоПРО загружены")
    public boolean verifyObjectsLoaded() {
        logger.info("Проверяем статус объектов плагина КриптоПРО");

        try {
            objectsLoadedImg.shouldBeVisible();
            objectsLoadedImg.shouldHaveClass("green");
            objectsLoadedTxt.shouldBeVisible();

            String text = objectsLoadedTxt.getText();
            if (text.contains("ожидание")) {
                throw new AssertionError("Объекты плагина находятся в состоянии ожидания");
            }

            logger.info("✓ Объекты плагина КриптоПРО успешно загружены");
            return true;
        } catch (Exception e) {
            logger.error("✗ Проверка объектов плагина не пройдена: {}", e.getMessage());
            return false;
        }
    }

    @Step("Проверяем все статусы КриптоПРО")
    public boolean verifyAllStatusesLoaded() {
        boolean result = verifyExtensionLoaded() && verifyPluginLoaded() &&
                verifyCspLoaded() && verifyObjectsLoaded();

        if (!result) {
            logger.error("Не все статусы КриптоПРО загружены успешно");
        }
        return result;
    }

    @Step("Проверяем доступность выбора сертификатов")
    public boolean verifyCertificateSelectionAvailable() {
        logger.info("Проверяем доступность выбора сертификатов");
        try {
            certificateSelection.shouldBeVisible();
            logger.info("✓ Выбор сертификатов доступен");
            return true;
        } catch (Exception e) {
            logger.warn("Выбор сертификатов не найден: {}", e.getMessage());
            return false;
        }
    }

    @Step("Нажимаем кнопку 'Подписать'")
    public CryptoProDemoPage clickSignButton() {
        logger.info("Нажимаем кнопку 'Подписать'");
        signButton.click();
        logger.info("✓ Кнопка 'Подписать' нажата");
        return this;
    }

    @Step("Ожидаем диалог выбора сертификата")
    public CryptoProDemoPage waitForCertificateDialog() {
        logger.info("Ожидаем появления диалога выбора сертификата");

        waitWithStrategy(WaitStrategies.jsCondition(
                "document.querySelectorAll('[role=\"dialog\"], .modal, .popup').length > 0"
        ), "диалог выбора сертификата");

        waitForJsCondition("!document.querySelector('.loading, .spinner, [aria-busy=\"true\"]')",
                SHORT_WAIT, "исчезновение индикаторов загрузки");

        try {
            Selenide.confirm();
            logger.info("✓ Системный диалог выбора сертификата принят");
        } catch (Exception e) {
            logger.debug("Системный диалог не появился или уже обработан");
        }

        return this;
    }

    @Step("Проверяем результат подписи")
    public boolean verifySignatureResult() {
        try {
            $(By.xpath("//div[contains(@class, 'spinner') or contains(@class, 'loading')]"))
                    .should(disappear, SHORT_WAIT);
        } catch (Exception e) {
            // Спиннеры не найдены или уже исчезли
        }

        if ($(By.xpath("//*[contains(text(), 'подпись') and contains(text(), 'создан')]")).exists()) {
            return true;
        }

        SelenideElement codeField = $("textarea, pre, code");
        if (codeField.exists() && !codeField.getText().isEmpty()) {
            return true;
        }

        if ($(By.xpath("//button[contains(text(), 'Подписано') or contains(text(), 'Готово')]")).exists()) {
            return true;
        }

        return false;
    }

    @Step("Проверяем заголовок страницы")
    public boolean verifyPageTitle() {
        try {
            pageTitle.shouldBeVisible();
            logger.info("✓ Заголовок страницы корректный");
            return true;
        } catch (Exception e) {
            logger.error("Заголовок страницы не найден");
            return false;
        }
    }

    @Step("Проверяем диагностическую информацию")
    public boolean verifyDiagnosticInfo() {
        try {
            platformInfo.shouldBeVisible();
            userAgentInfo.shouldBeVisible();
            logger.info("✓ Диагностическая информация отображается");
            return true;
        } catch (Exception e) {
            logger.error("Диагностическая информация не найдена");
            return false;
        }
    }

    @Step("Проверяем все статусные элементы")
    public boolean verifyStatusElements(String[] selectors) {
        logger.info("Проверяем {} статусных элементов", selectors.length);
        boolean allFound = true;

        for (String selector : selectors) {
            try {
                SmartElement element = ElementFactory.css(selector)
                        .named("Статусный элемент: " + selector)
                        .waitVisible(SHORT_WAIT)
                        .build();
                element.shouldBeVisible();
                logger.debug("✓ Найден элемент: {}", selector);
            } catch (Exception e) {
                logger.error("✗ Элемент не найден: {}", selector);
                allFound = false;
            }
        }

        return allFound;
    }

    /**
     * Проверяет наличие статусов ожидания (негативный сценарий)
     * Исправлено: убран stream(), используется фильтр Selenide
     */
    public boolean hasWaitingStatuses() {
        ElementsCollection elements = $$("*").filterBy(text("ожидание"));
        boolean hasWaiting = !elements.isEmpty();
        logger.debug("Статусы ожидания: {}", hasWaiting ? "присутствуют" : "отсутствуют");
        return hasWaiting;
    }

    /**
     * Проверяет наличие зеленой точки у расширения
     */
    public boolean hasGreenExtensionDot() {
        try {
            String classes = extensionEnabledImg.getAttribute("class");
            boolean hasGreen = classes != null && classes.contains("green");
            logger.debug("Зеленая точка расширения: {}", hasGreen ? "присутствует" : "отсутствует");
            return hasGreen;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке зеленой точки: {}", e.getMessage());
            return false;
        }
    }

    @Step("Выполняем комплексную диагностику плагина КриптоПРО")
    public boolean performFullDiagnostic() {
        return verifyPageTitle() && verifyDiagnosticInfo() && verifyAllStatusesLoaded() && verifyCertificateSelectionAvailable();
    }

    @Step("Выполняем полный сценарий электронной подписи")
    public boolean performFullSigningScenario() {
        try {
            if (!isPluginReady()) {
                logger.error("Плагин не готов к работе");
                return false;
            }
            clickSignButton();
            waitForCertificateDialog();
            return waitForSignatureResult();
        } catch (Exception e) {
            logger.error("Ошибка при выполнении сценария подписи: {}", e.getMessage());
            takeScreenshot("signing_error_" + System.currentTimeMillis());
            return false;
        }
    }

    private boolean isPluginReady() {
        return !hasWaitingStatuses() && hasGreenExtensionDot();
    }

    private boolean waitForSignatureResult() {
        logger.info("Ожидаем результат подписи");
        waitWithStrategy(WaitStrategies.jsCondition(
                "document.querySelector('textarea, pre, code') !== null || " +
                        "document.querySelector('button:contains(\"Подписано\"), button:contains(\"Готово\")') !== null"
        ), "результат подписи");
        return verifySignatureResult();
    }

    public CryptoProStatusInfo getPluginStatusInfo() {
        return new CryptoProStatusInfo(
                extensionEnabledTxt.getText(),
                pluginEnabledTxt.getText(),
                cspEnabledTxt.getText(),
                objectsLoadedTxt.getText(),
                hasGreenExtensionDot(),
                hasWaitingStatuses()
        );
    }

    public static class CryptoProStatusInfo {
        public final String extensionStatus;
        public final String pluginStatus;
        public final String cspStatus;
        public final String objectsStatus;
        public final boolean hasGreenDot;
        public final boolean hasWaitingStatuses;

        public CryptoProStatusInfo(String extensionStatus, String pluginStatus,
                                   String cspStatus, String objectsStatus,
                                   boolean hasGreenDot, boolean hasWaitingStatuses) {
            this.extensionStatus = extensionStatus;
            this.pluginStatus = pluginStatus;
            this.cspStatus = cspStatus;
            this.objectsStatus = objectsStatus;
            this.hasGreenDot = hasGreenDot;
            this.hasWaitingStatuses = hasWaitingStatuses;
        }

        public boolean isFullyReady() {
            return hasGreenDot && !hasWaitingStatuses &&
                    extensionStatus.contains("загружено") &&
                    !pluginStatus.contains("ожидание") &&
                    !cspStatus.contains("ожидание") &&
                    !objectsStatus.contains("ожидание");
        }

        @Override
        public String toString() {
            return String.format(
                    "CryptoProStatus{ext='%s', plugin='%s', csp='%s', obj='%s', green=%s, wait=%s}",
                    extensionStatus, pluginStatus, cspStatus, objectsStatus, hasGreenDot, hasWaitingStatuses
            );
        }
    }
}