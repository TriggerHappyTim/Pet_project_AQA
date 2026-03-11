package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика для создания конфигурации Chrome браузера
 */
public class ChromeBrowserFactory extends BaseBrowserFactory {

    private static final Logger log = LoggerFactory.getLogger(ChromeBrowserFactory.class);

    public ChromeBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        super(cryptoProPath, cryptoProXpiPath, isRemote);
    }

    @Override
    public Capabilities getCapabilities() {
        ChromeOptions options = new ChromeOptions();

        configureChromeOptions(options);

        DesiredCapabilities capabilities = createBaseCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        return capabilities;
    }

    @Override
    public String getBrowserName() {
        return "chrome";
    }

    @Override
    public boolean checkLocalBrowserAvailability() {
        // Проверяем наличие Chrome бинарного файла
        String chromeBinary = findChromeBinary();
        if (chromeBinary != null) {
            return true;
        }

        // Проверяем наличие chromedriver из переменной окружения
        String driverPath = System.getenv("CHROME_DRIVER_PATH");
        if (driverPath != null && !driverPath.isEmpty()) {
            File driverFile = new File(driverPath);
            if (driverFile.exists()) {
                System.setProperty("webdriver.chrome.driver", driverPath);
                return true;
            }
        }

        return false;
    }

    @Override
    public void configureSpecificOptions() {
        // Дополнительная настройка может быть добавлена здесь
    }

    private void configureChromeOptions(ChromeOptions options) {
        if (!isRemote) {
            configureLocalChrome(options);
        } else {
            configureRemoteChrome(options);
        }

        // Общие настройки для всех режимов
        options.setAcceptInsecureCerts(true);
        options.addArguments("--disable-notifications", "--disable-popup-blocking");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("intl.accept_languages", "ru");
        options.setExperimentalOption("prefs", prefs);
    }

    private void configureLocalChrome(ChromeOptions options) {
        // Явно устанавливаем путь к бинарному файлу Chrome
        String chromeBinary = findChromeBinary();
        if (chromeBinary != null) {
            options.setBinary(chromeBinary);
        }

        // Добавляем расширение CryptoPro
        if (isExtensionFileExists(cryptoProPath)) {
            File cryptoProExtension = new File(cryptoProPath);
            options.addExtensions(cryptoProExtension);
            log.info("Расширение КриптоПРО добавлено: {}", cryptoProExtension.getAbsolutePath());
        } else {
            logMissingExtensionWarning(cryptoProPath, "Chrome");
        }

        // Установка драйвера из переменной окружения
        String driverPath = System.getenv("CHROME_DRIVER_PATH");
        if (driverPath != null && !driverPath.isEmpty()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
    }

    private String findChromeBinary() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return findWindowsChromeBinary();
        } else {
            return findUnixChromeBinary();
        }
    }

    private String findWindowsChromeBinary() {
        String[] chromePaths = {
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe"
        };

        for (String path : chromePaths) {
            if (path != null && new File(path).exists()) {
                return path;
            }
        }
        return null;
    }

    private String findUnixChromeBinary() {
        try {
            Process process = new ProcessBuilder("which", "google-chrome").start();
            if (process.waitFor() == 0) {
                return "google-chrome";
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private void configureRemoteChrome(ChromeOptions options) {
        // Для удаленного запуска кодируем расширение в base64
        addExtensionForRemote(options, cryptoProPath);
    }

    /**
     * Добавляет расширение для удаленного запуска Chrome
     * Использует переменную окружения CRYPTOPRO_BASE64 или автоматически кодирует файл расширения
     */
    private void addExtensionForRemote(ChromeOptions options, String extensionPath) {
        String encodedExtension = encodeExtensionToBase64(extensionPath);
        if (encodedExtension != null && !encodedExtension.isEmpty()) {
            options.addEncodedExtensions(encodedExtension);
            log.info("Расширение КриптоПРО добавлено в Chrome для удаленного запуска");
        } else {
            log.warn("Расширение КриптоПРО не установлено для удаленного Chrome. Задайте CRYPTOPRO_BASE64 или CRYPTOPRO_PATH.");
        }
    }
}