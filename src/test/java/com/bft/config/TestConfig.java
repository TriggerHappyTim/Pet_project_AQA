package com.bft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Конфигурационные данные для тестового окружения
 * 
 * Инкапсулирует все настройки, необходимые для выполнения тестов:
 * - Настройки браузера (тип, версия, headless режим)
 * - Пути к расширениям КриптоПРО
 * - Параметры окружения (dev, test, uat, prod)
 * - Настройки удаленного запуска (Selenium Grid, VNC, Video)
 * 
 * <p>Конфигурация загружается из системных свойств и переменных окружения
 * с приоритетом: системные свойства > переменные окружения > значения по умолчанию.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Создание конфигурации с настройками по умолчанию
 * TestConfig config = new TestConfig();
 * 
 * // Программная настройка через fluent API
 * TestConfig customConfig = new TestConfig()
 *     .setBrowser("chrome")
 *     .setHeadless(true)
 *     .setEnvironment("uat");
 * 
 * // Использование в TestConfiguration
 * TestConfiguration.initialize(customConfig);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see TestConfiguration для инициализации с использованием TestConfig
 * @see TestContext для хранения конфигурации в контексте тестов
 * @since 1.0
 */
public class TestConfig {

    private static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    // Browser settings
    private String browser;
    private String browserVersion;
    private boolean headless;
    private boolean remote;

    // Paths
    private String cryptoProPath;
    private String cryptoProXpiPath;

    // Environment
    private String environment;
    private String testSuite;

    // Remote settings
    private String remoteUrl;
    private boolean enableVNC;
    private boolean enableVideo;

    /**
     * Создает новую конфигурацию с настройками по умолчанию
     * 
     * Загружает значения из системных свойств и переменных окружения.
     * Если значение не найдено, используются значения по умолчанию:
     * - Браузер: "chrome"
     * - Headless: false
     * - Окружение: "dev"
     * - Test Suite: "smoke"
     * 
     * <p>Поддерживаемые системные свойства:
     * <ul>
     *   <li>selenide.browser - тип браузера</li>
     *   <li>selenide.browserVersion - версия браузера</li>
     *   <li>selenide.headless - режим headless (true/false)</li>
     *   <li>selenide.remote - URL удаленного Selenium сервера</li>
     *   <li>environment - окружение (dev, test, uat, prod)</li>
     *   <li>suiteXmlFileName - название тестового suite</li>
     * </ul>
     * 
     * <p>Поддерживаемые переменные окружения:
     * <ul>
     *   <li>BROWSER - тип браузера</li>
     *   <li>BROWSER_VERSION - версия браузера</li>
     *   <li>HEADLESS - режим headless</li>
     *   <li>SELENIDE_REMOTE - URL удаленного сервера</li>
     *   <li>ENVIRONMENT - окружение</li>
     *   <li>TEST_SUITE - название suite</li>
     *   <li>CRYPTOPRO_PATH - путь к расширению КриптоПРО</li>
     *   <li>ENABLE_VNC - включить VNC для удаленного запуска</li>
     *   <li>ENABLE_VIDEO - включить запись видео</li>
     * </ul>
     */
    public TestConfig() {
        // Default values from system properties and environment variables
        this.browser = System.getProperty("selenide.browser",
                System.getenv().getOrDefault("BROWSER", "chrome"));
        this.browserVersion = System.getProperty("selenide.browserVersion",
                System.getenv().getOrDefault("BROWSER_VERSION", null));
        this.headless = Boolean.parseBoolean(System.getProperty("selenide.headless",
                System.getenv().getOrDefault("HEADLESS", "false")));
        // Only consider remote if there's an actual remote URL configured
        String remoteUrl = System.getProperty("selenide.remote",
                System.getenv().getOrDefault("SELENIDE_REMOTE", null));
        this.remote = remoteUrl != null && !remoteUrl.trim().isEmpty();

        // CryptoPro paths
        this.cryptoProPath = System.getenv().getOrDefault("CRYPTOPRO_PATH",
                "src/test/resources/CryptoPro Chrome 1.2.13.0.crx");
        this.cryptoProXpiPath = System.getenv().getOrDefault("CRYPTOPRO_XPI_PATH",
                "src/test/resources/cryptopro.ru.xpi");

        if (!new java.io.File(this.cryptoProPath).exists() &&
            !new java.io.File(this.cryptoProXpiPath).exists() &&
            (System.getenv("CRYPTOPRO_BASE64") == null || System.getenv("CRYPTOPRO_BASE64").isEmpty())) {
            log.warn("[TestConfig] Расширение КриптоПРО не найдено. Задайте CRYPTOPRO_PATH, CRYPTOPRO_XPI_PATH или CRYPTOPRO_BASE64. Тесты группы 'crypto' будут пропущены.");
        }

        // Environment settings
        this.environment = System.getProperty("environment",
                System.getenv().getOrDefault("ENVIRONMENT", "dev"));
        this.testSuite = System.getProperty("suiteXmlFileName",
                System.getenv().getOrDefault("TEST_SUITE", "smoke"));

        // Remote settings
        this.remoteUrl = System.getProperty("selenide.remote",
                System.getenv().getOrDefault("SELENIDE_REMOTE", null));
        this.enableVNC = Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_VNC", "true"));
        this.enableVideo = Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_VIDEO", "false"));
    }

