package com.bft.browser.factory;

/**
 * Менеджер фабрик браузеров
 */
public class BrowserFactoryManager {

    private final String cryptoProPath;
    private final String cryptoProXpiPath;
    private final boolean isRemote;

    public BrowserFactoryManager(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        this.cryptoProPath = cryptoProPath;
        this.cryptoProXpiPath = cryptoProXpiPath;
        this.isRemote = isRemote;
    }

    /**
     * Создает фабрику браузера на основе типа
     */
    public BrowserConfigFactory createFactory(String browserType) {
        if (browserType == null) {
            throw new IllegalArgumentException("Browser type cannot be null");
        }

        String browser = browserType.toLowerCase().trim();

        switch (browser) {
            case "chrome":
                return new ChromeBrowserFactory(cryptoProPath, cryptoProXpiPath, isRemote);
            case "yandex":
                return new YandexBrowserFactory(cryptoProPath, cryptoProXpiPath, isRemote);
            case "firefox":
            case "mozilla":
                return new FirefoxBrowserFactory(cryptoProPath, cryptoProXpiPath, isRemote);
            default:
                throw new IllegalArgumentException("Unsupported browser type: " + browserType +
                    ". Supported browsers: chrome, yandex, firefox");
        }
    }

    /**
     * Получает фабрику браузера по умолчанию (Chrome)
     */
    public BrowserConfigFactory getDefaultFactory() {
        return createFactory("chrome");
    }

    /**
     * Проверяет, поддерживается ли указанный браузер
     */
    public boolean isBrowserSupported(String browserType) {
        if (browserType == null) {
            return false;
        }

        String browser = browserType.toLowerCase().trim();
        return "chrome".equals(browser) || "yandex".equals(browser) ||
               "firefox".equals(browser) || "mozilla".equals(browser);
    }
}