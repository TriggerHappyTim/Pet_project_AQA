# Архитектура EVS Testing Framework

## 📐 Обзор архитектуры

EVS Testing Framework построен на принципах **чистой архитектуры** и **SOLID**. Фреймворк разделен на несколько слоев с четким разделением ответственности, что обеспечивает высокую поддерживаемость, тестируемость и расширяемость.

## 🏗️ Архитектурные принципы

### SOLID принципы
- **S**ingle Responsibility - каждый класс имеет одну ответственность
- **O**pen/Closed - классы открыты для расширения, закрыты для модификации
- **L**iskov Substitution - подклассы могут заменять базовые классы
- **I**nterface Segregation - клиенты не зависят от неиспользуемых интерфейсов
- **D**ependency Inversion - зависимости от абстракций, а не от конкретных реализаций

### Дополнительные принципы
- **DRY** (Don't Repeat Yourself) - избегание дублирования кода
- **Composition over Inheritance** - предпочтение композиции наследованию
- **Fail Fast** - раннее обнаружение ошибок
- **Convention over Configuration** - соглашения вместо конфигурации

## 📦 Структура пакетов

### Configuration Layer (Конфигурация)

```
config/                      # Конфигурация фреймворка
├── TestConfiguration.java      # Главная конфигурация
├── TestContext.java           # Контекст выполнения тестов
├── TestStrategy.java          # Базовый интерфейс стратегий
├── TestStrategyType.java      # Типы стратегий
├── ApiTestStrategy.java       # Стратегия API тестов
├── UITestStrategy.java        # Стратегия UI тестов
└── BaseTestStrategy.java      # Базовая стратегия
```

**Ответственность:**
- Глобальная конфигурация фреймворка
- Управление жизненным циклом тестов
- Предоставление контекста для всех компонентов

### UI Layer (Пользовательский интерфейс)

```
ui/
├── pages/                   # Page Object'ы (новый пакет)
│   ├── BasePage.java            # Базовый класс для страниц
│   ├── MainPage.java            # Главная страница приложения
│   └── LoginPage.java          # Страница авторизации
├── components/              # Переиспользуемые UI компоненты
│   ├── BaseComponent.java       # Базовый класс компонентов
│   ├── ButtonComponent.java     # Компоненты кнопок
│   ├── InputComponent.java      # Компоненты полей ввода
│   ├── SelectComponent.java     # Компоненты выпадающих списков
│   ├── CheckboxComponent.java   # Компоненты чекбоксов
│   ├── RadioButtonComponent.java # Компоненты радиокнопок
│   ├── DateComponent.java       # Компоненты дат
│   ├── TextareaComponent.java   # Компоненты текстовых областей
│   ├── FileComponent.java       # Компоненты загрузки файлов
│   ├── TableComponent.java      # Компоненты таблиц
│   ├── NavigationComponent.java # Компоненты навигации
│   └── StatusIndicatorComponent.java # Компоненты статусов
└── core/                    # Базовые классы и элементы
    ├── BasePage.java            # Базовый класс страниц (core)
    ├── element/                 # Умные элементы
    │   ├── SmartElement.java        # Умный элемент с ожиданиями
    │   ├── SmartElementList.java   # Список умных элементов
    │   └── ElementFactory.java     # Фабрика элементов
    └── wait/                    # Стратегии ожидания
        ├── WaitStrategy.java       # Интерфейс стратегий
        └── WaitStrategies.java     # Реализации стратегий

gui/                         # Page Object'ы (legacy, используется)
├── LoginPage.java           # Используется в SzvReportsSteps
├── MainPage.java            # Используется в тестах
└── CryptoProDemoPage.java   # Демо-страница КриптоПРО
```

**Ответственность:**
- Инкапсуляция UI элементов и их поведения
- Предоставление высокоуровневого API для взаимодействия с интерфейсом
- Повторное использование компонентов между страницами

### Browser Factory Layer (Фабрики браузеров)

```
browser/
└── factory/               # Конфигурация браузеров
    ├── BrowserFactoryManager.java   # Менеджер фабрик браузеров
    ├── BaseBrowserFactory.java      # Базовая фабрика браузеров
    ├── BrowserConfigFactory.java    # Фабрика конфигурации браузеров
    ├── ChromeBrowserFactory.java    # Фабрика Chrome
    ├── FirefoxBrowserFactory.java   # Фабрика Firefox
    └── YandexBrowserFactory.java   # Фабрика Yandex
```

**Ответственность:**
- Управление браузерами и их конфигурацией
- Настройка WebDriver'ов
- Поддержка разных браузеров и режимов (локальный, удаленный)

### Security Layer (Безопасность)

```
security/
├── CredentialManager.java          # Менеджер учетных данных
├── CredentialProvider.java         # Интерфейс провайдера учетных данных
├── EnvironmentCredentialProvider.java # Провайдер из переменных окружения
├── MockCredentialProvider.java     # Mock провайдер для тестов
├── TestUsers.java                  # Enum тестовых пользователей
├── TestUserSelector.java           # Селектор тестовых пользователей
├── OrganizationProfile.java       # Профиль организации
├── masking/                        # Маскировка данных
│   ├── SecureLogger.java               # Безопасное логирование
│   ├── CompositeDataMasker.java        # Композитный маскировщик
│   └── DataMasker.java                 # Базовый интерфейс маскировщика
└── providers/                      # Дополнительные провайдеры
    └── PropertiesCredentialProvider.java # Провайдер из properties файлов
```

**Ответственность:**
- Безопасное хранение и получение учетных данных
- Автоматическая маскировка чувствительных данных в логах
- Защита от утечек конфиденциальной информации
- Управление тестовыми пользователями через enum TestUsers

### Helpers Layer (Вспомогательные классы)

```
helpers/
├── ConfigReader.java           # Централизованное чтение конфигурации
├── StandardWaits.java          # Типовые ожидания для UI тестов
├── TestConfig.java             # Тестовые конфигурации и данные
├── DataFiller.java            # Заполнение данными
├── TestDataGenerator.java     # Генерация тестовых данных
├── api/                       # API helper классы
│   ├── Specifications.java        # RequestSpec/ResponseSpec для RestAssured
│   └── ApiCoreRequests.java       # Переиспользуемые API методы с retry
└── forms/                     # Работа с формами
    ├── FormElements.java          # Элементы форм
    ├── FormFacade.java            # Фасад для работы с формами
    ├── FormValidator.java         # Валидация форм
    └── InsuredPerson.java         # Модель застрахованного лица
```

**Ответственность:**
- Централизованное чтение конфигурации (ConfigReader)
- Типовые ожидания для UI (StandardWaits)
- API helper классы для RestAssured (Specifications, ApiCoreRequests)
- Доменная логика для работы с формами
- Генерация и управление тестовыми данными
- Валидация бизнес-правил

### Strategies Layer (Стратегии)

```
strategy/                      # Стратегии тестирования
├── TestExecutionStrategy.java     # Интерфейс стратегий выполнения
├── BaseTestExecutionStrategy.java # Базовая реализация стратегии
├── TestStrategyManager.java       # Менеджер стратегий
├── ExecutionStrategyType.java    # Типы стратегий выполнения
├── ApiTestExecutionStrategy.java  # Стратегия API тестов
├── CryptoProValidationStrategy.java # Стратегия валидации КриптоПРО
├── DataPreparationStrategy.java   # Стратегия подготовки данных
└── ValidationStrategy.java        # Общие стратегии валидации
```

**Ответственность:**
- Определение различных подходов к выполнению тестов
- Инкапсуляция сложной логики тестирования
- Переиспользование стратегий между тестами

### Test Infrastructure Layer (Тестовая инфраструктура)

```
test/
├── base/                    # Базовые классы тестов
│   ├── UITestBase.java          # Базовый класс UI тестов (extends BaseTest)
│   ├── ApiTestBase.java         # Базовый класс API тестов (extends BaseTest)
│   └── DataDrivenTestBase.java  # Базовый класс data-driven тестов (extends UITestBase)
├── annotations/             # Кастомные аннотации
│   ├── AutomationAction.java    # Действия автоматизации
│   ├── Requirement.java         # Требования
│   ├── TestEnvironment.java     # Среда тестирования
│   ├── TestPriority.java        # Приоритеты тестов
│   ├── TestType.java            # Типы тестов
│   ├── ZephyrTest.java          # Интеграция с Zephyr
│   └── AllureAnnotationProcessor.java # Процессор аннотаций
├── logging/                 # Логирование
│   ├── TestLogger.java          # Специализированный логгер
│   └── AllureIntegration.java   # Интеграция с Allure
└── examples/                # Примеры использования
    ├── ImprovedTestExamples.java    # Улучшенные примеры
    └── AdvancedReportingExample.java # Примеры отчетности

BaseTest.java                # Базовый класс всех тестов (корень пакета com.bft)
```

**Ответственность:**
- Предоставление базовой функциональности для тестов
- Кастомные аннотации для метаданных
- Специализированное логирование
- Примеры лучших практик

### TestData Layer (TestData Builders)

```
testdata/
├── TestDataBuilder.java          # Базовый класс для builders
├── UserTestDataBuilder.java     # Builder для создания пользователей
└── ReportTestDataBuilder.java   # Builder для создания отчетов
```

**Ответственность:**
- Создание тестовых данных через Builder pattern
- Интеграция с Faker для генерации реалистичных данных
- Упрощение создания тестовых объектов

### Monitoring Layer (Мониторинг)

```
monitoring/
├── PerformanceMonitor.java      # Отслеживание производительности тестов
└── FlakyTestDetector.java       # Выявление нестабильных тестов
```

**Ответственность:**
- Отслеживание времени выполнения тестов
- Выявление медленных тестов
- Автоматическое обнаружение нестабильных тестов
- Статистика по производительности

### Constants Layer (Константы)

```
constants/
├── TimeoutConstants.java         # Константы таймаутов для ожиданий
└── UrlConstants.java             # Константы URL для тестов
```

**Ответственность:**
- Централизация магических чисел (таймауты)
- Централизация URL адресов
- Улучшение поддерживаемости кода

### Steps Layer (Шаги бизнес-процессов)

```
steps/
└── SzvReportsSteps.java      # Шаги для работы с отчетами СЗВ
```

**Ответственность:**
- Инкапсуляция сложных бизнес-процессов
- Переиспользуемые шаги для тестов
- Упрощение написания тестов через высокоуровневые методы

### Integration Layer (Интеграции)

```
integration/
├── zephyr/                  # Интеграция с Zephyr
│   ├── ZephyrClient.java         # Интерфейс клиента
│   ├── ZephyrSquadClient.java   # Клиент для Zephyr Squad
│   ├── ZephyrScaleClient.java   # Клиент для Zephyr Scale
│   └── ZephyrClientFactory.java # Фабрика клиентов
├── jira/                    # Интеграция с Jira
│   └── JiraClient.java          # Клиент для Jira
├── allure/                  # Интеграция с Allure
│   ├── AllureResultsPublisher.java # Публикация результатов
│   └── AllureTestResult.java      # Модель результата
└── listeners/               # TestNG listeners
    └── ZephyrTestListener.java   # Listener для Zephyr
```

**Ответственность:**
- Интеграция с внешними системами (Zephyr, Jira)
- Публикация результатов тестов
- Автоматизация процессов тестирования

### Utils Layer (Утилиты)

```
utils/
└── CryptoProPluginVerifier.java  # Верификация плагина КриптоПРО
```

**Ответственность:**
- Специализированные утилиты для конкретных задач
- Вспомогательные функции общего назначения

## 🔄 Поток данных

### Инициализация теста

```
TestNG Suite
    ↓
BaseTest.setUp()
    ↓
TestConfiguration.initialize()
    ↓
Создание TestContext
    ↓
Инициализация браузера через BrowserFactoryManager
    ↓
Создание WebDriver
    ↓
Тест готов к выполнению
```

### Выполнение UI теста

```
UITestBase.arrangeActAssert()
    ↓
Arrange: подготовка данных
    ↓
Act: взаимодействие через Page Objects
    ↓
Page Object вызывает UI Components
    ↓
Components работают через SmartElement'ы
    ↓
SmartElement'ы используют Selenide
    ↓
Assert: валидация через SoftAssert
    ↓
Генерация Allure отчета
```

### Стратегическое выполнение

```
TestStrategyManager.executeTestWithStrategy()
    ↓
Выбор подходящей стратегии
    ↓
TestExecutionStrategy.execute()
    ↓
Выполнение специфической логики
    ↓
Валидация результатов
    ↓
Возврат TestResult
```

## 🏛️ Ключевые паттерны проектирования

### 1. Page Object Pattern
```java
public class MainPage extends BasePage<MainPage> {
    public MainPage performAction() {
        // инкапсуляция UI логики
        return this;
    }
}
```

### 2. Component Pattern
```java
public class ButtonComponent extends BaseComponent {
    public static ButtonComponent createButton(String text) {
        return new ButtonComponent(
            By.xpath("//button[contains(text(), '" + text + "')]"),
            "Button: " + text
        );
    }
}
```

### 3. Strategy Pattern
```java
public interface TestExecutionStrategy {
    TestResult execute(TestContext context);
}

public class CryptoProValidationStrategy implements TestExecutionStrategy {
    @Override
    public TestResult execute(TestContext context) {
        // специфическая логика
    }
}
```

### 4. Factory Pattern
```java
public class BrowserFactoryManager {
    public BrowserConfigFactory createFactory(String browserType) {
        switch (browserType) {
            case "chrome": return new ChromeBrowserFactory();
            case "firefox": return new FirefoxBrowserFactory();
            default: throw new IllegalArgumentException();
        }
    }
}
```

### 5. Builder Pattern (Fluent Interface)
```java
public class SmartElementBuilder {
    public SmartElementBuilder named(String name) {
        this.name = name;
        return this;
    }

    public SmartElementBuilder waitVisible() {
        this.waitStrategy = WaitStrategies.visibility();
        return this;
    }
}
```

### 6. Decorator Pattern
```java
public class SecureLogger implements Logger {
    private final Logger delegate;

    @Override
    public void info(String message, Object... args) {
        String maskedMessage = maskSensitiveData(message, args);
        delegate.info(maskedMessage);
    }
}
```

### 7. Composite Pattern
```java
public class CompositeDataMasker implements DataMasker {
    private final List<DataMasker> maskers;

    @Override
    public String mask(String data) {
        String result = data;
        for (DataMasker masker : maskers) {
            result = masker.mask(result);
        }
        return result;
    }
}
```

## 🔗 Зависимости между компонентами

### Компиляционные зависимости

```
Test Classes
    ↓ (extends)
BaseTest / UITestBase / ApiTestBase
    ↓ (uses)
Page Objects / Components
    ↓ (uses)
SmartElement / ElementFactory
    ↓ (uses)
Selenide WebDriver
```

### Runtime зависимости

```
TestExecutionStrategy
    ↔ TestContext
    ↔ BrowserFactoryManager
    ↔ CredentialManager
    ↔ TestLogger
```

## 🎯 Принципы масштабируемости

### Горизонтальное масштабирование
- Независимые компоненты могут разрабатываться параллельно
- Стратегии позволяют добавлять новые типы тестов без изменения существующих
- Фабрики браузеров поддерживают новые браузеры

### Вертикальное масштабирование
- Наследование от базовых классов позволяет расширять функциональность
- Композиция компонентов позволяет строить сложные структуры
- Аннотации позволяют добавлять метаданные без изменения логики

## 🧪 Тестируемость архитектуры

### Unit тестирование
```java
@Test
public void testButtonComponent() {
    // Моки для SmartElement
    SmartElement mockElement = mock(SmartElement.class);
    ButtonComponent button = new ButtonComponent(mockElement, "Test Button");

    button.click();

    verify(mockElement).click();
}
```

### Integration тестирование
```java
@Test
public void testPageObjectIntegration() {
    // Тестирование взаимодействия компонентов
    MainPage page = new MainPage();
    page.openTab("Reports")
        .clickButton("Generate")
        .verifyResult("Success");
}
```

### E2E тестирование
```java
@Test
public void testCompleteWorkflow() {
    // Полный сценарий через все слои
    TestStrategyManager manager = new TestStrategyManager();
    TestResult result = manager.executeTestWithStrategy(
        "full_workflow_test",
        TestStrategyType.UI_CRYPTO_PRO_VALIDATION
    );
    assertTrue(result.isSuccess());
}
```

## 🚀 Расширение архитектуры

### Добавление нового компонента

1. **Создать класс компонента**
```java
public class NewComponent extends BaseComponent {
    // Реализация специфической логики
}
```

2. **Добавить статические фабричные методы**
```java
public static NewComponent create(String selector, String name) {
    return new NewComponent(
        ElementFactory.css(selector).named(name).build(),
        name
    );
}
```

3. **Интегрировать в Page Object**
```java
public class MyPage extends BasePage<MyPage> {
    public MyPage useNewComponent() {
        NewComponent.create("#new-element", "New Element").performAction();
        return this;
    }
}
```

### Добавление новой стратегии

1. **Реализовать интерфейс**
```java
public class NewStrategy implements TestExecutionStrategy {
    @Override
    public TestStrategyType getType() {
        return TestStrategyType.NEW_STRATEGY;
    }

    @Override
    public TestResult execute(TestContext context) {
        // Логика выполнения
    }
}
```

2. **Зарегистрировать в менеджере**
```java
strategyManager.registerStrategy(new NewStrategy());
```

3. **Использовать в тестах**
```java
manager.executeTestWithStrategy("test", TestStrategyType.NEW_STRATEGY);
```

## 📊 Метрики качества архитектуры

### Цикломатическая сложность
- BaseComponent: 1 (идеально)
- Page Objects: 2-3 (хорошо)
- Стратегии: 3-5 (приемлемо)

### Связность (Cohesion)
- Высокая внутри компонентов
- Низкая между компонентами

### Связанность (Coupling)
- Абстрактная связанность через интерфейсы
- Зависимость от абстракций, не реализаций

## 🔄 Эволюция архитектуры

### Версия 1.0 - Базовая структура
- Простые Page Objects
- Базовая конфигурация
- Минимальная безопасность

### Версия 2.0 - Компонентная архитектура
- Введение компонентов
- Стратегический подход
- Расширенная безопасность

### Версия 3.0 - Enterprise-ready (планируется)
- Микросервисная архитектура
- Распределенное выполнение
- AI-powered тестирование
- Cloud интеграция

---

Эта архитектура обеспечивает надежную основу для роста и развития фреймворка, сохраняя при этом простоту использования и высокую поддерживаемость.</contents>
</xai:function_call">Создал детальную архитектурную документацию