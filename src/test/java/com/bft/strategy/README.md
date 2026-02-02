# Test Strategy Pattern Implementation

Этот модуль реализует паттерн Strategy для различных типов тестирования.

## Архитектура

### Основные компоненты:

1. **TestExecutionStrategy** - интерфейс стратегий выполнения тестов
2. **DataPreparationStrategy** - интерфейс стратегий подготовки данных
3. **ValidationStrategy** - интерфейс стратегий валидации
4. **TestStrategyManager** - менеджер для управления стратегиями
5. **BaseTestExecutionStrategy** - абстрактный базовый класс

## Быстрый старт

### Автоматический выбор стратегии

```java
@Test
public void myTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

    // Менеджер автоматически выберет подходящую стратегию
    var context = manager.executeTest("crypto_pro_validation_test", softAssert);

    System.out.println("Выполнено за: " + context.getDuration() + " мс");
}
```

### Явное указание стратегии

```java
@Test
public void explicitStrategyTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

    // Явно указываем тип стратегии
    var context = manager.executeTestWithStrategy(
        "my_test",
        TestStrategyType.UI_CRYPTO_PRO_VALIDATION,
        softAssert
    );
}
```

### Полная настройка стратегий

```java
@Test
public void fullStrategyTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

    // Настройка всех аспектов тестирования
    var context = manager.executeTestWithFullSetup(
        "full_test",
        TestStrategyType.UI_CRYPTO_PRO_VALIDATION,
        DataPreparationStrategy.DataPreparationType.DATABASE,
        ValidationStrategy.ValidationType.STRICT,
        softAssert
    );
}
```

## Реализованные стратегии

### UI стратегии

#### CryptoProValidationStrategy
```java
// Базовая валидация
CryptoProValidationStrategy strategy = new CryptoProValidationStrategy();

// С проверкой всех статусов
CryptoProValidationStrategy strategy = new CryptoProValidationStrategy(true, false);

// С проверкой всех статусов и подписью
CryptoProValidationStrategy strategy = new CryptoProValidationStrategy(true, true);
```

### API стратегии

#### ApiTestExecutionStrategy
```java
// Простой GET запрос
ApiTestExecutionStrategy apiStrategy = new ApiTestExecutionStrategy(
    "/health", "GET", 200
);

// Полная настройка
ApiTestExecutionStrategy apiStrategy = new ApiTestExecutionStrategy(
    "http://api.example.com",
    "/users/123",
    "GET",
    Map.of("Authorization", "Bearer token"),
    Map.of("format", "json"),
    "{\"param\": \"value\"}",
    200
);
```

## Создание кастомной стратегии

### 1. Создание стратегии выполнения

```java
public class CustomTestStrategy extends BaseTestExecutionStrategy<String> {

    public CustomTestStrategy() {
        super("Custom Strategy", 5);
    }

    @Override
    public TestStrategyType getType() {
        return TestStrategyType.UI_NAVIGATION; // или создайте новый тип
    }

    @Override
    public boolean isApplicable(TestContext context) {
        return context.getTestName().contains("custom");
    }

    @Override
    protected void performPreparation(TestContext context) {
        // Подготовка данных и окружения
        System.out.println("Подготовка кастомного теста");
    }

    @Override
    protected void performExecution(TestContext context) {
        // Основная логика теста
        System.out.println("Выполнение кастомного теста");
        context.setActualResult("success");
    }

    @Override
    protected void performValidation(TestContext context, SoftAssert softAssert) {
        // Валидация результатов
        String result = (String) context.getActualResult();
        softAssert.assertEquals(result, "success");
    }

    @Override
    protected void performCleanup(TestContext context) {
        // Очистка после теста
        System.out.println("Очистка кастомного теста");
    }
}
```

### 2. Регистрация и использование

```java
@Test
public void customStrategyTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

    // Регистрация стратегии
    manager.registerExecutionStrategy(new CustomTestStrategy());

    // Выполнение
    var context = manager.executeTest("custom_test_name", softAssert);

    softAssert.assertAll();
}
```

## Стратегии подготовки данных

### Создание стратегии подготовки данных

```java
public class DatabaseDataStrategy implements DataPreparationStrategy<User> {

    @Override
    public User createTestData() {
        return new User("test@example.com", "Test User");
    }

    @Override
    public void prepareTestData(User data) {
        // Сохранение в базу данных
        database.save(data);
    }

    @Override
    public void cleanupTestData(User data) {
        // Удаление из базы данных
        database.delete(data);
    }

    @Override
    public User generateRandomData() {
        return new User(
            "user" + System.currentTimeMillis() + "@example.com",
            "Random User " + System.currentTimeMillis()
        );
    }

    @Override
    public boolean validateTestData(User data) {
        return data != null && data.getEmail() != null;
    }

    @Override
    public DataPreparationType getType() {
        return DataPreparationType.DATABASE;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
```

## Стратегии валидации

### Создание стратегии валидации

