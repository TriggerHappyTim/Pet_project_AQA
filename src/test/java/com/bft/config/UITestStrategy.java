package com.bft.config;

import com.bft.pw.PwSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Стратегия для UI тестирования с использованием браузеров.
 * Настраивает Playwright-сессию через {@link PwSession}.
 */
public class UITestStrategy {

    private static final Logger log = LoggerFactory.getLogger(UITestStrategy.class);

    private final TestConfig config;
    private final String browser;
    private final boolean isRemote;
    private final boolean headless;

    public UITestStrategy(TestConfig config) {
        this.config = config;
        this.browser = config.getBrowser();
        this.isRemote = config.isRemote();
        this.headless = config.isHeadless();
    }

    /**
     * Настройка переменных окружения (путей к драйверам).
     * Для Playwright не требуется — драйверы встроены.
     */
    public void setupEnvironment() {
        if (isRemote) {
            log.warn("Remote execution (Selenoid/Selenium Grid) не поддерживается Playwright напрямую. " +
                    "Будет запущен локальный браузер '{}'.", browser);
        }
        log.info("Playwright использует встроенные браузеры, внешние драйверы не требуются.");
    }

    /**
     * Основная настройка Playwright-сессии.
     */
    public void configureSelenide() {
        try {
            PwSession.configure(config);
            log.info("Playwright-сессия настроена: Browser={} (headless={})", browser, headless);
        } catch (Exception e) {
            log.error("Ошибка настройки браузера: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure browser: " + browser, e);
        }
    }

    /**
     * Действия перед каждым тестом (открытие браузера).
     */
    public void beforeTest() {
        com.bft.pw.Selenide.open();
        if (!headless) {
            PwSession.driver().manage().window().maximize();
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            addSitesToCryptoProTrusted();
        }
    }

    /**
     * Действия после каждого теста (закрытие браузера).
     */
    public void afterTest() {
        PwSession.close();
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