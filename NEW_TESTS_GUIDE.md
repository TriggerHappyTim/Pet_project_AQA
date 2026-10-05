# Руководство: новые тесты, новые ЛК, подключение инфраструктуры

> Практическое руководство для автоматизатора. Предполагается, что вы уже прочитали
> [BEGINNERS_GUIDE.md](BEGINNERS_GUIDE.md) и понимаете устройство проекта.
>
> Содержание:
>
> **Часть I** — добавление нового теста (пошагово, с шаблонами)
> **Часть II** — добавление нового Личного кабинета (полный чек-лист из 8 шагов)
> **Часть III** — подключение GraphQL / БД / Kafka / Jira TMS (детально: локально и в CI,
> с командами проверки и разбором ошибок)

---

# Часть I. Добавление нового теста

## I.0. Сначала ответьте на 3 вопроса

| Вопрос | Влияет на |
|--------|-----------|
| Что именно проверяем (бизнес-сценарий)? | Имя теста, `@Story`, `@Description` |
| Где живёт сценарий: существующие шаги покрывают его? | Нужно ли писать новые Steps/Page Objects |
| Как часто тест будет запускаться? | Тег: `smoke` — каждый коммит, `web`/`archive` — регресс |

## I.1. Выберите пакет и создайте класс

Тестовые классы живут по кабинетам:

```
src/test/java/com/bft/
├── LK_Insurence/          ← ЛК Страхователя
│   ├── EFS_1/  SZV_M/  SZV_TD/  SZV_ISH/   ← подпакеты по типам отчётов
│   └── ReportLifecycleTests.java, NegativeTests.java ...
└── LK_Archive/            ← ЛК Архивной организации
    └── ArchivesTest.java
```

**Правила именования:**
- Класс тестов отчёта СЗВ-М → `SZV_M/Szv_m.java`
- Класс смешанных сценариев → понятное имя: `ReportLifecycleTests`, `NegativeTests`
- Один класс = одна фича/страница. Не сваливайте всё в один файл.

## I.2. Скелет класса (копируйте и адаптируйте)

```java
package com.bft.LK_Insurence.SZV_M;

import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("web")                       // ОБЯЗАТЕЛЬНО: см. таблицу тегов ниже
@Epic("ЛК Страхователя")          // группировка в Allure
@Feature("СЗВ-М")
public class SzvMNewScenarioTest extends UITestBase {

    @Test
    @AllureId("SZVM-010")         // ОБЯЗАТЕЛЬНО: уникальный! формат ПРЕФИКС-NUM
    @Story("Создание отчёта СЗВ-М")
    @Description("Проверка создания отчёта СЗВ-М с одним застрахованным лицом")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_m_singleEmployee() {
        // Arrange — подготовка
        SzvReportsSteps steps = new SzvReportsSteps();
        steps.authorizeEVS(UITypeSelector.getSelectedUIType());

        // Act — действия
        steps.addReports();
        steps.selectReportType(/* ReportFormType.SZV_M */ null);
        steps.addNewReport();
        // ... заполнение ...

        // Assert — проверки
        assertions.assertTrue(true, "TODO: замените на реальную проверку");
    }
}
```

## I.3. Теги (обязательны!)

CI фильтрует тесты по тегам (`mvn test -Dgroups=...` / `-DexcludedGroups=...`).

| Тег | Когда ставить | Кто запускает |
|-----|---------------|---------------|
| `smoke` | критичный путь, «система жива» | каждый пайплайн |
| `web` | любой UI-тест ЛК Страхователя | регресс |
| `archive` | любой тест ЛК Архива | регресс (исключается при «только ЛК Страхователя») |
| `xml-upload` | загрузка отчётов XML-файлом | сценарий «Загрузка через XML» |
| `negative` | негативные кейсы (ожидаемые ошибки) | отдельный прогон |
| `efs` | ЕФС-специфика | регресс |
| `example` | только учебные примеры (@Disabled) | никогда в CI |

⚠️ Для тестов ЛК Архива ставьте ДВА тега: `@Tag("web") @Tag("archive")`.
CI исключает группу `archive`, когда гоняет «только ЛК Страхователя».

