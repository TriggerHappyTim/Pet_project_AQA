package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Фабрика для создания конфигурации Firefox браузера
 */
public class FirefoxBrowserFactory extends BaseBrowserFactory {

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
            System.out.println("Запуск в удаленном режиме. Расширение должно быть предустановлено на узле.");
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
        String encodedExtension = encodeExtensionToBase64(cryptoProXpiPath);
        if (encodedExtension != null && !encodedExtension.isEmpty()) {
            try {
                // Создаем временный FirefoxProfile для установки расширения
                // ВАЖНО: Это может вызвать проблемы с merge в некоторых версиях Selenium
                FirefoxProfile tempProfile = new FirefoxProfile();

                // Настройки для установки расширений (дублируем из configureFirefoxOptionsForRemote)
                tempProfile.setPreference("xpinstall.signatures.required", false);
                tempProfile.setPreference("extensions.allowPrivateBrowsingByDefault", true);
                tempProfile.setPreference("extensions.enabledScopes", 15);
                tempProfile.setPreference("extensions.autoDisableScopes", 0);

                // Декодируем base64 и создаем временный файл для расширения
                byte[] extensionBytes = Base64.getDecoder().decode(encodedExtension);
                File tempExtensionFile = File.createTempFile("cryptopro_", ".xpi");
                tempExtensionFile.deleteOnExit();
                Files.write(tempExtensionFile.toPath(), extensionBytes);

                // Добавляем расширение в профиль
                tempProfile.addExtension(tempExtensionFile);

                // Устанавливаем профиль в опции
                // ВАЖНО: Это может вызвать проблемы с merge, но это единственный способ установить расширение программно
                options.setProfile(tempProfile);

                System.out.println("Расширение КриптоПРО добавлено в Firefox для удаленного запуска через временный профиль");
                System.out.println("Если возникают ошибки с merge capabilities, рекомендуется предустановить расширение в Docker-образе Selenium");
            } catch (Exception e) {
                System.err.println("Ошибка при добавлении расширения в Firefox для удаленного запуска: " + e.getMessage());
                e.printStackTrace();
                System.err.println("ВНИМАНИЕ: Расширение КриптоПРО не установлено для удаленного Firefox.");
                System.err.println("Рекомендуется предустановить расширение в Docker-образе Selenium или установить переменную окружения CRYPTOPRO_BASE64");
            }
        } else {
            System.err.println("ВНИМАНИЕ: Расширение КриптоПРО не установлено для удаленного Firefox.");
            System.err.println("Установите переменную окружения CRYPTOPRO_BASE64 или укажите путь к файлу расширения в CRYPTOPRO_PATH");
            System.err.println("Альтернативно: предустановите расширение в Docker-образе Selenium для стабильной работы");
        }
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
                System.out.println("Расширение CryptoPro добавлено в Firefox: " + cryptoProExtension.getAbsolutePath());
            } else {
                System.err.println("Файл расширения должен иметь расширение .xpi: " + cryptoProXpiPath);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении расширения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}