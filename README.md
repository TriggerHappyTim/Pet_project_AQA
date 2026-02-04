# EVS Testing Framework

[![Java](https://img.shields.io/badge/Java-11%2B-orange)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![Selenide](https://img.shields.io/badge/Selenide-6.0%2B-green)](https://selenide.org/)
[![TestNG](https://img.shields.io/badge/TestNG-7.0%2B-red)](https://testng.org/)
[![Allure](https://img.shields.io/badge/Allure-2.0%2B-purple)](https://docs.qameta.io/allure/)

## 📋 Описание

**EVS Testing Framework** - это современный, масштабируемый фреймворк для автоматизированного тестирования веб-приложений. Фреймворк построен на принципах SOLID, следует паттернам проектирования и предоставляет мощные инструменты для создания надежных и поддерживаемых тестов.

### 🎯 Основные возможности

- **Многоуровневая архитектура** с четким разделением ответственности
- **Расширяемая система компонентов** для работы с UI элементами
- **Стратегический подход** к организации тестовых сценариев
- **Интеграция с Allure** для красивой отчетности
- **Поддержка Docker** для изолированного выполнения тестов
- **Безопасное управление** учетными данными и чувствительной информацией

## 🏗️ Архитектура

Фреймворк построен по многоуровневой архитектуре:

```
┌─────────────────────────────────────────────────────────────┐
│                    Тестовые сценарии                        │
│                    (Test Classes)                           │
├─────────────────────────────────────────────────────────────┤
│                    Тестовая инфраструктура                 │
│                    (Base Classes, Strategies)              │
├─────────────────────────────────────────────────────────────┤
│                    UI слой                                  │
│                    (Pages, Components)                      │
├─────────────────────────────────────────────────────────────┤
│                    Ядро фреймворка                          │
│                    (Configuration, Security)               │
├─────────────────────────────────────────────────────────────┤
│                    Внешние инструменты                      │
│                    (Selenide, TestNG, Allure)              │
└─────────────────────────────────────────────────────────────┘
```

### 📦 Структура пакетов

```
src/test/java/com/bft/
├── config/                  # Конфигурация фреймворка
│   ├── TestConfiguration.java    # Главная конфигурация
│   ├── TestContext.java          # Контекст выполнения тестов
│   ├── TestStrategy.java         # Базовый интерфейс стратегий
│   ├── TestStrategyType.java     # Типы стратегий
│   ├── ApiTestStrategy.java      # Стратегия API тестов (ЗАКОММЕНТИРОВАНО: не используется)
│   └── UITestStrategy.java       # Стратегия UI тестов
├── ui/                      # Пользовательский интерфейс
│   ├── pages/               # Page Object'ы
│   ├── components/          # Переиспользуемые компоненты
│   └── core/                # Базовые классы и элементы
├── gui/                     # Page Object'ы (legacy, используется)
│   ├── LoginPage.java
│   ├── MainPage.java
│   └── CryptoProDemoPage.java
├── security/                # Безопасность и маскировка данных
│   ├── CredentialManager.java
│   ├── EnvironmentCredentialProvider.java
│   ├── TestUsers.java
│   └── masking/             # Маскировка чувствительных данных
├── helpers/                 # Вспомогательные классы
│   ├── ConfigReader.java        # Централизованное чтение конфигурации
│   ├── StandardWaits.java        # Типовые ожидания для UI
│   ├── api/                      # API helper классы
│   │   ├── Specifications.java   # RequestSpec/ResponseSpec
│   │   └── ApiCoreRequests.java  # Переиспользуемые API методы
│   └── forms/                    # Работа с формами
├── strategy/                # Стратегии тестирования
│   ├── TestStrategyManager.java
│   ├── BaseTestExecutionStrategy.java
│   └── CryptoProValidationStrategy.java
├── test/                    # Тестовая инфраструктура
│   ├── base/                # Базовые классы тестов
│   │   ├── UITestBase.java
│   │   ├── ApiTestBase.java      # ЗАКОММЕНТИРОВАНО: API тесты не используются
│   │   └── DataDrivenTestBase.java
│   ├── annotations/         # Кастомные аннотации
│   ├── logging/             # Логирование
│   └── examples/            # Примеры использования
├── testdata/                # TestData builders
│   ├── TestDataBuilder.java      # Базовый builder
│   ├── UserTestDataBuilder.java # Builder для пользователей
│   └── ReportTestDataBuilder.java # Builder для отчетов
├── monitoring/              # Мониторинг тестов
│   ├── PerformanceMonitor.java   # Отслеживание производительности
│   └── FlakyTestDetector.java    # Выявление нестабильных тестов
├── constants/               # Константы
│   ├── TimeoutConstants.java    # Константы таймаутов
│   └── UrlConstants.java         # Константы URL
├── steps/                   # Шаги для сложных бизнес-процессов
│   └── SzvReportsSteps.java
├── browser/                 # Фабрики браузеров
│   └── factory/
└── utils/                   # Утилиты
```

## 🚀 Быстрый старт

### Требования

- **Java 11+**
- **Maven 3.6+**
- **Chrome/Firefox** браузеры
- **Docker** (опционально, для изолированного запуска)

### Установка и запуск

```bash
# 1. Клонировать репозиторий
git clone <repository-url>
cd evs-testing-framework

# 2. Установить зависимости
mvn clean install

# 3. Запустить простой тест
mvn test -Dtest=CryptoProCertificateTest#verifyCryptoProPluginLoaded

# 4. Сгенерировать отчет Allure
mvn allure:serve
```

### Docker запуск

```bash
# Сборка образа
docker build -t evs-testing-framework .

# Запуск с Selenium Grid
docker-compose up -d
docker run --network evs-testing-network evs-testing-framework
```

## 📚 Документация

- **[Архитектура проекта](ARCHITECTURE.md)** - детальное описание структуры
- **[Руководство по компонентам](COMPONENTS_GUIDE.md)** - использование UI компонентов
- **[Настройка среды](SETUP_GUIDE.md)** - конфигурация и развертывание
- **[Руководство разработчика](DEVELOPMENT_GUIDE.md)** - лучшие практики
- **[API Reference](API_REFERENCE.md)** - справочник API

## 🧪 Примеры использования

### Простой UI тест

```java
@Epic("КриптоПРО")
@Feature("Электронная подпись")
public class CryptoProCertificateTest extends UITestBase {

    private CryptoProDemoPage cryptoProPage;

    @BeforeMethod
    public void initializePageObject() {
        cryptoProPage = new CryptoProDemoPage(softAssert);
    }

    @Test
    @Story("Проверка загрузки плагина")
    @Description("Проверка что плагин КриптоПРО загружен")
    public void verifyCryptoProPluginLoaded() {
        arrangeActAssert(
            // Arrange
            () -> logger.info("Подготовка проверки плагина"),

            // Act
            () -> cryptoProPage.openPageAndVerify("Демо-страница КриптоПРО"),

            // Assert
            (softAssert) -> {
                softAssert.assertTrue(cryptoProPage.verifyExtensionLoaded(),
                    "Расширение должно быть загружено");
                softAssert.assertTrue(cryptoProPage.verifyPluginLoaded(),
                    "Плагин должен быть загружен");
            },

            "Проверка загрузки плагина КриптоПРО"
        );
    }
}
```

### Использование компонентов

```java
// Вместо низкоуровневых селекторов
$x("//button[contains(text(), 'Добавить')]").click();

// Используем высокоуровневые компоненты
ButtonComponent.createButton("Добавить").click();

// Работа с формами
InputComponent.createLabeledInput("ФИО", "ФИО")
    .setValue("Иванов Иван Иванович");

SelectComponent.createMuiInput("Регион", "Регион")
    .selectByText("Москва");
```

### Использование TestData builders

```java
// Создание тестовых данных пользователя
UserData user = UserTestDataBuilder.createDefault()
    .withFirstName("Иван")
    .withLastName("Иванов")
    .withEmail("ivan@example.com")
    .build();

// Создание случайного пользователя
UserData randomUser = UserTestDataBuilder.createRandom().build();

// Создание отчета
ReportData report = ReportTestDataBuilder.createDefault()
    .withReportType(ReportType.EFS_1)
    .withPeriod(LocalDate.now().minusMonths(1), LocalDate.now())
    .build();
```

### Мониторинг производительности и нестабильных тестов

```java
public class MyTest extends UITestBase {
    private static final PerformanceMonitor monitor = PerformanceMonitor.getInstance();
    private static final FlakyTestDetector detector = FlakyTestDetector.getInstance();
    
    @BeforeMethod
    public void setup(ITestResult result) {
        monitor.startTest(result);
    }
    
    @AfterMethod
    public void teardown(ITestResult result) {
        monitor.endTest(result);
        detector.recordTestResult(result);
    }
    
    @AfterSuite
    public void printStatistics() {
        monitor.printStatistics();
        detector.reportFlakyTests();
    }
}
```

## 🔧 Конфигурация

### Основные настройки

```java
// Инициализация с нужной стратегией
TestConfiguration.initialize(TestStrategyType.UI);

// Получение конфигурации браузера
BrowserFactoryManager browserFactory = TestConfiguration.getBrowserFactoryManager();

// Настройка логирования
TestLogger logger = TestLogger.forTest("MyTest");

// Использование ConfigReader для чтения конфигурации
ConfigReader config = ConfigReader.getInstance();
String baseUrl = config.getProperty("api.base.url", "https://api.example.com");
int timeout = config.getIntProperty("test.timeout", 30000);

// Использование StandardWaits для типовых ожиданий
StandardWaits.waitForVisible(By.id("element-id"));
StandardWaits.waitForSpinnerToDisappear();
StandardWaits.waitForPageLoad();
```

### Переменные окружения

```bash
# Браузер для тестов
TEST_BROWSER=chrome

# URL тестового стенда
TEST_BASE_URL=https://test.example.com

# Учетные данные (шифруются)
TEST_USERNAME=encrypted_username
TEST_PASSWORD=encrypted_password
```

## 📊 Отчетность

Фреймворк интегрирован с **Allure Reports** для создания красивых и информативных отчетов:

```bash
# Генерация отчета
mvn allure:report

# Запуск сервера с отчетом
mvn allure:serve

# Просмотр в браузере
# http://localhost:8080
```

### Особенности отчетов

- Автоматические скриншоты при падениях
- Шаги теста с описаниями
- Метрики производительности
- История запусков
- Интеграция с CI/CD

## 🔒 Безопасность

### Маскировка чувствительных данных

```java
// Автоматическая маскировка в логах
logger.info("Login attempt for user: {}", user); // user будет замаскирован

// Ручная маскировка
String maskedCard = SecureLogger.maskCreditCard("4111111111111111");
// Результат: "4111****1111"
```

### Управление учетными данными

```java
// Получение учетных данных
CredentialProvider provider = new EnvironmentCredentialProvider();
String password = provider.getCredential("user.password");

// Или через менеджер
CredentialManager manager = CredentialManager.getInstance();
Credentials creds = manager.getCredentials("test-user");
```

## 🐳 Docker поддержка

### Локальная разработка

```yaml
# docker-compose.yml
version: '3.8'
services:
  selenium-hub:
    image: selenium/hub:4.0
    ports:
      - "4444:4444"

  chrome:
    image: selenium/node-chrome:4.0
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4443
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4442

  tests:
    build: .
    depends_on:
      - selenium-hub
    environment:
      - REMOTE_URL=http://selenium-hub:4444/wd/hub
```

### CI/CD интеграция

```gitlab-ci
stages:
  - test
  - report

test:
  stage: test
  image: openjdk:11
  services:
    - selenium/standalone-chrome
  script:
    - mvn clean test
  artifacts:
    reports:
      allure: allure-results/

report:
  stage: report
  script:
    - mvn allure:aggregate
```

## 🤝 Разработка

### Добавление нового компонента

```java
// 1. Создать компонент
public class MyComponent extends BaseComponent {

    public static MyComponent create(String selector, String name) {
        return new MyComponent(
            ElementFactory.css(selector).named(name).waitVisible().build(),
            name
        );
    }

    // 2. Добавить методы
    public MyComponent performAction() {
        // логика
        return this;
    }
}

// 3. Использовать в Page Object
public class MyPage extends BasePage<MyPage> {
    public MyPage doSomething() {
        MyComponent.create("#my-element", "My Element").performAction();
        return this;
    }
}
```

### Создание новой стратегии

```java
// 1. Реализовать интерфейс
public class MyStrategy implements TestExecutionStrategy {

    @Override
    public TestStrategyType getType() {
        return TestStrategyType.MY_STRATEGY;
    }

    @Override
    public TestResult execute(TestContext context) {
        // логика выполнения
    }
}

// 2. Зарегистрировать в менеджере
strategyManager.registerStrategy(new MyStrategy());
```

## 📈 Метрики и мониторинг

### Сбор метрик

```java
@Test
@PerformanceTest
public void performanceTest() {
    long startTime = System.currentTimeMillis();

    // тестовая логика

    long duration = System.currentTimeMillis() - startTime;
    Allure.addAttachment("Execution Time", String.valueOf(duration));
}
```

### Кастомные аннотации

```java
@Test
@Requirement("REQ-001")
@AutomationStatus(AutomationStatus.Status.AUTOMATED)
@Priority(Priority.Level.HIGH)
public void criticalBusinessTest() {
    // тест
}
```

## 🐛 Устранение неполадок

### Распространенные проблемы

#### Тесты не запускаются
```bash
# Проверить Java версию
java -version

# Проверить Maven
mvn -version

# Очистить и переустановить зависимости
mvn clean install -U
```

#### Браузер не запускается
```bash
# Проверить WebDriver
mvn exec:java -Dexec.mainClass="io.github.bonigarcia.wdm.WebDriverManager"

# Локальный запуск
System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");
```

#### Allure отчет пустой
```bash
# Проверить генерацию результатов
ls -la allure-results/

# Перегенерировать отчет
mvn allure:report -Dallure.results.directory=allure-results
```

## 📞 Поддержка

### Контакты
- **Email:** support@evs-testing-framework.com
- **Issues:** [GitHub Issues](https://github.com/evs-testing-framework/issues)
- **Wiki:** [Project Wiki](https://github.com/evs-testing-framework/wiki)

### Сообщество
- **Slack:** #evs-testing-framework
- **Telegram:** @evs_testing_chat
- **Forum:** [Community Forum](https://forum.evs-testing-framework.com)

## 📄 Лицензия

Этот проект распространяется под лицензией MIT. Подробности в файле [LICENSE](LICENSE).

## 🙏 Благодарности

- [Selenide](https://selenide.org/) - для удобной работы с Selenium
- [TestNG](https://testng.org/) - для мощной тестовой инфраструктуры
- [Allure](https://docs.qameta.io/allure/) - для красивых отчетов
- [Docker](https://www.docker.com/) - за контейнеризацию

---

**EVS Testing Framework** - сделайте тестирование проще, надежнее и эффективнее! 🚀</contents>
</xai:function_call">Создал основной README.md файл с подробным описанием фреймворка