package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

/**
 * Базовый класс для фабрик браузеров
 */
public abstract class BaseBrowserFactory implements BrowserConfigFactory {

    protected final String cryptoProPath;
    protected final String cryptoProXpiPath;
    protected final boolean isRemote;

    protected BaseBrowserFactory(String cryptoProPath, String cryptoProXpiPath, boolean isRemote) {
        this.cryptoProPath = cryptoProPath;
        this.cryptoProXpiPath = cryptoProXpiPath;
        this.isRemote = isRemote;
    }

    @Override
    public boolean isAvailable() {
        if (isRemote) {
            return true; // В удаленном режиме браузер должен быть доступен на узле
        }
        return checkLocalBrowserAvailability();
    }

    /**
     * Проверяет доступность браузера в локальной среде
     */
    protected abstract boolean checkLocalBrowserAvailability();

    /**
     * Создает базовые capabilities
     */
    protected DesiredCapabilities createBaseCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        // VNC and Video capabilities are only for remote/Selenoid execution
        if (isRemote) {
            capabilities.setCapability("enableVNC", true);
            capabilities.setCapability("enableVideo", false);
        }

        return capabilities;
    }

    /**
     * Проверяет существование файла расширения
     */
    protected boolean isExtensionFileExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File extensionFile = new File(path);
        return extensionFile.exists() && extensionFile.isFile();
    }

    /**
     * Возвращает путь к расширению CryptoPro
     */
    protected String getCryptoProExtensionPath() {
        return cryptoProPath;
    }

    /**
     * Возвращает путь к XPI расширению для Firefox
     */
    protected String getCryptoProXpiPath() {
        return cryptoProXpiPath;
    }
}