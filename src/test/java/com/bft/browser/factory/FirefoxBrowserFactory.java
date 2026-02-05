package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;

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
        FirefoxProfile profile = new FirefoxProfile();

        configureFirefoxProfile(profile);
        options.setProfile(profile);

        configureFirefoxOptions(options);

        // Для Selenium 4.x возвращаем FirefoxOptions напрямую, а не через DesiredCapabilities
        // Это избегает проблем с NullPointerException при слиянии capabilities
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

    private void addCryptoProExtension(FirefoxProfile profile) {
        if (!isExtensionFileExists(cryptoProXpiPath)) {
            System.err.println("Файл расширения не найден: " + cryptoProXpiPath);
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