package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика для создания конфигурации Yandex браузера
 */
public class YandexBrowserFactory extends BaseBrowserFactory {

    public YandexBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        super(cryptoProPath, cryptoProXpiPath, isRemote);
    }

    @Override
    public Capabilities getCapabilities() {
        ChromeOptions options = new ChromeOptions();

        // Устанавливаем бинарный файл Яндекс.Браузера
        String yandexPath = System.getenv("YANDEX_BROWSER_PATH");
        if (yandexPath != null && !yandexPath.isEmpty()) {
            options.setBinary(yandexPath);
        }

        configureYandexOptions(options);

        DesiredCapabilities capabilities = createBaseCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        return capabilities;
    }

    @Override
    public String getBrowserName() {
        return "yandex";
    }

    @Override
    public boolean checkLocalBrowserAvailability() {
        String yandexPath = System.getenv("YANDEX_BROWSER_PATH");
        if (yandexPath != null && !yandexPath.isEmpty()) {
            File yandexBinary = new File(yandexPath);
            return yandexBinary.exists() && yandexBinary.canExecute();
        }

        // Ищем в стандартных путях
        String[] possiblePaths = {
            "/usr/bin/yandex-browser",
            "/usr/local/bin/yandex-browser",
            "C:\\Program Files\\Yandex\\YandexBrowser\\browser.exe",
            "C:\\Program Files (x86)\\Yandex\\YandexBrowser\\browser.exe"
        };

        for (String path : possiblePaths) {
            File yandexBinary = new File(path);
            if (yandexBinary.exists() && yandexBinary.canExecute()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void configureSpecificOptions() {
        // Специфичная настройка для Yandex браузера
    }

    private void configureYandexOptions(ChromeOptions options) {
        if (!isRemote) {
            configureLocalYandex(options);
        } else {
            configureRemoteYandex(options);
        }

        // Общие настройки для всех режимов
        options.setAcceptInsecureCerts(true);
        options.addArguments("--disable-notifications", "--disable-popup-blocking");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("intl.accept_languages", "ru");
        options.setExperimentalOption("prefs", prefs);
    }

    private void configureLocalYandex(ChromeOptions options) {
        // Добавляем расширение CryptoPro
        if (isExtensionFileExists(cryptoProPath)) {
            File cryptoProExtension = new File(cryptoProPath);
            options.addExtensions(cryptoProExtension);
        }

        // Установка драйвера из переменной окружения
        String driverPath = System.getenv("YANDEX_DRIVER_PATH");
        if (driverPath != null) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
    }

    private void configureRemoteYandex(ChromeOptions options) {
        // Для удаленного запуска кодируем расширение в base64
        addExtensionForRemote(options, cryptoProPath);
    }

    /**
     * Добавляет расширение для удаленного запуска Yandex Browser
     * Использует переменную окружения CRYPTOPRO_BASE64 или автоматически кодирует файл расширения
     */
    private void addExtensionForRemote(ChromeOptions options, String extensionPath) {
        String encodedExtension = encodeExtensionToBase64(extensionPath);
        if (encodedExtension != null && !encodedExtension.isEmpty()) {
            options.addEncodedExtensions(encodedExtension);
            System.out.println("Расширение КриптоПРО добавлено в Yandex Browser для удаленного запуска");
        } else {
            System.err.println("ВНИМАНИЕ: Расширение КриптоПРО не установлено для удаленного Yandex Browser. " +
                    "Установите переменную окружения CRYPTOPRO_BASE64 или укажите путь к файлу расширения в CRYPTOPRO_PATH");
        }
    }
}