## I.4. Нужен новый шаг? Добавьте его правильно

**Не пишите селекторы в тесте.** Цепочка всегда такая:

```
ТЕСТ → вызывает ШАГ → шаг работает со СТРАНИЦЕЙ/КОМПОНЕНТОМ
```

Если действия нет в шагах — добавьте его в существующий класс шагов:

```java
// com.bft.steps.ReportDataEntrySteps.java
@Step("Заполнить раздел «Основания» для {reportName}")     // ← попадёт в Allure
public void fillBasisSection(String reportName) {
    $x("//label[text()='Основание']/following-sibling::input")
            .shouldBe(Condition.visible, DEFAULT_WAIT)      // явное ожидание!
            .setValue("Приказ №123 от 01.01.2026");
}
```

Правила:
- каждый метод = один осмысленный бизнес-шаг (не «клик по 5 кнопкам»);
- `@Step` с читаемым описанием — это текст в отчёте Allure;
- селектор + `shouldBe(visible, timeout)` — никаких `Thread.sleep()`;
- если селектор используется в 2+ местах — выносите в Page Object/компонент.

## I.5. Локальный запуск

```bash
# компиляция без запуска (быстрая проверка синтаксиса):
mvn test-compile

# ваш тест:
mvn test -Dtest=SzvMNewScenarioTest

# один метод:
mvn test -Dtest=SzvMNewScenarioTest#szv_m_singleEmployee

# с указанием контура и браузера:
mvn test -Dtest=SzvMNewScenarioTest \
    --activate-profiles test,chrome \
    -Devs.ui.type=EVS_TEST_LKS

# смотреть браузер своими глазами (по умолчанию headless в CI):
mvn test -Dtest=SzvMNewScenarioTest -Dheadless=false
```

## I.6. Проверка в CI

1. Запушьте ветку — пайплайн соберётся автоматически.
2. Ручной запуск: **CI/CD → Pipelines → Run pipeline**, задайте переменные:
   - `TEST_SCRIPT` — «Системные» / «Ручной ввод» / «Загрузка через XML»;
   - `LK_SCOPE` — «ЛК Страхователя» / «ЛК Архива» / «Все ЛК»;
   - `ENVIRONMENT` — test / uat / int;
   - `BROWSERS` — chrome / firefox / yandex.
3. Если ваш тест должен попасть в ручной список CI — добавьте его метод в
   case-блок `.gitlab-ci.yml` (строки с `TEST_LIST="..."`) и предупредите тимлида в MR.

---

# Часть II. Добавление нового Личного кабинета (ЛК)

Сейчас в системе два кабинета: **ЛК Страхователя** (`LKS`) и **ЛК Архивной организации**
(`LKA`). Когда появится новый (например, ЛК Налоговой), нужно согласованно изменить
**8 мест**. Пройдите их по порядку — ничего не забудете.

## Шаг 1. Контур в `UIType.java`

Добавьте пару значений (TEST + UAT) по образцу существующих:

```java
// src/test/java/com/bft/enums/UIType.java

/** EVS Test окружение для нового ЛК (например, ЛК НСО) */
EVS_TEST_LKNSO("https://evs-oi-portal-front.test.ecp/nso/#/"),

/** EVS UAT окружение для нового ЛК */
EVS_UAT_LKNSO("https://ecp-test.sfr.gov.ru/nso/#/"),
```

Конвенция имени: `EVS_<КОНТУР>_LK<КОД>` — код кабинета короткой аббревиатурой
(LKS = страхователь, LKA = архив). URL возьмите у команды разработки стенда.

## Шаг 2. Выбор контура в `UITypeSelector`

Ничего менять не нужно, если новый ЛК ходит через общее свойство `evs.ui.type`.
Но убедитесь, что значение из шага 1 парсится — `parseUIType()` использует
`valueOf()`, поэтому имя свойства должно ТОЧНО совпадать с именем enum.

Проверка:
```bash
mvn test -Dtest=CredentialsTest -Devs.ui.type=EVS_TEST_LKNSO
# в логе: "evs.ui.type установлен в System Property. Выбран контур: EVS_TEST_LKNSO"
```

