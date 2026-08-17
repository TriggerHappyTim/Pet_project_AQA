package com.bft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Конфигурационные данные для тестового окружения.
 * Инкапсулирует все настройки, необходимые для выполнения тестов.
 *
 * <p>Приоритет загрузки настроек:
 * 1. Системные свойства ({@code -Dproperty=value})
 * 2. Переменные окружения
 * 3. Значения по умолчанию
 */
public class TestConfig {

    private static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    // Допустимые значения для окружения и браузеров
    private static final List<String> VALID_ENVIRONMENTS = Arrays.asList("dev", "test", "uat", "prod", "int");
    private static final List<String> VALID_BROWSERS = Arrays.asList("chrome", "firefox", "yandex", "edge");

    // Browser settings
    private String browser;
    private String browserVersion;
    private boolean headless;
    private boolean remote;
    private String remoteUrl;

    // Paths
    private String cryptoProPath;
    private String cryptoProXpiPath;

    // Environment
    private String environment;
    private String testSuite;

    // Remote settings
    private boolean enableVNC;
    private boolean enableVideo;

    /**
     * Создает новую конфигурацию с настройками по умолчанию.
     * Загружает значения из системных свойств и переменных окружения.
     */
    public TestConfig() {
        // --- Browser Settings ---
        this.browser = getSystemOrEnvProperty("selenide.browser", "BROWSER", "chrome");
        this.browserVersion = getSystemOrEnvProperty("selenide.browserVersion", "BROWSER_VERSION", null);
        this.headless = Boolean.parseBoolean(
                getSystemOrEnvProperty("selenide.headless", "HEADLESS", "false")
        );

        String rUrl = getSystemOrEnvProperty("selenide.remote", "SELENIDE_REMOTE", null);
        this.remote = rUrl != null && !rUrl.trim().isEmpty();
        this.remoteUrl = rUrl;

        // --- CryptoPro Paths ---
        // Пути по умолчанию, если не заданы
        String defaultCrx = "src/test/resources/CryptoPro Chrome 1.2.13.0.crx";
        String defaultXpi = "src/test/resources/cryptopro.ru.xpi";

        this.cryptoProPath = getSystemOrEnvProperty(null, "CRYPTOPRO_PATH", defaultCrx);
        this.cryptoProXpiPath = getSystemOrEnvProperty(null, "CRYPTOPRO_XPI_PATH", defaultXpi);

        // --- Environment ---
        String env = getSystemOrEnvProperty("environment", "ENVIRONMENT", "dev");
        if (!VALID_ENVIRONMENTS.contains(env.toLowerCase())) {
            log.warn("Неизвестное окружение '{}'. Используется 'dev'. Допустимые: {}", env, VALID_ENVIRONMENTS);
            this.environment = "dev";
        } else {
            this.environment = env;
        }

        this.testSuite = getSystemOrEnvProperty("suiteXmlFileName", "TEST_SUITE", "smoke");

        // --- Remote Settings ---
        this.enableVNC = Boolean.parseBoolean(
                getSystemOrEnvProperty(null, "ENABLE_VNC", "true")
        );
        this.enableVideo = Boolean.parseBoolean(
                getSystemOrEnvProperty(null, "ENABLE_VIDEO", "false")
        );

        // --- Validation & Logging ---
        validateAndLog();
    }

