package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;

/**
 * Фабрика для создания конфигурации Firefox браузера
 */
public class FirefoxBrowserFactory extends BaseBrowserFactory {

    private static final Logger log = LoggerFactory.getLogger(FirefoxBrowserFactory.class);

    public FirefoxBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        super(cryptoProPath, cryptoProXpiPath, isRemote);
    }

    @Override
    public Capabilities getCapabilities() {
        FirefoxOptions options = new FirefoxOptions();

        if (isRemote) {
            // Для удаленного запуска используем preferences напрямую, без FirefoxProfile
            // Это избегает проблем с NullPointerException при слиянии capabilities
            configureFirefoxOptionsForRemote(options);
        } else {
            // Для локального запуска используем FirefoxProfile
            FirefoxProfile profile = new FirefoxProfile();
            configureFirefoxProfile(profile);
            options.setProfile(profile);
            configureFirefoxOptions(options);
        }

        // Базовые capabilities (VNC/Video для Selenoid) добавляем напрямую в FirefoxOptions
        String remoteUrl = System.getProperty("selenide.remote", "");
        if (isRemote && remoteUrl.contains("selenoid")) {
            // Используем Selenoid capabilities только если URL содержит "selenoid"
            options.setCapability("enableVNC", true);
            options.setCapability("enableVideo", false);
        }
        // Для стандартного Selenium Grid (chrome, firefox) capabilities не добавляем

        return options;
    }

    @Override
    public String getBrowserName() {
        return "firefox";
    }

    @Override
    public boolean checkLocalBrowserAvailability() {
        String driverPath = System.getenv("GECKO_DRIVER_PATH");
        if (driverPath != null) {
            System.setProperty("webdriver.gecko.driver", driverPath);
            return true;
        }

        try {
            // Пробуем найти Firefox в системе
            Process process = Runtime.getRuntime().exec("which firefox");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void configureSpecificOptions() {
        // Специфичная настройка для Firefox
    }

    private void configureFirefoxProfile(FirefoxProfile profile) {
        // Настройки локализации
        profile.setPreference("intl.accept_languages", "ru");
        profile.setPreference("dom.webnotifications.enabled", false);

        // Важные настройки для установки расширений
        profile.setPreference("xpinstall.signatures.required", false); // Отключаем проверку подписи
        profile.setPreference("extensions.allowPrivateBrowsingByDefault", true);
        profile.setPreference("extensions.experiments.enabled", true);

        // Настройка установки расширений
        profile.setPreference("extensions.enabledScopes", 15); // Все области установки
        profile.setPreference("extensions.autoDisableScopes", 0); // Не отключать автоматически

        // Добавляем расширение CryptoPro
        if (!isRemote) {
            addCryptoProExtension(profile);
        } else {
            // Для удаленного режима расширение должно быть предустановлено
            log.info("Запуск в удаленном режиме. Расширение должно быть предустановлено на узле.");
        }

        // Установка драйвера
        String driverPath = System.getenv("GECKO_DRIVER_PATH");
        if (driverPath != null) {
            System.setProperty("webdriver.gecko.driver", driverPath);
        }
    }

    private void configureFirefoxOptions(FirefoxOptions options) {
        options.setAcceptInsecureCerts(true);

        // Опции для лучшей совместимости
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
    }

    /**
     * Настраивает FirefoxOptions для удаленного запуска без использования FirefoxProfile
     * Использует preferences напрямую через FirefoxOptions, что избегает проблем при слиянии capabilities
     */
    private void configureFirefoxOptionsForRemote(FirefoxOptions options) {
        options.setAcceptInsecureCerts(true);

        // Устанавливаем preferences напрямую через FirefoxOptions
        // Это безопаснее для удаленного запуска, чем использование FirefoxProfile
        options.addPreference("intl.accept_languages", "ru");
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("xpinstall.signatures.required", false);
        options.addPreference("extensions.allowPrivateBrowsingByDefault", true);
        options.addPreference("extensions.experiments.enabled", true);
        options.addPreference("extensions.enabledScopes", 15);
        options.addPreference("extensions.autoDisableScopes", 0);

        // Опции для лучшей совместимости
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        // Добавляем расширение для удаленного запуска через FirefoxProfile
        // В удаленном режиме создаем временный профиль с расширением
        addExtensionForRemoteFirefox(options);
    }

    /**
     * Добавляет расширение КриптоПРО для удаленного запуска Firefox
     *
     * ВАЖНО: Для Firefox в удаленном режиме через Selenium Grid установка расширений через FirefoxProfile
     * может вызвать проблемы с merge capabilities в некоторых версиях Selenium.
     *
     * Рекомендуется предустановить расширение в Docker-образе Selenium для стабильной работы.
     * Альтернативно: установите переменную окружения CRYPTOPRO_BASE64 с закодированным расширением.
     */
    private void addExtensionForRemoteFirefox(FirefoxOptions options) {
        log.warn("Для удалённого Firefox расширение КриптоПРО НЕ устанавливается программно. " +
                "Предустановите расширение в Docker-образе Selenium для стабильной работы.");
    }

    private void addCryptoProExtension(FirefoxProfile profile) {
        if (!isExtensionFileExists(cryptoProXpiPath)) {
            logMissingExtensionWarning(cryptoProXpiPath, "Firefox");
            return;
        }

        try {
            File cryptoProExtension = new File(cryptoProXpiPath);

            // Убедимся, что это файл XPI
            if (cryptoProXpiPath.toLowerCase().endsWith(".xpi")) {
                profile.addExtension(cryptoProExtension);
                log.info("Расширение CryptoPro добавлено в Firefox: {}", cryptoProExtension.getAbsolutePath());
            } else {
                log.warn("Файл расширения должен иметь расширение .xpi: {}", cryptoProXpiPath);
            }
        } catch (Exception e) {
            log.warn("Ошибка при добавлении расширения: {}", e.getMessage());
        }
    }
}