## Шаг 3. Тестовый пользователь в `TestUsers.java`

```java
// src/test/java/com/bft/security/TestUsers.java

/**
 * Иванова Анна (ЛК НСО)
 * Credential prefix: evs.user4
 */
NSO_IVANOVA_ANNA("evs.user4", "Иванова Анна Сергеевна"),
```

Префиксы идут по порядку: user1 занят, user2 занят, user3 занят → берёте следующий свободный.

## Шаг 4. Учётные данные в credentials.properties

```properties
# ---- Новый ЛК: НСО ----
evs.user4.username=СНИЛС или логин от ЕПГУ
evs.user4.password=пароль
evs.user4.organization=ОРГАНИЗАЦИЯ -9999999999
```

⚠️ `organization` — это точное название карточки организации, которую кликер выбирает
на ЕПГУ после ввода логина/пароля. Возьмите его у аналитика ДОСЛОВНО, с регистром и
дефисами (метод `selectUserCardEPGU` ищет карточку по этому тексту).

В git коммитится только `credentials.properties.example` с пустыми значениями!

## Шаг 5. Метод авторизации в `AuthSteps`

Если новый ЛК авторизуется через ЕПГУ так же, как существующие — достаточно
универсального `authorizeEVS(uiType, TestUsers.NSO_IVANOVA_ANNA)`:

```java
steps.authorizeEVS(
    UIType.EVS_TEST_LKNSO,
    TestUsers.NSO_IVANOVA_ANNA);
```

Специальный метод нужен только при нестандартном флоу (другой провайдер входа):

```java
// src/test/java/com/bft/steps/AuthSteps.java
@Step(value = "Авторизация в ЛК НСО (ЕПГУ)")
public void authorizeNsoEVS(UIType uiType) {
    var credentials = credentialManager.getUserCredentials("evs.user4");
    if (credentials == null || !credentials.isValid()) {
        throw new RuntimeException(
            "NSO credentials not available. Set evs.user4.username/password.");
    }
    String organization = credentialManager.getCredential(
        "evs.user4.organization", "ОРГАНИЗАЦИЯ -9999999999");

    performEpguAuth(uiType, credentials.username, credentials.password, organization);
    // ↑ переиспользует общий флоу: пропуск повторной авторизации, logout при смене юзера
}
```

## Шаг 6. Навигация в `MainPage`

После входа тесты переключаются между кабинетами вкладками главного меню:

```java
new MainPage().openTab("ЛК Страхователя").openTab("Отчеты");
```

Для нового кабинета:
1. Узнайте точный текст пункта меню у разработчиков фронта («ЛК Налоговых органов»?).
2. Если пункт меню виден всем пользователям — ничего добавлять не надо,
   `openTab("<точное название>")` заработает сам.
3. Если нужна спецлогика (ожидание загрузки, редирект) — добавьте метод в `MainPage`.

Заведите Page Object для главной страницы кабинета, если там свои специфичные элементы.

## Шаг 7. Пакет тестов

```
src/test/java/com/bft/LK_NSO/           ← новый пакет
└── NsoSmokeTest.java                   ← начните со smoke
```

```java
@Tag("web")
@Tag("nso")                    // ← свой тег кабинета (см. ниже про CI)
@Tag("smoke")                  // ← чтобы попал в быстрый прогон
@Epic("ЛК НСО")
public class NsoSmokeTest extends UITestBase {

    @Test
    @AllureId("NSO-001")
    @Story("Авторизация и открытие реестра")
    @Severity(SeverityLevel.BLOCKER)
    public void nso_login_and_open_registry() {
        SzvReportsSteps steps = new SzvReportsSteps(); // или свои шаги
        steps.authorizeEVS(UIType.EVS_TEST_LKNSO, TestUsers.NSO_IVANOVA_ANNA);
        assertions.assertTrue(true, "Реестр открыт");
    }
}
```

## Шаг 8. CI (.gitlab-ci.yml)

Чтобы новый ЛК можно было выбирать в ручном пайплайне:

