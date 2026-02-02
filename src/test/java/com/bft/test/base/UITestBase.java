package com.bft.test.base;

import com.bft.BaseTest;
import com.bft.config.TestConfiguration;
import com.bft.test.logging.AllureIntegration;
import com.bft.test.logging.TestLogger;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;

import java.util.function.Consumer;

/**
 * Базовый класс для UI тестов с разделением ответственности
 * 
 * Предоставляет инфраструктуру для написания UI тестов с использованием Selenide.
 * Реализует паттерн Arrange-Act-Assert для структурирования UI тестов.
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>Паттерн Arrange-Act-Assert для структурирования тестов</li>
 *   <li>Автоматическое логирование этапов теста</li>
 *   <li>Интеграция с Allure для отчетности</li>
 *   <li>Автоматическое создание скриншотов при ошибках</li>
 *   <li>Вспомогательные методы для действий и проверок</li>
 *   <li>Проверка типа окружения (UI/API)</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * public class LoginTest extends UITestBase {
 *     
 *     @Test(groups = {"web", "smoke"})
 *     public void successfulLogin() {
 *         arrangeActAssert(
 *             // Arrange - подготовка
 *             () -> {
 *                 logger.info("Подготавливаем тестовые данные");
 *                 // Подготовка данных
 *             },
 *             // Act - выполнение
 *             () -> {
 *                 new LoginPage()
 *                     .open(UIType.EVS_UAT_LKS)
 *                     .authorize("user@test.com", "password");
 *             },
 *             // Assert - проверка
 *             softAssert -> {
 *                 softAssert.assertTrue(
 *                     $(".user-name").isDisplayed(),
 *                     "Пользователь должен быть авторизован"
 *                 );
 *             },
 *             "Successful Login Test"
 *         );
 *     }
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseTest для базовой функциональности
 * @see ApiTestBase для API тестов
 * @see TestLogger для логирования тестов
 * @since 1.0
 */
