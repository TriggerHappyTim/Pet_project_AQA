package com.bft.test.base;

import com.bft.config.TestConfig;
import com.bft.pw.PwSession;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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

    /**
     * Гарантирует живую Playwright-сессию перед каждым тестом.
     *
     * <p>История: при миграции на JUnit 5 настройка браузера выпала из жизненного
     * цикла (раньше её выполняла цепочка стратегий TestNG), из-за чего все UI-тесты
     * падали с «Playwright session is not configured». Здесь она восстановлена явно.
     *
     * <p>{@link PwSession#configure} идемпотентен: если сессия уже поднята,
     * повторный вызов ничего не делает. {@link #teardownUITestMethod()} закрывает
     * сессию после каждого теста — так каждый тест получает чистый контекст.
     */
    @BeforeEach
    protected void ensurePlaywrightSession() {
        try {
            PwSession.configure(new TestConfig());
            PwSession.resetSigningData();
            // Запись шагов (trace): -Devs.trace=true → zip со скриншотами и снимками DOM в Allure
            if (Boolean.parseBoolean(System.getProperty("evs.trace",
                    System.getenv().getOrDefault("EVS_TRACE", "false")))) {
                PwSession.startTracing();
            }
        } catch (Exception e) {
            logger.error("Не удалось настроить Playwright-сессию: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Дополнительная настройка перед каждым UI тестом.
     *
     * <p>Может быть переопределён в наследниках (вызов {@code super.setupUITestMethod()}).
     * По умолчанию логирует начало теста.
     */
    @BeforeEach
    protected void setupUITestMethod() {
        logger.info("Подготовка UI теста: {}", getClass().getSimpleName());
    }

    /**
     * Завершение UI теста: закрывает Playwright-сессию, чтобы каждый тест
     * получал свежий браузер и изолированное состояние приложения.
     * Повторяет поведение Selenide по умолчанию ({@code holdBrowserOpen=false}).
     */
    @AfterEach
    protected void teardownUITestMethod() {
        try {
            // Сохраняем trace-запись шагов, если она велась
            java.nio.file.Path trace = PwSession.stopTracing(
                    getClass().getSimpleName() + "-" + System.currentTimeMillis() % 100000);
            if (trace != null && java.nio.file.Files.exists(trace)) {
                Allure.addAttachment("Playwright trace (zip)", "application/zip",
                        java.nio.file.Files.newInputStream(trace), ".zip");
            }
        } catch (Exception e) {
            logger.warn("Не удалось сохранить trace: {}", e.getMessage());
        }
        try {
            PwSession.close();
        } catch (Exception e) {
            logger.warn("Ошибка при закрытии Playwright-сессии: {}", e.getMessage());
        }
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