1. Добавьте значение в выпадающий список `LK_SCOPE`:
   ```yaml
   LK_SCOPE:
     description: "Какие ЛК тестируем"
     value: "Все ЛК"
     options: ["Все ЛК", "ЛК Страхователя", "ЛК Архива", "ЛК НСО"]   # ← добавить
   ```
2. Добавьте ветку в case-блок выбора тестов:
   ```bash
   if [ "${LK_SCOPE}" = "ЛК НСО" ]; then
     TEST_LIST="NsoSmokeTest"
     EVS_UI_TYPE_OVERRIDE="EVS_TEST_LKNSO"
   fi
   ```
   (для UAT-контура не забудьте второе значение `EVS_UAT_LKNSO`, см. блок Архива).

## Чек-лист нового ЛК одной картинкой

- [ ] `UIType`: +2 значения (TEST/UAT) с URL от команды стенда
- [ ] `-Devs.ui.type=EVS_TEST_LKXX` парсится без ошибки
- [ ] `TestUsers`: +1 пользователь, свободный префикс `evs.userN`
- [ ] `credentials.properties`: username/password/organization (organization — дословно!)
- [ ] `AuthSteps`: универсальный вызов или новый @Step-метод
- [ ] Навигация: `openTab("<пункт меню>")` работает
- [ ] Пакет `LK_XX/` + smoke-тест с тегами
- [ ] `.gitlab-ci.yml`: option в `LK_SCOPE` + case-блок
- [ ] Smoke-тест зелёный локально И в пайплайне

---

# Часть III. Подключение инфраструктуры: GraphQL, БД, Kafka, Jira

Общая схема для всех четырёх технологий ОДИНАКОВАЯ:

```
Где взять доступ?  →  Настройка локально  →  Настройка в CI  →  Проверка
```

**Два способа передачи настроек** (работают для всего сервисного слоя):

| Способ | Формат | Когда использовать |
|--------|--------|--------------------|
| Системное свойство | `-Ddb.url=...` | локально, в команде mvn, в IDEA |
| Переменная окружения | `DB_URL=...` | CI (GitLab Variables), терминал |

Свойство имеет приоритет над переменной. Оба варианта описаны в javadoc каждого
конфиг-класса (`DbConfig`, `TmsConfig`, `KafkaConfig`, ...).

---

## III.1. GraphQL (подписание документов)

### Что это даёт

Подписание ЭЦП без плагина КриптоПРО: тест отправляет mutation напрямую в Camunda
через graphql-mesh-java. Дефолтный режим — UI; API-режим включается явно и требует
реальный ID задачи Camunda (`-Devs.sign.task-id`).

### Что запросить у команды разработки

| Что | У кого | Зачем |
|-----|--------|-------|
| URL graphql-mesh-java | devops / разработчики бэкенда | адрес сервиса |
| Порт (обычно 20266) | тот же | для port-forward |
| Пример userTaskId/itemId | разработчики Camunda-процесса | для первого ручного прогона |
| Имена форм (formName/formType) | аналитик | параметры mutation |

### Настройка локально

**Вариант А: port-forward до стендового сервиса** (как делает CI):

```bash
# 1. Получите kubeconfig нужного контура у devops (файл test-user.yaml)
# 2. Поднимите проброс порта:
kubectl --kubeconfig=test-user.yaml --namespace=<namespace> \
    port-forward service/graphql-mesh-java 20266:20266

# 3. Окно терминала держите открытым. Проверьте доступность:
curl http://localhost:20266/graphql ^
     -H "Content-Type: application/json" ^
     -d "{\"query\":\"{ __typename }\"}"
# Успех: JSON вида {"data":{"__typename":"Query"}}
```

**Вариант Б: сервис доступен по сети напрямую:**

```bash
mvn test -Dtest=ArchivesTest#createRequestInRPU \
    -Dservice.graphql-mesh.url=http://<host>:<port>/graphql
```

**В IDEA:** Run → Edit Configurations → VM options:
`-Dservice.graphql-mesh.url=http://localhost:20266`

### Настройка в CI

Уже работает «из коробки»: `.gitlab-ci.yml` поднимает port-forward на
`graphql-mesh-java:20266` перед тестами. Ничего настраивать не нужно.