    // Getters
    
    /**
     * Возвращает тип браузера для тестов
     * 
     * @return название браузера (например, "chrome", "firefox", "yandex")
     */
    public String getBrowser() { return browser; }
    
    /**
     * Возвращает версию браузера
     * 
     * @return версия браузера или null если не указана
     */
    public String getBrowserVersion() { return browserVersion; }
    
    /**
     * Проверяет, включен ли headless режим
     * 
     * @return true если браузер запускается в headless режиме, false в противном случае
     */
    public boolean isHeadless() { return headless; }
    
    /**
     * Проверяет, используется ли удаленный Selenium сервер
     * 
     * @return true если указан URL удаленного сервера, false для локального запуска
     */
    public boolean isRemote() { return remote; }
    
    /**
     * Возвращает путь к расширению КриптоПРО для Chrome
     * 
     * @return абсолютный или относительный путь к .crx файлу
     */
    public String getCryptoProPath() { return cryptoProPath; }
    
    /**
     * Возвращает путь к расширению КриптоПРО для Firefox
     * 
     * @return абсолютный или относительный путь к .xpi файлу
     */
    public String getCryptoProXpiPath() { return cryptoProXpiPath; }
    
    /**
     * Возвращает название окружения
     * 
     * @return окружение (dev, test, uat, prod)
     */
    public String getEnvironment() { return environment; }
    
    /**
     * Возвращает название тестового suite
     * 
     * @return название suite (smoke, regression, sanity и т.д.)
     */
    public String getTestSuite() { return testSuite; }
    
    /**
     * Возвращает URL удаленного Selenium сервера
     * 
     * @return URL сервера или null если используется локальный запуск
     */
    public String getRemoteUrl() { return remoteUrl; }
    
    /**
     * Проверяет, включен ли VNC для удаленного запуска
     * 
     * @return true если VNC включен, false в противном случае
     */
    public boolean isEnableVNC() { return enableVNC; }
    
    /**
     * Проверяет, включена ли запись видео для удаленного запуска
     * 
     * @return true если запись видео включена, false в противном случае
     */
    public boolean isEnableVideo() { return enableVideo; }

    // Setters for programmatic configuration
    
    /**
     * Устанавливает тип браузера для тестов
     * 
     * @param browser название браузера (chrome, firefox, yandex и т.д.)
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    /**
     * Устанавливает версию браузера
     * 
     * @param browserVersion версия браузера (например, "120", "latest")
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
        return this;
    }

    /**
     * Устанавливает headless режим
     * 
     * @param headless true для запуска браузера без GUI, false для обычного режима
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setHeadless(boolean headless) {
        this.headless = headless;
        return this;
    }

    /**
     * Устанавливает использование удаленного Selenium сервера
     * 
     * @param remote true для использования удаленного сервера, false для локального запуска
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setRemote(boolean remote) {
        this.remote = remote;
        return this;
    }

    /**
     * Устанавливает путь к расширению КриптоПРО для Chrome
     * 
     * @param cryptoProPath абсолютный или относительный путь к .crx файлу
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setCryptoProPath(String cryptoProPath) {
        this.cryptoProPath = cryptoProPath;
        return this;
    }

    /**
     * Устанавливает окружение для тестов
     * 
     * @param environment название окружения (dev, test, uat, prod)
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Устанавливает название тестового suite
     * 
     * @param testSuite название suite (smoke, regression, sanity и т.д.)
     * @return текущий экземпляр TestConfig для цепочки вызовов (fluent API)
     */
    public TestConfig setTestSuite(String testSuite) {
        this.testSuite = testSuite;
        return this;
    }

    /**
     * Возвращает строковое представление конфигурации
     * 
     * Используется для логирования и отладки. Включает основные параметры:
     * браузер, версию, режим headless, удаленный запуск, окружение и suite.
     * 
     * @return строковое представление конфигурации
     */
    @Override
    public String toString() {
        return "TestConfig{" +
                "browser='" + browser + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                ", headless=" + headless +
                ", remote=" + remote +
                ", environment='" + environment + '\'' +
                ", testSuite='" + testSuite + '\'' +
                '}';
    }
}