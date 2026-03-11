package com.bft.config;

import io.qameta.allure.selenide.AllureSelenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codeborne.selenide.logevents.SelenideLogger;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Контекст выполнения тестов
 * 
 * Управляет жизненным циклом тестового окружения, объединяя конфигурацию
 * ({@link TestConfig}), стратегию тестирования ({@link TestStrategy}) и состояние тестов.
 * 
 * <p>Основные функции:
 * <ul>
 *   <li>Создание и инициализация тестового окружения</li>
 *   <li>Управление стратегией тестирования (UI/API)</li>
 *   <li>Предоставление доступа к конфигурации и SoftAssert</li>
 *   <li>Выполнение действий перед/после тестов</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Создание контекста с автоматическим выбором стратегии
 * TestContext context = TestContext.create();
 * context.initializeEnvironment();
 * 
 * // Создание контекста с явно указанной стратегией
 * TestContext uiContext = TestContext.create(TestStrategyType.UI);
 * 
 * // Создание контекста с кастомной конфигурацией
 * TestConfig config = new TestConfig().setBrowser("chrome").setHeadless(true);
 * TestContext customContext = TestContext.create(config);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see TestConfiguration для централизованного управления контекстом
 * @see TestStrategy для различных стратегий тестирования
 * @see TestConfig для настроек окружения
 * @since 1.0
 */
public class TestContext {

    private static final Logger log = LoggerFactory.getLogger(TestContext.class);

    private final TestConfig config;
    private final TestStrategy strategy;
    private final SoftAssert softAssert;

    private TestContext(TestConfig config, TestStrategy strategy) {
        this.config = config;
        this.strategy = strategy;
        this.softAssert = new SoftAssert();
    }

    /**
     * Создает контекст с автоматическим выбором стратегии
     * 
     * Автоматически определяет подходящую стратегию тестирования на основе
     * доступных ресурсов и конфигурации. Выбирает стратегию с наивысшим приоритетом.
     * 
     * <p>Процесс выбора:
     * <ol>
     *   <li>Создает конфигурацию с настройками по умолчанию</li>
     *   <li>Проверяет доступность всех стратегий (UI, API)</li>
     *   <li>Выбирает стратегию с максимальным приоритетом</li>
     *   <li>Создает и возвращает контекст</li>
     * </ol>
     * 
     * @return новый экземпляр TestContext с автоматически выбранной стратегией
     * @see #create(TestStrategyType) для явного указания стратегии
     * @see #create(TestConfig) для использования кастомной конфигурации
     */
    public static TestContext create() {
        TestConfig config = new TestConfig();
        TestStrategy strategy = selectStrategy(config);
        return new TestContext(config, strategy);
    }

    /**
     * Создает контекст с явно указанной стратегией тестирования
     * 
     * Используйте этот метод когда нужно принудительно задать тип тестирования
     * (UI или API), независимо от доступности ресурсов.
     * 
     * @param strategyType тип стратегии из {@link TestStrategyType}
     *                    (UI для UI тестов, API для API тестов)
     * @return новый экземпляр TestContext с указанной стратегией
     * @throws IllegalArgumentException если указан неподдерживаемый тип стратегии
     * @see TestStrategyType для доступных типов стратегий
     */
    public static TestContext create(TestStrategyType strategyType) {
        TestConfig config = new TestConfig();
        TestStrategy strategy = createStrategy(strategyType, config);
        return new TestContext(config, strategy);
    }

    /**
     * Создает контекст с кастомной конфигурацией
     * 
     * Позволяет полностью настроить тестовое окружение через объект {@link TestConfig}.
     * Стратегия выбирается автоматически на основе предоставленной конфигурации.
     * 
     * @param config кастомная конфигурация с настройками браузера, окружения и т.д.
     *               не должен быть null
     * @return новый экземпляр TestContext с указанной конфигурацией
     * @throws NullPointerException если config равен null
     * @see TestConfig для всех доступных настроек
     */
    public static TestContext create(TestConfig config) {
        TestStrategy strategy = selectStrategy(config);
        return new TestContext(config, strategy);
    }

    /**
     * Инициализирует тестовое окружение перед запуском тестов
     * 
     * Выполняет настройку всех компонентов, необходимых для выполнения тестов:
     * - Настройка Allure Selenide listener для логирования
     * - Конфигурация стратегии тестирования
     * - Настройка Selenide Configuration
     * 
     * <p>Должен вызываться один раз перед запуском всех тестов в suite.
     * Обычно вызывается из {@link TestConfiguration#initialize()}.
     * 
     * @see TestStrategy#configureEnvironment() для деталей настройки окружения
     * @see TestStrategy#configureSelenide() для деталей настройки Selenide
     */
    public void initializeEnvironment() {
        // Настройка Allure Selenide
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());

        // Настройка стратегии
        strategy.configureEnvironment();
        strategy.configureSelenide();

