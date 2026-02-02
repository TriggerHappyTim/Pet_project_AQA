package com.bft.config;

import com.bft.browser.factory.BrowserFactoryManager;

/**
 * Центральная точка управления конфигурацией тестового фреймворка
 * 
 * Предоставляет единый интерфейс для:
 * - Инициализации тестового окружения (UI/API)
 * - Управления стратегией тестирования ({@link TestStrategy})
 * - Доступа к конфигурации ({@link TestConfig})
 * - Управления lifecycle тестов (before/after hooks)
 * - Потокобезопасного доступа к SoftAssert
 * 
 * <p>Класс реализует паттерн Singleton с lazy initialization.
 * Все методы синхронизированы для потокобезопасности.
 * 
 * <p>Пример использования в BaseTest:
 * <pre>{@code
 * @BeforeSuite
 * public void setup() {
 *     // Автоматический выбор стратегии
 *     TestConfiguration.initialize();
 *     
 *     // Или явное указание
 *     TestConfiguration.initialize(TestStrategyType.UI);
 * }
 * 
 * @Test
 * public void myTest() {
 *     SoftAssert softAssert = TestConfiguration.getSoftAssert();
 *     TestConfig config = TestConfiguration.getCurrentConfig();
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see TestContext для хранения состояния конфигурации
 * @see TestStrategy для различных стратегий тестирования
 * @see TestConfig для настроек окружения
 * @since 1.0
 */
public class TestConfiguration {

    /**
     * Текущий контекст тестирования
     * Содержит активную стратегию, конфигурацию и состояние
     */
    private static TestContext currentContext;
    
    /**
     * Флаг инициализации конфигурации
     * volatile для видимости изменений между потоками
     */
    private static volatile boolean initialized = false;

    /**
     * Инициализирует тестовое окружение с автоматическим выбором стратегии
     * 
     * Выполняется один раз перед запуском всех тестов в suite.
     * Автоматически определяет стратегию тестирования (UI по умолчанию).
     * Повторные вызовы игнорируются (идемпотентность).
     * 
     * <p>Выполняемые действия:
     * <ol>
     *   <li>Создает {@link TestContext} с автоопределением стратегии</li>
     *   <li>Инициализирует окружение (Selenide, RestAssured и т.д.)</li>
     *   <li>Устанавливает флаг initialized = true</li>
     * </ol>
     * 
     * @throws RuntimeException если инициализация не удалась
     * @see #initialize(TestStrategyType) для явного указания стратегии
     * @see TestContext#initializeEnvironment() для деталей инициализации
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            currentContext = TestContext.create();
            currentContext.initializeEnvironment();
            initialized = true;

            System.out.println("Test configuration initialized successfully");

        } catch (Exception e) {
            System.err.println("Failed to initialize test configuration: " + e.getMessage());
            throw new RuntimeException("Test configuration initialization failed", e);
        }
    }

    /**
     * Инициализирует тестовое окружение с явно указанной стратегией
     * 
     * Используйте когда нужно принудительно задать стратегию тестирования.
     * Повторные вызовы игнорируются (идемпотентность).
     * 
     * @param strategyType тип стратегии тестирования из {@link TestStrategyType}
     *                     (UI для UI тестов, API для API тестов)
     * @throws RuntimeException если инициализация не удалась
     * @see TestStrategyType для доступных стратегий
     */
    public static synchronized void initialize(TestStrategyType strategyType) {
        if (initialized) {
            return;
        }

        try {
            currentContext = TestContext.create(strategyType);
            currentContext.initializeEnvironment();
            initialized = true;

            System.out.println("Test configuration initialized with strategy: " + strategyType);

        } catch (Exception e) {
            System.err.println("Failed to initialize test configuration: " + e.getMessage());
            throw new RuntimeException("Test configuration initialization failed", e);
        }
    }

    /**
     * Инициализирует тестовое окружение с кастомной конфигурацией
     * 
     * Позволяет полностью настроить тестовое окружение через объект {@link TestConfig}.
     * Используйте для специфичных сценариев тестирования.
     * 
     * @param config кастомная конфигурация с настройками браузера, URL, таймаутов и т.д.
     * @throws RuntimeException если инициализация не удалась
     * @see TestConfig для всех доступных настроек
     */
    public static synchronized void initialize(TestConfig config) {
        if (initialized) {
            return;
        }

        try {
            currentContext = TestContext.create(config);
            currentContext.initializeEnvironment();
            initialized = true;

            System.out.println("Test configuration initialized with custom config");

        } catch (Exception e) {
            System.err.println("Failed to initialize test configuration: " + e.getMessage());
            throw new RuntimeException("Test configuration initialization failed", e);
        }
    }

