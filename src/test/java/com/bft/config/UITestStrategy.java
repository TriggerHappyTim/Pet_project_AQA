package com.bft.config;

import com.bft.browser.factory.ChromeBrowserFactory;
import com.bft.browser.factory.FirefoxBrowserFactory;
import com.bft.browser.factory.YandexBrowserFactory;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

/**
 * Стратегия для UI тестирования с использованием браузеров.
 * Самодостаточный класс, управляющий настройкой Selenide, драйверов и расширений.
 */
public class UITestStrategy {

    private static final Logger log = LoggerFactory.getLogger(UITestStrategy.class);

    private final TestConfig config;
    private final String browser;
    private final boolean isRemote;
    private final String cryptoProPath;
    private final String cryptoProXpiPath;
    private final boolean headless;

    public UITestStrategy(TestConfig config) {
        this.config = config;
        this.browser = config.getBrowser();
        this.isRemote = config.isRemote();
        this.cryptoProPath = config.getCryptoProPath();
        this.cryptoProXpiPath = config.getCryptoProXpiPath();
        this.headless = config.isHeadless();
    }

    /**
     * Настройка переменных окружения (путей к драйверам).
     * Вызывается один раз при старте.
     */
    public void setupEnvironment() {
        setupBrowserDrivers();
    }

    /**
     * Основная настройка Selenide и_capabilities браузера.
     */
    public void configureSelenide() {
        // Базовые настройки Selenide
        Configuration.remote = isRemote ? config.getRemoteUrl() : null;
        Configuration.browserVersion = config.getBrowserVersion();
        Configuration.browserSize = "1920x1080";
        Configuration.headless = headless;
        Configuration.timeout = isRemote ? 30000 : 10000;
        Configuration.pageLoadTimeout = isRemote ? 300000 : 60000;
        Configuration.pollingInterval = 500;

        try {
            // 1. Инициализация драйвера через WebDriverManager
            initDriverManager();

            // 2. Создание capabilities через соответствующую фабрику
            Capabilities capabilities = createCapabilities();

            if (capabilities == null) {
                throw new IllegalStateException("Не удалось создать capabilities для браузера: " + browser);
            }

            // 3. Применение настроек к Selenide
            if (capabilities instanceof org.openqa.selenium.firefox.FirefoxOptions) {
                Configuration.browserCapabilities = (org.openqa.selenium.firefox.FirefoxOptions) capabilities;
            } else {
                Configuration.browserCapabilities = new MutableCapabilities(capabilities);
            }

            Configuration.browser = capabilities.getBrowserName();

            log.info("Браузер настроен: {} (Remote: {})", Configuration.browser, isRemote);

            // 4. Спец. настройки для Selenoid (видео)
            if (isRemote && config.getRemoteUrl() != null && config.getRemoteUrl().contains("selenoid")) {
                Configuration.browserCapabilities.setCapability("videoName",
                        browser + "_" + System.currentTimeMillis() + ".mp4");
                log.info("Настроена запись видео для Selenoid");
            }

        } catch (Exception e) {
            log.error("Ошибка настройки браузера: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure browser: " + browser, e);
        }
    }

    /**
     * Действия перед каждым тестом (открытие браузера).
     */
    public void beforeTest() {
        open();
        if (!headless) {
            WebDriverRunner.getWebDriver().manage().window().maximize();
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            addSitesToCryptoProTrusted();
        }
    }

    /**
     * Действия после каждого теста (закрытие браузера).
     */
    public void afterTest() {
        closeWebDriver();
    }

    // --- Внутренняя логика ---

    private void initDriverManager() {
        String b = browser.toLowerCase();
        if ("chrome".equals(b) || "yandex".equals(b)) {
            log.info("Инициализация ChromeDriver через WebDriverManager...");
            WebDriverManager.chromedriver().setup();
        } else if ("firefox".equals(b)) {
            log.info("Инициализация GeckoDriver через WebDriverManager...");
            WebDriverManager.firefoxdriver().setup();
        } else {
            log.warn("Неизвестный тип браузера для WebDriverManager: {}. Попытка использования стандартного драйвера.", browser);
        }
    }

    private Capabilities createCapabilities() {
        String b = browser.toLowerCase();
        switch (b) {
            case "chrome":
                return new ChromeBrowserFactory(cryptoProPath, cryptoProXpiPath, isRemote).getCapabilities();
            case "yandex":
                return new YandexBrowserFactory(cryptoProPath, cryptoProXpiPath, isRemote).getCapabilities();
            case "firefox":
                return new FirefoxBrowserFactory(cryptoProPath, cryptoProXpiPath, isRemote).getCapabilities();
            default:
                log.error("Неподдерживаемый браузер: {}", browser);
                return null;
        }
    }

    private void setupBrowserDrivers() {
        String b = browser.toLowerCase();
        switch (b) {
            case "chrome":
                setDriverIfEnvExists("CHROME_DRIVER_PATH", "webdriver.chrome.driver");
                break;
            case "firefox":
                setDriverIfEnvExists("GECKO_DRIVER_PATH", "webdriver.gecko.driver");
                break;
            case "yandex":
                setDriverIfEnvExists("YANDEX_DRIVER_PATH", "webdriver.chrome.driver");
                break;
        }
    }

    private void setDriverIfEnvExists(String envVar, String sysProp) {
        String path = System.getenv(envVar);
        if (path != null && !path.isEmpty()) {
            File f = new File(path);
            if (f.exists()) {
                System.setProperty(sysProp, path);
                log.debug("Установлен драйвер из {}: {}", envVar, path);
            } else {
                log.warn("Путь к драйверу в {} указан, но файл не найден: {}", envVar, path);
            }
        }
    }

    private void addSitesToCryptoProTrusted() {
        String[] trustedSites = {
                "https://cryptopro.ru",
                "https://cryptopro.ru/sites/default/files/products/cades/demopage/"
        };
        for (String site : trustedSites) {
            addSiteToCryptoProTrusted(site);
        }
    }

    private void addSiteToCryptoProTrusted(String url) {
        try {
            String command = String.format(
                    "reg add \"HKCU\\Software\\Crypto Pro\\ETOKEN\\Settings\\TrustedSites\" " +
                            "/v \"%s\" /d \"%s\" /f", url, url);
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.debug("Добавлен доверенный сайт: {}", url);
            }
        } catch (Exception e) {
            log.trace("Ошибка добавления сайта в доверенные (это не критично): {}", e.getMessage());
        }
    }
}