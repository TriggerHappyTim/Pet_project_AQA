package com.bft.gui;

import com.bft.ui.core.BasePage;
import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import com.bft.ui.core.wait.WaitStrategies;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.asserts.SoftAssert;

import static com.bft.constants.TimeoutConstants.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;

/**
 * Улучшенный Page Object для демо-страницы КриптоПРО
 * Использует новый слой абстракции с умными элементами и стратегиями ожидания
 */
public class CryptoProDemoPage extends BasePage<CryptoProDemoPage> {

    private final SoftAssert softAssert;

    public CryptoProDemoPage(SoftAssert softAssert) {
        this.softAssert = softAssert;
    }

    // Умные элементы диагностики расширения
    private final SmartElement extensionEnabledImg = ElementFactory.id("ExtensionEnabledImg")
            .named("Иконка статуса расширения")
            .waitVisible()
            .build();

    private final SmartElement extensionEnabledTxt = ElementFactory.id("ExtensionEnabledTxt")
            .named("Текст статуса расширения")
            .waitVisible()
            .build();

    // Умные элементы диагностики плагина
    private final SmartElement pluginEnabledImg = ElementFactory.id("PluginEnabledImg")
            .named("Иконка статуса плагина")
            .waitVisible()
            .build();

    private final SmartElement pluginEnabledTxt = ElementFactory.id("PluginEnabledTxt")
            .named("Текст статуса плагина")
            .waitVisible()
            .build();

    // Умные элементы диагностики криптопровайдера
    private final SmartElement cspEnabledImg = ElementFactory.id("CspEnabledImg")
            .named("Иконка статуса криптопровайдера")
            .waitVisible()
            .build();

    private final SmartElement cspEnabledTxt = ElementFactory.id("CspEnabledTxt")
            .named("Текст статуса криптопровайдера")
            .waitVisible()
            .build();

    // Умные элементы диагностики объектов плагина
    private final SmartElement objectsLoadedImg = ElementFactory.id("ObjectsLoadedImg")
            .named("Иконка статуса объектов плагина")
            .waitVisible()
            .build();

    private final SmartElement objectsLoadedTxt = ElementFactory.id("ObjectsLoadedTxt")
            .named("Текст статуса объектов плагина")
            .waitVisible()
            .build();

    // Умные элементы управления подписью
    private final SmartElement signButton = ElementFactory.xpath("//button[contains(text(), 'Подписать')]")
            .named("Кнопка 'Подписать'")
            .waitClickable()
            .build();

    private final SmartElement certificateSelection = ElementFactory.xpath("//*[contains(text(), 'Выберите сертификат')]")
            .named("Текст выбора сертификата")
            .waitVisible()
            .build();

