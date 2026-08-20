package com.bft.test.base;

import com.bft.config.TestConfig;
import com.bft.config.UITestStrategy;
import com.bft.pw.PwSession;
import io.qameta.allure.Allure;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

import static com.bft.pw.Selenide.dismiss;
import static com.bft.pw.Selenide.screenshot;

/**
 * Базовый класс для UI тестов.
 *
 * <p>Расширяет {@link BaseTest} и добавляет утилиты для работы с браузером:
 * <ul>
 *   <li>{@link #setupUITestMethod()} — дополнительная настройка перед каждым UI тестом</li>
 *   <li>{@link #isUIEnvironment()} — признак выполнения теста в UI окружении</li>
 *   <li>{@link #takeScreenshot(String)} — сохранение скриншота с прикреплением к Allure</li>
 *   <li>{@link #waitForPageLoad()} — ожидание полной загрузки страницы</li>
 *   <li>{@link #confirmAlert()} / {@link #dismissAlert()} — работа с модальными alert-окнами</li>
 * </ul>
 *
 * <p>Мягкие проверки {@link BaseTest#assertions} автоматически проверяются после каждого
 * тестового метода, поэтому явный вызов {@code softAssert.assertAll()} не требуется.
 *
 * @author QA Automation Team
 * @version 1.0
 * @see BaseTest
 */
public abstract class UITestBase extends BaseTest {

    /** Таймаут ожидания загрузки страницы */
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(15);

    /** Признак однократной настройки браузера с расширением КриптоПРО */
    private static volatile boolean cryptoProConfigured = false;

    /**
     * Дополнительная настройка перед каждым UI тестом.
     *
     * <p>Может быть переопределён в наследниках (вызов {@code super.setupUITestMethod()}).
     * По умолчанию один раз настраивает Playwright через {@link UITestStrategy}
     * (Chrome + расширение КриптоПРО) и логирует начало теста.
     */
    @BeforeMethod
    protected void setupUITestMethod() {
        if (!cryptoProConfigured) {
            try {
                new UITestStrategy(new TestConfig()).configureSelenide();
                cryptoProConfigured = true;
                logger.info("Браузер настроен с расширением КриптоПРО");
            } catch (Exception e) {
                logger.warn("Не удалось настроить браузер с расширением КриптоПРО: {}", e.getMessage());
            }
        }
        logger.info("Подготовка UI теста: {}", getClass().getSimpleName());
    }

    /**
     * Признак выполнения теста в UI окружении.
     *
     * @return всегда {@code true}, т.к. класс предназначен для UI тестов
     */
    protected boolean isUIEnvironment() {
        return true;
    }

    /**
     * Сохраняет скриншот текущего состояния страницы.
     *
     * <p>Скриншот сохраняется через Selenide и дополнительно прикрепляется к отчёту Allure.
     *
     * @param name имя файла скриншота (без расширения)
     */
    protected void takeScreenshot(String name) {
        String filePath = null;
        try {
            filePath = screenshot(name);
            logger.info("Скриншот сохранён: {}", filePath);
            if (filePath != null) {
                try (java.io.FileInputStream is = new java.io.FileInputStream(filePath)) {
                    Allure.addAttachment(name, "image/png", is, "png");
                }
            }
        } catch (Exception e) {
            logger.warn("Не удалось сделать скриншот '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Ожидает полной загрузки страницы (document.readyState == "complete").
     */
    protected void waitForPageLoad() {
        try {
            long deadline = System.currentTimeMillis() + PAGE_LOAD_TIMEOUT.toMillis();
            while (System.currentTimeMillis() < deadline) {
                Object readyState = PwSession.executeJavaScript("return document.readyState");
                if ("complete".equals(readyState)) {
                    return;
                }
                Thread.sleep(250);
            }
        } catch (Exception e) {
            logger.warn("Не удалось дождаться загрузки страницы: {}", e.getMessage());
        }
    }

    /**
     * Подтверждает модальное alert-окно (нажимает "OK").
     */
    protected void confirmAlert() {
        try {
            com.bft.pw.Selenide.confirm();
        } catch (Exception e) {
            logger.warn("Не удалось подтвердить alert: {}", e.getMessage());
        }
    }

    /**
     * Отклоняет модальное alert-окно (нажимает "Отмена").
     */
    protected void dismissAlert() {
        try {
            dismiss();
        } catch (Exception e) {
            logger.warn("Не удалось отклонить alert: {}", e.getMessage());
        }
    }
}
