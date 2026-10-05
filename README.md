# EVS Testing Framework

[![Java](https://img.shields.io/badge/Java-11-orange)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![Playwright](https://img.shields.io/badge/Playwright-1.49.0-green)](https://playwright.dev/java/)
[![JUnit 5](https://img.shields.io/badge/JUnit_5-5.10.2-green)](https://junit.org/junit5/)
[![Allure](https://img.shields.io/badge/Allure-2.22.2-purple)](https://docs.qameta.io/allure/)

## 📋 Описание

**EVS Testing Framework** — фреймворк для автоматизированного UI-тестирования системы ЕВС (Единая Вычислительная Система). Построен на Java 11 + JUnit 5 + Playwright + Allure. Поддерживает два ЛК: **ЛК Страхователя** и **ЛК Архивной организации**.

### 🎯 Основные возможности

- **Многоуровневая архитектура** с четким разделением ответственности
- **12 переиспользуемых UI-компонентов** (Button, Input, Select, Checkbox, Table и др.)
- **Page Object + Steps** — два уровня абстракции для UI
- **Интеграция с Allure** для отчетности, скриншоты при падениях
- **3 браузера** — Chrome, Firefox, Yandex (локально и удаленно через Selenoid)
- **Безопасность** — CredentialManager, SecureLogger, маскировка данных
- **Мониторинг** — отслеживание медленных и нестабильных тестов
- **GitLab CI/CD** с выбором ЛК, группы тестов, контура и браузера
- **Сервисный слой полного стека**: подписание ЭЦП через GraphQL, Kafka-события,
  БД (Phoenix), gRPC health-check, Telegram-уведомления, Jira TMS, визуальная регрессия

> 🎓 **Новичок?** Начните с [BEGINNERS_GUIDE.md](BEGINNERS_GUIDE.md) — подробный учебник
> «с нуля до первого теста», включая построчный разбор теста и все настройки.
>
> 🛠 **Пишете тесты?** Практическое руководство: [NEW_TESTS_GUIDE.md](NEW_TESTS_GUIDE.md) —
> пошаговое добавление тестов, чек-лист нового ЛК (8 шагов), подключение
> GraphQL / БД / Kafka / Jira TMS с командами проверки.

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
│                    (Playwright, JUnit 5, Allure)              │
└─────────────────────────────────────────────────────────────┘
```

### 📦 Структура пакетов

```
src/test/java/com/bft/
├── BaseTest.java                # Базовый класс всех тестов
├── config/                      # Конфигурация фреймворка (7 файлов)
│   ├── TestConfiguration.java       # Главная конфигурация
│   ├── TestContext.java             # Контекст выполнения тестов
│   ├── TestConfig.java              # Настройки теста (браузер, таймауты)
│   ├── TestStrategy.java            # Интерфейс стратегий
│   ├── BaseTestStrategy.java        # Базовая реализация стратегии
│   ├── UITestStrategy.java          # Стратегия UI тестов
│   └── TestStrategyType.java        # Enum типов стратегий
├── ui/                          # Пользовательский интерфейс (18 файлов)
│   ├── pages/                       # Page Objects (BasePage, LoginPage, MainPage)
│   ├── component/                   # 12 переиспользуемых UI-компонентов
│   └── core/                        # BasePage, SmartElement, WaitStrategies
├── LK_Insurence/                # Тесты ЛК Страхователя (14 файлов)
│   ├── ReportXmlUploadTest.java     # Универсальный smoke XML-загрузки
│   ├── EFS_1/, SZV_M/, SZV_TD/, SZV_ISH/
│   └── CredentialsTest.java
├── LK_Archive/                  # Тесты ЛК Архивной организации
│   └── ArchivesTest.java            # 5 тестов (РПУ → ЕВС → подписание)
├── security/                    # Безопасность (13 файлов)
│   ├── CredentialManager.java, CredentialProvider.java
│   ├── EnvironmentCredentialProvider.java, TestUsers.java
│   ├── OrganizationProfile.java
│   ├── masking/                     # SecureLogger, DataMasker
│   └── providers/                   # PropertiesCredentialProvider
├── helpers/                     # Вспомогательные классы (9 файлов)
│   ├── ConfigReader.java            # Чтение конфигурации с кешированием
│   ├── StandardWaits.java           # Типовые ожидания для UI
│   ├── DataFiller.java, TestDataGenerator.java
│   └── forms/                       # FormElements, FormFacade, FormValidator
├── strategy/                    # Стратегии тестирования (8 файлов)
│   ├── TestStrategyManager.java, TestExecutionStrategy.java
│   ├── BaseTestExecutionStrategy.java
│   └── CryptoProValidationStrategy.java
├── test/                        # Тестовая инфраструктура (16 файлов)
│   ├── base/                        # UITestBase, BaseTest
│   ├── annotations/                 # @TestType, @TestPriority, @Requirement и др.
│   ├── examples/                    # 🎓 Обучающие примеры (@Disabled, см. BEGINNERS_GUIDE.md)
│   ├── logging/                     # TestLogger, AllureIntegration
│   ├── helpers/                     # TestSetupHelper, LoginHelper, SmartWaits
│   ├── negative/                    # NegativeTestCases
│   ├── retry/                       # RetryAnalyzer
├── service/                       # Сервисный слой полного стека
│   ├── graphql/                     # Подписание ЭЦП через GraphQL/Camunda (без КриптоПРО)
│   │   ├── GraphQLClient.java         # REST-обёртка graphql-mesh
│   │   ├── SignRequest.java           # Шаблоны mutations
│   │   └── SignService(+Impl).java    # API-подписание (evs.sign.mode=api|ui)
│   ├── db/                          # Прямой доступ к БД (Apache Phoenix)
│   │   ├── DbConfig.java              # -Ddb.url=...
│   │   └── DbClient.java              # Параметризованные SELECT/UPDATE + Allure
│   ├── kafka/                       # Асинхронные события
│   │   ├── KafkaConfig.java           # -Dkafka.servers=...
│   │   └── KafkaEventConsumer.java    # waitForMessage / readRecent
│   └── grpc/                        # Внутренние сервисы ЕВС
│       ├── GrpcConfig.java            # Реестр эндпоинтов (порты CI port-forward)
│       ├── GrpcChannelFactory.java    # Каналы plaintext/TLS
│       └── GrpcHealthCheck.java       # Connectivity-проверки без стабов
├── testdata/                    # TestData builders (3 файла)
│   ├── TestDataBuilder.java, UserTestDataBuilder.java, ReportTestDataBuilder.java
├── monitoring/                  # Мониторинг (2 файла)
│   ├── PerformanceMonitor.java, FlakyTestDetector.java
├── helpers/                     # Утилиты
│   ├── TelegramNotifier.java        # Уведомления о результатах в Telegram
│   ├── VisualComparator.java        # Визуальная регрессия (эталоны в resources/screens/)
│   ├── InfrastructureCheck.java     # Pre-flight проверки сервисов
│   └── ConfigReader.java, StandardWaits.java, DataFiller.java ...
├── integration/
│   ├── tms/                         # Jira TMS: автообновление статусов по @TmsLink
│   │   ├── TmsConfig.java, TmsClient.java, TmsStatusMapper.java
│   └── ...                          # (исторический skeleton Zephyr — не активен)
├── enums/                       # Перечисления (6 файлов)
│   ├── UIType.java, UITypeSelector.java, ReportType.java
│   ├── ReportFormType.java, ReportXmlResource.java, TabType.java
├── constants/                   # Константы (2 файла)
│   ├── TimeoutConstants.java, UrlConstants.java
├── steps/                       # Бизнес-шаги (5 файлов)
│   ├── AuthSteps.java
│   ├── ReportNavigationSteps.java
│   ├── ReportDataEntrySteps.java
│   ├── ArchiveSteps.java
│   └── SzvReportsSteps.java
├── browser/factory/             # Фабрики браузеров (6 файлов)
│   ├── ChromeBrowserFactory, FirefoxBrowserFactory, YandexBrowserFactory
└── utils/                       # Утилиты (3 файла)
    ├── FormStructureParser.java, FormFieldMetadata.java
    └── CryptoProPluginVerifier.java
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

# 3. Запустить простой тест (smoke: XML-загрузка отчёта)
mvn test -Dtest=ReportXmlUploadTest

# Или один метод конкретного класса:
mvn test -Dtest=ArchivesTest#createRequestInRPU

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

### Основные документы
- **[Архитектура проекта](ARCHITECTURE.md)** — слои, пакеты, паттерны, зависимости
- **[Руководство по компонентам](COMPONENTS_GUIDE.md)** — 12 UI-компонентов с примерами
- **[Настройка среды](SETUP_GUIDE.md)** — установка, конфигурация, Docker, CI/CD
- **[Руководство разработчика](DEVELOPMENT_GUIDE.md)** — SOLID, Arrange-Act-Assert, best practices
- **[API Reference](API_REFERENCE.md)** — справочник всех классов и методов
- **[Contributing](CONTRIBUTING.md)** — правила написания тестов, quality gates, DoD
- **[Гайд по ЛК](docs/LK_GUIDE.md)** — конвенции ЛК, масштабирование до 9 кабинетов

### Дополнительно
- **[Roadmap](src/test/resources/docs/roadmap.md)** — план улучшений и прогресс
- **[Анализ архитектуры](src/test/resources/docs/ARCHITECTURE_ANALYSIS.md)** — аудит кода
- **[Тест-кейсы ЛК](src/test/resources/docs/test-cases-lks-krivonosov.md)** — тест-кейсы
- **[Баг-репорт ЛК](src/test/resources/docs/bug-report-lks.md)** — найденные дефекты

## 🧪 Примеры использования

### Простой UI тест

```java
@Epic("ЕВС")
@Feature("Загрузка XML отчета")
@Tag("smoke")
@Tag("xml-upload")
public class ReportXmlUploadTest extends BaseTest {

    @Test
    @Story("Загрузка XML-файла отчета")
    @Description("Проверка загрузки XML-файла отчета через UI")
    public void uploadReportXml() {
        ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
        ReportDataEntrySteps dataEntrySteps = new ReportDataEntrySteps();
        
        reportNavSteps.addReports();
        reportNavSteps.selectReportType(ReportFormType.SZV_M);
        reportNavSteps.addNewReport();
        
        assertNotNull("Страница загрузки XML должна открыться");
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
public class MyTest extends BaseTest {
    private static final PerformanceMonitor monitor = PerformanceMonitor.getInstance();
    private static final FlakyTestDetector detector = FlakyTestDetector.getInstance();
    
    @BeforeEach
    public void setup(TestInfo testInfo) {
        monitor.startTest(testInfo);
    }
    
    @AfterEach
    public void teardown(TestInfo testInfo) {
        monitor.endTest(testInfo);
        detector.recordTestResult(testInfo);
    }
    
    @AfterAll
    public static void printStatistics() {
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
# Install Playwright browsers
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

# Playwright needs no chromedriver - it drives the browser directly
```

#### Allure отчет пустой
```bash
# Проверить генерацию результатов
ls -la allure-results/

# Перегенерировать отчет
mvn allure:report -Dallure.results.directory=allure-results
```

## 🛠 Стек технологий

| Технология | Версия | Назначение |
|-----------|--------|------------|
| Java | 11 | Язык |
| JUnit 5 | 5.10.2 | Тестовый фреймворк |
| Playwright | 1.49.0 | UI-автоматизация |
| Allure | 2.22.2 | Отчетность |
| Spring Boot | 2.6.14 | Базовый фреймворк |
| Lombok | 1.18.20 | Сокращение boilerplate |
| JavaFaker | 1.0.2 | Генерация тестовых данных |
| Maven | 3.6+ | Сборка |

---

**EVS Testing Framework** — надежная автоматизация тестирования системы ЕВС.</contents>
</xai:function_call">Создал основной README.md файл с подробным описанием фреймворка