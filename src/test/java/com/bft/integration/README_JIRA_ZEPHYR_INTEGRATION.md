# Интеграция с Jira и Zephyr

> **ВНИМАНИЕ**: Вся интеграция с Jira/Zephyr **ЗАКОММЕНТИРОВАНА** и не используется в текущей версии проекта. Код сохранён для будущего использования.

---

## Обзор

Интеграция с Jira и Zephyr позволяет автоматически публиковать результаты тестов из CI/CD pipeline в систему управления тестированием.

**Поддерживаемые возможности:**
- ✅ Автоматическая отправка результатов тестов в Zephyr
- ✅ Два режима интеграции: real-time (TestNG listener) и batch (Allure results)
- ✅ Поддержка Zephyr Scale и Zephyr Squad
- ✅ Интеграция с GitLab CI/CD pipeline
- ✅ Безопасное хранение credentials
- ✅ Опциональное включение/отключение

---

## Архитектура

```
TestNG Tests
    ↓
┌───────────────────────────────────────┐
│  ZephyrTestListener (real-time)      │
│  или                                   │
│  AllureResultsPublisher (batch)       │
└───────────────────────────────────────┘
    ↓
TestResultMapper
    ↓
ZephyrClient (Scale/Squad)
    ↓
Zephyr API → Jira
```

---

## Быстрый старт

### 1. Настройка credentials

Добавьте в `credentials.properties`:

```properties
# Jira/Zephyr Integration
jira.url=https://jira.bft.local
jira.username=automation@bft.local
jira.api.token=YOUR_API_TOKEN
jira.project.key=EVS

# Zephyr Configuration
zephyr.enabled=true
zephyr.type=scale
zephyr.project.key=EVS
zephyr.test.cycle.key=AUTO-CYCLE
```

Или установите переменные окружения в GitLab CI/CD:
- `JIRA_URL`
- `JIRA_USERNAME`
- `JIRA_API_TOKEN`
- `JIRA_PROJECT_KEY`
- `ZEPHYR_ENABLED`
- `ZEPHYR_TYPE`
- `ZEPHYR_PROJECT_KEY`
- `ZEPHYR_TEST_CYCLE_KEY`

### 2. Добавьте аннотацию к тестам

```java
@Test(groups = {"web", "smoke"})
@ZephyrTest(testKey = "EVS-T-123")
public void loginTest() {
    // Тест автоматически отправится в Zephyr после выполнения
}
```

### 3. Запустите тесты

**Локально:**
```bash
mvn clean test
```

**В GitLab CI/CD:**
1. Откройте pipeline
2. Установите `ZEPHYR_ENABLED=true`
3. Выберите `ZEPHYR_MODE=allure` (или `testng`, `both`)
4. Запустите pipeline

---

## Режимы интеграции

### Режим 1: Real-time (TestNG Listener)

Результаты отправляются сразу после выполнения каждого теста.

**Преимущества:**
- Мгновенная публикация результатов
- Не требует дополнительных шагов в CI/CD

**Недостатки:**
- Больше API запросов
- Может замедлить выполнение тестов

**Использование:**
```java
// Listener зарегистрирован в Allure через allure-junit5
@ZephyrTest(testKey = "EVS-T-101")
@Test
public void myTest() {
    // Test implementation
}
```

### Режим 2: Batch (Allure Results)

Результаты собираются и отправляются пакетом после завершения всех тестов.

**Преимущества:**
- Меньше API запросов
- Не влияет на скорость выполнения тестов
- Можно запустить повторно

**Недостатки:**
- Результаты появляются только после завершения всех тестов

**Использование:**
```bash
# Локально
mvn clean test
mvn exec:java -Dexec.mainClass="com.bft.integration.ZephyrPublisherMain" \
  -Dexec.args="--mode=allure --results-dir=target/allure-results"

# В GitLab CI/CD (автоматически)
# Установите ZEPHYR_MODE=allure
```

---

## Примеры использования

### Пример 1: Простой тест с Zephyr

```java
package com.bft.LK_Insurence;

import com.bft.test.annotations.ZephyrTest;
import com.bft.test.base.UITestBase;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

@Feature("User Authentication")
public class LoginTest extends UITestBase {
    
    @Test(groups = {"web", "smoke"})
    @ZephyrTest(testKey = "EVS-T-101")
    public void successfulLogin() {
        arrangeActAssert(
            () -> {
                // Arrange: подготовка
            },
            () -> {
                // Act: выполнение
                new LoginPage()
                    .open(UIType.EVS_UAT_LKS)
                    .authorize(credentials.username, credentials.password);
            },
            softAssert -> {
                // Assert: проверка
                softAssert.assertTrue($(".user-profile").isDisplayed());
            }
        );
    }
}
```

### Пример 2: Тест с test cycle

```java
@ZephyrTest(testKey = "EVS-T-102", testCycle = "Sprint-23")
@Test(groups = {"web", "regression"})
public void complexTest() {
    // Test implementation
}
```

### Пример 3: Аннотация на уровне класса

```java
@Feature("Report Management")
@ZephyrTest(testCycle = "Sprint-23")
public class ReportTests extends UITestBase {
    
    @Test(groups = {"web", "smoke"})
    @ZephyrTest(testKey = "EVS-T-201")
    public void createReport() {
        // Test implementation
    }
    
    @Test(groups = {"web", "smoke"})
    @ZephyrTest(testKey = "EVS-T-202")
    public void deleteReport() {
        // Test implementation
    }
}
```

---

## Конфигурация

### IntegrationConfig

Центральный класс для управления конфигурацией:

