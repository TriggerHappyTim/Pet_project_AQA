package com.bft.test.base;

import com.bft.jupiter.extension.MakeScreenshotsExtension;
import com.bft.jupiter.extension.TestResultWatcher;
import com.bft.test.TestAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Consumer;

/**
 * Базовый класс для всех тестов.
 *
 * <p>Предоставляет общую инфраструктуру:
 * <ul>
 *   <li>Логгер {@link #logger} для каждого тестового класса</li>
 *   <li>Мягкие проверки {@link #assertions} ({@link TestAssertions}), создаваемые перед каждым
 *       тестовым методом и автоматически проверяемые ({@code assertAll}) после него</li>
 *   <li>Паттерн Arrange-Act-Assert через {@link #arrangeActAssert(Runnable, Runnable, Consumer)}
 *       и {@link #arrangeActAssert(Runnable, Runnable, Consumer, String)}</li>
 *   <li>Утилиты {@link #performAction(String, Runnable)} и {@link #performCheck(String, Runnable)}
 *       для структурирования шагов теста</li>
 * </ul>
 *
 * <p>Пример использования:
 * <pre>{@code
 * public class MyTest extends BaseTest {
 *     @Test
 *     public void example() {
 *         arrangeActAssert(
 *             () -> logger.info("Arrange"),
 *             () -> logger.info("Act"),
 *             softAssert -> softAssert.assertTrue(true),
 *             "Пример теста"
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author QA Automation Team
 * @version 1.0
 * @see UITestBase для UI тестов
 */
@ExtendWith({MakeScreenshotsExtension.class, TestResultWatcher.class})
public abstract class BaseTest {

    /** Логгер для текущего тестового класса */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Мягкие проверки текущего тестового метода (инициализируются в {@link #setupAssertions()}) */
    protected TestAssertions assertions;

    /**
     * Создаёт новый экземпляр {@link TestAssertions} перед каждым тестовым методом.
     */
    @BeforeEach
    protected void setupAssertions() {
        TestAssertions.reset();
        assertions = new TestAssertions();
    }

    /**
     * Автоматически выполняет assertAll() после каждого тестового метода,
     * собирая все мягкие проверки. Если проверок не было — метод безопасно пропускается.
     */
    @AfterEach
    protected void verifyAssertions() {
        if (assertions != null) {
            assertions.assertAll();
        }
    }

    /**
     * Выполняет тест по паттерну Arrange-Act-Assert:
     * <ol>
     *   <li><b>Arrange</b> — подготовка данных (аргумент {@code arrange})</li>
     *   <li><b>Act</b> — взаимодействие с системой (аргумент {@code act})</li>
     *   <li><b>Assert</b> — проверки через мягкие утверждения (аргумент {@code assertConsumer})</li>
     * </ol>
     *
     * @param arrange        подготовка данных
     * @param act            действие
     * @param assertConsumer проверки результата (получает текущий {@link #assertions})
     */
    protected void arrangeActAssert(Runnable arrange, Runnable act, Consumer<TestAssertions> assertConsumer) {
        arrangeActAssert(arrange, act, assertConsumer, null);
    }

    /**
     * Выполняет тест по паттерну Arrange-Act-Assert с указанием имени теста для логирования.
     *
     * @param arrange        подготовка данных
     * @param act            действие
     * @param assertConsumer проверки результата (получает текущий {@link #assertions})
     * @param testName       имя теста для читаемого логирования (может быть null)
     */
    protected void arrangeActAssert(Runnable arrange, Runnable act, Consumer<TestAssertions> assertConsumer,
                                    String testName) {
        String name = testName != null ? testName : getClass().getSimpleName();
        logger.info("=== ARRANGE-ACT-ASSERT: {} ===", name);

        try {
            if (arrange != null) {
                logger.info("--- Arrange: {} ---", name);
                arrange.run();
            }
            if (act != null) {
                logger.info("--- Act: {} ---", name);
                act.run();
            }
            if (assertConsumer != null) {
                logger.info("--- Assert: {} ---", name);
                assertConsumer.accept(assertions);
            }
        } catch (AssertionError e) {
            logger.warn("Проверка не пройдена в '{}': {}", name, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Ошибка выполнения '{}': {}", name, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Выполняет действие и логирует его описание.
     *
     * @param description описание действия
     * @param action      выполняемое действие
     */
    protected void performAction(String description, Runnable action) {
        logger.info("ACTION: {}", description);
        if (action != null) {
            action.run();
        }
    }

    /**
     * Выполняет проверку и логирует её описание.
     *
     * @param description описание проверки
     * @param check       выполняемая проверка
     */
    protected void performCheck(String description, Runnable check) {
        logger.info("CHECK: {}", description);
        if (check != null) {
            check.run();
        }
    }
}
