package com.bft.test.helpers;

import com.bft.config.TestConfiguration;
import com.bft.config.TestStrategyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

/**
 * Вспомогательный класс для общей логики инициализации тестов
 * 
 * <p>Предоставляет методы для:
 * - Инициализации конфигурации тестов
 * - Проверки состояния конфигурации
 * - Создания и инициализации SoftAssert
 * 
 * <p>Пример использования:
 * <pre>{@code
 * @BeforeMethod
 * public void setup() {
 *     TestSetupHelper.ensureConfigurationInitialized(TestStrategyType.UI);
 *     softAssert = TestSetupHelper.ensureSoftAssert(softAssert);
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class TestSetupHelper {

    private static final Logger logger = LoggerFactory.getLogger(TestSetupHelper.class);

    /**
     * Гарантирует инициализацию конфигурации тестов
     * 
     * Проверяет, инициализирована ли конфигурация, и если нет - инициализирует её
     * с указанной стратегией.
     * 
     * @param strategyType тип стратегии тестирования (UI или API)
     * @throws RuntimeException если инициализация не удалась
     */
    public static void ensureConfigurationInitialized(TestStrategyType strategyType) {
        if (!TestConfiguration.isInitialized()) {
            logger.info("Конфигурация не инициализирована, инициализируем с стратегией: {}", strategyType);
            TestConfiguration.initialize(strategyType);
        } else {
            logger.debug("Конфигурация уже инициализирована");
        }
    }

    /**
     * Гарантирует инициализацию конфигурации с автоматическим определением стратегии
     * 
     * Определяет тип стратегии на основе текущего контекста или использует UI по умолчанию.
     * 
     * @throws RuntimeException если инициализация не удалась
     */
    public static void ensureConfigurationInitialized() {
        ensureConfigurationInitialized(TestStrategyType.UI);
    }

    /**
     * Гарантирует инициализацию SoftAssert
     * 
     * Проверяет, инициализирован ли SoftAssert, и если нет - создает новый экземпляр.
     * 
     * @param softAssert существующий SoftAssert (может быть null)
     * @return инициализированный SoftAssert (новый или существующий)
     */
    public static SoftAssert ensureSoftAssert(SoftAssert softAssert) {
        if (softAssert == null) {
            logger.debug("SoftAssert не инициализирован, создаем новый экземпляр");
            softAssert = new SoftAssert();
        }
        return softAssert;
    }

    /**
     * Проверяет, что конфигурация инициализирована, и выбрасывает исключение если нет
     * 
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    public static void requireConfigurationInitialized() {
        if (!TestConfiguration.isInitialized()) {
            throw new IllegalStateException(
                "Конфигурация тестов не инициализирована. Вызовите TestConfiguration.initialize() перед использованием."
            );
        }
    }

    /**
     * Получает текущую конфигурацию с проверкой инициализации
     * 
     * @return текущая конфигурация тестов
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    public static com.bft.config.TestConfig getCurrentConfigSafe() {
        requireConfigurationInitialized();
        return TestConfiguration.getCurrentConfig();
    }

    /**
     * Получает текущую стратегию с проверкой инициализации
     * 
     * @return текущая стратегия тестирования
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    public static com.bft.config.TestStrategy getCurrentStrategySafe() {
        requireConfigurationInitialized();
        return TestConfiguration.getCurrentStrategy();
    }

    /**
     * Проверяет, что тест выполняется в UI окружении
     * 
     * @return true если текущее окружение - UI тесты
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    public static boolean isUIEnvironment() {
        requireConfigurationInitialized();
        return TestConfiguration.isUITest();
    }

    /**
     * Проверяет, что тест выполняется в API окружении
     * 
     * ЗАКОММЕНТИРОВАНО: API тесты не используются в проекте
     * 
     * @return всегда false, так как API тесты не используются
     */
    public static boolean isAPIEnvironment() {
        // ЗАКОММЕНТИРОВАНО: API тесты не используются
        // requireConfigurationInitialized();
        // return TestConfiguration.isApiTest();
        return false;
    }
}
