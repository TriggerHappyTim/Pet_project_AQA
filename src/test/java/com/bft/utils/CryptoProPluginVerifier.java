package com.bft.utils;

import com.bft.pw.Selenide;
import com.bft.pw.SelenideElement;
import com.bft.pw.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.bft.pw.Condition.*;
import static com.bft.pw.Selenide.$;

/**
 * Утилита для проверки состояния плагина КриптоПРО
 */
public class CryptoProPluginVerifier {

    private static final Logger log = LoggerFactory.getLogger(CryptoProPluginVerifier.class);

    /**
     * Проверяет все статусы плагина
     */
    public static boolean verifyAllStatuses(int timeoutSeconds) {
        log.info("=== ВЕРИФИКАЦИЯ ПЛАГИНА КРИПТОПРО ===");

        boolean allPassed = true;

        allPassed &= verifyExtensionLoaded(timeoutSeconds);
        allPassed &= verifyCSPPluginLoaded(timeoutSeconds);
        allPassed &= verifyProviderReady(timeoutSeconds);
        allPassed &= verifyCertificatesAvailable(timeoutSeconds);
        allPassed &= verifyViaJavaScript();

        if (allPassed) {
            log.info("✓ ВСЕ СТАТУСЫ: ПЛАГИН ГОТОВ К РАБОТЕ");
        } else {
            log.warn("✗ НЕКОТОРЫЕ СТАТУСЫ: ПРОБЛЕМЫ С ПЛАГИНОМ");
        }

        return allPassed;
    }

    /**
     * Проверяет, что расширение загружено и отмечено зеленым
     */
    public static boolean verifyExtensionLoaded(int timeoutSeconds) {
        try {
            log.info("1. Расширение: ");

        // Ждем загрузки элемента с умным ожиданием
        try {
            $(By.id("ExtensionEnabledImg")).shouldBe(visible, Duration.ofSeconds(10));
        } catch (Exception e) {
            // Если элемент не загрузился, ждем готовности страницы через JavaScript
            try {
                Selenide.executeJavaScript("return document.readyState === 'complete'");
                // Дополнительно ждем появления элемента с увеличенным таймаутом
                $(By.id("ExtensionEnabledImg")).shouldBe(visible, Duration.ofSeconds(15));
            } catch (Exception e2) {
                // Если элемент все еще не появился, логируем и продолжаем
                log.warn("Элемент ExtensionEnabledImg не появился после ожидания готовности страницы");
            }
        }

            // Проверяем зеленую точку
            SelenideElement greenDot = $(By.id("ExtensionEnabledImg"));
            if (!greenDot.exists()) {
                log.warn("Элемент ExtensionEnabledImg не найден");
                return false;
            }

            greenDot.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            // Проверяем CSS класс "green"
            String classes = greenDot.getAttribute("class");
            if (classes == null || !classes.contains("green")) {
                log.warn("Точка не зеленая! Классы: " + classes);
                return false;
            }

            // Проверяем текст
            SelenideElement textElement = $(By.id("ExtensionEnabledTxt"));
            textElement.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            String extensionText = textElement.getText().trim();
            if (!extensionText.equals("Расширение загружено")) {
                log.warn("Неверный текст: " + extensionText);
                return false;
            }

            log.info("✓ ЗАГРУЖЕНО (зеленая точка)");
            return true;

        } catch (Exception e) {
            log.warn("✗ ОШИБКА: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет плагин КриптоПРО CSP
     */
    public static boolean verifyCSPPluginLoaded(int timeoutSeconds) {
        try {
            log.info("2. Плагин CSP: ");

            // Ищем элемент, содержащий текст о плагине
            SelenideElement pluginElement = $(By.xpath(
                    "//*[contains(text(), 'КриптоПРО CSP') or contains(text(), 'плагин')]"
            ));

            if (!pluginElement.exists()) {
                log.warn("Элемент плагина не найден");
                return false;
            }

            pluginElement.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            String pluginText = pluginElement.getText();

            // Проверяем, что не в состоянии ожидания
            if (pluginText.contains("ожидание")) {
                log.warn("В состоянии ожидания: " + pluginText);
                return false;
            }

            log.info("✓ ЗАГРУЖЕН");
            return true;

        } catch (Exception e) {
            log.warn("✗ НЕ НАЙДЕН: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет готовность провайдера
     */
    public static boolean verifyProviderReady(int timeoutSeconds) {
        try {
            log.info("3. Провайдер: ");

            SelenideElement providerElement = $(By.xpath(
                    "//*[contains(text(), 'Объекты плагина') or contains(text(), 'провайдер')]"
            ));

            if (!providerElement.exists()) {
                log.warn("Элемент провайдера не найден");
                return false;
            }

            providerElement.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            String providerText = providerElement.getText();

            if (providerText.contains("ожидание")) {
                log.warn("В состоянии ожидания: " + providerText);
                return false;
            }

            log.info("✓ ГОТОВ");
            return true;

        } catch (Exception e) {
            log.warn("✗ ОШИБКА: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность сертификатов
     */
    public static boolean verifyCertificatesAvailable(int timeoutSeconds) {
        try {
            log.info("4. Сертификаты: ");

            // Ищем элементы выбора сертификата
            boolean hasCertificateLabel = $(By.xpath(
                    "//*[contains(text(), 'Выберите сертификат')]"
            )).exists();

            boolean hasCertificateSelect = $(By.xpath(
                    "//select"
            )).exists();

            boolean hasCertificateButton = $(By.xpath(
                    "//button[contains(text(), 'Подписать')]"
            )).exists();

            if (hasCertificateLabel || hasCertificateSelect || hasCertificateButton) {
                log.info("✓ ДОСТУПНЫ");
                return true;
            } else {
                log.warn("✗ ЭЛЕМЕНТЫ ВЫБОРА НЕ НАЙДЕНЫ");
                return false;
            }

        } catch (Exception e) {
            log.warn("✗ ОШИБКА: " + e.getMessage());
            return false;
        }
    }

    /**
     * JavaScript проверка наличия плагина
     */
    public static boolean verifyViaJavaScript() {
        try {
            log.info("5. JavaScript проверка: ");

            // Проверяем наличие объектов КриптоПРО
            boolean hasCadesPlugin = false;
            try {
                hasCadesPlugin = Boolean.TRUE.equals(Selenide.executeJavaScript(
                        "return typeof cadesplugin !== 'undefined'"));
            } catch (Exception e) {
                // Игнорируем, если объект не определен
            }

            boolean hasCryptoProExtension = false;
            try {
                hasCryptoProExtension = Boolean.TRUE.equals(Selenide.executeJavaScript(
                        "return typeof window.CryptoPro !== 'undefined'"));
            } catch (Exception e) {
                // Игнорируем
            }

            log.info("cadesplugin=" + hasCadesPlugin +
                    ", CryptoPro=" + hasCryptoProExtension);

            return hasCadesPlugin || hasCryptoProExtension;

        } catch (Exception e) {
            log.warn("✗ ОШИБКА JS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Делает скриншот статусов
     */
    public static void takeStatusScreenshot(String testName) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "cryptopro_status_" + testName + "_" + timestamp;
            Selenide.screenshot(filename);
            log.info("Скриншот сохранен: " + filename);
        } catch (Exception e) {
            log.warn("Не удалось сделать скриншот: " + e.getMessage());
        }
    }
}