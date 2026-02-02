# Улучшенная отчетность и логирование

Этот модуль предоставляет расширенную систему отчетности и логирования для автоматизированных тестов с глубокой интеграцией Allure.

## Архитектура

### Основные компоненты:

1. **Кастомные аннотации** - метаданные для тестов и отчетов
2. **TestLogger** - структурированная система логирования
3. **AllureIntegration** - автоматическое прикрепление данных
4. **AllureAnnotationProcessor** - обработчик аннотаций
5. **TestNG конфигурация** - интеграция с фреймворком

## Кастомные аннотации

### @TestType
Определяет тип теста для категоризации в отчетах.

```java
@TestType(TestType.Type.UI)
@TestType(value = TestType.Type.API, description = "REST API тестирование")
public class MyTest {
    // ...
}
```

### @TestPriority
Указывает приоритет и важность теста.

```java
@TestPriority(TestPriority.Priority.CRITICAL)
@TestPriority(value = TestPriority.Priority.HIGH, reason = "Блокирует релиз")
public void criticalTest() {
    // ...
}
```

### @Requirement
Связывает тест с требованиями/спецификациями.

```java
@Requirement(
    id = "REQ-001",
    name = "Пользователь должен авторизоваться",
    source = "Functional Requirements v2.0",
    version = "2.0",
    author = "Business Analyst",
    status = Requirement.Status.ACTIVE
)
public void loginTest() {
    // ...
}
```

### @AutomationAction
Описывает автоматизированные действия.

```java
@AutomationAction(
    value = "Ввод логина и пароля",
    type = AutomationAction.ActionType.INPUT,
    logLevel = AutomationAction.LogLevel.INFO,
    takeScreenshot = true,
    timeout = 30
)
public void performLogin() {
    // ...
}
```

### @TestEnvironment
Определяет требования к тестовому окружению.

```java
@TestEnvironment(
    browsers = {TestEnvironment.Browser.CHROME, TestEnvironment.Browser.FIREFOX},
    operatingSystems = {TestEnvironment.OS.WINDOWS},
    minJavaVersion = "11",
    requiredProperties = {"db.url", "api.key"}
)
public class EnvironmentSpecificTest {
    // ...
}
```

## Система логирования

### TestLogger
Расширенная система логирования с интеграцией Allure.

```java
private final TestLogger logger = TestLogger.forTest("MyTest");

@Test
public void myTest() {
    logger.testStarted();

    try {
        // Логирование действий
        logger.step("Открытие страницы", () -> {
            // код
        });

        logger.action("Клик по кнопке", () -> {
            // код
        }, true); // true = успех

        logger.check("Проверка результата", true);

        // Прикрепление данных
        logger.attachJson("response", responseData);
        logger.attachPerformanceMetrics("api_call", 150, 1024);

        logger.testFinished(true, 1000);

    } catch (Exception e) {
        logger.testFinished(false, 1000);
        throw e;
    }
}
```

### Методы TestLogger

#### Основные методы логирования
- `info(message)` - информационные сообщения
- `debug(message)` - отладочные сообщения
- `warn(message)` - предупреждения
- `error(message)` - ошибки

#### Специализированные методы
- `step(name, action)` - логирование шагов теста
- `action(name, runnable, success)` - логирование действий с результатом
- `check(name, result)` - логирование проверок

#### Прикрепление данных
- `attachScreenshot(name)` - скриншот
- `attachJson(name, data)` - JSON данные
- `attachFile(name, content)` - текстовый файл
- `attachPerformanceMetrics(operation, duration, memory)` - метрики производительности

## Allure интеграция

### Автоматическое прикрепление данных

```java
import static com.bft.test.logging.AllureIntegration.*;

@Test
public void comprehensiveTest() {
    // Автоматическое прикрепление данных
    attachScreenshot("test_start");
    attachPageInfo("Login Page");
    attachSystemInfo();
    attachTestConfiguration();
    attachBrowserLogs();
    attachNetworkLogs();

    // При ошибке
    try {
        // тест код
    } catch (Exception e) {
        attachErrorInfo("test_failure", e);
        attachDebugInfo("full_debug");
    }
}
```

