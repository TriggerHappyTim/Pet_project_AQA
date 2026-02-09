package com.bft.browser.factory;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

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

        // VNC and Video capabilities работают только с Selenoid
        // Для стандартного Selenium Grid эти capabilities не нужны
        String remoteUrl = System.getProperty("selenide.remote", "");
        if (isRemote && remoteUrl.contains("selenoid")) {
            // Используем Selenoid capabilities только если URL содержит "selenoid"
            capabilities.setCapability("enableVNC", true);
            capabilities.setCapability("enableVideo", false);
        }
        // Для стандартного Selenium Grid (chrome, firefox) capabilities не добавляем

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

    /**
     * Кодирует файл расширения в base64 для использования в удаленном режиме
     * 
     * @param extensionPath путь к файлу расширения (.crx для Chrome, .xpi для Firefox)
     * @return строка base64 или null, если файл не найден или произошла ошибка
     */
    protected String encodeExtensionToBase64(String extensionPath) {
        if (extensionPath == null || extensionPath.isEmpty()) {
            return null;
        }

        // Сначала проверяем переменную окружения CRYPTOPRO_BASE64
        String encodedFromEnv = System.getenv("CRYPTOPRO_BASE64");
        if (encodedFromEnv != null && !encodedFromEnv.isEmpty()) {
            System.out.println("Используется расширение из переменной окружения CRYPTOPRO_BASE64");
            return encodedFromEnv;
        }

        // Если переменной нет, пытаемся закодировать файл
        if (!isExtensionFileExists(extensionPath)) {
            System.err.println("Файл расширения не найден: " + extensionPath);
            return null;
        }

        try {
            File extensionFile = new File(extensionPath);
            byte[] fileContent = Files.readAllBytes(extensionFile.toPath());
            String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
            System.out.println("Расширение успешно закодировано в base64: " + extensionPath);
            return base64Encoded;
        } catch (IOException e) {
            System.err.println("Ошибка при кодировании расширения в base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}