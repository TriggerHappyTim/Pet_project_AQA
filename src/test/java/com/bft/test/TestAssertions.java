package com.bft.test;

import com.bft.utils.DebugUtils; // Ваш класс утилит отладки
import com.bft.pw.Selenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import static com.bft.pw.Condition.exist;

/**
 * Обертка над SoftAssert для стандартизации проверок в UI тестах.
 *
 * Преимущества:
 * 1. Гарантированный вызов assertAll() (через try-finally в тестах или явный вызов).
 * 2. Автоматический скриншот при первой ошибке.
 * 3. Единый формат сообщений об ошибках.
 * 4. Удобные методы для частых проверок (видимость, текст, наличие).
 */
public class TestAssertions extends SoftAssert {

    private static final Logger log = LoggerFactory.getLogger(TestAssertions.class);

    // Флаг, чтобы сделать скриншот только один раз за прогон теста, даже если ошибок много
    private boolean screenshotTaken = false;

    /**
     * Стандартный конструктор.
     */
    public TestAssertions() {
        super();
    }

    /**
     * Делегирует проверку родительскому методу, но при неудаче:
     * 1. Логгирует ошибку.
     * 2. Делает скриншот (если еще не был сделан).
     */
    @Override
    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            handleFailure(message);
        }
        super.assertTrue(condition, message);
    }

    @Override
    public void assertFalse(boolean condition, String message) {
        if (condition) {
            handleFailure(message);
        }
        super.assertFalse(condition, message);
    }

    @Override
    public void assertEquals(Object actual, Object expected, String message) {
        if (!java.util.Objects.equals(actual, expected)) {
            handleFailure(message + String.format(" (Ожидалось: '%s', Получено: '%s')", expected, actual));
        }
        super.assertEquals(actual, expected, message);
    }

    /**
     * Проверка видимости элемента с кастомным сообщением.
     */
    public void assertVisible(com.bft.pw.SelenideElement element, String elementName) {
        String msg = "Элемент '" + elementName + "' должен быть видимым";
        try {
            element.shouldBe(com.bft.pw.Condition.visible);
            super.assertTrue(true, msg); // Записываем как успех в softAssert
        } catch (AssertionError e) {
            handleFailure(msg);
            super.fail(msg);
        }
    }

    /**
     * Проверка отсутствия элемента на странице.
     */
    public void assertNotExists(com.bft.pw.SelenideElement element, String elementName) {
        String msg = "Элемент '" + elementName + "' не должен существовать на странице";
        try {
            element.shouldNot(exist);
            super.assertTrue(true, msg);
        } catch (AssertionError e) {
            handleFailure(msg);
            super.fail(msg);
        }
    }

    /**
     * Обрабатывает провал проверки: логирование и скриншот.
     */
    private void handleFailure(String message) {
        log.error("❌ ПРОВЕРКА НЕ ПРОЙДЕНА: {}", message);

        if (!screenshotTaken) {
            try {
                String screenshotName = "failure_assert_" + System.currentTimeMillis();
                Selenide.screenshot(screenshotName);
                DebugUtils.savePageStateOnError("assert-failure-" + screenshotName, null);
                screenshotTaken = true;
                log.info("📸 Скриншот сохранен: {}", screenshotName);
            } catch (Exception e) {
                log.warn("Не удалось сделать скриншот при провале ассерта: {}", e.getMessage());
            }
        }
    }

    /**
     * Переопределяем assertAll, чтобы сбросить флаг скриншота для следующего теста (если объект переиспользуется)
     * Хотя лучше создавать новый экземпляр на каждый тест.
     */
    @Override
    public void assertAll() {
        try {
            super.assertAll();
        } finally {
            screenshotTaken = false;
        }
    }
}