### Комплексная отладочная информация

```java
// Прикрепляет всю доступную отладочную информацию
AllureIntegration.attachDebugInfo("context_name");

// Включает:
// - Скриншот
// - Информация о странице
// - Системная информация
// - Конфигурация теста
// - Логи браузера
```

## Базовые классы с логированием

### UITestBase
Расширенный базовый класс для UI тестов с встроенным логированием.

```java
public class MyUITest extends UITestBase {

    @Test
    public void myTest() {
        arrangeActAssert(
            // Arrange
            () -> testLogger.info("Подготовка данных"),

            // Act
            () -> performAction("Открытие страницы", () -> {
                page.openPageAndVerify("Home");
            }),

            // Assert
            (softAssert) -> performCheck("Проверка заголовка", () -> {
                softAssert.assertEquals(page.getTitle(), "Home Page");
            }),

            "Мой UI тест"
        );
    }
}
```

### DataDrivenTestBase
Базовый класс для параметризованных тестов с расширенной отчетностью.

```java
public class MyDataTest extends DataDrivenTestBase {

    @Test(dataProvider = "testData")
    public void parameterizedTest(String param1, String param2) {
        runParameterizedTest(new Object[]{param1, param2}, () -> {
            // Тест с параметрами
            testLogger.info("Тестирование с параметрами: {} {}", param1, param2);

            arrangeActAssert(
                () -> {},
                () -> performAction("Тестовое действие", () -> {
                    // код теста
                }),
                (softAssert) -> performCheck("Проверка", () -> {
                    softAssert.assertTrue(true);
                }),
                "Параметризованный тест: " + param1
            );
        });
    }
}
```

## Конфигурация TestNG

### testng.xml
```xml
<suite name="EVS Testing Suite" verbose="2" parallel="methods" thread-count="3">

    <listeners>
        <!-- Allure TestNG listener -->
        <listener class-name="io.qameta.allure.testng.AllureTestNg"/>

        <!-- Custom annotation processor -->
        <listener class-name="com.bft.test.annotations.AllureAnnotationProcessor"/>
    </listeners>

    <test name="UI Tests" group-by-instances="true">
        <groups>
            <run>
                <include name="ui"/>
                <include name="smoke"/>
                <include name="regression"/>
            </run>
        </groups>
        <packages>
            <package name="com.bft.LK_Insurence"/>
        </packages>
    </test>
</suite>
```

## Примеры использования

### Полный тест с аннотациями

```java
@TestType(TestType.Type.UI)
@TestPriority(TestPriority.Priority.HIGH)
@Requirement(
    id = "REQ-LOGIN-001",
    name = "Пользователь должен авторизоваться",
    source = "SRS v2.0"
)
@TestEnvironment(
    browsers = {TestEnvironment.Browser.CHROME},
    minJavaVersion = "11"
)
public class LoginTest extends UITestBase {

    private final TestLogger logger = TestLogger.forTest("LoginTest");

    @Test(description = "Успешная авторизация пользователя")
    @AutomationAction(value = "Авторизация пользователя", type = AutomationAction.ActionType.ACTION)
    public void successfulLoginTest() {
        logger.testStarted();

        arrangeActAssert(
            // Arrange
            () -> {
                logger.info("Подготовка тестовых учетных данных");
                AllureIntegration.attachTestConfiguration();
            },

            // Act
            () -> performAction("Вход в систему", () -> {
                // Получаем credentials из безопасного хранилища
                var credentials = CredentialManager.getInstance().getUserCredentials("test");
                loginPage.openPageAndVerify("Login Page")
                        .typeUsername(credentials.username)
                        .typePassword(credentials.password)
                        .clickLogin();
            }),

            // Assert
            (softAssert) -> performCheck("Проверка успешного входа", () -> {
                var credentials = CredentialManager.getInstance().getUserCredentials("test");
                softAssert.assertTrue(dashboardPage.isDisplayed(),
                    "Дашборд должен отображаться после входа");
                softAssert.assertEquals(userMenu.getUsername(), credentials.username,
                    "Имя пользователя должно отображаться в меню");
            }),

            "Успешная авторизация пользователя"
        );

        logger.testFinished(true, 5000);
    }
}
```