```java
public class StrictValidationStrategy implements ValidationStrategy<Order> {

    @Override
    public void validate(Order actual, Order expected, SoftAssert softAssert) {
        softAssert.assertEquals(actual.getId(), expected.getId());
        softAssert.assertEquals(actual.getStatus(), expected.getStatus());
    }

    @Override
    public void validateAdditionalConditions(Order result, SoftAssert softAssert) {
        softAssert.assertNotNull(result.getCreatedDate());
        softAssert.assertTrue(result.getTotal() > 0);
    }

    @Override
    public void validateBusinessRules(Order result, SoftAssert softAssert) {
        // Проверка бизнес-правил
        if (result.getStatus().equals("COMPLETED")) {
            softAssert.assertNotNull(result.getCompletedDate());
        }
    }

    @Override
    public void validatePerformance(long executionTime, SoftAssert softAssert) {
        softAssert.assertTrue(executionTime < 5000, "Операция должна выполниться менее чем за 5 секунд");
    }

    @Override
    public void validateDataIntegrity(Order result, SoftAssert softAssert) {
        softAssert.assertTrue(isValidOrder(result), "Заказ должен быть валидным");
    }

    @Override
    public ValidationType getType() {
        return ValidationType.STRICT;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean isResultSuccessful(Order result) {
        return result != null && "COMPLETED".equals(result.getStatus());
    }

    private boolean isValidOrder(Order order) {
        return order != null &&
               order.getId() != null &&
               order.getStatus() != null &&
               order.getTotal() >= 0;
    }
}
```

## Контекст выполнения

### Использование контекста

```java
TestExecutionStrategy.TestContext context = strategy.createContext();
context.setTestName("my_test");
context.setTestData(testData);
context.setExpectedResult(expectedResult);

// После выполнения
Object actualResult = context.getActualResult();
long duration = context.getDuration();
```

## Менеджер стратегий

### Основные методы

```java
TestStrategyManager manager = TestStrategyManager.createDefault();

// Регистрация стратегий
manager.registerExecutionStrategy(new MyExecutionStrategy());
manager.registerDataStrategy(new MyDataStrategy());
manager.registerValidationStrategy(new MyValidationStrategy());

// Выполнение тестов
var context1 = manager.executeTest("auto_test", softAssert);
var context2 = manager.executeTestWithStrategy("explicit_test", TestStrategyType.UI_FORM_SUBMISSION, softAssert);
var context3 = manager.executeTestWithFullSetup("full_test", executionType, dataType, validationType, softAssert);

// Проверка доступных стратегий
List<TestStrategyType> executionTypes = manager.getAvailableExecutionStrategies();
List<DataPreparationType> dataTypes = manager.getAvailableDataStrategies();
List<ValidationType> validationTypes = manager.getAvailableValidationStrategies();
```

## Лучшие практики

### 1. Принципы SOLID
- **Single Responsibility**: Каждая стратегия отвечает за один аспект тестирования
- **Open/Closed**: Добавление новых стратегий не требует изменения существующих
- **Liskov Substitution**: Все стратегии взаимозаменяемы
- **Interface Segregation**: Четкие интерфейсы для разных нужд
- **Dependency Inversion**: Зависимости инвертированы через интерфейсы

### 2. Соглашения по именованию
- Стратегии выполнения: `*ExecutionStrategy`
- Стратегии данных: `*DataStrategy`
- Стратегии валидации: `*ValidationStrategy`
- Типы: `*Type`

### 3. Обработка ошибок
```java
@Override
protected void performExecution(TestContext context) {
    try {
        // Логика выполнения
        context.setActualResult(result);
    } catch (Exception e) {
        context.setActualResult(e);
        throw e; // Передаем исключение выше
    }
}
```

### 4. Логирование
```java
@Override
protected void performPreparation(TestContext context) {
    System.out.println("Подготовка: " + context.getTestName());
    // ... подготовка ...
}

@Override
protected void performCleanup(TestContext context) {
    System.out.println("Очистка: " + context.getTestName());
    // ... очистка ...
}
```

### 5. Приоритизация
```java
@Override
public int getPriority() {
    // Чем выше приоритет, тем предпочтительнее стратегия
    return 10; // Высокий приоритет
}
```

## Примеры в проекте

### CryptoPro стратегии
- `CryptoProValidationStrategy` - валидация плагина через UI
- `ApiTestExecutionStrategy` - API тестирование

### Тесты с использованием стратегий
- `ExampleTest` - демонстрация различных способов использования
- `CryptoProCertificateTest` - интеграция стратегий в реальные тесты

## Расширение функциональности

### Добавление нового типа стратегии

1. Добавить значение в `TestStrategyType`
2. Создать реализацию интерфейса
3. Зарегистрировать в `TestStrategyManager`
4. Обновить документацию

### Интеграция с CI/CD

```yaml
# GitLab CI
test:strategy:
  script:
    - mvn test -Dtest.strategy=UI_CRYPTO_PRO_VALIDATION
  artifacts:
    reports:
      strategy_results: results/
```

Этот паттерн обеспечивает гибкость, поддерживаемость и расширяемость системы тестирования.