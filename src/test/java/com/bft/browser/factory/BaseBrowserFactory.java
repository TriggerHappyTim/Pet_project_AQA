package com.bft.browser.factory;

import org.openqa.selenium.Capabilities;
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

    // МЕТОД ИЗ ИНТЕРФЕЙСА - ДОЛЖЕН БЫТЬ АБСТРАКТНЫМ,
    // так как каждая фабрика реализует его по-своему
    @Override
    public abstract Capabilities getCapabilities();

    // МЕТОД ИЗ ИНТЕРФЕЙСА - ДОЛЖЕН БЫТЬ АБСТРАКТНЫМ,
    // так как каждая фабрика возвращает свое имя браузера
    @Override
    public abstract String getBrowserName();

    // МЕТОД ИЗ ИНТЕРФЕЙСА - базовая реализация пустая
    @Override
    public void configureSpecificOptions() {
        // Базовая реализация - ничего не делает
        // Конкретные фабрики могут переопределить
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

        // Для удаленного режима устанавливаем pageLoadStrategy "none"
        // чтобы вообще не ждать загрузки страницы и сразу переходить к взаимодействию с элементами
        // Это помогает при проблемах с зависанием страницы при загрузке ресурсов
        if (isRemote) {
            capabilities.setCapability("pageLoadStrategy", "none");
        }

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
     * Проверяет доступность расширения КриптоПРО (файл или env CRYPTOPRO_BASE64)
     *
     * @return true если расширение доступно любым способом
     */
    public boolean isCryptoProExtensionAvailable() {
        if (isExtensionFileExists(cryptoProPath)) {
            return true;
        }
        if (isExtensionFileExists(cryptoProXpiPath)) {
            return true;
        }
        String base64 = System.getenv("CRYPTOPRO_BASE64");
        return base64 != null && !base64.isEmpty();
    }

    /**
     * Выводит предупреждение с инструкцией при отсутствии файла расширения
     */
    protected void logMissingExtensionWarning(String path, String browser) {
        System.err.println("╔══════════════════════════════════════════════════════════════╗");
        System.err.println("║  ВНИМАНИЕ: Расширение КриптоПРО НЕ НАЙДЕНО для " + browser);
        System.err.println("║  Ожидаемый путь: " + (path != null ? new File(path).getAbsolutePath() : "не задан"));
        System.err.println("║");
        System.err.println("║  Тесты КриптоПРО будут ПРОПУЩЕНЫ.");
        System.err.println("║");
        System.err.println("║  Как исправить:");
        System.err.println("║  1. Скачайте расширение 'Extension for CAdES Browser Plug-in'");
        System.err.println("║     из Chrome Web Store (ID: pfhgbfnnjiafkhfdkmpiflachepdcjod)");
        System.err.println("║  2. Сохраните .crx файл в: src/test/resources/");
        System.err.println("║  3. Или задайте env: CRYPTOPRO_PATH=<путь к .crx>");
        System.err.println("║  4. Или задайте env: CRYPTOPRO_BASE64=<base64 расширения>");
        System.err.println("╚══════════════════════════════════════════════════════════════╝");
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