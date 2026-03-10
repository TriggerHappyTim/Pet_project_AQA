# Test Configuration Layer

Этот модуль предоставляет слой абстракции для конфигурации автоматизированных тестов.

> **Примечание**: Код API стратегии сохранён в проекте, но закомментирован. Активна только UI стратегия.

## Архитектура

### Файлы пакета (7):

1. **TestStrategy** — интерфейс стратегий тестирования
2. **TestConfig** — конфигурационные данные
3. **TestContext** — контекст выполнения тестов (методы ApiTestStrategy закомментированы)
4. **TestConfiguration** — основной класс конфигурации (`isApiTest()` закомментирован)
5. **BaseTestStrategy** — базовый класс стратегий
6. **UITestStrategy** — для UI тестирования (активна)
7. **TestStrategyType** — тип стратегии (значение API закомментировано)

## Быстрый старт

### Автоматическая конфигурация

```java
public class MyTest extends BaseTest {
    // Конфигурация инициализируется автоматически
    // Доступна только UI стратегия
}
```

### Явное указание стратегии

```java
public class UITest extends BaseTest {
    @BeforeSuite
    public void initializeTestConfiguration() {
        TestConfiguration.initialize(TestStrategyType.UI);
    }
}
```

### Кастомная конфигурация

```java
public class CustomTest extends BaseTest {
    @BeforeSuite
    public void initializeTestConfiguration() {
        TestConfig config = new TestConfig()
            .setBrowser("chrome")
            .setHeadless(true)
            .setEnvironment("test");

        TestConfiguration.initialize(config);
    }
}
```

## Стратегии

Активна только **UI стратегия** (`UITestStrategy`). API стратегия закомментирована.

## Использование в тестах

### Получение конфигурации

```java
@Test
public void myTest() {
    // Получение текущей конфигурации
    TestConfig config = TestConfiguration.getCurrentConfig();

    // Получение стратегии
    TestStrategy strategy = TestConfiguration.getCurrentStrategy();

    // Проверка типа теста (только UI)
    if (TestConfiguration.isUITest()) {
        // UI тест логика
    }

    // Получение SoftAssert
    SoftAssert softAssert = TestConfiguration.getSoftAssert();
}
```

### Работа с браузерами (UI тесты)

```java
@Test
public void browserTest() {
    if (TestConfiguration.isUITest()) {
        BrowserFactoryManager factoryManager = TestConfiguration.getBrowserFactoryManager();
        BrowserConfigFactory browserFactory = factoryManager.createFactory("chrome");

        // Работа с фабрикой браузеров
    }
}
```

## Конфигурационные параметры

### Переменные окружения

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `BROWSER` | Тип браузера | `chrome` |
| `BROWSER_VERSION` | Версия браузера | `null` |
| `HEADLESS` | Режим без интерфейса | `false` |
| `CRYPTOPRO_PATH` | Путь к расширению CryptoPro | `src/test/resources/...` |
| `ENVIRONMENT` | Окружение (dev/test/uat) | `dev` |

### Свойства системы

| Свойство | Описание |
|----------|----------|
| `selenide.browser` | Тип браузера |
| `selenide.browserVersion` | Версия браузера |
| `selenide.headless` | Режим без интерфейса |
| `selenide.remote` | URL удаленного Selenium |

## Создание новой стратегии

```java
public class MobileTestStrategy extends BaseTestStrategy {

    public MobileTestStrategy(TestConfig config) {
        super(config);
    }

    @Override
    public TestStrategyType getType() {
        return TestStrategyType.MOBILE;
    }

    @Override
    public boolean isApplicable() {
        return checkMobileEnvironment();
    }

    @Override
    protected void setupEnvironmentVariables() {
        // Настройка переменных для мобильного тестирования
    }

    @Override
    protected void performPreTestActions() {
        // Действия перед мобильным тестом
    }

    @Override
    protected void performPostTestActions() {
        // Действия после мобильного теста
    }

    @Override
    protected boolean checkEnvironmentAvailability() {
        return true;
    }
}
```

## Примеры запуска

### UI тестирование

```bash
# Локальный Chrome
mvn test

# Firefox в CI
BROWSER=firefox mvn test

# Удаленный Chrome
mvn test -Dselenide.remote=http://localhost:4444/wd/hub

# Headless режим
HEADLESS=true mvn test
```

## Расширение функциональности

### Добавление новой стратегии

1. Создайте класс, наследующий `BaseTestStrategy`
2. Реализуйте абстрактные методы
3. Добавьте тип в `TestStrategyType`
4. Обновите `TestContext.createStrategy()` и `getAvailableStrategies()`

### Кастомная конфигурация

```java
TestConfig customConfig = new TestConfig()
    .setBrowser("custom_browser")
    .setEnvironment("staging")
    .setTestSuite("regression");

TestConfiguration.initialize(customConfig);
```

## Лучшие практики

1. **Инициализация**: Всегда инициализируйте конфигурацию в `@BeforeSuite`
2. **Стратегии**: Используйте UI стратегию для тестов
3. **Конфигурация**: Используйте переменные окружения для разных сред
4. **Обработка ошибок**: Проверяйте доступность окружения перед тестами
5. **Чистка**: Используйте `TestConfiguration.reset()` в интеграционных тестах

## Диагностика

### Проверка конфигурации

```java
@Test
public void configurationTest() {
    System.out.println("Current context: " + TestConfiguration.getCurrentContext());
    System.out.println("Strategy: " + TestConfiguration.getCurrentStrategy().getType());
    System.out.println("Config: " + TestConfiguration.getCurrentConfig());
}
```

### Логирование

Конфигурация автоматически логирует:
- Выбранную стратегию
- Параметры конфигурации
- Статус инициализации
- Ошибки конфигурации
