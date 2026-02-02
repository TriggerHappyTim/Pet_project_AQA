package com.bft.utils;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Утилита для проверки состояния плагина КриптоПРО
 */
public class CryptoProPluginVerifier {

    /**
     * Проверяет все статусы плагина
     */
    public static boolean verifyAllStatuses(int timeoutSeconds) {
        System.out.println("\n=== ВЕРИФИКАЦИЯ ПЛАГИНА КРИПТОПРО ===");

        boolean allPassed = true;

        allPassed &= verifyExtensionLoaded(timeoutSeconds);
        allPassed &= verifyCSPPluginLoaded(timeoutSeconds);
        allPassed &= verifyProviderReady(timeoutSeconds);
        allPassed &= verifyCertificatesAvailable(timeoutSeconds);
        allPassed &= verifyViaJavaScript();

        if (allPassed) {
            System.out.println("✓ ВСЕ СТАТУСЫ: ПЛАГИН ГОТОВ К РАБОТЕ");
        } else {
            System.err.println("✗ НЕКОТОРЫЕ СТАТУСЫ: ПРОБЛЕМЫ С ПЛАГИНОМ");
        }

        return allPassed;
    }

    /**
     * Проверяет, что расширение загружено и отмечено зеленым
     */
    public static boolean verifyExtensionLoaded(int timeoutSeconds) {
        try {
            System.out.print("1. Расширение: ");

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
                System.err.println("Элемент ExtensionEnabledImg не появился после ожидания готовности страницы");
            }
        }

            // Проверяем зеленую точку
            SelenideElement greenDot = $(By.id("ExtensionEnabledImg"));
            if (!greenDot.exists()) {
                System.err.println("Элемент ExtensionEnabledImg не найден");
                return false;
            }

            greenDot.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            // Проверяем CSS класс "green"
            String classes = greenDot.getAttribute("class");
            if (classes == null || !classes.contains("green")) {
                System.err.println("Точка не зеленая! Классы: " + classes);
                return false;
            }

            // Проверяем текст
            SelenideElement textElement = $(By.id("ExtensionEnabledTxt"));
            textElement.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            String extensionText = textElement.getText().trim();
            if (!extensionText.equals("Расширение загружено")) {
                System.err.println("Неверный текст: " + extensionText);
                return false;
            }

            System.out.println("✓ ЗАГРУЖЕНО (зеленая точка)");
            return true;

        } catch (Exception e) {
            System.err.println("✗ ОШИБКА: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет плагин КриптоПРО CSP
     */
    public static boolean verifyCSPPluginLoaded(int timeoutSeconds) {
        try {
            System.out.print("2. Плагин CSP: ");

            // Ищем элемент, содержащий текст о плагине
            SelenideElement pluginElement = $(By.xpath(
                    "//*[contains(text(), 'КриптоПРО CSP') or contains(text(), 'плагин')]"
            ));

            if (!pluginElement.exists()) {
                System.err.println("Элемент плагина не найден");
                return false;
            }

            pluginElement.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            String pluginText = pluginElement.getText();

            // Проверяем, что не в состоянии ожидания
            if (pluginText.contains("ожидание")) {
                System.err.println("В состоянии ожидания: " + pluginText);
                return false;
            }

            System.out.println("✓ ЗАГРУЖЕН");
            return true;

        } catch (Exception e) {
            System.err.println("✗ НЕ НАЙДЕН: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет готовность провайдера
     */
    public static boolean verifyProviderReady(int timeoutSeconds) {
        try {
            System.out.print("3. Провайдер: ");

            SelenideElement providerElement = $(By.xpath(
                    "//*[contains(text(), 'Объекты плагина') or contains(text(), 'провайдер')]"
            ));

            if (!providerElement.exists()) {
                System.err.println("Элемент провайдера не найден");
                return false;
            }

            providerElement.shouldBe(visible, Duration.ofSeconds(timeoutSeconds));

            String providerText = providerElement.getText();

            if (providerText.contains("ожидание")) {
                System.err.println("В состоянии ожидания: " + providerText);
                return false;
            }

            System.out.println("✓ ГОТОВ");
            return true;

        } catch (Exception e) {
            System.err.println("✗ ОШИБКА: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность сертификатов
     */
    public static boolean verifyCertificatesAvailable(int timeoutSeconds) {
        try {
            System.out.print("4. Сертификаты: ");

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
                System.out.println("✓ ДОСТУПНЫ");
                return true;
            } else {
                System.err.println("✗ ЭЛЕМЕНТЫ ВЫБОРА НЕ НАЙДЕНЫ");
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ ОШИБКА: " + e.getMessage());
            return false;
        }
    }

    /**
     * JavaScript проверка наличия плагина
     */
    public static boolean verifyViaJavaScript() {
        try {
            System.out.print("5. JavaScript проверка: ");

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

            System.out.println("cadesplugin=" + hasCadesPlugin +
                    ", CryptoPro=" + hasCryptoProExtension);

            return hasCadesPlugin || hasCryptoProExtension;

        } catch (Exception e) {
            System.err.println("✗ ОШИБКА JS: " + e.getMessage());
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
            System.out.println("Скриншот сохранен: " + filename);
        } catch (Exception e) {
            System.err.println("Не удалось сделать скриншот: " + e.getMessage());
        }
    }
}