```java
IntegrationConfig config = IntegrationConfig.fromCredentials();

if (config.isZephyrEnabled()) {
    ZephyrClient client = ZephyrClientFactory.create(config);
    // ... работа с клиентом
}
```

### Типы Zephyr

**Zephyr Scale** (рекомендуется):
```properties
zephyr.type=scale
```

**Zephyr Squad**:
```properties
zephyr.type=squad
```

---

## GitLab CI/CD интеграция

### Переменные pipeline

При запуске pipeline доступны переменные:

| Переменная | Значения | Описание |
|-----------|----------|----------|
| `ZEPHYR_ENABLED` | `true`, `false` | Включить интеграцию |
| `ZEPHYR_MODE` | `allure`, `testng`, `both` | Режим публикации |

### Job: publish to zephyr

Автоматически запускается если `ZEPHYR_ENABLED=true`:

```yaml
publish to zephyr:
  stage: deploy
  needs:
    - job: maven test
      artifacts: true
  rules:
    - if: $ZEPHYR_ENABLED == "true"
  script:
    - mvn exec:java -Dexec.mainClass="com.bft.integration.ZephyrPublisherMain" \
        -Dexec.args="--mode=allure --results-dir=target/allure-results"
```

---

## API клиенты

### JiraClient

Для работы с Jira REST API:

```java
JiraClient client = new JiraClient(jiraUrl, username, apiToken, projectKey);

// Создать bug
Issue bug = client.createBug("Test failed", "Description", "High");

// Добавить комментарий
client.addComment("EVS-123", "Additional info");

// Поиск issues
List<Issue> issues = client.searchIssues("project = EVS AND status = Open");
```

### ZephyrClient

Для работы с Zephyr API:

```java
IntegrationConfig config = IntegrationConfig.fromCredentials();
ZephyrClient client = ZephyrClientFactory.create(config);

// Обновить test execution
client.updateTestExecution("EVS-T-123", TestStatus.PASS, "Test passed");

// Создать test cycle
String cycleKey = client.createTestCycle("Sprint 23", "Automated tests");

// Опубликовать результаты
client.publishTestResults(testResults);
```

---

## Структура пакетов

```
com.bft.integration/
├── config/
│   ├── IntegrationConfig.java      # Конфигурация интеграции
│   └── ZephyrType.java              # Enum типов Zephyr
├── jira/
│   └── JiraClient.java              # Клиент для Jira API
├── zephyr/
│   ├── ZephyrClient.java            # Интерфейс
│   ├── ZephyrScaleClient.java       # Реализация для Scale
│   ├── ZephyrSquadClient.java       # Реализация для Squad
│   └── ZephyrClientFactory.java     # Фабрика клиентов
├── mapper/
│   ├── TestResult.java              # DTO результата теста
│   ├── TestResultMapper.java        # Маппер результатов
│   └── TestStatus.java              # Enum статусов
├── listeners/
│   └── ZephyrTestListener.java      # TestNG listener
├── allure/
│   ├── AllureResultsPublisher.java  # Publisher для Allure
│   └── AllureTestResult.java        # DTO для Allure
└── ZephyrPublisherMain.java         # CLI для публикации
```

---

## Troubleshooting

### Проблема: Zephyr integration disabled

**Причина:** `zephyr.enabled=false` или не установлен.

**Решение:**
```properties
zephyr.enabled=true
```

### Проблема: Invalid Zephyr configuration

**Причина:** Отсутствуют обязательные credentials.

**Решение:** Проверьте наличие всех обязательных параметров:
- `jira.url`
- `jira.username`
- `jira.api.token`
- `jira.project.key`

### Проблема: Test does not have @ZephyrTest annotation

**Причина:** Тест не помечен аннотацией `@ZephyrTest`.

**Решение:** Добавьте аннотацию:
```java
@ZephyrTest(testKey = "EVS-T-123")
@Test
public void myTest() {
    // ...
}
```

### Проблема: Zephyr API is not available

**Причина:** Неверный URL или проблемы с сетью.

**Решение:**
1. Проверьте `jira.url`
2. Проверьте доступность Jira из CI/CD runner
3. Проверьте валидность API token

---

## Безопасность

### Хранение credentials

**Локально:**
- Файл `credentials.properties` (добавлен в `.gitignore`)
- Никогда не коммитьте реальные credentials

**CI/CD:**
- GitLab CI/CD Variables (masked & protected)
- Переменные окружения

### Логирование

Используется `SecureLogger` для маскировки чувствительных данных:

```java
private static final Logger logger = LoggerFactory.getLogger(ZephyrClient.class);
logger.info("Publishing results to Zephyr"); // API token не логируется
```

---

## Дополнительные возможности

### Автоматическое создание багов (будущее)

```java
@ZephyrTest(testKey = "EVS-T-123", createBugOnFailure = true)
@Test
public void criticalTest() {
    // При падении автоматически создастся bug в Jira
}
```

### Attachment скриншотов (будущее)

```java
TestResult result = TestResult.builder()
    .testKey("EVS-T-123")
    .status(TestStatus.FAIL)
    .addAttachment("screenshot.png")
    .build();
```

---

## См. также

- [TestUsers.java](../security/TestUsers.java) - Управление тестовыми пользователями
- [TestConfiguration.java](../config/TestConfiguration.java) - Конфигурация тестов
- [AllureAnnotationProcessor.java](../test/annotations/AllureAnnotationProcessor.java) - Обработка аннотаций

---

## Поддержка

При возникновении проблем:
1. Проверьте логи: `target/logs/test-execution.log`
2. Проверьте конфигурацию: `credentials.properties`
3. Проверьте доступность Jira API
4. Обратитесь к команде QA Automation