    // Умные элементы диагностической информации
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
        return com.bft.constants.UrlConstants.CRYPTOPRO_DEMO_PAGE_URL;
    }

    @Override
    protected String getPageTitle() {
        return "Проверка создания электронной подписи";
    }

    @Override
    protected boolean isPageLoaded() {
        try {
            return pageTitle.isVisible() &&
                   extensionEnabledImg.isPresent() &&
                   signButton.isPresent();
        } catch (Exception e) {
            logger.warn("Ошибка при проверке загрузки страницы: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Ожидает загрузки страницы с расширенной логикой
     */
    @Override
    @Step("Ожидаем полной загрузки демо-страницы КриптоПРО")
    public CryptoProDemoPage waitForPageLoad() {
        // Вызываем базовую логику загрузки страницы
        super.waitForPageLoad();

        // Дополнительное ожидание для динамического контента КриптоПРО
        waitForJsCondition("typeof cadesplugin !== 'undefined' || typeof window.CryptoPro !== 'undefined'",
                          CRYPTO_PLUGIN_WAIT, "КриптоПРО плагин должен загрузиться");

        logger.info("Демо-страница КриптоПРО полностью загружена");
        return this;
    }

    /**
     * Проверяет, что расширение загружено (зеленая точка и текст)
     */
    @Step("Проверяем, что расширение КриптоПРО загружено")
    public boolean verifyExtensionLoaded() {
        logger.info("Проверяем статус расширения КриптоПРО");

        boolean extensionVisible = false;
        boolean extensionHasGreenClass = false;
        boolean extensionTextVisible = false;
        boolean extensionTextContains = false;

        try {
            extensionEnabledImg.shouldBeVisible();
            extensionVisible = true;
            softAssert.assertTrue(extensionVisible, "Иконка статуса расширения должна быть видимой");
        } catch (Exception e) {
            softAssert.assertTrue(extensionVisible, "Иконка статуса расширения должна быть видимой: " + e.getMessage());
        }

        try {
            extensionEnabledImg.shouldHaveClass("green");
            extensionHasGreenClass = true;
            softAssert.assertTrue(extensionHasGreenClass, "Иконка расширения должна иметь зеленый класс");
        } catch (Exception e) {
            softAssert.assertTrue(extensionHasGreenClass, "Иконка расширения должна иметь зеленый класс: " + e.getMessage());
        }

        try {
            extensionEnabledTxt.shouldBeVisible();
            extensionTextVisible = true;
            softAssert.assertTrue(extensionTextVisible, "Текст статуса расширения должен быть видимым");
        } catch (Exception e) {
            softAssert.assertTrue(extensionTextVisible, "Текст статуса расширения должен быть видимым: " + e.getMessage());
        }

        try {
            extensionEnabledTxt.shouldContainText("Расширение загружено");
            extensionTextContains = true;
            softAssert.assertTrue(extensionTextContains, "Текст статуса расширения должен содержать 'Расширение загружено'");
        } catch (Exception e) {
            softAssert.assertTrue(extensionTextContains, "Текст статуса расширения должен содержать 'Расширение загружено': " + e.getMessage());
        }

        boolean result = extensionVisible && extensionHasGreenClass && extensionTextVisible && extensionTextContains;
        if (result) {
            logger.info("✓ Расширение КриптоПРО успешно загружено");
        }
        return result;
    }

    /**
     * Проверяет, что плагин загружен (зеленая точка и статус)
     */
    @Step("Проверяем, что плагин КриптоПРО загружен")
    public boolean verifyPluginLoaded() {
        logger.info("Проверяем статус плагина КриптоПРО");

        boolean pluginImgVisible = false;
        boolean pluginImgHasGreenClass = false;
        boolean pluginTxtVisible = false;
        boolean noWaitingStatus = true;

        try {
            pluginEnabledImg.shouldBeVisible();
            pluginImgVisible = true;
            softAssert.assertTrue(pluginImgVisible, "Иконка статуса плагина должна быть видимой");
        } catch (Exception e) {
            softAssert.assertTrue(pluginImgVisible, "Иконка статуса плагина должна быть видимой: " + e.getMessage());
        }

        try {
            pluginEnabledImg.shouldHaveClass("green");
            pluginImgHasGreenClass = true;
            softAssert.assertTrue(pluginImgHasGreenClass, "Иконка плагина должна иметь зеленый класс");
        } catch (Exception e) {
            softAssert.assertTrue(pluginImgHasGreenClass, "Иконка плагина должна иметь зеленый класс: " + e.getMessage());
        }

        try {
            pluginEnabledTxt.shouldBeVisible();
            pluginTxtVisible = true;
            softAssert.assertTrue(pluginTxtVisible, "Текст статуса плагина должен быть видимым");
        } catch (Exception e) {
            softAssert.assertTrue(pluginTxtVisible, "Текст статуса плагина должен быть видимым: " + e.getMessage());
        }

        // Проверяем, что нет статуса ожидания
        try {
            String pluginText = pluginEnabledTxt.getText();
            if (pluginText.contains("ожидание")) {
                noWaitingStatus = false;
            }
            softAssert.assertTrue(noWaitingStatus, "Плагин КриптоПРО не должен находиться в состоянии ожидания");
        } catch (Exception e) {
            softAssert.assertTrue(noWaitingStatus, "Ошибка при проверке статуса плагина: " + e.getMessage());
        }

        boolean result = pluginImgVisible && pluginImgHasGreenClass && pluginTxtVisible && noWaitingStatus;
        if (result) {
            logger.info("✓ Плагин КриптоПРО успешно загружен");
        }
        return result;
    }

    /**
     * Проверяет, что криптопровайдер загружен (зеленая точка и статус)
     */
    @Step("Проверяем, что криптопровайдер КриптоПРО загружен")
    public boolean verifyCspLoaded() {
        logger.info("Проверяем статус криптопровайдера КриптоПРО");

        boolean cspImgVisible = false;
        boolean cspImgHasGreenClass = false;
        boolean cspTxtVisible = false;
        boolean noWaitingStatus = true;

        try {
            cspEnabledImg.shouldBeVisible();
            cspImgVisible = true;
            softAssert.assertTrue(cspImgVisible, "Иконка статуса криптопровайдера должна быть видимой");
        } catch (Exception e) {
            softAssert.assertTrue(cspImgVisible, "Иконка статуса криптопровайдера должна быть видимой: " + e.getMessage());
        }

        try {
            cspEnabledImg.shouldHaveClass("green");
            cspImgHasGreenClass = true;
            softAssert.assertTrue(cspImgHasGreenClass, "Иконка криптопровайдера должна иметь зеленый класс");
        } catch (Exception e) {
            softAssert.assertTrue(cspImgHasGreenClass, "Иконка криптопровайдера должна иметь зеленый класс: " + e.getMessage());
        }

        try {
            cspEnabledTxt.shouldBeVisible();
            cspTxtVisible = true;
            softAssert.assertTrue(cspTxtVisible, "Текст статуса криптопровайдера должен быть видимым");
        } catch (Exception e) {
            softAssert.assertTrue(cspTxtVisible, "Текст статуса криптопровайдера должен быть видимым: " + e.getMessage());
        }

        // Проверяем, что нет статуса ожидания
        try {
            String cspText = cspEnabledTxt.getText();
            if (cspText.contains("ожидание")) {
                noWaitingStatus = false;
            }
            softAssert.assertTrue(noWaitingStatus, "Криптопровайдер не должен находиться в состоянии ожидания");
        } catch (Exception e) {
            softAssert.assertTrue(noWaitingStatus, "Ошибка при проверке статуса криптопровайдера: " + e.getMessage());
        }

        boolean result = cspImgVisible && cspImgHasGreenClass && cspTxtVisible && noWaitingStatus;
        if (result) {
            logger.info("✓ Криптопровайдер КриптоПРО успешно загружен");
        }
        return result;
    }

    /**
     * Проверяет, что объекты плагина загружены (зеленая точка и статус)
     */
    @Step("Проверяем, что объекты плагина КриптоПРО загружены")
    public boolean verifyObjectsLoaded() {
        logger.info("Проверяем статус объектов плагина КриптоПРО");

        boolean objectsImgVisible = false;
        boolean objectsImgHasGreenClass = false;
        boolean objectsTxtVisible = false;
        boolean noWaitingStatus = true;

        try {
            objectsLoadedImg.shouldBeVisible();
            objectsImgVisible = true;
            softAssert.assertTrue(objectsImgVisible, "Иконка статуса объектов плагина должна быть видимой");
        } catch (Exception e) {
            softAssert.assertTrue(objectsImgVisible, "Иконка статуса объектов плагина должна быть видимой: " + e.getMessage());
        }

        try {
            objectsLoadedImg.shouldHaveClass("green");
            objectsImgHasGreenClass = true;
            softAssert.assertTrue(objectsImgHasGreenClass, "Иконка объектов плагина должна иметь зеленый класс");
        } catch (Exception e) {
            softAssert.assertTrue(objectsImgHasGreenClass, "Иконка объектов плагина должна иметь зеленый класс: " + e.getMessage());
        }

        try {
            objectsLoadedTxt.shouldBeVisible();
            objectsTxtVisible = true;
            softAssert.assertTrue(objectsTxtVisible, "Текст статуса объектов плагина должен быть видимым");
        } catch (Exception e) {
            softAssert.assertTrue(objectsTxtVisible, "Текст статуса объектов плагина должен быть видимым: " + e.getMessage());
        }

        // Проверяем, что нет статуса ожидания
        try {
            String objectsText = objectsLoadedTxt.getText();
            if (objectsText.contains("ожидание")) {
                noWaitingStatus = false;
            }
            softAssert.assertTrue(noWaitingStatus, "Объекты плагина не должны находиться в состоянии ожидания");
        } catch (Exception e) {
            softAssert.assertTrue(noWaitingStatus, "Ошибка при проверке статуса объектов плагина: " + e.getMessage());
        }

        boolean result = objectsImgVisible && objectsImgHasGreenClass && objectsTxtVisible && noWaitingStatus;
        if (result) {
            logger.info("✓ Объекты плагина КриптоПРО успешно загружены");
        }
        return result;
    }

    /**
     * Проверяет все диагностические элементы
     */
    public boolean verifyAllStatusesLoaded() {
        boolean extensionLoaded = verifyExtensionLoaded();
        boolean pluginLoaded = verifyPluginLoaded();
        boolean cspLoaded = verifyCspLoaded();
        boolean objectsLoaded = verifyObjectsLoaded();

        boolean result = extensionLoaded && pluginLoaded && cspLoaded && objectsLoaded;
        softAssert.assertTrue(result, "Все статусы КриптоПРО должны быть успешно загружены");
        return result;
    }

    /**
     * Проверяет наличие выбора сертификатов
     */
    @Step("Проверяем доступность выбора сертификатов")
    public boolean verifyCertificateSelectionAvailable() {
        logger.info("Проверяем доступность выбора сертификатов");
        boolean selectionVisible = false;

        try {
            certificateSelection.shouldBeVisible();
            selectionVisible = true;
            softAssert.assertTrue(selectionVisible, "Выбор сертификатов должен быть видимым");
            logger.info("✓ Выбор сертификатов доступен");
        } catch (Exception e) {
            softAssert.assertTrue(selectionVisible, "Выбор сертификатов должен быть видимым: " + e.getMessage());
        }

        return selectionVisible;
    }

    /**
     * Нажимает кнопку "Подписать"
     */
    @Step("Нажимаем кнопку 'Подписать'")
    public CryptoProDemoPage clickSignButton() {
        logger.info("Нажимаем кнопку 'Подписать'");
        signButton.click();
        logger.info("✓ Кнопка 'Подписать' нажата");
        return this;
    }

    /**
     * Ожидает диалог выбора сертификата с улучшенной логикой
     */
    @Step("Ожидаем диалог выбора сертификата")
    public CryptoProDemoPage waitForCertificateDialog() {
        logger.info("Ожидаем появления диалога выбора сертификата");

        // Используем стратегию ожидания с JavaScript проверкой
        waitWithStrategy(WaitStrategies.jsCondition(
            "document.querySelectorAll('[role=\"dialog\"], .modal, .popup').length > 0"
        ), "диалог выбора сертификата");

        // Умное ожидание стабилизации UI
        waitForJsCondition("!document.querySelector('.loading, .spinner, [aria-busy=\"true\"]')",
                          SHORT_WAIT, "исчезновение индикаторов загрузки");

        // Пробуем обработать системный диалог
        try {
            Selenide.confirm();
            logger.info("✓ Системный диалог выбора сертификата принят");
        } catch (Exception e) {
            logger.debug("Системный диалог не появился или уже обработан");
        }

        return this;
    }

    /**
     * Проверяет результат подписи
     * 
     * Использует умное ожидание появления признаков успешной подписи вместо жесткой задержки.
     * Проверяет несколько вариантов индикации успешной подписи.
     */
    public boolean verifySignatureResult() {
        // Ожидаем появления одного из признаков успешной подписи
        // Используем умное ожидание вместо sleep(5000)
        try {
            // Ожидаем исчезновения спиннеров загрузки
            $(By.xpath("//div[contains(@class, 'spinner') or contains(@class, 'loading')]"))
                    .should(disappear, SHORT_WAIT);
        } catch (Exception e) {
            // Если спиннеры не найдены, продолжаем проверку
        }

        // Вариант 1: Проверяем текст об успешной подписи
        try {
            $(By.xpath("//*[contains(text(), 'подпись') and contains(text(), 'создан')]"))
                    .shouldBe(visible, DEFAULT_WAIT);
            return true;
        } catch (Exception e) {
            // Продолжаем проверять другие варианты
        }

        // Вариант 2: Проверяем поле с подписью
        try {
            $("textarea, pre, code").shouldNotBe(empty, DEFAULT_WAIT);
            return true;
        } catch (Exception e) {
            // Продолжаем
        }

        // Вариант 3: Проверяем изменение кнопки
        try {
            $(By.xpath("//button[contains(text(), 'Подписано') or contains(text(), 'Готово')]"))
                    .shouldBe(visible, DEFAULT_WAIT);
            return true;
        } catch (Exception e) {
            // Продолжаем
        }

        return false;
    }

    /**
     * Проверяет заголовок страницы
     */
    @Step("Проверяем заголовок страницы")
    public boolean verifyPageTitle() {
        logger.info("Проверяем заголовок страницы");
        boolean titleVisible = false;

        try {
            pageTitle.shouldBeVisible();
            titleVisible = true;
            softAssert.assertTrue(titleVisible, "Заголовок страницы должен быть видимым");
            logger.info("✓ Заголовок страницы корректный");
        } catch (Exception e) {
            softAssert.assertTrue(titleVisible, "Заголовок страницы должен быть видимым: " + e.getMessage());
        }

        return titleVisible;
    }

    /**
     * Проверяет диагностическую информацию
     */
    @Step("Проверяем диагностическую информацию")
    public boolean verifyDiagnosticInfo() {
        logger.info("Проверяем диагностическую информацию");

        boolean platformVisible = false;
        boolean userAgentVisible = false;

        try {
            platformInfo.shouldBeVisible();
            platformVisible = true;
            softAssert.assertTrue(platformVisible, "Информация о платформе должна быть видимой");
        } catch (Exception e) {
            softAssert.assertTrue(platformVisible, "Информация о платформе должна быть видимой: " + e.getMessage());
        }

        try {
            userAgentInfo.shouldBeVisible();
            userAgentVisible = true;
            softAssert.assertTrue(userAgentVisible, "Информация о UserAgent должна быть видимой");
        } catch (Exception e) {
            softAssert.assertTrue(userAgentVisible, "Информация о UserAgent должна быть видимой: " + e.getMessage());
        }

        boolean result = platformVisible && userAgentVisible;
        if (result) {
            logger.info("✓ Диагностическая информация отображается");
        }
        return result;
    }

    /**
     * Проверяет все статусные элементы с использованием фабрики
     */
    @Step("Проверяем все статусные элементы")
    public boolean verifyStatusElements(String[] selectors) {
        logger.info("Проверяем {} статусных элементов", selectors.length);

        int foundCount = 0;
        boolean allElementsFound = true;

        for (String selector : selectors) {
            boolean elementVisible = false;
            try {
                SmartElement element = ElementFactory.css(selector)
                    .named("Статусный элемент: " + selector)
                    .waitVisible(SHORT_WAIT)
                    .build();

                element.shouldBeVisible();
                elementVisible = true;
                foundCount++;
                logger.debug("✓ Найден элемент: {}", selector);

            } catch (Exception e) {
                logger.error("✗ Элемент не найден: {}", selector);
                softAssert.assertTrue(elementVisible, "Элемент должен быть найден: " + selector);
                allElementsFound = false;
            }
        }

        softAssert.assertTrue(allElementsFound, "Все статусные элементы должны быть найдены");
        logger.info("✓ Проверено {} статусных элементов", foundCount);
        return allElementsFound;
    }

    /**
     * Проверяет наличие статусов ожидания (негативный сценарий)
     */
    public boolean hasWaitingStatuses() {
        logger.debug("Проверяем наличие статусов ожидания");
        boolean hasWaiting = $$("*").filterBy(text("ожидание")).size() > 0;
        logger.debug("Статусы ожидания: {}", hasWaiting ? "присутствуют" : "отсутствуют");
        return hasWaiting;
    }

    /**
     * Проверяет наличие зеленой точки у расширения
     */
    public boolean hasGreenExtensionDot() {
        logger.debug("Проверяем зеленую точку у расширения");
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

    /**
     * Выполняет комплексную диагностику плагина
     */
    @Step("Выполняем комплексную диагностику плагина КриптоПРО")
    public boolean performFullDiagnostic() {
        logger.info("Выполняем комплексную диагностику плагина КриптоПРО");

        // Проверяем базовую загрузку страницы
        boolean pageTitleOk = verifyPageTitle();
        boolean diagnosticInfoOk = verifyDiagnosticInfo();

        // Проверяем все компоненты плагина
        boolean extensionLoaded = verifyExtensionLoaded();
        boolean pluginLoaded = verifyPluginLoaded();
        boolean cspLoaded = verifyCspLoaded();
        boolean objectsLoaded = verifyObjectsLoaded();

        // Проверяем функциональность
        boolean certificateSelectionOk = verifyCertificateSelectionAvailable();

        boolean result = pageTitleOk && diagnosticInfoOk && extensionLoaded && pluginLoaded &&
                        cspLoaded && objectsLoaded && certificateSelectionOk;

        softAssert.assertTrue(result, "Комплексная диагностика плагина должна завершиться успешно");
        if (result) {
            logger.info("✓ Комплексная диагностика плагина завершена успешно");
        }
        return result;
    }

    /**
     * Выполняет полный сценарий подписи
     */
    @Step("Выполняем полный сценарий электронной подписи")
    public boolean performFullSigningScenario() {
        logger.info("Выполняем полный сценарий электронной подписи");

        try {
            // Проверяем готовность плагина
            if (!isPluginReady()) {
                logger.error("Плагин не готов к работе");
                return false;
            }

            // Нажимаем кнопку подписи
            clickSignButton();

            // Ожидаем диалог
            waitForCertificateDialog();

            // Ждем результат подписи
            return waitForSignatureResult();

        } catch (Exception e) {
            logger.error("Ошибка при выполнении сценария подписи: {}", e.getMessage());
            takeScreenshot("signing_error_" + System.currentTimeMillis());
            return false;
        }
    }

    /**
     * Проверяет готовность плагина к работе
     */
    private boolean isPluginReady() {
        return !hasWaitingStatuses() && hasGreenExtensionDot();
    }

    /**
     * Ожидает результат подписи с улучшенной логикой
     */
    private boolean waitForSignatureResult() {
        logger.info("Ожидаем результат подписи");

        // Умное ожидание результата подписи
        waitWithStrategy(WaitStrategies.jsCondition(
            "document.querySelector('textarea, pre, code') !== null || " +
            "document.querySelector('button:contains(\"Подписано\"), button:contains(\"Готово\")') !== null || " +
            "document.querySelector('.signature-result, .signed-document') !== null"
        ), "результат подписи");

        return verifySignatureResult();
    }

    /**
     * Получает детальную информацию о статусе плагина
     */
    public CryptoProStatusInfo getPluginStatusInfo() {
        logger.debug("Получаем детальную информацию о статусе плагина");

        return new CryptoProStatusInfo(
            extensionEnabledTxt.getText(),
            pluginEnabledTxt.getText(),
            cspEnabledTxt.getText(),
            objectsLoadedTxt.getText(),
            hasGreenExtensionDot(),
            hasWaitingStatuses()
        );
    }

    /**
     * Класс для хранения информации о статусе КриптоПРО
     */
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
                "CryptoProStatus{extension='%s', plugin='%s', csp='%s', objects='%s', greenDot=%s, waiting=%s}",
                extensionStatus, pluginStatus, cspStatus, objectsStatus, hasGreenDot, hasWaitingStatuses
            );
        }
    }
}