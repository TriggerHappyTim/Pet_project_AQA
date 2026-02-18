# TestData Builders

Модуль для создания тестовых данных с использованием Builder pattern и Faker для генерации реалистичных данных.

## Классы

### TestDataBuilder
Базовый класс для всех builders. Предоставляет:
- Интеграцию с Faker (русская локаль)
- Базовые методы для генерации данных
- Fluent API через метод `self()`

### UserTestDataBuilder
Builder для создания тестовых данных пользователей.

**Примеры использования:**

```java
// Дефолтный пользователь
UserData user = UserTestDataBuilder.createDefault().build();

// Пользователь с кастомными данными
UserData customUser = UserTestDataBuilder.createDefault()
    .withFirstName("Иван")
    .withLastName("Иванов")
    .withEmail("ivan@example.com")
    .withPhone("+7-999-123-45-67")
    .withSnils("123-456-789-01")
    .build();

// Случайный пользователь
UserData randomUser = UserTestDataBuilder.createRandom().build();

// Пользователь из TestUsers enum
UserData testUser = UserTestDataBuilder.fromTestUser(TestUsers.KRIVONOSOV_ALEXANDER).build();
```

### ReportTestDataBuilder
Builder для создания тестовых данных отчетов.

**Примеры использования:**

```java
// Дефолтный отчет
ReportData report = ReportTestDataBuilder.createDefault().build();

// Отчет ЕФС-1 с кастомными данными
ReportData efs1Report = ReportTestDataBuilder.createDefault()
    .withReportType(ReportFormType.EFS1)
    .withReportNumber("EFS-1-2024-001")
    .withPeriod(LocalDate.now().minusMonths(1), LocalDate.now())
    .withStatus("Черновик")
    .build();

// Случайный отчет
ReportData randomReport = ReportTestDataBuilder.createRandom().build();
```

## Зависимости

- `javafaker` версии 1.0.2 (уже добавлена в `pom.xml`)

## Расширение

Для создания нового builder:
1. Наследуйтесь от `TestDataBuilder<YourDataClass, YourBuilderClass>`
2. Реализуйте метод `build()`
3. Добавьте методы `with*()` для установки значений
4. Добавьте статические фабричные методы `createDefault()`, `createRandom()`
