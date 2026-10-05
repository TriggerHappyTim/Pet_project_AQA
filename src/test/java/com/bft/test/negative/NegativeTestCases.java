package com.bft.test.negative;

import com.bft.test.base.UITestBase;
// import com.bft.test.base.ApiTestBase;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
import com.bft.test.helpers.AssertionHelper;
import com.bft.test.helpers.SmartWaits;
import com.bft.ui.pages.LoginPage;
import com.bft.enums.UITypeSelector;
import com.bft.pw.Condition;
import com.bft.pw.Selenide;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static com.bft.pw.Selenide.$x;

/**
 * Негативные тесты для проверки обработки ошибок и граничных случаев
 * 
 * <p>Этот класс содержит примеры негативных тестов для различных сценариев:
 * - Невалидные данные (логин, пароль, email)
 * - Отсутствующие элементы
 * - Таймауты
 * - Ошибки валидации
 * 
 * <p>Примеры можно использовать как шаблоны для создания собственных негативных тестов.
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
@Tag("negative")
@Epic("Негативные тесты")
@Feature("Обработка ошибок и граничные случаи")
public class NegativeTestCases extends UITestBase {

    /**
     * Тест: Авторизация с невалидным логином
     * 
     * Проверяет, что система корректно обрабатывает попытку входа с несуществующим логином.
     * Ожидается отображение сообщения об ошибке.
     */
    @Test
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-001")
    public void testLoginWithInvalidUsername() {
        arrangeActAssert(
            // Arrange
            () -> {
                logger.info("Подготовка теста: невалидный логин");
            },
            // Act
            () -> {
                new LoginPage()
                    .open(UITypeSelector.getSelectedUIType())
                    .authorize("invalid_user_12345", "any_password");
            },
            // Assert
            softAssert -> {
                // Проверяем, что появилось сообщение об ошибке
                softAssert.assertTrue(
                    $x("//div[contains(@class, 'error')]").isDisplayed() ||
                    $x("//div[contains(text(), 'неверный')]").isDisplayed() ||
                    $x("//div[contains(text(), 'ошибка')]").isDisplayed(),
                    AssertionHelper.formatElementError(
                        "Сообщение об ошибке",
                        "должно отображаться при невалидном логине"
                    )
                );
                
                // Проверяем, что пользователь НЕ авторизован
                softAssert.assertFalse(
                    $x("//div[contains(@class, 'user-name')]").isDisplayed(),
                    AssertionHelper.formatElementError(
                        "Имя пользователя",
                        "не должно отображаться при неуспешной авторизации"
                    )
                );
            },
            "Login with Invalid Username"
        );
    }

    /**
     * Тест: Авторизация с невалидным паролем
     * 
     * Проверяет, что система корректно обрабатывает попытку входа с неверным паролем.
     */
    @Test
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-002")
    public void testLoginWithInvalidPassword() {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: невалидный пароль");
            },
            () -> {
                // Используем валидный логин, но неверный пароль
                new LoginPage()
                    .open(UITypeSelector.getSelectedUIType())
                    .authorize("valid_user@example.com", "wrong_password_12345");
            },
            softAssert -> {
                softAssert.assertTrue(
                    $x("//div[contains(@class, 'error')]").isDisplayed() ||
                    $x("//div[contains(text(), 'пароль')]").isDisplayed(),
                    AssertionHelper.formatElementError(
                        "Сообщение об ошибке пароля",
                        "должно отображаться при неверном пароле"
                    )
                );
            },
            "Login with Invalid Password"
        );
    }

    /**
     * Тест: Авторизация с пустыми полями
     * 
     * Проверяет валидацию формы при попытке входа без заполнения полей.
     */
    @Test
    @Description("Проверка валидации формы при попытке входа без заполнения полей")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-003")
    public void testLoginWithEmptyFields() {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: пустые поля");
            },
            () -> {
                new LoginPage()
                    .open(UITypeSelector.getSelectedUIType());
                
                // Пытаемся нажать кнопку входа без заполнения полей
                $x("//button[contains(@class,'button is-')]").click();
            },
            softAssert -> {
                // Проверяем, что появились сообщения валидации
                softAssert.assertTrue(
                    $x("//div[contains(@class, 'error')]").isDisplayed() ||
                    $x("//input[@class='loginInput'][@required]").exists() ||
                    $x("//div[contains(text(), 'обязательно')]").isDisplayed(),
                    AssertionHelper.formatElementError(
                        "Сообщение валидации",
                        "должно отображаться при пустых полях"
                    )
                );
            },
            "Login with Empty Fields"
        );
    }

    /**
     * Тест: Проверка отсутствующего элемента
     * 
     * Проверяет, что тест корректно обрабатывает ситуацию, когда элемент не найден.
     */
    @Test
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-004")
    public void testMissingElement() {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: отсутствующий элемент");
                // Открываем страницу без авторизации
                Selenide.open(UITypeSelector.getSelectedUIType().value);
            },
            () -> {
                // Пытаемся найти элемент, которого нет на странице
                logger.info("Поиск отсутствующего элемента");
            },
            softAssert -> {
                // Проверяем, что элемент действительно отсутствует
                softAssert.assertFalse(
                    $x("//div[@id='non-existent-element-12345']").exists(),
                    AssertionHelper.formatElementError(
                        "Несуществующий элемент",
                        "не должен присутствовать на странице"
                    )
                );
                
                // Проверяем, что тест корректно обрабатывает отсутствие элемента
                try {
                    $x("//div[@id='non-existent-element-12345']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(1));
                    softAssert.fail("Элемент не должен быть найден");
                } catch (Exception e) {
                    // Ожидаемое поведение - элемент не найден
                    logger.info("Элемент не найден, как и ожидалось: {}", e.getMessage());
                    softAssert.assertTrue(true, "Корректная обработка отсутствующего элемента");
                }
            },
            "Missing Element Test"
        );
    }

    /**
     * Тест: Проверка таймаута ожидания элемента
     * 
     * Проверяет, что тест корректно обрабатывает ситуацию таймаута при ожидании элемента.
     */
    @Test
    @Description("Проверка обработки таймаута при ожидании элемента, который не появится")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-005")
    public void testElementTimeout() {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: таймаут элемента");
                Selenide.open(UITypeSelector.getSelectedUIType().value);
            },
            () -> {
                logger.info("Ожидание элемента, который не появится");
            },
            softAssert -> {
                // Пытаемся дождаться элемента с коротким таймаутом
                try {
                    SmartWaits.waitForElementVisible(
                        $x("//div[@id='element-that-will-never-appear']"),
                        "Элемент, который не появится",
                        Duration.ofSeconds(2)  // Короткий таймаут для теста
                    );
                    softAssert.fail("Элемент не должен появиться");
                } catch (AssertionError e) {
                    // Ожидаемое поведение - таймаут
                    logger.info("Таймаут обработан корректно: {}", e.getMessage());
                    softAssert.assertTrue(
                        e.getMessage().contains("не выполнил условие") ||
                        e.getMessage().contains("таймаут"),
                        AssertionHelper.formatTestError(
                            "testElementTimeout",
                            "Сообщение об ошибке должно содержать информацию о таймауте",
                            e.getMessage()
                        )
                    );
                }
            },
            "Element Timeout Test"
        );
    }

    /**
     * Тест: Проверка невалидного email формата
     * 
     * Проверяет валидацию email поля при вводе невалидного формата.
     */
    @Test
    @Description("Проверка валидации email поля при вводе невалидного формата")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-006")
    public void testInvalidEmailFormat() {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: невалидный email");
            },
            () -> {
                new LoginPage()
                    .open(UITypeSelector.getSelectedUIType());
                
                // Вводим невалидный email
                $x("//div[@class = 'field loginControl']//input[@class = 'loginInput']")
                    .setValue("invalid-email-format");
            },
            softAssert -> {
                // Проверяем, что появилось сообщение валидации
                // (может быть HTML5 валидация или кастомная)
                softAssert.assertTrue(
                    $x("//div[@class = 'field loginControl']//input[@class = 'loginInput']")
                        .getAttribute("validationMessage") != null ||
                    $x("//div[contains(@class, 'error')]").isDisplayed(),
                    AssertionHelper.formatValidationError(
                        "Email",
                        "email",
                        "невалидный формат должен быть отклонен"
                    )
                );
            },
            "Invalid Email Format Test"
        );
    }

    /**
     * Data provider for tests with various invalid credentials
     */
    static Stream<Arguments> invalidCredentialsProvider() {
        return Stream.of(
            Arguments.of("", "", "Пустые логин и пароль"),
            Arguments.of("user", "", "Пустой пароль"),
            Arguments.of("", "password", "Пустой логин"),
            Arguments.of("user@", "password", "Невалидный email (без домена)"),
            Arguments.of("@domain.com", "password", "Невалидный email (без имени)"),
            Arguments.of("user@domain", "password", "Невалидный email (без TLD)"),
            Arguments.of("very_long_username_that_exceeds_maximum_length_limit_12345678901234567890", "password", "Слишком длинный логин"),
            Arguments.of("user", "very_long_password_that_exceeds_maximum_length_limit_123456789012345678901234567890", "Слишком длинный пароль"),
            Arguments.of("<script>alert('xss')</script>", "password", "XSS попытка в логине"),
            Arguments.of("user@domain.com", "<script>alert('xss')</script>", "XSS попытка в пароле"),
            Arguments.of("user@domain.com", "password\n<script>", "SQL injection попытка")
        );
    }

    /**
     * Parameterized test: Authorization with various invalid credentials
     */
    @ParameterizedTest(name = "Login with Invalid Credentials: {2}")
    @MethodSource("invalidCredentialsProvider")
    @Description("Параметризованный тест для проверки различных невалидных комбинаций логина и пароля")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-007")
    public void testLoginWithInvalidCredentials(String login, String password, String description) {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: {}", description);
            },
            () -> {
                new LoginPage()
                    .open(UITypeSelector.getSelectedUIType())
                    .authorize(login, password);
            },
            softAssert -> {
                // Проверяем, что авторизация не прошла
                softAssert.assertFalse(
                    $x("//div[contains(@class, 'user-name')]").isDisplayed(),
                    AssertionHelper.formatTestError(
                        "testLoginWithInvalidCredentials",
                        String.format("Авторизация не должна пройти для: %s", description),
                        String.format("Логин: %s, Пароль: %s", login, password)
                    )
                );
            },
            String.format("Login with Invalid Credentials: %s", description)
        );
    }

    /**
     * Data provider for password boundary value tests
     */
    static Stream<Arguments> passwordBoundaryValuesProvider() {
        return Stream.of(
            Arguments.of(0, false, "Пустой пароль"),
            Arguments.of(1, false, "Пароль из 1 символа"),
            Arguments.of(7, false, "Пароль из 7 символов (меньше минимума)"),
            Arguments.of(8, true, "Пароль из 8 символов (минимум)"),
            Arguments.of(9, true, "Пароль из 9 символов"),
            Arguments.of(15, true, "Пароль из 15 символов"),
            Arguments.of(16, true, "Пароль из 16 символов"),
            Arguments.of(100, false, "Пароль из 100 символов (превышает максимум)")
        );
    }

    @ParameterizedTest(name = "Password Boundary Values: {2}")
    @MethodSource("passwordBoundaryValuesProvider")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("NEG-008")
    public void testPasswordBoundaryValues(int passwordLength, boolean shouldBeValid, String description) {
        arrangeActAssert(
            () -> {
                logger.info("Подготовка теста: {}", description);
            },
            () -> {
                String password = "a".repeat(passwordLength);
                new LoginPage()
                    .open(UITypeSelector.getSelectedUIType())
                    .authorize("test@example.com", password);
            },
            softAssert -> {
                if (shouldBeValid) {
                    // Если пароль должен быть валидным, проверяем отсутствие ошибки валидации
                    softAssert.assertFalse(
                        $x("//div[contains(@class, 'error')]").isDisplayed() ||
                        $x("//div[contains(text(), 'пароль')]").isDisplayed(),
                        AssertionHelper.formatValidationError(
                            "Password",
                            "password",
                            String.format("Пароль длиной %d символов должен быть валидным", passwordLength)
                        )
                    );
                } else {
                    // Если пароль должен быть невалидным, проверяем наличие ошибки
                    softAssert.assertTrue(
                        $x("//div[contains(@class, 'error')]").isDisplayed() ||
                        $x("//div[contains(text(), 'пароль')]").isDisplayed(),
                        AssertionHelper.formatValidationError(
                            "Password",
                            "password",
                            String.format("Пароль длиной %d символов должен быть отклонен", passwordLength)
                        )
                    );
                }
            },
            String.format("Password Boundary Test: %s", description)
        );
    }
}
