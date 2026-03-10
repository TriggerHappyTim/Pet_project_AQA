# Test Strategy Pattern Implementation

Этот модуль реализует паттерн Strategy для различных типов тестирования.

> **Примечание**: `ApiTestExecutionStrategy` закомментирован целиком. Активны только `CryptoProValidationStrategy` и `BaseTestExecutionStrategy`.

## Архитектура

### Файлы пакета (8):

- **TestExecutionStrategy** — интерфейс стратегий выполнения тестов
- **DataPreparationStrategy** — интерфейс стратегий подготовки данных
- **ValidationStrategy** — интерфейс стратегий валидации
- **TestStrategyManager** — менеджер стратегий
- **BaseTestExecutionStrategy** — абстрактный базовый класс (активен)
- **CryptoProValidationStrategy** — валидация CryptoPro через UI (активна)
- **ExecutionStrategyType** — типы стратегий выполнения
- **ApiTestExecutionStrategy** — закомментирован

## Быстрый старт

### Автоматический выбор стратегии

```java
@Test
public void myTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

    var context = manager.executeTest("crypto_pro_validation_test", softAssert);

    System.out.println("Выполнено за: " + context.getDuration() + " мс");
}
```

### Явное указание стратегии

```java
@Test
public void explicitStrategyTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

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

### Активные стратегии

#### CryptoProValidationStrategy
```java
// Базовая валидация
CryptoProValidationStrategy strategy = new CryptoProValidationStrategy();

// С проверкой всех статусов
CryptoProValidationStrategy strategy = new CryptoProValidationStrategy(true, false);

// С проверкой всех статусов и подписью
CryptoProValidationStrategy strategy = new CryptoProValidationStrategy(true, true);
```

#### BaseTestExecutionStrategy
Базовый абстрактный класс для создания кастомных стратегий выполнения.

## Создание кастомной стратегии

### 1. Создание стратегии выполнения

```java
public class CustomTestStrategy extends BaseTestExecutionStrategy<String> {

    public CustomTestStrategy() {
        super("Custom Strategy", 5);
    }

    @Override
    public TestStrategyType getType() {
        return TestStrategyType.UI_NAVIGATION;
    }

    @Override
    public boolean isApplicable(TestContext context) {
        return context.getTestName().contains("custom");
    }

    @Override
    protected void performPreparation(TestContext context) {
        System.out.println("Подготовка кастомного теста");
    }

    @Override
    protected void performExecution(TestContext context) {
        System.out.println("Выполнение кастомного теста");
        context.setActualResult("success");
    }

    @Override
    protected void performValidation(TestContext context, SoftAssert softAssert) {
        String result = (String) context.getActualResult();
        softAssert.assertEquals(result, "success");
    }

    @Override
    protected void performCleanup(TestContext context) {
        System.out.println("Очистка кастомного теста");
    }
}
```

### 2. Регистрация и использование

```java
@Test
public void customStrategyTest() {
    TestStrategyManager manager = TestStrategyManager.createDefault();

    manager.registerExecutionStrategy(new CustomTestStrategy());

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
        database.save(data);
    }

    @Override
    public void cleanupTestData(User data) {
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

manager.registerExecutionStrategy(new MyExecutionStrategy());
manager.registerDataStrategy(new MyDataStrategy());
manager.registerValidationStrategy(new MyValidationStrategy());

var context1 = manager.executeTest("auto_test", softAssert);
var context2 = manager.executeTestWithStrategy("explicit_test", TestStrategyType.UI_FORM_SUBMISSION, softAssert);
var context3 = manager.executeTestWithFullSetup("full_test", executionType, dataType, validationType, softAssert);

List<TestStrategyType> executionTypes = manager.getAvailableExecutionStrategies();
List<DataPreparationType> dataTypes = manager.getAvailableDataStrategies();
List<ValidationType> validationTypes = manager.getAvailableValidationStrategies();
```

## Лучшие практики

### 1. Принципы SOLID
- **Single Responsibility**: Каждая стратегия отвечает за один аспект
- **Open/Closed**: Новые стратегии не требуют изменения существующих
- **Liskov Substitution**: Стратегии взаимозаменяемы
- **Interface Segregation**: Четкие интерфейсы
- **Dependency Inversion**: Зависимости через интерфейсы

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
        context.setActualResult(result);
    } catch (Exception e) {
        context.setActualResult(e);
        throw e;
    }
}
```

### 4. Приоритизация
```java
@Override
public int getPriority() {
    return 10; // Высокий приоритет
}
```

## Примеры в проекте

### CryptoPro стратегии
- `CryptoProValidationStrategy` — валидация плагина через UI

### Тесты с использованием стратегий
- `ExampleTest` — демонстрация использования
- `CryptoProCertificateTest` — интеграция стратегий в тесты

## Расширение функциональности

### Добавление нового типа стратегии

1. Добавить значение в `TestStrategyType` (или `ExecutionStrategyType`)
2. Создать реализацию интерфейса
3. Зарегистрировать в `TestStrategyManager`
4. Обновить документацию

Этот паттерн обеспечивает гибкость, поддерживаемость и расширяемость системы тестирования.
