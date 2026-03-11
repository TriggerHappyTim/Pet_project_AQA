package com.bft.config;

import com.bft.browser.factory.BrowserConfigFactory;
import com.bft.browser.factory.BrowserFactoryManager;
import com.codeborne.selenide.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

/**
 * Стратегия для UI тестирования с использованием браузеров
 * 
 * Реализует стратегию тестирования пользовательского интерфейса через
 * автоматизацию браузеров с поддержкой КриптоПРО и других расширений.
 * 
 * <p>Основные функции:
 * <ul>
 *   <li>Настройка браузеров (Chrome, Firefox, Yandex) с поддержкой КриптоПРО</li>
 *   <li>Конфигурация Selenide для UI тестов</li>
 *   <li>Управление жизненным циклом браузера (открытие/закрытие)</li>
 *   <li>Настройка доверенных сайтов для КриптоПРО (Windows)</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * TestConfig config = new TestConfig()
 *     .setBrowser("chrome")
 *     .setHeadless(false);
 * 
 * UITestStrategy strategy = new UITestStrategy(config);
 * strategy.configureEnvironment();
 * strategy.configureSelenide();
 * strategy.beforeTest();
 * // выполнение UI тестов
 * strategy.afterTest();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseTestStrategy для базовой реализации
 * @see TestStrategy для интерфейса стратегий
 * @see BrowserFactoryManager для управления фабриками браузеров
 * @since 1.0
 */
public class UITestStrategy extends BaseTestStrategy {

    private static final Logger log = LoggerFactory.getLogger(UITestStrategy.class);

    /**
     * Менеджер фабрик браузеров для создания и настройки браузеров
     */
    private BrowserFactoryManager browserFactoryManager;
    
    /**
     * Фабрика конфигурации браузера для текущего типа браузера
     */
    private BrowserConfigFactory browserFactory;

    /**
     * Создает UI стратегию с указанной конфигурацией
     * 
     * @param config конфигурация тестового окружения с настройками браузера
     *               не должна быть null
     * @throws NullPointerException если config равен null
     */
    public UITestStrategy(TestConfig config) {
        super(config);
    }

    /**
     * Возвращает тип стратегии - UI
     * 
     * @return {@link TestStrategyType#UI}
     * @see TestStrategy#getType()
     */
    @Override
    public TestStrategyType getType() {
        return TestStrategyType.UI;
    }

    /**
     * Проверяет, применима ли UI стратегия для текущего окружения
     * 
     * Проверяет доступность указанного браузера и возможность создания
     * фабрики браузера с поддержкой КриптоПРО.
     * 
     * @return true если браузер доступен и может быть настроен, false в противном случае
     * @see TestStrategy#isApplicable()
     */
    @Override
    public boolean isApplicable() {
        // UI стратегия применима, если есть поддерживаемый браузер
        return checkEnvironmentAvailability();
    }

    /**
     * Возвращает приоритет UI стратегии
     * 
     * UI стратегия имеет высокий приоритет (10), так как обычно предпочтительнее
     * для тестирования пользовательского интерфейса.
     * 
     * @return приоритет стратегии (10)
     * @see TestStrategy#getPriority()
     */
    @Override
    public int getPriority() {
        return 10; // Высокий приоритет для UI тестов
    }

    /**
     * Настраивает переменные окружения для UI тестов
     * 
     * Устанавливает пути к драйверам браузеров в системных свойствах
     * на основе типа браузера из конфигурации.
     * 
     * @see BaseTestStrategy#setupEnvironmentVariables()
     */
    @Override
    protected void setupEnvironmentVariables() {
        // Настройка путей к драйверам браузеров
        setupBrowserDrivers();
    }