        log.info("Test environment initialized with strategy: {}", strategy.getType());
        log.debug("Configuration: {}", config);
    }

    /**
     * Выполняет действия перед каждым тестом
     * 
     * Делегирует выполнение текущей стратегии тестирования.
     * Типичные действия зависят от типа стратегии:
     * <ul>
     *   <li>UI: открытие браузера, максимизация окна, настройка КриптоПРО</li>
     *   <li>API: инициализация HTTP клиента, проверка доступности API</li>
     * </ul>
     * 
     * <p>Вызывается автоматически из {@link TestConfiguration#beforeTest()},
     * который должен быть вызван из {@link org.testng.annotations.BeforeTest}.
     * 
     * @see TestStrategy#beforeTest() для деталей реализации
     */
    public void beforeTest() {
        strategy.beforeTest();
    }

    /**
     * Выполняет действия после каждого теста
     * 
     * Делегирует выполнение текущей стратегии тестирования.
     * Типичные действия зависят от типа стратегии:
     * <ul>
     *   <li>UI: закрытие браузера, очистка сессии</li>
     *   <li>API: очистка тестовых данных, сброс состояния HTTP клиента</li>
     * </ul>
     * 
     * <p>Вызывается автоматически из {@link TestConfiguration#afterTest()},
     * который должен быть вызван из {@link org.testng.annotations.AfterMethod}.
     * 
     * @see TestStrategy#afterTest() для деталей реализации
     */
    public void afterTest() {
        strategy.afterTest();
    }

    /**
     * Возвращает текущую стратегию тестирования
     * 
     * @return объект {@link TestStrategy} (UITestStrategy или ApiTestStrategy)
     * @see TestStrategy для методов стратегии
     */
    public TestStrategy getStrategy() {
        return strategy;
    }

    /**
     * Возвращает конфигурацию тестового окружения
     * 
     * @return объект {@link TestConfig} с настройками браузера, окружения и т.д.
     * @see TestConfig для всех доступных настроек
     */
    public TestConfig getConfig() {
        return config;
    }

    /**
     * Возвращает SoftAssert для мягких проверок в тестах
     * 
     * SoftAssert позволяет накапливать ошибки проверок и выбросить их все
     * в конце теста через {@link org.testng.asserts.SoftAssert#assertAll()}.
     * 
     * @return объект {@link org.testng.asserts.SoftAssert} для текущего теста
     * @see org.testng.asserts.SoftAssert#assertAll() для финализации проверок
     */
    public SoftAssert getSoftAssert() {
        return softAssert;
    }

    /**
     * Проверяет, является ли текущая стратегия UI стратегией
     * 
     * @return true если используется {@link UITestStrategy}, false в противном случае
     */
    public boolean isUITest() {
        return strategy.getType() == TestStrategyType.UI;
    }

    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // /**
    //  * Проверяет, является ли текущая стратегия API стратегией
    //  * 
    //  * @return true если используется {@link ApiTestStrategy}, false в противном случае
    //  */
    // public boolean isApiTest() {
    //     return strategy.getType() == TestStrategyType.API;
    // }

    /**
     * Автоматически выбирает подходящую стратегию на основе конфигурации
     * 
     * Проверяет доступность всех стратегий и выбирает ту, которая:
     * - Применима для текущего окружения (isApplicable() == true)
     * - Имеет наивысший приоритет (getPriority())
     * 
     * <p>Если ни одна стратегия не доступна, выбрасывает исключение.
     * // Использование ApiTestStrategy как fallback - ЗАКОММЕНТИРОВАНО: API тесты не используются
     * 
     * @param config конфигурация для проверки доступности стратегий
     * @return выбранная стратегия тестирования
     * @throws RuntimeException если не удалось выбрать стратегию
     */
    private static TestStrategy selectStrategy(TestConfig config) {
        List<TestStrategy> availableStrategies = getAvailableStrategies(config);

        if (availableStrategies.isEmpty()) {
            // ЗАКОММЕНТИРОВАНО: API тесты не используются
            // Fallback: если нет доступных стратегий, создаем API стратегию как запасной вариант
            // System.out.println("Warning: No suitable test strategy found for current configuration. Using API strategy as fallback.");
            // return new ApiTestStrategy(config);
            throw new RuntimeException("No suitable test strategy found for current configuration");
        }

        // Выбираем стратегию с наивысшим приоритетом
        return availableStrategies.stream()
                .max(Comparator.comparingInt(TestStrategy::getPriority))
                .orElseThrow(() -> new RuntimeException("Failed to select test strategy"));
    }

    /**
     * Создает стратегию указанного типа
     * 
     * @param type тип стратегии из {@link TestStrategyType}
     * @param config конфигурация для создания стратегии
     * @return новый экземпляр стратегии указанного типа
     * @throws IllegalArgumentException если указан неподдерживаемый тип стратегии
     */
    private static TestStrategy createStrategy(TestStrategyType type, TestConfig config) {
        switch (type) {
            case UI:
                return new UITestStrategy(config);
            // case API:  // ЗАКОММЕНТИРОВАНО: API тесты не используются
            //     return new ApiTestStrategy(config);
            default:
                throw new IllegalArgumentException("Unsupported strategy type: " + type);
        }
    }

    /**
     * Возвращает список всех доступных стратегий для указанной конфигурации
     * 
     * Проверяет применимость каждой стратегии через метод isApplicable()
     * и возвращает только те, которые подходят для текущего окружения.
     * 
     * @param config конфигурация для проверки доступности стратегий
     * @return список доступных стратегий (может быть пустым)
     */
    private static List<TestStrategy> getAvailableStrategies(TestConfig config) {
        List<TestStrategy> strategies = new ArrayList<>();

        // UI стратегия
        UITestStrategy uiStrategy = new UITestStrategy(config);
        if (uiStrategy.isApplicable()) {
            strategies.add(uiStrategy);
        }

        // ЗАКОММЕНТИРОВАНО: API тесты не используются
        // API стратегия
        // ApiTestStrategy apiStrategy = new ApiTestStrategy(config);
        // if (apiStrategy.isApplicable()) {
        //     strategies.add(apiStrategy);
        // }

        return strategies;
    }

    /**
     * Возвращает строковое представление контекста
     * 
     * Используется для логирования и отладки. Включает тип стратегии
     * и строковое представление конфигурации.
     * 
     * @return строковое представление контекста
     */
    @Override
    public String toString() {
        return "TestContext{" +
                "strategy=" + strategy.getType() +
                ", config=" + config +
                '}';
    }
}