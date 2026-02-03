package com.bft;

import com.bft.config.TestConfiguration;
import com.bft.test.annotations.AllureAnnotationProcessor;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.asserts.SoftAssert;

/**
 * Базовый класс для всех тестов в EVS Testing Framework
 * 
 * Предоставляет общую инфраструктуру тестирования:
 * - Инициализацию TestNG конфигурации через {@link TestConfiguration}
 * - SoftAssert для мягких проверок (не прерывают выполнение теста)
 * - Lifecycle методы (@BeforeSuite, @BeforeTest, @AfterMethod)
 * - Автоматический выбор стратегии тестирования (UI/API)
 * 
 * <p>Все тестовые классы должны наследоваться от этого класса
 * либо от его специализированных версий ({@link com.bft.test.base.UITestBase}, 
 * {@link com.bft.test.base.ApiTestBase}).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * @Feature("User Authentication")
 * public class LoginTest extends BaseTest {
 *     
 *     @Test(groups = {"web", "smoke"})
 *     public void successfulLogin() {
 *         // softAssert доступен из базового класса
 *         softAssert.assertTrue(condition, "message");
 *         softAssert.assertAll(); // в конце теста
 *     }
 * }
 * }</pre>
 * 
 * @see com.bft.test.base.UITestBase для UI тестов с Selenide
 * @see com.bft.test.base.ApiTestBase для API тестов с RestAssured
 * @see TestConfiguration для управления конфигурацией
 * @author QA Automation Team
 * @since 1.0
 */
@Listeners({AllureTestNg.class, AllureAnnotationProcessor.class})
public class BaseTest {

    /**
     * SoftAssert для выполнения мягких проверок
     * 
     * Позволяет продолжать выполнение теста даже после провала проверки.
     * Все провалы аккумулируются и выбрасываются в конце через assertAll().
     */
    protected SoftAssert softAssert;

    /**
     * Инициализирует конфигурацию тестов один раз для всего test suite
     * 
     * Выполняется перед запуском всех тестов в suite.
     * Автоматически выбирает стратегию тестирования (UI по умолчанию).
     * 
     * @see TestConfiguration#initialize() для деталей инициализации
     */
    @BeforeSuite
    public void initializeTestConfiguration() {
        // Автоматический выбор стратегии (UI по умолчанию)
        TestConfiguration.initialize();
    }

    /**
     * Альтернативный метод инициализации с явным указанием стратегии
     * Раскомментируйте нужную строку для переопределения стратегии
     */
    /*
    @BeforeSuite
    public void initializeTestConfiguration() {
        // Для UI тестирования
        TestConfiguration.initialize(TestStrategyType.UI);

        // Для API тестирования
        // TestConfiguration.initialize(TestStrategyType.API);
    }
    */

    /**
     * Настройка перед каждым тестом
     * 
     * Выполняется перед каждым тестовым методом (@Test).
     * Инициализирует SoftAssert и выполняет подготовку стратегии тестирования.
     * 
     * @see TestConfiguration#beforeTest() для деталей подготовки
     */
    @BeforeTest
    public void setUpTest() {
        ensureSoftAssert();
        TestConfiguration.beforeTest();
    }

    /**
     * Гарантирует инициализацию SoftAssert перед каждым тестовым методом.
     * Нужно при parallel="methods": @BeforeTest может выполниться не для всех потоков.
     */
    @BeforeMethod
    public void ensureSoftAssert() {
        if (softAssert == null) {
            try {
                softAssert = TestConfiguration.getSoftAssert();
            } catch (IllegalStateException e) {
                softAssert = new SoftAssert();
            }
        }
    }


    /**
     * Очистка после каждого тестового метода
     * 
     * Выполняется после каждого тестового метода (@Test).
     * Выполняет cleanup стратегии тестирования (закрытие браузера, сброс состояния и т.д.).
     * 
     * @see TestConfiguration#afterTest() для деталей cleanup
     */
    @AfterMethod
    public void tearDownTest() {
        // Выполняем действия стратегии после теста
        TestConfiguration.afterTest();
    }
}