package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Фабрика для создания конфигурации Firefox браузера.
 * Полностью самодостаточна, не зависит от базовых классов.
 */
public class FirefoxBrowserFactory {

    private static final Logger log = LoggerFactory.getLogger(FirefoxBrowserFactory.class);

    private final String cryptoProPath;
    private final String cryptoProXpiPath;
    private final boolean isRemote;

    public FirefoxBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        this.cryptoProPath = cryptoProPath;
        this.cryptoProXpiPath = cryptoProXpiPath;
        this.isRemote = isRemote;
    }

    /**
     * Возвращает настроенные Capabilities для Firefox
     */
    public Capabilities getCapabilities() {
        FirefoxOptions options = new FirefoxOptions();

        if (isRemote) {
            configureFirefoxOptionsForRemote(options);
        } else {
            configureFirefoxOptionsLocal(options);
        }

        // Базовые capabilities (VNC/Video для Selenoid)
        String remoteUrl = System.getProperty("selenide.remote", "");
        if (isRemote && remoteUrl != null && remoteUrl.contains("selenoid")) {
            options.setCapability("enableVNC", true);
            options.setCapability("enableVideo", false);
        }

        return options;
    }

    /**
     * Возвращает имя браузера
     */
    public String getBrowserName() {
        return "firefox";
    }

    /**
     * Проверяет доступность браузера в локальной среде
     */
    public boolean checkLocalBrowserAvailability() {
        // 1. Проверяем наличие драйвера через ENV
        String driverPath = System.getenv("GECKO_DRIVER_PATH");
        if (driverPath != null && !driverPath.isEmpty()) {
            File driverFile = new File(driverPath);
            if (driverFile.exists()) {
                System.setProperty("webdriver.gecko.driver", driverPath);
                return true;
            }
        }

        // 2. Пробуем найти Firefox в системе (Linux/Mac)
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                Process process = new ProcessBuilder("which", "firefox").start();
                return process.waitFor() == 0;
            } catch (Exception e) {
                // Игнорируем, если команда не найдена
            }
        }

        // 3. Для Windows возвращаем true, полагаясь на стандартный путь или авто-поиск драйвера
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Настройка для локального запуска с использованием FirefoxProfile
     */
    private void configureFirefoxOptionsLocal(FirefoxOptions options) {
        FirefoxProfile profile = new FirefoxProfile();

        // Настройки локализации и уведомлений
        profile.setPreference("intl.accept_languages", "ru");
        profile.setPreference("dom.webnotifications.enabled", false);

        // Настройки для расширений (отключение проверки подписи важно для CryptoPro)
        profile.setPreference("xpinstall.signatures.required", false);
        profile.setPreference("extensions.allowPrivateBrowsingByDefault", true);
        profile.setPreference("extensions.experiments.enabled", true);
        profile.setPreference("extensions.enabledScopes", 15);
        profile.setPreference("extensions.autoDisableScopes", 0);

        // Добавляем расширение CryptoPro
        addCryptoProExtension(profile);

        options.setProfile(profile);
        options.setAcceptInsecureCerts(true);
        options.addArguments("--width=1920", "--height=1080");
    }

    /**
     * Настройка для удаленного запуска без FirefoxProfile (preferences напрямую)
     */
    private void configureFirefoxOptionsForRemote(FirefoxOptions options) {
        options.setAcceptInsecureCerts(true);

        // Preferences напрямую
        options.addPreference("intl.accept_languages", "ru");
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("xpinstall.signatures.required", false);
        options.addPreference("extensions.allowPrivateBrowsingByDefault", true);
        options.addPreference("extensions.experiments.enabled", true);
        options.addPreference("extensions.enabledScopes", 15);
        options.addPreference("extensions.autoDisableScopes", 0);

        options.addArguments("--width=1920", "--height=1080");

        // Попытка добавить расширение для remote (если задан CRYPTOPRO_BASE64)
        addExtensionForRemoteFirefox(options);
    }

    /**
     * Добавляет расширение КриптоПРО для удаленного запуска Firefox
     */
    private void addExtensionForRemoteFirefox(FirefoxOptions options) {
        String encodedExtension = encodeExtensionToBase64(cryptoProXpiPath);
        if (encodedExtension != null && !encodedExtension.isEmpty()) {
            log.info("Расширение КриптоПРО закодировано для удаленного Firefox (требуется поддержка со стороны Grid)");
            // Примечание: Selenium Grid часто игнорирует addExtension для Firefox в remote режиме,
            // поэтому основная рекомендация - предустановка в образе.
        } else {
            log.warn("Для удалённого Firefox расширение КриптоПРО не установлено. " +
                    "Рекомендуется предустановить расширение в Docker-образе Selenium.");
        }
    }

    /**
     * Добавляет расширение в локальный профиль Firefox
     */
    private void addCryptoProExtension(FirefoxProfile profile) {
        if (!isExtensionFileExists(cryptoProXpiPath)) {
            logMissingExtensionWarning(cryptoProXpiPath, "Firefox");
            return;
        }

        try {
            File cryptoProExtension = new File(cryptoProXpiPath);
            if (cryptoProXpiPath.toLowerCase().endsWith(".xpi")) {
                profile.addExtension(cryptoProExtension);
                log.info("Расширение CryptoPro добавлено в Firefox: {}", cryptoProExtension.getAbsolutePath());
            } else {
                log.warn("Файл расширения должен иметь расширение .xpi: {}", cryptoProXpiPath);
            }
        } catch (Exception e) {
            log.warn("Ошибка при добавлении расширения в Firefox: {}", e.getMessage(), e);
        }
    }

    // === УТИЛИТНЫЕ МЕТОДЫ (бывшие из BaseBrowserFactory) ===

    /**
     * Проверяет существование файла расширения
     */
    private boolean isExtensionFileExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File extensionFile = new File(path);
        return extensionFile.exists() && extensionFile.isFile();
    }

    /**
     * Выводит предупреждение при отсутствии файла расширения
     */
    private void logMissingExtensionWarning(String path, String browser) {
        String actualPath = (path != null) ? new File(path).getAbsolutePath() : "не задан";
        log.warn("ВНИМАНИЕ: Расширение КриптоПРО НЕ НАЙДЕНО для {}. Ожидаемый путь: {}. " +
                        "Тесты КриптоПРО будут ПРОПУЩЕНЫ. Скачайте расширение или задайте CRYPTOPRO_PATH / CRYPTOPRO_BASE64.",
                browser, actualPath);
    }

    /**
     * Кодирует файл расширения в base64 для использования в удаленном режиме
     * Приоритет: переменная окружения CRYPTOPRO_BASE64 > файл на диске
     */
    private String encodeExtensionToBase64(String extensionPath) {
        // 1. Сначала проверяем переменную окружения CRYPTOPRO_BASE64
        String encodedFromEnv = System.getenv("CRYPTOPRO_BASE64");
        if (encodedFromEnv != null && !encodedFromEnv.isEmpty()) {
            log.info("Используется расширение из переменной окружения CRYPTOPRO_BASE64");
            return encodedFromEnv;
        }

        // 2. Если переменной нет, пытаемся прочитать файл
        if (extensionPath == null || extensionPath.isEmpty()) {
            return null;
        }

        if (!isExtensionFileExists(extensionPath)) {
            log.debug("Файл расширения не найден для кодирования: {}", extensionPath);
            return null;
        }

        try {
            File extensionFile = new File(extensionPath);
            byte[] fileContent = Files.readAllBytes(extensionFile.toPath());
            String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
            log.debug("Расширение успешно закодировано в base64: {}", extensionPath);
            return base64Encoded;
        } catch (IOException e) {
            log.warn("Ошибка при кодировании расширения в base64: {}", e.getMessage());
            return null;
        }
    }
}