    /**
     * Возвращает текущий контекст тестирования
     * 
     * Контекст содержит активную стратегию, конфигурацию и состояние тестов.
     * 
     * @return объект {@link TestContext} с текущими настройками
     * @throws IllegalStateException если конфигурация не инициализирована
     * @see #initialize() для инициализации конфигурации
     */
    public static TestContext getCurrentContext() {
        if (!initialized || currentContext == null) {
            throw new IllegalStateException("Test configuration not initialized. Call initialize() first.");
        }
        return currentContext;
    }

    /**
     * Возвращает текущую стратегию тестирования
     * 
     * Стратегия определяет поведение тестов (UI или API).
     * 
     * @return объект {@link TestStrategy} (UITestStrategy или ApiTestStrategy)
     * @throws IllegalStateException если конфигурация не инициализирована
     * @see TestStrategy для методов стратегии
     */
    public static TestStrategy getCurrentStrategy() {
        return getCurrentContext().getStrategy();
    }

    /**
     * Возвращает текущую конфигурацию тестов
     * 
     * Конфигурация содержит настройки браузера, URL, таймауты, пути к КриптоПРО и т.д.
     * 
     * @return объект {@link TestConfig} с настройками окружения
     * @throws IllegalStateException если конфигурация не инициализирована
     * @see TestConfig для всех доступных настроек
     */
    public static TestConfig getCurrentConfig() {
        return getCurrentContext().getConfig();
    }

    /**
     * Возвращает потокобезопасный SoftAssert для текущего теста
     * 
     * SoftAssert позволяет накапливать ошибки проверок и выбросить их все в конце.
     * Создается новый экземпляр для каждого потока выполнения.
     * 
     * @return новый объект {@link org.testng.asserts.SoftAssert}
     * @throws IllegalStateException если конфигурация не инициализирована
     * @see org.testng.asserts.SoftAssert#assertAll() для финализации проверок
     */
    public static org.testng.asserts.SoftAssert getSoftAssert() {
        return getCurrentContext().getSoftAssert();
    }

    /**
     * Проверяет, является ли текущий тест UI тестом
     * 
     * @return true если используется UITestStrategy, false в противном случае
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    public static boolean isUITest() {
        return getCurrentContext().isUITest();
    }

    /**
     * Проверяет, является ли текущий тест API тестом
     * 
     * @return true если используется ApiTestStrategy, false в противном случае
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    public static boolean isApiTest() {
        return getCurrentContext().isApiTest();
    }

    /**
     * Возвращает менеджер фабрик браузеров (только для UI тестов)
     * 
     * BrowserFactoryManager управляет созданием и настройкой браузеров
     * с поддержкой КриптоПРО и других расширений.
     * 
     * @return объект {@link BrowserFactoryManager} для управления браузерами
     * @throws IllegalStateException если вызван для API тестов или конфигурация не инициализирована
     * @see BrowserFactoryManager для работы с различными браузерами
     */
    public static BrowserFactoryManager getBrowserFactoryManager() {
        if (!isUITest()) {
            throw new IllegalStateException("Browser factory manager is only available for UI tests");
        }

        TestConfig config = getCurrentConfig();
        return new BrowserFactoryManager(
                config.getCryptoProPath(),
                config.getCryptoProXpiPath(),
                config.isRemote()
        );
    }

    /**
     * Выполняет действия перед каждым тестом
     * 
     * Вызывается из {@link org.testng.annotations.BeforeTest}.
     * Делегирует выполнение текущей стратегии тестирования.
     * 
     * <p>Типичные действия:
     * <ul>
     *   <li>UI: настройка браузера, очистка cookies</li>
     *   <li>API: подготовка базовых спецификаций запросов</li>
     * </ul>
     * 
     * @see TestStrategy#beforeTest() для деталей реализации
     */
    public static void beforeTest() {
        if (currentContext != null) {
            currentContext.beforeTest();
        }
    }

    /**
     * Выполняет действия после каждого теста
     * 
     * Вызывается из {@link org.testng.annotations.AfterMethod}.
     * Делегирует выполнение текущей стратегии тестирования.
     * 
     * <p>Типичные действия:
     * <ul>
     *   <li>UI: закрытие браузера, cleanup сессии</li>
     *   <li>API: cleanup тестовых данных, сброс состояния</li>
     * </ul>
     * 
     * @see TestStrategy#afterTest() для деталей реализации
     */
    public static void afterTest() {
        if (currentContext != null) {
            currentContext.afterTest();
        }
    }

    /**
     * Сбрасывает конфигурацию в исходное состояние
     * 
     * Используется для тестирования самого фреймворка или при необходимости
     * полной переинициализации. В обычных тестах не требуется.
     * 
     * <p><b>ВНИМАНИЕ:</b> После вызова необходима повторная инициализация
     * через {@link #initialize()}.
     */
    public static synchronized void reset() {
        currentContext = null;
        initialized = false;
    }

    /**
     * Проверяет статус инициализации конфигурации
     * 
     * @return true если {@link #initialize()} был вызван успешно, false в противном случае
     */
    public static boolean isInitialized() {
        return initialized;
    }
}