### Проверка, что подписание реально идёт через API

1. Запустите тест с подписью.
2. Откройте Allure-отчёт → выберите тест → шаг `Sign document via GraphQL...`.
3. Внутри шага должны быть вложения **GraphQL Request** и **GraphQL Response**.
   Есть оба + тест зелёный → API-подписание работает.
4. Если хотите проверить UI-режим (дефолт): просто уберите api-флаги — нужен КриптоПРО!

### Типичные проблемы

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `Connection refused: localhost:20266` | Не поднят port-forward | Выполните команду из Варианта А |
| `GraphQL request failed with HTTP 404` | Неправильный путь `/graphql` | Уточните path у devops, поправьте URL |
| `HTTP 502/503` | Сервис на стенде лежит | Смотреть статус пода, звать devops |
| Тест падает на шаге «Подписать» в ui-режиме | Нет плагина КриптоПРО | Используйте дефолтный api-режим |

---

## III.2. База данных (Apache Phoenix)

### Что важно понимать

БД ЕВС — это **HBase + Apache Phoenix** (SQL поверх HBase), подключение через
Phoenix Query Server (PQS, HTTP-порт 8765). Это НЕ PostgreSQL — SQL немного другой
(имена таблиц обычно ВЕРХНИМ регистром, нет привычных транзакций).

Известные адреса PQS (уже в application-*.yml):

| Контур | JDBC URL |
|--------|----------|
| int | `jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF` |
| dev/local | `jdbc:phoenix:thin:url=http://172.18.34.22:8765;serialization=PROTOBUF` |

### Настройка локально

```bash
# Проверка доступности PQS (должен ответить чем угодно, главное не таймаут):
curl http://172.18.36.33:8765 --max-time 5 -o NUL -w "%{http_code}"

# Запуск тестов с БД:
mvn test -Dtest=MyDbTest ^
    -Ddb.url="jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF"
```

**IDEA:** VM options →
`-Ddb.url="jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF"`
(кавычки внутри значения URL обязательны только если в нём есть пробелы; здесь их нет).

### Настройка в CI

GitLab → Settings → CI/CD → Variables → Add variable:

```
Key:     DB_URL
Value:   jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF
Flags:   ✅ Protected (если стенд внутренний), ❌ Masked (содержит ; и : — маскирование может ломать вывод)
Scope:   все окружения или конкретный
```

Переменная попадёт в окружение джобы, наш код прочитает её через `System.getenv("DB_URL")`.

### Как проверить, что всё работает (первый тест)

```java
@Tag("db")
public class DbConnectivityCheck extends BaseTest {
    private final DbClient db = new DbClient();

    @Test
    void connectionWorks() {
        // Самый дешёвый запрос: системная функция Phoenix
        Long one = db.queryScalar("SELECT COUNT(*) FROM SYSTEM.CATALOG");
        assertions.assertNotNull(one, "Phoenix отвечает на запросы");
    }
}
```

```bash
mvn test -Dtest=DbConnectivityCheck -Ddb.url="jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF"
```

### Типичные проблемы

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `Database is not configured. Set -Ddb.url` | Свойство не долетело | Проверьте VM options / GitLab Variable |
| `No suitable driver found` | Драйвер не в classpath | Убедитесь, что `phoenix-queryserver-client` не удалён из pom.xml |
| Таймаут соединения | VPN/сеть, хост недоступен | Проверьте curl-командой выше; включите VPN |
| `Table undefined` | Опечатка в имени таблицы | Посмотрите список таблиц: `SELECT DISTINCT TABLE_NAME FROM SYSTEM.CATALOG` |
| Гирлянда зависимостей конфликтует | Jackson/httpclient версии | Исключите конфликтующий транзитивный модуль (`<exclusions>`) |

### Правила безопасности (повторение — мать учения)

1. Только `?`-плейсхолдеры. Никогда: `"SELECT ... WHERE id = " + id`.
2. SELECT'ы — свободно. UPDATE/DELETE на общем стенде — только согласовав.
3. Все запросы автоматически пишутся в Allure — не кладите туда секреты.

---

## III.3. Kafka (асинхронные события)

