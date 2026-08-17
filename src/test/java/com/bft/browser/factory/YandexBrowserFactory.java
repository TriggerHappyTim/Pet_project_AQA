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
 * Фабрика для создания конфигурации Яндекс.Браузера.
 * Реализация полностью самодостаточна (не зависит от BaseBrowserFactory).
 * Основана на ChromeOptions, так как Яндекс использует движок Chromium.
 */
public class YandexBrowserFactory {

    private static final Logger log = LoggerFactory.getLogger(YandexBrowserFactory.class);

    // Стандартные пути к исполняемому файлу Яндекс.Браузера
    private static final String[] YANDEX_BINARY_PATHS = {
            "/usr/bin/yandex-browser",
            "/usr/local/bin/yandex-browser",
            "C:\\Program Files\\Yandex\\YandexBrowser\\browser.exe",
            "C:\\Program Files (x86)\\Yandex\\YandexBrowser\\browser.exe"
    };

    private final String cryptoProPath;
    private final String cryptoProXpiPath;
    private final boolean isRemote;

    public YandexBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        this.cryptoProPath = cryptoProPath;
        this.cryptoProXpiPath = cryptoProXpiPath;
        this.isRemote = isRemote;
    }

    /**
     * Возвращает настроенные Capabilities для Яндекс.Браузера.
     */
    public Capabilities getCapabilities() {
        ChromeOptions options = new ChromeOptions();

        // Пытаемся найти бинарник, если не задан явно через ENV
        String yandexBinary = findYandexBinary();
        if (yandexBinary != null) {
            options.setBinary(yandexBinary);
            log.debug("Бинарный файл Яндекс.Браузера установлен: {}", yandexBinary);
        } else {
            log.warn("Бинарный файл Яндекс.Браузера не найден. Будет использован браузер по умолчанию из PATH.");
        }

        configureYandexOptions(options);

        // Добавляем базовые capabilities (VNC/Video для Selenoid)
        DesiredCapabilities capabilities = createBaseCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        return capabilities;
    }

    /**
     * Возвращает имя браузера.
     */
    public String getBrowserName() {
        return "yandex";
    }

    /**
     * Проверяет доступность браузера в локальной среде.
     */
    public boolean checkLocalBrowserAvailability() {
        // Проверяем переменную окружения
        String envPath = System.getenv("YANDEX_BROWSER_PATH");
        if (envPath != null && !envPath.isEmpty()) {
            File file = new File(envPath);
            if (file.exists()) {
                return true;
            }
        }
        // Проверяем стандартные пути
        return findYandexBinary() != null;
    }

    /**
     * Ищет исполняемый файл Яндекс.Браузера в системе.
     */
    private String findYandexBinary() {
        // Сначала проверяем ENV
        String envPath = System.getenv("YANDEX_BROWSER_PATH");
        if (envPath != null && !envPath.isEmpty()) {
            File file = new File(envPath);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        // Затем стандартные пути
        for (String path : YANDEX_BINARY_PATHS) {
            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return null;
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
            log.info("Расширение КриптоПРО добавлено в Yandex: {}", cryptoProExtension.getAbsolutePath());
        } else {
            logMissingExtensionWarning(cryptoProPath, "Yandex");
        }

        // Установка драйвера из переменной окружения (если нужно переопределить авто-загрузку)
        String driverPath = System.getenv("YANDEX_DRIVER_PATH");
        if (driverPath != null && !driverPath.isEmpty()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
    }

    private void configureRemoteYandex(ChromeOptions options) {
        addExtensionForRemote(options, cryptoProPath);
    }

    /**
     * Добавляет расширение для удаленного запуска через base64.
     */
    private void addExtensionForRemote(ChromeOptions options, String extensionPath) {
        String encodedExtension = encodeExtensionToBase64(extensionPath);
        if (encodedExtension != null && !encodedExtension.isEmpty()) {
            options.addEncodedExtensions(encodedExtension);
            log.info("Расширение КриптоПРО добавлено в Yandex (remote) через base64");
        } else {
            log.warn("Расширение КриптоПРО не установлено для удаленного Yandex. Задайте CRYPTOPRO_BASE64 или проверьте путь к файлу.");
        }
    }

    // --- Встроенные утилитные методы (бывшие из BaseBrowserFactory) ---

    /**
     * Создает базовые capabilities (VNC, Video для Selenoid).
     */
    private DesiredCapabilities createBaseCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        String remoteUrl = System.getProperty("selenide.remote", "");
        if (isRemote && remoteUrl.contains("selenoid")) {
            capabilities.setCapability("enableVNC", true);
            capabilities.setCapability("enableVideo", false);
        }
        return capabilities;
    }

    /**
     * Проверяет существование файла расширения.
     */
    private boolean isExtensionFileExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File extensionFile = new File(path);
        return extensionFile.exists() && extensionFile.isFile();
    }

    /**
     * Выводит предупреждение при отсутствии файла расширения.
     */
    private void logMissingExtensionWarning(String path, String browser) {
        log.warn("ВНИМАНИЕ: Расширение КриптоПРО НЕ НАЙДЕНО для {}. Ожидаемый путь: {}. " +
                        "Тесты КриптоПРО будут ПРОПУЩЕНЫ. Скачайте расширение или задайте CRYPTOPRO_PATH / CRYPTOPRO_BASE64.",
                browser, path != null ? new File(path).getAbsolutePath() : "не задан");
    }

    /**
     * Кодирует файл расширения в base64.
     */
    private String encodeExtensionToBase64(String extensionPath) {
        if (extensionPath == null || extensionPath.isEmpty()) {
            return null;
        }

        // Приоритет: переменная окружения CRYPTOPRO_BASE64
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