### Тест производительности с метриками

```java
@TestType(TestType.Type.PERFORMANCE)
@TestPriority(TestPriority.Priority.HIGH)
public class PerformanceTest extends UITestBase {

    @Test(description = "Тест производительности загрузки страницы")
    public void pageLoadPerformanceTest() {
        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory();

        arrangeActAssert(
            () -> testLogger.info("Начало замера производительности"),

            () -> {
                page.openPageAndVerify("Performance Test Page");
                // Дополнительные операции для замера
            },

            (softAssert) -> {
                long duration = System.currentTimeMillis() - startTime;
                long memoryUsed = Runtime.getRuntime().totalMemory() - startMemory;

                performCheck("Проверка времени загрузки", () -> {
                    softAssert.assertTrue(duration < 5000,
                        "Страница должна загружаться менее 5 секунд");
                });

                // Прикрепление метрик производительности
                testLogger.attachPerformanceMetrics("page_load", duration, memoryUsed / 1024);
                AllureIntegration.attachPerformanceMetrics("full_page_load", duration, memoryUsed / 1024);
            },

            "Тест производительности загрузки страницы"
        );
    }
}
```

## Генерация отчетов

### Команды для генерации отчетов

```bash
# Запуск тестов с генерацией Allure отчета
mvn clean test

# Генерация Allure отчета
mvn allure:report

# Открытие отчета в браузере
mvn allure:serve
```

### Структура отчета

Allure отчет будет содержать:
- **Параметры тестов** - из аннотаций и DataProvider'ов
- **Шаги выполнения** - из TestLogger.step()
- **Прикрепленные файлы** - скриншоты, логи, JSON данные
- **Метрики производительности** - время выполнения, потребление памяти
- **Информация об окружении** - системная информация, конфигурация
- **Логи браузера** - консольные сообщения
- **Сетевые логи** - HTTP запросы (если доступны)

## Лучшие практики

### Использование аннотаций

1. **Всегда указывайте @TestType** для категоризации тестов
2. **Используйте @TestPriority** для определения важности
3. **Связывайте тесты с требованиями** через @Requirement
4. **Документируйте действия** с @AutomationAction

### Логирование

1. **Используйте TestLogger** вместо обычного логгера
2. **Структурируйте логи** с помощью step(), action(), check()
3. **Прикрепляйте данные** при важных событиях
4. **Логируйте производительность** для длительных операций

### Отчетность

1. **Автоматически прикрепляйте** скриншоты при ошибках
2. **Добавляйте контекст** для отладки проблем
3. **Используйте метки** для фильтрации и поиска
4. **Документируйте окружение** для воспроизводимости

## Расширение системы

### Добавление новой аннотации

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAnnotation {
    String value();
    // дополнительные параметры
}
```

### Расширение AllureAnnotationProcessor

```java
// Добавить обработку новой аннотации
Optional.ofNullable(method.getAnnotation(CustomAnnotation.class))
    .ifPresent(annotation -> {
        Allure.label("custom.label", annotation.value());
        // дополнительная логика
    });
```

### Добавление нового типа логирования

```java
public void customLog(String message, Object... data) {
    // Кастомная логика логирования
    Allure.addAttachment("Custom Data", "application/json",
        objectMapper.writeValueAsString(data));
}
```

Эта система обеспечивает комплексную отчетность и логирование, делая отладку и анализ результатов тестирования максимально эффективными.