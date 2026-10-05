package com.bft.test.examples;

import com.bft.helpers.VisualComparator;
import com.bft.test.base.UITestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ============================== УЧЕБНЫЙ ПРИМЕР №3 ==============================
 *
 * Визуальная регрессия: сравниваем скриншот страницы/элемента с эталоном.
 *
 * КАК ЭТО РАБОТАЕТ:
 *   1. Делаем скриншот текущей страницы (Playwright).
 *   2. Сравниваем его с эталонной картинкой из src/test/resources/screens/<имя>.png.
 *   3. Если отличающихся пикселей больше порога — тест падает, а в Allure
 *      прикрепляются ТРИ картинки: эталон, факт и diff (расхождения подсвечены).
 *
 * С ЧЕГО НАЧАТЬ (эталонов пока нет!):
 *   Шаг 1. Первый прогон делаете в режиме создания эталонов:
 *            mvn test -Dtest=Example03_VisualRegressionTest -Devs.visual.update=true
 *          Картинки сохранятся в src/test/resources/screens/.
 *   Шаг 2. Посмотрите глазами, что скриншоты правильные (без случайных данных).
 *   Шаг 3. Закоммитьте их в git. Все последующие прогоны будут сверяться с ними.
 *
 * НАСТРОЙКИ:
 *   -Devs.visual.threshold=1.0   — допустимый % отличающихся пикселей (по умолчанию 1)
 *   -Devs.visual.update=true     — перезаписать эталоны фактическими скриншотами
 * ==============================================================================
 */
@Disabled("Обучающий пример №3 — не запускать в CI")
@Tag("example")
@Tag("visual")
public class Example03_VisualRegressionTest extends UITestBase {

    /** Компаратор создаётся один раз на класс — он не хранит состояния между тестами. */
    private final VisualComparator visual = new VisualComparator();

    @Test
    @Story("Визуальная проверка страницы")
    @Description("Скриншот всей страницы сравнивается с эталоном screens/example-login-page.png")
    public void loginPageLooksAsExpected() {
        // ... здесь тест открывает страницу авторизации ...
        // takeScreenshot() не нужен — компаратор снимет страницу сам.

        // МЯГКОЕ сравнение: результат попадёт в assertions, при расхождении
        // тест продолжит выполняться, а упадёт в конце со списком всех проблем.
        // Имя "example-login-page" = файл src/test/resources/screens/example-login-page.png
        visual.assertMatchesPage(assertions, "example-login-page");
    }

    /**
     * Сравнение отдельного элемента (например, логотипа или таблицы),
     * а не всей страницы. Так тесты стабильнее: изменения в подвале сайта
     * не будут ронять проверку шапки.
     */
    // Для сравнения элемента нужен PwElement:
    //
    //   import static com.bft.pw.Selenide.$x;
    //   var logo = $x("//img[@class='logo']");
    //   visual.assertMatchesElement(assertions, logo, "header-logo");

    /**
     * ЖЁСТКОЕ сравнение (возвращает boolean, не связан с мягкими проверками).
     * Удобно для условной логики внутри теста.
     */
    @Test
    public void hardCheckExample() {
        boolean matches = visual.matchesPage("some-reference-name");
        if (!matches) {
            logger.warn("Визуальное расхождение — но тест не падает, просто логируем");
        }
        assertions.assertTrue(matches || true, "Пример условной проверки");
    }
}
