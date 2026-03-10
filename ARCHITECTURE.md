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
config/                          # Конфигурация фреймворка (7 файлов)
├── TestConfiguration.java          # Главная конфигурация (Singleton)
├── TestContext.java                # Контекст выполнения тестов
├── TestConfig.java                 # Настройки теста (браузер, таймауты, credentials)
├── TestStrategy.java               # Интерфейс стратегий
├── BaseTestStrategy.java           # Абстрактная реализация стратегии
├── UITestStrategy.java             # Стратегия UI тестов (основная)
└── TestStrategyType.java           # Enum типов стратегий (UI; API — закомментировано)
```

**Ответственность:**
- Глобальная конфигурация фреймворка
- Управление жизненным циклом тестов
- Предоставление контекста для всех компонентов
- Выбор стратегии выполнения (UI)

### UI Layer (Пользовательский интерфейс)

```
ui/                              # 18 файлов
├── pages/                       # Page Objects (новый пакет)
│   ├── BasePage.java                # Базовый класс для страниц
│   ├── MainPage.java                # Главная страница ЛК (~80 методов)
│   └── LoginPage.java              # Страница авторизации
├── component/                   # 12 переиспользуемых UI-компонентов
│   ├── BaseComponent.java           # Базовый класс компонентов
│   ├── ButtonComponent.java         # Кнопки (primary, secondary, modal, footer)
│   ├── InputComponent.java          # Поля ввода (labeled, masked, numeric)
│   ├── SelectComponent.java         # Выпадающие списки (MUI, native)
│   ├── CheckboxComponent.java       # Чекбоксы
│   ├── RadioButtonComponent.java    # Радиокнопки
│   ├── DateComponent.java           # Поля дат
│   ├── TextareaComponent.java       # Текстовые области
│   ├── FileComponent.java           # Загрузка файлов
│   ├── TableComponent.java          # Таблицы
│   ├── NavigationComponent.java     # Навигация и вкладки
│   └── StatusIndicatorComponent.java # Индикаторы статуса
└── core/                        # Базовые элементы
    ├── BasePage.java                # Абстрактный Page Object (generic)
    ├── element/                     # SmartElement, SmartElementList, ElementFactory
    └── wait/                        # WaitStrategy, WaitStrategies

gui/                             # Legacy Page Objects (4 файла, активно используются)
├── BasePage.java                # Базовый (WebDriver + SoftAssert)
├── LoginPage.java               # Используется в SzvReportsSteps
├── MainPage.java                # Используется в тестах
└── CryptoProDemoPage.java       # Демо-страница КриптоПРО
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
helpers/                         # 9 файлов
├── ConfigReader.java                # Чтение конфигурации с кешированием и профилями
├── StandardWaits.java               # Типовые ожидания для UI (spinner, modal, ajax)
├── TestConfig.java                  # Тестовые конфигурации и данные (селекторы)
├── DataFiller.java                  # Заполнение форм тестовыми данными
├── TestDataGenerator.java           # Генерация тестовых данных
└── forms/                           # Работа с формами
    ├── FormElements.java                # Элементы форм (extends BasePage)
    ├── FormFacade.java                  # Фасад для работы с формами
    ├── FormValidator.java               # Валидация форм
    └── InsuredPerson.java               # Модель застрахованного лица
```

**Ответственность:**
- Централизованное чтение конфигурации (ConfigReader)
- Типовые ожидания для UI (StandardWaits)
- Доменная логика для работы с формами
- Генерация и управление тестовыми данными
- Валидация бизнес-правил

### Strategies Layer (Стратегии)

```
strategy/                        # Стратегии тестирования (8 файлов)
├── TestExecutionStrategy.java       # Интерфейс стратегий выполнения
├── BaseTestExecutionStrategy.java   # Базовая реализация стратегии
├── TestStrategyManager.java         # Менеджер регистрации и выполнения
├── ExecutionStrategyType.java       # Enum типов стратегий
├── CryptoProValidationStrategy.java # Стратегия валидации КриптоПРО
├── DataPreparationStrategy.java     # Интерфейс подготовки данных
├── ValidationStrategy.java          # Интерфейс валидации
└── ApiTestExecutionStrategy.java    # Стратегия API тестов (закомментировано)
```

**Ответственность:**
- Определение различных подходов к выполнению тестов
- Инкапсуляция сложной логики тестирования (КриптоПРО и др.)
- Переиспользование стратегий между тестами

### Test Infrastructure Layer (Тестовая инфраструктура)

```
test/                            # 16 файлов
├── base/                        # Базовые классы тестов
│   ├── UITestBase.java              # Базовый класс UI тестов (extends BaseTest)
│   ├── ApiTestBase.java             # API тесты (закомментировано)
│   └── DataDrivenTestBase.java      # Data-driven тесты (extends UITestBase)
├── annotations/                 # Кастомные аннотации (6 файлов)
│   ├── TestType.java, TestPriority.java, TestEnvironment.java
│   ├── Requirement.java, AutomationAction.java
│   ├── ZephyrTest.java              # (закомментировано)
│   └── AllureAnnotationProcessor.java
├── helpers/                     # Хелперы тестов (5 файлов)
│   ├── TestSetupHelper.java, LoginHelper.java, PageObjectHelper.java
│   ├── AssertionHelper.java, SmartWaits.java
├── logging/                     # Логирование
│   ├── TestLogger.java, AllureIntegration.java
├── negative/                    # Негативные тест-кейсы
│   ├── NegativeTestCases.java       # UI негативные тесты
│   └── ApiNegativeTestCases.java    # (закомментировано)
├── retry/                       # Повторный запуск тестов
│   ├── RetryAnalyzer.java, Retry.java
└── examples/                    # Примеры (закомментированы)