### Что запросить у devops

| Что | Зачем |
|-----|-------|
| Bootstrap-адрес брокера (`host:9092`) | единственная обязательная настройка |
| Имена топиков вашего процесса | куда смотреть сообщениями |
| Доступ из вашей сети/VPN до брокера | иначе будет таймаут |

Известные адреса (application-*.yml): int `172.18.38.9:9092`,
dev `172.18.32.223:9092`, локальный docker `localhost:29093`.

### Настройка локально

```bash
# Проверка TCP-доступности (PowerShell):
Test-NetConnection -ComputerName 172.18.38.9 -Port 9092
# Ждём TcpTestSucceeded : True

# Запуск тестов:
mvn test -Dtest=MyKafkaTest -Dkafka.servers=172.18.38.9:9092
```

### Настройка в CI

GitLab Variables:

```
Key:     KAFKA_SERVERS
Value:   172.18.38.9:9092
```

### Первый проверочный тест

```java
@Tag("kafka")
public class KafkaConnectivityCheck extends BaseTest {
    @Test
    void brokerIsReachableAndTopicHasMessages() {
        try (KafkaEventConsumer kafka = new KafkaEventConsumer()) {
            var messages = kafka.readRecent("ВАШ_ТОПИК", 5, Duration.ofSeconds(10));
            logger.info("Прочитано сообщений: {}", messages.size());
            // Пока просто смотрим в лог/Allure: брокер жив, топик существует.
            // Пусто — либо топик пуст, либо имя неверное.
        }
    }
}
```

### Как писать тест с ожиданием события (рецепт)

```java
// 1. ПОДГОТОВКА: узнайте формат сообщения у разработчиков (JSON? protobuf-as-json?)
// 2. Действие в UI:
steps.signRequest();

// 3. Ожидание события:
try (KafkaEventConsumer kafka = new KafkaEventConsumer()) {
    Optional<KafkaMessage> event = kafka.waitForMessage(
        topic,
        msg -> msg.value.contains("\"requestNumber\":\"" + requestNumber + "\""),
        Duration.ofSeconds(30));
    assertions.assertTrue(event.isPresent(),
        "Событие по запросу " + requestNumber + " не пришло за 30 сек");
}
```

Советы:
- матчер делайте МИНИМАЛЬНЫМ (одно поле-идентификатор), иначе будет ложно-зелёным;
- таймаут берите с запасом ×2 от реального времени доставки (узнайте у бэкендеров);
- если событие не приходит — откройте Allure: там список всех виденных сообщений.

### Типичные проблемы

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `Timed out waiting for a node assignment` | Нет сети до брокера | Test-NetConnection; VPN |
| Всегда empty в waitForMessage | Матчер слишком строгий / событие реально не ушло | Посмотрите виденные сообщения в Allure |
| `Unknown topic` | Опечатка в имени топика | Список топиков: `kafka-topics.sh --list --bootstrap-server ...` |
| Сообщения-«кракозябры» | Protobuf в сыром виде | Матчите по ключу/метаданным, уточните формат у бэкенда |

---

## III.4. Jira TMS (обновление статусов тест-кейсов)

### Как это устроено

```
@Test @TmsLink("EVS-T-101") myTest()
        │ прогон завершился
        ▼
TestResultWatcher видит результат PASS/FAIL/SKIP
        │ есть ли @TmsLink?  ──нет──► ничего не делаем
        ▼ да
TmsClient: GET /rest/tests/1.0/testrun/{runId}/testrunitems
        │ находим внутренний ID результата кейса EVS-T-101
        ▼
PUT /rest/tests/1.0/testresult  →  статус кейса обновлён (Pass=94/Fail=95/Skip=96)
```

Гарантии: без конфигурации интеграция спит; недоступность Jira НЕ роняет тесты
(только warning); обновляются только тесты с `@TmsLink`.

### Что запросить у администратора Jira/TMS

| Что | Где взять |
|-----|-----------|
| Base URL Jira | известен команде (`https://srv-ecp-jira-01...`) |
| Bearer token | личный токен TMS: профиль Jira → Personal Access Tokens (или у админа TMS) |
| ID тест-рана (`tms.test-run-id`) | создайте прогон в TMS, ID — число в URL прогона |
| Project ID | админ TMS или URL проекта |
| user-key | ваш идентификатор в TMS (`JIRAUSER#####`) |

