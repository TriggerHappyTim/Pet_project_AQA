package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика для создания конфигурации Chrome браузера с поддержкой КриптоПРО.
 * Полностью самодостаточный класс, не зависящий от базовых абстракций.
 */
public class ChromeBrowserFactory {

    private static final Logger log = LoggerFactory.getLogger(ChromeBrowserFactory.class);

    private final String cryptoProPath;
    private final String cryptoProXpiPath; // Не используется в Chrome, но оставлен для сигнатуры
    private final boolean isRemote;

    public ChromeBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        this.cryptoProPath = cryptoProPath;
        this.cryptoProXpiPath = cryptoProXpiPath;
        this.isRemote = isRemote;
    }

    /**
     * Возвращает настроенные Capabilities для Chrome
     */
    public Capabilities getCapabilities() {
        ChromeOptions options = new ChromeOptions();
        configureChromeOptions(options);

        DesiredCapabilities capabilities = createBaseCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        return capabilities;
    }

    /**
     * Возвращает имя браузера
     */
    public String getBrowserName() {
        return "chrome";
    }

    /**
     * Проверяет доступность браузера локально
     */
    public boolean checkLocalBrowserAvailability() {
        String chromeBinary = findChromeBinary();
        if (chromeBinary != null) {
            return true;
        }

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

    private void configureChromeOptions(ChromeOptions options) {
        if (!isRemote) {
            configureLocalChrome(options);
        } else {
            configureRemoteChrome(options);
        }

        options.setAcceptInsecureCerts(true);
        options.addArguments("--disable-notifications", "--disable-popup-blocking");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("intl.accept_languages", "ru");
        options.setExperimentalOption("prefs", prefs);
    }

    private void configureLocalChrome(ChromeOptions options) {
        String chromeBinary = findChromeBinary();
        if (chromeBinary != null) {
            options.setBinary(chromeBinary);
        }

        if (isExtensionFileExists(cryptoProPath)) {
            File cryptoProExtension = new File(cryptoProPath);
            options.addExtensions(cryptoProExtension);
            log.info("Расширение КриптоПРО добавлено: {}", cryptoProExtension.getAbsolutePath());
        } else {
            logMissingExtensionWarning(cryptoProPath, "Chrome");
        }

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
        addExtensionForRemote(options, cryptoProPath);
    }

    private void addExtensionForRemote(ChromeOptions options, String extensionPath) {
        String encodedExtension = encodeExtensionToBase64(extensionPath);
        if (encodedExtension != null && !encodedExtension.isEmpty()) {
            options.addEncodedExtensions(encodedExtension);
            log.info("Расширение КриптоПРО добавлено в Chrome для удаленного запуска");
        } else {
            log.warn("Расширение КриптоПРО не установлено для удаленного Chrome.");
        }
    }

    // === Утилитные методы (бывшие из BaseBrowserFactory) ===

    private DesiredCapabilities createBaseCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        // VNC/Video только для Selenoid
        String remoteUrl = System.getProperty("selenide.remote", "");
        if (isRemote && remoteUrl.contains("selenoid")) {
            capabilities.setCapability("enableVNC", true);
            capabilities.setCapability("enableVideo", false);
        }
        return capabilities;
    }

    private boolean isExtensionFileExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File extensionFile = new File(path);
        return extensionFile.exists() && extensionFile.isFile();
    }

    private void logMissingExtensionWarning(String path, String browser) {
        log.warn("ВНИМАНИЕ: Расширение КриптоПРО НЕ НАЙДЕНО для {}. Ожидаемый путь: {}. " +
                        "Тесты КриптоПРО будут ПРОПУЩЕНЫ. Скачайте расширение или задайте CRYPTOPRO_PATH / CRYPTOPRO_BASE64.",
                browser, path != null ? new File(path).getAbsolutePath() : "не задан");
    }

    private String encodeExtensionToBase64(String extensionPath) {
        if (extensionPath == null || extensionPath.isEmpty()) {
            return null;
        }

        // Приоритет переменной окружения
        String encodedFromEnv = System.getenv("CRYPTOPRO_BASE64");
        if (encodedFromEnv != null && !encodedFromEnv.isEmpty()) {
            log.info("Используется расширение из переменной окружения CRYPTOPRO_BASE64");
            return encodedFromEnv;
        }

        if (!isExtensionFileExists(extensionPath)) {
            log.warn("Файл расширения не найден: {}", extensionPath);
            return null;
        }

        try {
            File extensionFile = new File(extensionPath);
            byte[] fileContent = Files.readAllBytes(extensionFile.toPath());
            String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
            log.info("Расширение успешно закодировано в base64: {}", extensionPath);
            return base64Encoded;
        } catch (IOException e) {
            log.warn("Ошибка при кодировании расширения в base64: {}", e.getMessage());
            return null;
        }
    }
}