BaseTest.java                    # Базовый класс всех тестов (корень com.bft)
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

### Integration Layer (Интеграции) — ЗАКОММЕНТИРОВАНО

```
integration/                     # 14 файлов (весь код закомментирован)
├── zephyr/                      # Zephyr Squad/Scale клиенты
├── jira/                        # JiraClient
├── allure/                      # AllureResultsPublisher, AllureTestResult
├── config/                      # IntegrationConfig, ZephyrType
├── mapper/                      # TestResult, TestResultMapper, TestStatus
└── listeners/                   # ZephyrTestListener
```

> **Примечание**: Jira/Zephyr интеграция подготовлена, но не используется в текущей версии.
> Код можно восстановить, раскомментировав соответствующие классы.

### Enums Layer (Перечисления)

```
enums/                           # 6 файлов
├── UIType.java                      # Контуры (EVS_UAT_LKS, EVS_TEST, RPU_UAT и др.)
├── UITypeSelector.java              # Выбор контура из system properties
├── ReportType.java                  # Типы отчётов (@Deprecated)
├── ReportFormType.java              # Типы форм отчётов (UI)
├── ReportXmlResource.java           # Пути к XML-ресурсам отчётов
└── TabType.java                     # Типы вкладок
```

### Utils Layer (Утилиты)

```
utils/                           # 3 файла
├── CryptoProPluginVerifier.java     # Проверка плагина КриптоПРО
├── FormStructureParser.java         # Парсинг структуры форм → JSON
└── FormFieldMetadata.java           # Метаданные полей формы
```

**Ответственность:**
- Проверка плагинов (КриптоПРО)
- Парсинг и сохранение структуры форм для отладки
- Вспомогательные функции общего назначения

### Test Classes Layer (Тестовые классы)

```
LK_Insurence/                    # ЛК Страхователя (14 файлов)
├── ReportXmlUploadTest.java         # Универсальный smoke XML-загрузки (DataProvider)
├── CryptoProCertificateTest.java    # Тесты электронной подписи
├── EFS_1/Efs1.java                  # Тесты ЕФС-1
├── SZV_M/Szv_m.java                # Тесты СЗВ-М
├── SZV_TD/Szv_td.java              # Тесты СЗВ-ТД
├── SZV_ISH/Szv_ish.java            # Тесты СЗВ-ИСХ
├── SZV_DSO/Szv_dso.java            # Тесты СЗВ-ДСО
├── SZV_K/Szv_k.java                # Тесты СЗВ-К
├── SZV_STAJ/Szv_staj.java          # Тесты СЗВ-СТАЖ
├── SZV_KORR/Szv_korr.java          # Тесты СЗВ-КОРР
├── ODV_1/Odv1.java                  # Тесты ОДВ-1
├── CredentialsTest.java             # Тесты credentials
├── TestConfigurationTest.java       # Тесты конфигурации
└── ExampleTest.java                 # Примеры стратегий (закомментировано)

LK_Archive/                      # ЛК Архивной организации (1 файл)
└── ArchivesTest.java                # 5 тестов (РПУ → ЕВС → подписание)
```

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

### Версия 1.0 — Базовая структура
- Простые Page Objects (`gui/`)
- Базовая конфигурация (`BaseTest`)
- Минимальная безопасность

### Версия 2.0 — Компонентная архитектура (текущая)
- 12 переиспользуемых UI-компонентов (`ui/component/`)
- Strategy Pattern для тестирования
- Расширенная безопасность (CredentialManager, SecureLogger)
- TestData builders с Faker
- Мониторинг (PerformanceMonitor, FlakyTestDetector)
- Два ЛК: Страхователя и Архивной организации
- 138 Java файлов, 15 тестовых классов
- GitLab CI/CD с выбором ЛК, контура, браузера

### Версия 3.0 — Планируется
- OpenAPI интеграция (генерация моделей)
- Раскомментирование Jira/Zephyr интеграции
- Расширение тестового покрытия ЛК Архива

---

Архитектура обеспечивает надежную основу для роста фреймворка, сохраняя простоту и поддерживаемость.</contents>
</xai:function_call">Создал детальную архитектурную документацию