    /**
     * Вспомогательный метод для получения свойства: System Property -> Env Variable -> Default.
     */
    private String getSystemOrEnvProperty(String sysPropKey, String envVarKey, String defaultValue) {
        String value = null;
        if (sysPropKey != null) {
            value = System.getProperty(sysPropKey);
        }
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVarKey);
        }
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Валидация критических настроек и логирование конфигурации.
     */
    private void validateAndLog() {
        if (!VALID_BROWSERS.contains(browser.toLowerCase())) {
            log.warn("Неподдерживаемый браузер '{}'. Будет использован как есть, но возможны ошибки.", browser);
        }

        // Проверка наличия расширения только если не задан BASE64
        String base64Extension = System.getenv("CRYPTOPRO_BASE64");
        if (base64Extension == null || base64Extension.isEmpty()) {
            boolean crxExists = new File(cryptoProPath).exists();
            boolean xpiExists = new File(cryptoProXpiPath).exists();

            if (!crxExists && !xpiExists) {
                log.warn("Файлы расширений КриптоПРО не найдены ни по пути CRX ({}), ни по пути XPI ({}). " +
                        "Убедитесь, что путь верен, или задайте переменную CRYPTOPRO_BASE64.", cryptoProPath, cryptoProXpiPath);
            } else {
                log.debug("Расширение КриптоПРО найдено: CRX={}, XPI={}", crxExists, xpiExists);
            }
        } else {
            log.info("Обнаружена переменная CRYPTOPRO_BASE64 (длина: {} симв.). Проверка файлов расширения пропускается.", base64Extension.length());
        }

        log.info("Конфигурация тестов инициализирована: Browser={}, Env={}, Remote={}", browser, environment, remote);
        if (remote) {
            log.info("Remote URL: {}", remoteUrl);
        }
    }

    // ================= Getters =================

    public String getBrowser() { return browser; }
    public String getBrowserVersion() { return browserVersion; }
    public boolean isHeadless() { return headless; }
    public boolean isRemote() { return remote; }
    public String getRemoteUrl() { return remoteUrl; }
    public String getCryptoProPath() { return cryptoProPath; }
    public String getCryptoProXpiPath() { return cryptoProXpiPath; }
    public String getEnvironment() { return environment; }
    public String getTestSuite() { return testSuite; }
    public boolean isEnableVNC() { return enableVNC; }
    public boolean isEnableVideo() { return enableVideo; }

    // ================= Setters (Fluent API) =================

    public TestConfig setBrowser(String browser) {
        this.browser = Objects.requireNonNull(browser, "Browser cannot be null");
        return this;
    }

    public TestConfig setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
        return this;
    }

    public TestConfig setHeadless(boolean headless) {
        this.headless = headless;
        return this;
    }

    public TestConfig setRemote(boolean remote) {
        this.remote = remote;
        if (!remote) {
            this.remoteUrl = null;
        }
        return this;
    }

    public TestConfig setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        this.remote = (remoteUrl != null && !remoteUrl.trim().isEmpty());
        return this;
    }

    public TestConfig setCryptoProPath(String cryptoProPath) {
        this.cryptoProPath = cryptoProPath;
        return this;
    }

    public TestConfig setCryptoProXpiPath(String cryptoProXpiPath) {
        this.cryptoProXpiPath = cryptoProXpiPath;
        return this;
    }

    public TestConfig setEnvironment(String environment) {
        if (environment != null && !VALID_ENVIRONMENTS.contains(environment.toLowerCase())) {
            log.warn("Попытка установить недопустимое окружение: {}. Будет использовано 'dev'.", environment);
            this.environment = "dev";
        } else {
            this.environment = environment;
        }
        return this;
    }

    public TestConfig setTestSuite(String testSuite) {
        this.testSuite = testSuite;
        return this;
    }

    public TestConfig setEnableVNC(boolean enableVNC) {
        this.enableVNC = enableVNC;
        return this;
    }

    public TestConfig setEnableVideo(boolean enableVideo) {
        this.enableVideo = enableVideo;
        return this;
    }

    @Override
    public String toString() {
        return "TestConfig{" +
                "browser='" + browser + '\'' +
                ", version='" + browserVersion + '\'' +
                ", headless=" + headless +
                ", remote=" + remote +
                ", env='" + environment + '\'' +
                ", suite='" + testSuite + '\'' +
                ", vnc=" + enableVNC +
                ", video=" + enableVideo +
                '}';
    }
}