### Настройка локально

```bash
mvn test -Dgroups=smoke ^
  -Dtms.url=https://srv-ecp-jira-01.bft.local ^
  -Dtms.token=NzQzODM5OTA2ODcz.... ^
  -Dtms.test-run-id=1370 ^
  -Dtms.project-id=10501 ^
  -Dtms.user-key=JIRAUSER16914
```

⚠️ Токен — это секрет. Не коммитьте его ни в код, ни в yml, ни в README.
Локально удобнее завести файл `local.properties` вне git и экспортировать переменные.

### Настройка в CI

GitLab → Settings → CI/CD → Variables (все — Masked ✅, Protected ✅):

```
TMS_URL          https://srv-ecp-jira-01.bft.local
TMS_TOKEN        <ваш bearer token>
TMS_TEST_RUN_ID  1370
TMS_PROJECT_ID   10501
TMS_USER_KEY     JIRAUSER16914
```

### Проверка связности без запуска тестов

```bash
# 1. Токен жив? Должен вернуться JSON со списком items (HTTP 200):
curl -H "Authorization: Bearer %TMS_TOKEN%" ^
  "%TMS_URL%/rest/tests/1.0/testrun/%TMS_TEST_RUN_ID%/testrunitems?fields=id,index"

# 2. Кейс существует в прогоне?
# Найдите в ответе ключ своего кейса ("key":"EVS-T-101").
# Если нет — сначала добавьте кейсы в прогон в интерфейсе TMS.
```

### Разметка тестов

```java
@Test
@TmsLink("EVS-T-101")     // метод — приоритет
public void oneCasePerMethod() { }

@TmsLink("EVS-T-200")     // можно на весь класс — тогда все методы пишут в этот кейс
public class WholeClassOneCaseTest extends BaseTest { }
```

Правило: **один метод = один кейс**. Если методов несколько и все должны обновлять
разные кейсы — размечайте каждый метод индивидуально.

### Типичные проблемы

| Симптом | Причина | Решение |
|---------|---------|---------|
| В логе `TMS: not configured, skipping` | Не хватает одного из трёх ключей url/token/run-id | Проверьте все пять переменных |
| `case 'EVS-T-101' not found in test run` | Кейс не добавлен в прогон 1370 | Добавьте кейсы в прогон через UI TMS |
| `failed to update ... 401/403` | Токен истёк/не те права | Перевыпустите токен |
| Статусы обновились не у тех кейсов | `@TmsLink` продублирован на классе и методах | Разметьте явно, уберите дубль |
| Тесты упали, но статусы не обновились | Джоба упала ДО watcher'а (браузер не поднялся) | Это нормально: TMS-репортинг работает только для реально выполненных тестов |

---

## III.5. Сводная таблица всех переменных для CI

Заведите один раз в GitLab → Settings → CI/CD → Variables:

| Key (GitLab) | Пример значения | Для чего |
|--------------|-----------------|----------|
| `TMS_URL` | `https://srv-ecp-jira-01.bft.local` | Jira TMS |
| `TMS_TOKEN` | *(masked)* | Jira TMS |
| `TMS_TEST_RUN_ID` | `1370` | Jira TMS |
| `TMS_PROJECT_ID` | `10501` | Jira TMS |
| `TMS_USER_KEY` | `JIRAUSER16914` | Jira TMS |
| `DB_URL` | `jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF` | БД |
| `KAFKA_SERVERS` | `172.18.38.9:9092` | Kafka |
| `TELEGRAM_BOT_TOKEN` | *(masked)* | Telegram |
| `TELEGRAM_CHAT_ID` | `-100...` | Telegram |

GraphQL отдельно настраивать не нужно — CI сам поднимает port-forward 20266.
gRPC-эндпоинты тоже соответствуют port-forward'ам CI по умолчанию.

---

*Нашли неточность или добавили новую интеграцию — дополните этот документ тем же PR.*