public abstract class UITestBase extends BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final TestLogger testLogger = TestLogger.forTest(getClass().getSimpleName());

    @BeforeMethod
    public void initializeTest() {
        // Инициализация для каждого теста
        logger.info("Инициализация UI теста: {}", getClass().getSimpleName());
    }

    /**
     * Паттерн Arrange-Act-Assert для UI тестов
     * 
     * Структурирует UI тест на три этапа:
     * 1. Arrange - подготовка тестовых данных и окружения
     * 2. Act - выполнение действий пользователя
     * 3. Assert - валидация результатов
     * 
     * <p>Автоматически логирует каждый этап, создает скриншоты при ошибках
     * и интегрируется с Allure для отчетности.
     * 
     * @param arrange функция для подготовки теста (создание данных, настройка окружения)
     * @param act функция для выполнения действий (взаимодействие с UI)
     * @param assertConsumer функция для валидации результатов (проверки через SoftAssert)
     */
    protected void arrangeActAssert(
            Runnable arrange,
            Runnable act,
            Consumer<SoftAssert> assertConsumer) {

        arrangeActAssert(arrange, act, assertConsumer, "UI Test");
    }

    /**
     * Расширенный паттерн Arrange-Act-Assert с указанием имени теста
     * 
     * Структурирует UI тест на три этапа с кастомным именем для логирования и отчетности.
     * 
     * @param arrange функция для подготовки теста (создание данных, настройка окружения)
     * @param act функция для выполнения действий (взаимодействие с UI)
     * @param assertConsumer функция для валидации результатов (проверки через SoftAssert)
     * @param testName имя теста для логирования и отчетности
     */
    protected void arrangeActAssert(
            Runnable arrange,
            Runnable act,
            Consumer<SoftAssert> assertConsumer,
            String testName) {

        logger.info("=== НАЧАЛО ТЕСТА: {} ===", testName);

        long startTime = System.currentTimeMillis();

        try {
            // Arrange - подготовка
            testLogger.info("📋 ШАГ 1: Подготовка тестовых данных");
            arrange.run();

            // Act - выполнение
            testLogger.info("🎯 ШАГ 2: Выполнение действий");
            act.run();

            // Assert - проверка
            testLogger.info("✅ ШАГ 3: Валидация результатов");
            assertConsumer.accept(softAssert);

            // Финализация проверок
            softAssert.assertAll();

            long duration = System.currentTimeMillis() - startTime;
            testLogger.testFinished(true, duration);
            testLogger.info("✅ ТЕСТ ПРОЙДЕН: {} ({}мс)", testName, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            testLogger.testFinished(false, duration);
            testLogger.error(String.format("❌ ТЕСТ ПРОВАЛЕН: %s - %s", testName, e.getMessage()));
            AllureIntegration.attachErrorInfo(testName, e);
            takeScreenshotOnFailure(testName);
            throw e;
        }
    }

    /**
     * Упрощенный паттерн для тестов без подготовки
     * 
     * Используется когда тест не требует предварительной подготовки данных.
     * Пропускает этап Arrange и сразу выполняет действия и проверки.
     * 
     * @param act функция для выполнения действий (взаимодействие с UI)
     * @param assertConsumer функция для валидации результатов (проверки через SoftAssert)
     */
    protected void actAssert(Runnable act, Consumer<SoftAssert> assertConsumer) {
        arrangeActAssert(() -> {}, act, assertConsumer);
    }

    /**
     * Паттерн для тестов только с проверками
     * 
     * Используется для тестов, которые только проверяют текущее состояние UI
     * без выполнения действий.
     * 
     * @param assertConsumer функция для валидации результатов (проверки через SoftAssert)
     */
    protected void assertOnly(Consumer<SoftAssert> assertConsumer) {
        arrangeActAssert(() -> {}, () -> {}, assertConsumer);
    }

    /**
     * Выполняет действие с логированием и обработкой ошибок
     * 
     * Обертка для выполнения действия с автоматическим логированием
     * и интеграцией с Allure (@Step).
     * 
     * @param actionName имя действия для логирования и Allure отчетов
     * @param action действие для выполнения
     */
    @Step("Выполнение действия: {actionName}")
    protected void performAction(String actionName, Runnable action) {
        testLogger.step("Выполнение действия: " + actionName, () -> {
            try {
                action.run();
                testLogger.action(actionName, () -> {}, true);
            } catch (Exception e) {
                testLogger.action(actionName, () -> {}, false);
                throw e;
            }
        });
    }

    /**
     * Выполняет проверку с логированием
     * 
     * Обертка для выполнения проверки с автоматическим логированием
     * и интеграцией с Allure (@Step).
     * 
     * @param checkName имя проверки для логирования и Allure отчетов
     * @param check проверка для выполнения
     * @throws AssertionError если проверка не прошла
     */
    @Step("Выполнение проверки: {checkName}")
    protected void performCheck(String checkName, Runnable check) {
        testLogger.step("Выполнение проверки: " + checkName, () -> {
            try {
                check.run();
                testLogger.check(checkName, true);
            } catch (AssertionError e) {
                testLogger.check(checkName, false);
                throw e;
            }
        });
    }

    /**
     * Создает скриншот при ошибке теста
     * 
     * Автоматически вызывается при провале теста для сохранения состояния UI.
     * Имя файла формируется на основе контекста и временной метки.
     * 
     * @param context контекст ошибки (обычно имя теста) для формирования имени файла
     */
    private void takeScreenshotOnFailure(String context) {
        try {
            String screenshotName = "failure_" + context.replaceAll("[^a-zA-Z0-9]", "_") +
                                  "_" + System.currentTimeMillis();
            // Используем улучшенный Page Object для скриншота
            Selenide.screenshot(screenshotName);
            logger.info("Скриншот ошибки сохранен: {}", screenshotName);
        } catch (Exception e) {
            logger.warn("Не удалось сделать скриншот ошибки: {}", e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для создания действий с логированием
     * 
     * Создает Runnable, который при выполнении логирует действие через performAction().
     * 
     * @param description описание действия для логирования
     * @param action действие для выполнения
     * @return Runnable с логированием действия
     */
    protected Runnable action(String description, Runnable action) {
        return () -> performAction(description, action);
    }

    /**
     * Вспомогательный метод для создания проверок с логированием
     * 
     * Создает Consumer<SoftAssert>, который при выполнении логирует проверку через performCheck().
     * 
     * @param description описание проверки для логирования
     * @param check функция проверки, принимающая SoftAssert
     * @return Consumer<SoftAssert> с логированием проверки
     */
    protected Consumer<SoftAssert> check(String description, Consumer<SoftAssert> check) {
        return softAssert -> {
            performCheck(description, () -> check.accept(softAssert));
        };
    }

    /**
     * Проверяет, что тест выполняется в UI окружении
     * 
     * Используется для условного выполнения кода только в UI тестах.
     * 
     * @return true если текущее окружение - UI тесты, false в противном случае
     */
    protected boolean isUIEnvironment() {
        return TestConfiguration.isUITest();
    }

    /**
     * Проверяет, что тест выполняется в API окружении
     * 
     * Используется для условного выполнения кода только в API тестах.
     * 
     * @return true если текущее окружение - API тесты, false в противном случае
     */
    protected boolean isAPIEnvironment() {
        return TestConfiguration.isApiTest();
    }
}