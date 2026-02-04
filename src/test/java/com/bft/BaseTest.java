package com.bft;

import com.bft.config.TestConfiguration;
import com.bft.config.TestStrategyType;
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
 * либо от его специализированных версий ({@link com.bft.test.base.UITestBase}). 
 * // {@link com.bft.test.base.ApiTestBase} - ЗАКОММЕНТИРОВАНО: API тесты не используются
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
 * // @see com.bft.test.base.ApiTestBase для API тестов с RestAssured - ЗАКОММЕНТИРОВАНО: API тесты не используются
 * @see TestConfiguration для управления конфигурацией
 * @author QA Automation Team
 * @since 1.0
 */
@Listeners({AllureTestNg.class, AllureAnnotationProcessor.class})
public class BaseTest {

    /**
     * ThreadLocal для потокобезопасного хранения SoftAssert
     * 
     * Обеспечивает изоляцию SoftAssert между потоками при параллельном выполнении тестов.
     * Каждый поток получает свой собственный экземпляр SoftAssert.
     */
    private static final ThreadLocal<SoftAssert> softAssertThreadLocal = new ThreadLocal<>();

    /**
     * SoftAssert для выполнения мягких проверок
     * 
     * Позволяет продолжать выполнение теста даже после провала проверки.
     * Все провалы аккумулируются и выбрасываются в конце через assertAll().
     * 
     * При параллельном выполнении каждый поток получает свой собственный экземпляр
     * через ThreadLocal для обеспечения потокобезопасности.
     */
    protected SoftAssert softAssert;

    /**
     * Инициализирует конфигурацию тестов один раз для всего test suite
     * 
     * Выполняется перед запуском всех тестов в suite.
     * Явно указывает UI стратегию для всех тестов, наследующих BaseTest.
     * 
     * @see TestConfiguration#initialize(TestStrategyType) для деталей инициализации
     */
    @BeforeSuite
    public void initializeTestConfiguration() {
        // Явно указываем UI стратегию для всех тестов
        TestConfiguration.initialize(TestStrategyType.UI);
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
     * 
     * Использует ThreadLocal для обеспечения потокобезопасности при параллельном выполнении.
     */
    @BeforeMethod
    public void ensureSoftAssert() {
        // Получаем или создаем SoftAssert для текущего потока
        softAssert = softAssertThreadLocal.get();
        
        if (softAssert == null) {
            try {
                softAssert = TestConfiguration.getSoftAssert();
            } catch (IllegalStateException e) {
                softAssert = new SoftAssert();
            }
            // Сохраняем в ThreadLocal для текущего потока
            softAssertThreadLocal.set(softAssert);
        }
    }


    /**
     * Очистка после каждого тестового метода
     * 
     * Выполняется после каждого тестового метода (@Test).
     * Выполняет cleanup стратегии тестирования (закрытие браузера, сброс состояния и т.д.).
     * Очищает ThreadLocal для освобождения памяти.
     * 
     * <p>Автоматически создает скриншот при провале теста для UI тестов.
     * 
     * @param result результат выполнения теста (для проверки статуса)
     * @see TestConfiguration#afterTest() для деталей cleanup
     */
    @AfterMethod
    public void tearDownTest(org.testng.ITestResult result) {
        // Создаем скриншот при провале теста (только для UI тестов)
        if (result.getStatus() == org.testng.ITestResult.FAILURE) {
            takeScreenshotOnFailure(result);
        }
        
        // Выполняем действия стратегии после теста
        TestConfiguration.afterTest();
        
        // Очищаем ThreadLocal для текущего потока после завершения теста
        // Это важно для предотвращения утечек памяти при параллельном выполнении
        softAssertThreadLocal.remove();
        softAssert = null;
    }
    
    /**
     * Создает скриншот при провале теста
     * 
     * Автоматически вызывается в @AfterMethod при провале теста.
     * Имя файла формируется на основе имени теста и временной метки.
     * 
     * @param result результат выполнения теста для получения контекста
     */
    private void takeScreenshotOnFailure(org.testng.ITestResult result) {
        // Проверяем, что это UI тест (только для UI тестов создаем скриншоты)
        if (!TestConfiguration.isUITest()) {
            return;
        }
        
        try {
            String testName = result.getMethod().getMethodName();
            String className = result.getTestClass().getName();
            String screenshotName = String.format("failure_%s_%s_%d",
                className.substring(className.lastIndexOf('.') + 1),
                testName,
                System.currentTimeMillis());
            
            // Используем Selenide для создания скриншота
            com.codeborne.selenide.Selenide.screenshot(screenshotName);
            
            // Логируем создание скриншота
            org.slf4j.LoggerFactory.getLogger(getClass())
                .info("Скриншот ошибки сохранен: {}", screenshotName);
            
            // Прикрепляем к Allure отчету
            io.qameta.allure.Allure.addAttachment(
                "Screenshot on failure",
                "image/png",
                new java.io.ByteArrayInputStream(
                    ((org.openqa.selenium.TakesScreenshot) 
                        com.codeborne.selenide.WebDriverRunner.getWebDriver())
                        .getScreenshotAs(org.openqa.selenium.OutputType.BYTES)
                ),
                ".png"
            );
            
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем выполнение
            org.slf4j.LoggerFactory.getLogger(getClass())
                .warn("Не удалось сделать скриншот ошибки: {}", e.getMessage());
        }
    }
}