    /**
     * Выполняет действия перед каждым UI тестом
     * 
     * Выполняет следующие действия:
     * <ol>
     *   <li>Открывает браузер через Selenide</li>
     *   <li>Максимизирует окно браузера (если не headless режим)</li>
     *   <li>Добавляет доверенные сайты КриптоПРО в реестр Windows</li>
     * </ol>
     * 
     * @see BaseTestStrategy#performPreTestActions()
     */
    @Override
    protected void performPreTestActions() {
        // Открываем браузер
        open();

        // Максимзируем окно, если не headless
        if (!headless) {
            WebDriverRunner.getWebDriver().manage().window().maximize();
        }

        // Добавляем сайты в доверенные (только для Windows)
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            addSitesToCryptoProTrusted();
        }
    }

    /**
     * Выполняет действия после каждого UI теста
     * 
     * Закрывает браузер и освобождает ресурсы через Selenide.
     * 
     * @see BaseTestStrategy#performPostTestActions()
     */
    @Override
    protected void performPostTestActions() {
        // Закрываем браузер
        closeWebDriver();
    }

    /**
     * Проверяет доступность окружения для UI тестов
     * 
     * Проверяет возможность создания фабрики браузера и доступность
     * указанного браузера с поддержкой КриптоПРО.
     * 
     * @return true если браузер доступен и может быть настроен, false в противном случае
     * @see BaseTestStrategy#checkEnvironmentAvailability()
     */
    @Override
    protected boolean checkEnvironmentAvailability() {
        try {
            browserFactoryManager = new BrowserFactoryManager(cryptoProPath, cryptoProXpiPath, isRemote);
            browserFactory = browserFactoryManager.createFactory(browser);
            return browserFactory.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Настраивает Selenide Configuration для UI тестов
     * 
     * Выполняет базовую настройку через {@link BaseTestStrategy#configureSelenide()},
     * затем добавляет специфичные настройки для UI тестов:
     * <ul>
     *   <li>Capabilities браузера с поддержкой КриптоПРО</li>
     *   <li>Имя браузера из фабрики</li>
     *   <li>Настройки записи видео для удаленного запуска</li>
     * </ul>
     * 
     * @throws RuntimeException если не удалось создать или настроить браузер
     * @see BaseTestStrategy#configureSelenide()
     */
    @Override
    public void configureSelenide() {
        super.configureSelenide();

        // Дополнительная настройка для UI тестов
        try {
            // Инициализируем browserFactoryManager если он еще не инициализирован
            if (browserFactoryManager == null) {
                log.info("Инициализация BrowserFactoryManager для браузера: " + browser);
                browserFactoryManager = new BrowserFactoryManager(cryptoProPath, cryptoProXpiPath, isRemote);
            }
            
            // Инициализируем browserFactory если он еще не инициализирован
            if (browserFactory == null) {
                log.info("Создание BrowserConfigFactory для браузера: " + browser);
                browserFactory = browserFactoryManager.createFactory(browser);
                
                if (browserFactory == null) {
                    throw new IllegalStateException("Не удалось создать BrowserConfigFactory для браузера: " + browser);
                }
            }
            
            Capabilities capabilities = browserFactory.getCapabilities();
            if (capabilities == null) {
                throw new IllegalStateException("BrowserConfigFactory вернул null capabilities для браузера: " + browser);
            }
            
            // Для FirefoxOptions используем напрямую, без обертки в MutableCapabilities
            // Это избегает проблем с NullPointerException при слиянии в Selenide
            if (capabilities instanceof org.openqa.selenium.firefox.FirefoxOptions) {
                Configuration.browserCapabilities = (org.openqa.selenium.firefox.FirefoxOptions) capabilities;
            } else {
                // Для других браузеров (Chrome) используем MutableCapabilities
                Configuration.browserCapabilities = new MutableCapabilities(capabilities);
            }
            
            // Получаем имя браузера с проверкой на null
            String browserName = browserFactory.getBrowserName();
            if (browserName == null || browserName.isEmpty()) {
                log.warn("Предупреждение: BrowserConfigFactory вернул пустое имя браузера, используем значение из конфигурации: " + browser);
                browserName = browser;
            }
            Configuration.browser = browserName;
            
            log.info("Браузер настроен: " + browserName + " (запрошен: " + browser + ")");

            // Настройки для удаленного запуска с Selenoid (videoName работает только с Selenoid)
            // Для стандартного Selenium Grid videoName не поддерживается и вызывает ошибку
            String remoteUrl = System.getProperty("selenide.remote", "");
            if (isRemote && remoteUrl.contains("selenoid")) {
                Configuration.browserCapabilities.setCapability("videoName",
                        browser + "_" + System.currentTimeMillis() + ".mp4");
                log.info("Настроена запись видео для удаленного запуска (Selenoid)");
            }

        } catch (IllegalStateException e) {
            log.warn("Ошибка состояния при настройке браузера: " + e.getMessage());
            throw new RuntimeException("Failed to configure browser: " + browser + ". " + e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Неожиданная ошибка при настройке браузера: " + e.getMessage());
            log.debug("Ошибка настройки браузера", e);
            throw new RuntimeException("Failed to configure browser: " + browser, e);
        }
    }

    /**
     * Настраивает пути к драйверам браузеров в системных свойствах
     * 
     * Устанавливает системные свойства webdriver.*.driver в зависимости от типа браузера.
     * Значения берутся из переменных окружения (CHROME_DRIVER_PATH, GECKO_DRIVER_PATH и т.д.).
     */
    private void setupBrowserDrivers() {
        // Настройка путей к драйверам
        switch (browser.toLowerCase()) {
            case "chrome":
                String chromeDriverPath = System.getenv("CHROME_DRIVER_PATH");
                if (chromeDriverPath != null) {
                    System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                }
                break;
            case "firefox":
                String geckoDriverPath = System.getenv("GECKO_DRIVER_PATH");
                if (geckoDriverPath != null) {
                    System.setProperty("webdriver.gecko.driver", geckoDriverPath);
                }
                break;
            case "yandex":
                String yandexDriverPath = System.getenv("YANDEX_DRIVER_PATH");
                if (yandexDriverPath != null) {
                    System.setProperty("webdriver.chrome.driver", yandexDriverPath);
                }
                break;
        }
    }

    /**
     * Добавляет список доверенных сайтов КриптоПРО в реестр Windows
     * 
     * Вызывается только на Windows системах перед запуском тестов.
     * Добавляет сайты КриптоПРО в список доверенных для корректной работы
     * расширения в браузере.
     */
    private void addSitesToCryptoProTrusted() {
        // Добавляем доверенные сайты в реестр Windows
        String[] trustedSites = {
            "https://cryptopro.ru",
            "https://cryptopro.ru/sites/default/files/products/cades/demopage/"
        };

        for (String site : trustedSites) {
            addSiteToCryptoProTrusted(site);
        }
    }

    /**
     * Добавляет один сайт в список доверенных КриптоПРО через реестр Windows
     * 
     * Выполняет команду reg add для добавления сайта в реестр Windows.
     * Ошибки логируются, но не прерывают выполнение тестов.
     * 
     * @param url URL сайта для добавления в доверенные
     */
    private void addSiteToCryptoProTrusted(String url) {
        try {
            String command = String.format(
                    "reg add \"HKCU\\Software\\Crypto Pro\\ETOKEN\\Settings\\TrustedSites\" " +
                            "/v \"%s\" /d \"%s\" /f", url, url);

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Добавлен доверенный сайт: " + url);
            } else {
                log.warn("Не удалось добавить доверенный сайт: " + url);
            }
        } catch (Exception e) {
            log.warn("Ошибка добавления сайта в доверенные: " + e.getMessage());
        }
    }
}