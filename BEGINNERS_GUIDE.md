# Учебник для нового тестировщика: с нуля до первого теста

> Этот документ написан для человека, который **никогда не занимался автотестами**.
> Читайте сверху вниз, ничего не пропуская — каждый следующий раздел опирается на предыдущий.
>
> Справочник по всем настройкам — в [разделе 8](#8-шпаргалка-все-настройки-проекта).
> Примеры кода из этого гайда лежат в `src/test/java/com/bft/test/examples/` — там же,
> где и этот текст, только в виде Java-файлов.
>
> **Следующий шаг после этого учебника:** [NEW_TESTS_GUIDE.md](NEW_TESTS_GUIDE.md) —
> как добавлять новые тесты и новые ЛК, детальное подключение GraphQL/БД/Kafka/Jira.

---

## Оглавление

1. [Что происходит, когда запускается тест](#1-что-происходит-когда-запускается-тест)
2. [Как устроен проект](#2-как-устроен-проект)
3. [Первый запуск](#3-первый-запуск)
4. [Анатомия теста: разбор каждой строки](#4-анатомия-теста-разбор-каждой-строки)
5. [Мягкие проверки (TestAssertions)](#5-мягкие-проверки-testassertions)
6. [Новая инфраструктура: 7 возможностей](#6-новая-инфраструктура-7-возможностей)
7. [Отчёты Allure](#7-отчёты-allure)
8. [Шпаргалка: все настройки проекта](#8-шпаргалка-все-настройки-проекта)
9. [Частые ошибки и их решения](#9-частые-ошибки-и-их-решения)
10. [Чек-лист перед pull request](#10-чек-лист-перед-pull-request)

---

## 1. Что происходит, когда запускается тест

Вы вводите команду:

```bash
mvn test -Dtest=ArchivesTest#createRequestInRPU
```

И дальше происходит вот что (читайте стрелки сверху вниз):

```
mvn (Maven — «сборщик» проекта)
 │  находит тестовый класс по имени ArchivesTest
 ▼
JUnit 5 (тестовый фреймворк — «диспетчер» тестов)
 │  ищет в классе методы с аннотацией @Test → нашёл createRequestInRPU
 │  перед запуском вызывает расширения:
 │    • MakeScreenshotsExtension — будет делать скриншоты при падении
 │    • TestResultWatcher      — логирует результат, шлёт Telegram/TMS
 ▼
UITestBase (базовый класс нашего UI-теста)
 │  создаёт объект мягких проверок assertions
 │  открывает браузер через Playwright
 ▼
Тест выполняется по шагам
 │  steps.authorizeEVS(...)   ← авторизация
 │  steps.createZaprosRPU()   ← клики по вкладкам
 │  ...                       ← заполнение форм
 ▼
После теста (автоматически!)
 │  assertions.assertAll()    ← все накопленные проверки проверяются
 │  скриншоты при падении     ← приложены к отчёту
 │  браузер закрывается
 │  Telegram/Jira TMS получают результат
 ▼
Отчёт Allure собирается из результатов
```

**Главная мысль:** вы пишете только «сценарий» (какие шаги выполнить и что проверить).
Всё остальное — браузер, скриншоты, отчёты, уведомления — фреймворк делает сам.

---

## 2. Как устроен проект

Проект — это Maven-проект. Весь тестовый код лежит в `src/test/java/com/bft/`.
Вот карта самых важных папок (полная — в README.md):

| Папка | Что это | Простыми словами |
|-------|---------|------------------|
| `LK_Insurence/`, `LK_Archive/` | **Тестовые классы** | Сами сценарии: «авторизуйся, сделай то-то, проверь это». Вы будете писать здесь |
| `steps/` | **Шаги** | Классы-помощники с бизнес-действиями: `authorizeEVS()`, `addReports()`. Тест вызывает шаги, а не тыкает кнопки сам |
| `ui/pages/` | **Page Object'ы** | Описание страниц: где какая кнопка, какое поле. Шаги используют страницы |
| `ui/component/` | **UI-компоненты** | Переиспользуемые детали: кнопка, таблица, дата-пикер |
| `pw/` | **Обёртка Playwright** | Наша реализация «Selenide-стиля» поверх Playwright. Трогать почти никогда не нужно |
| `test/base/` | **Базовые классы** | `BaseTest` и `UITestBase` — от них наследуются все тесты |
| `test/TestAssertions.java` | **Мягкие проверки** | Объект `assertions`, доступный в каждом тесте |
| `service/graphql/` | **Подписание через API** | Эмуляция ЭЦП без плагина КриптоПРО (см. раздел 6.1) |
| `service/db/` | **База данных** | Прямые SQL-запросы для проверки данных (6.3) |
| `service/kafka/` | **Kafka** | Проверка асинхронных событий (6.4) |
| `service/grpc/` | **gRPC** | Проверка внутренних сервисов (6.5) |
| `helpers/` | **Утилиты** | Скриншоты, Telegram, визуальная регрессия и пр. |
| `jupiter/` | **Расширения JUnit** | Автоскриншоты при падении, логирование, пропуск тестов |
| `integration/tms/` | **Jira TMS** | Автообновление статусов тест-кейсов (6.7) |

### Правило слоёв (самое важное правило!)

```
ТЕСТ  →  вызывает  →  ШАГИ  →  используют  →  СТРАНИЦЫ/КОМПОНЕНТЫ
```

- В тесте **нельзя** писать селекторы (`$x("//button...")`) — кроме коротких проверок.
- Новое действие на странице добавляется в **Page Object**, бизнес-действие — в **Steps**.
- Тогда при изменении вёрстки правится ОДНО место, а не 20 тестов.

---

## 3. Первый запуск

### 3.1 Что должно быть установлено

| Инструмент | Версия | Проверка |
|------------|--------|----------|
| JDK | **11** (обязательно!) | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| IntelliJ IDEA | Community подойдёт | — |

Если Java не 11-я — тесты не соберутся. В IDEA: `File → Project Structure → SDK` → corretto-11.

### 3.2 Учётные данные

Логины/пароли хранятся в `src/test/resources/credentials.properties`
(его нет в git — попросите коллегу или скопируйте из `credentials.properties.example`).

Пароли **никогда** не пишутся в коде теста. Никогда.

### 3.3 Запуск первого теста

```bash
# Один конкретный тестовый метод:
mvn test -Dtest=ArchivesTest#createRequestInRPU

# Весь класс целиком:
mvn test -Dtest=ArchivesTest

# Все smoke-тесты (быстрая проверка, что система жива):
mvn test -Dgroups=smoke
```

Полезные флаги контура и браузера:

```bash
# Контур (тестовый стенд): test / uat / int / dev
mvn test --activate-profiles test

# Браузер: chrome / firefox / yandex
mvn test --activate-profiles chrome
```

### 3.4 Как понять, что произошло

В консоли ищите строки:

```
TEST PASSED: ArchivesTest.createRequestInRPU        ← успех
TEST FAILED: ArchivesTest.addIspolnitel - <ошибка>  ← провал
```

Красивый отчёт — раздел 7.

---

## 4. Анатомия теста: разбор каждой строки

Возьмём реальный тест из проекта (`ArchivesTest.createRequestInRPU`)
и объясним каждую строку:

```java
@Tag("archive")              // ① тег для фильтрации в CI
@Tag("web")
@Epic("ЛК Архивной организации")   // ② группировка в отчёте Allure
@Feature("Реестр запросов в архивы")
public class ArchivesTest extends UITestBase {
//                        ↑ ③ НАСЛЕДОВАНИЕ: даёт assertions, logger, браузер, скриншоты

    @Test                    // ④ «это тест» — JUnit его выполнит
    @AllureId("ARCH-001")    // ⑤ уникальный код теста (для связи с TMS)
    @Story("Создание запроса в РПУ")
    @Description("Полный цикл создания запроса...") // ⑥ описание в отчёте
    @Severity(SeverityLevel.CRITICAL)               // ⑦ важность
    public void createRequestInRPU() {
        SzvReportsSteps steps = new SzvReportsSteps();
        // ↑ ⑧ создаём объект шагов (один раз на метод — так проще читать)

        steps.authorizeArhivRPU(UITypeSelector.getSelectedRpuType());
        // ↑ ⑨ Arrange: готовим систему. Контур выбирается автоматически.
        //    Логин/пароль — из credentials.properties, НЕ из кода.

        steps.createZaprosRPU();     // ⑩ Act: действие
        steps.genegalInfo();         //    каждый вызов = отдельный шаг в Allure
        steps.infoCitizen();
        steps.organizationData();
        steps.totalPeriod();
        steps.typeFormEmployment();
        steps.requestSubjectPeriodWork();
        steps.saveRequest();
        steps.signRequest();         // ⑪ подпись: API по умолчанию, UI по флагу (см. 6.1)
        steps.selectCertificate();
        steps.get_nomber_send_request();

        // ⑫ Assert: здесь могли бы быть проверки assertions.assertTrue(...)
        //    Если проверок нет, тест проверяет лишь то, что все шаги не упали.
    }
}
```

**Запомните:** ①–⑦ — аннотации («ярлыки», которые читают инструменты),
⑧–⑫ — обычный код сценария.

---

## 5. Мягкие проверки (TestAssertions)

В каждом тесте вам доступен объект `assertions` (достался от `BaseTest`).

### Почему «мягкие»?

Обычная проверка (`assertEquals`) **останавливает тест сразу** при провале:
вы узнаёте об одной ошибке за прогон.

Мягкая проверка работает иначе: ошибка **записывается в список**, тест продолжает
работать, а в конце — все ошибки разом. За один прогон видите ВСЕ проблемы.

```java
// Можно проверить сразу 5 вещей — увидите все 5 ошибок, а не первую:
assertions.assertTrue(table.isVisible(), "Таблица отчётов должна быть видна");
assertions.assertEquals("СЗВ-М", reportType, "Тип отчёта");
assertions.assertNotNull(reportNumber, "Номер отчёта должен присвоиться");
assertions.assertFalse(errorBanner.exists(), "Не должно быть баннера ошибки");
```

### Все методы (подробно)

| Метод | Что проверяет | Пример |
|-------|---------------|--------|
| `assertTrue(cond, msg)` | условие истинно | `assertTrue(count > 0, "Должны быть записи")` |
| `assertFalse(cond, msg)` | условие ложно | `assertFalse(banner.exists(), "Без ошибок")` |
| `assertEquals(actual, expected, msg)` | значения равны | `assertEquals(status, "SIGNED", "Статус")` |
| `assertNotEquals(a, b, msg)` | значения НЕ равны | — |
| `assertNotNull(obj, msg)` | не null | `assertNotNull(id, "ID присвоен")` |
| `assertVisible(el, name)` | элемент виден | `assertVisible($x("//h1"), "Заголовок")` |
| `assertNotExists(el, name)` | элемента нет | — |
| `fail(msg)` | немедленный провал | `fail("Система в неожиданном состоянии")` |

⚠️ **Порядок аргументов в assertEquals: сначала ФАКТИЧЕСКОЕ значение, потом ОЖИДАЕМОЕ.**
(`assertEquals(что_получили, что_ожидали, сообщение)`)

❗ Вызывать `assertions.assertAll()` вручную **не нужно** — `BaseTest` делает это
автоматически после каждого теста.

---

## 6. Новая инфраструктура: 7 возможностей

Недавно проект получил сервисный слой. Ниже — каждая возможность с примером
«копируй и адаптируй». Рабочие примеры — в `src/test/java/com/bft/test/examples/`.

### 6.1 Подписание через API (GraphQL) — вместо КриптоПРО

**Проблема раньше:** подпись требовала плагин КриптоПРО в браузере — медленно и хрупко.

**Теперь два режима** (переключаются одним свойством):

| Режим | Свойство | Когда использовать |
|-------|----------|--------------------|
| UI (по умолчанию) | `-Devs.sign.mode=ui` | Полное E2E через диалог и плагин КриптоПРО |
| API (экспериментальный) | `-Devs.sign.mode=api -Devs.sign.task-id=<userTaskId>` | Эмуляция подписи через Camunda. ТРЕБУЕТ реальный ID задачи — без него шаг упадёт с понятной ошибкой |

**Как пользоваться (вариант А — ничего не меняйте в тесте):**

```java
steps.signRequest();   // сам выберет режим по свойству evs.sign.mode;
                       // в api-режиме последующий selectCertificate() пропустится сам
```

**Как пользоваться (вариант Б — прямое управление):**

```java
SignService signer = new SignServiceImpl();
boolean ok = signer.signDocument(
        "user-task-id",     // ID задачи Camunda
        "decision-id",      // ID подписываемого решения
        "РООСКЛ",           // форма
        "1978",             // тип формы
        "file-ref-123");    // ссылка на предподписанный файл
assertions.assertTrue(ok, "Подпись должна пройти");
```

URL сервиса: `-Dservice.graphql-mesh.url=http://localhost:20266` (дефолт совпадает с CI).

Запрос и ответ GraphQL автоматически прикрепляются к Allure — при падении видно тело.

### 6.2 Визуальная регрессия (скриншоты vs эталоны)

Сравниваем, как выглядит страница, с сохранённым эталоном.

```java
public class MyVisualTest extends UITestBase {
    private final VisualComparator visual = new VisualComparator();

    @Test
    public void pageLooksRight() {
        // открыть страницу...

        // Мягкое сравнение всей страницы:
        visual.assertMatchesPage(assertions, "login-page");

        // Или отдельного элемента:
        // visual.assertMatchesElement(assertions, logoElement, "header-logo");
    }
}
```

**Первый запуск (эталонов ещё нет!):**

```bash
mvn test -Dtest=MyVisualTest -Devs.visual.update=true
# → эталоны сохранятся в src/test/resources/screens/
# → посмотрите их ГЛАЗАМИ, потом закоммитьте
```

При расхождении тест падает, а в Allure лежат три картинки: эталон / факт / diff
(расхождения подсвечены цветом). Диффы также пишутся в `build/reports/visual-diff/`.

### 6.3 Прямые запросы в базу данных

UI говорит «сохранено»? Самый честный способ проверить — спросить базу напрямую.

```java
public class MyDbTest extends BaseTest {   // BaseTest: браузер не нужен!
    private final DbClient db = new DbClient();

    @Test
    public void reportIsSaved() {
        long count = db.queryCount("FROM REPORTS WHERE SNILS = ?", "351-818-056-74");
        assertions.assertEquals(1L, count, "Ровно один отчёт по СНИЛС");

        var row = db.queryRow("SELECT STATUS FROM REQUESTS WHERE NUMBER = ?", "2026-001");
        assertions.assertEquals("SIGNED", String.valueOf(row.get("STATUS")));
    }
}
```

**Железные правила:**

1. Только плейсхолдеры `?` — никогда не склеивайте SQL строками (+ защита от инъекций).
2. Без `-Ddb.url=...` клиент бросит понятную ошибку с подсказкой (не зависнет).
3. Каждый SQL и результат прикрепляются к Allure.
4. UPDATE/DELETE на общем стенде — только после согласования.

Подключение: `-Ddb.url="jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF"`

### 6.4 События Kafka (асинхронные процессы)

Проверяем, что после действия в UI система отправила событие в Kafka.

```java
try (KafkaEventConsumer kafka = new KafkaEventConsumer()) {
    Optional<KafkaMessage> event = kafka.waitForMessage(
            "evs.report.events",                          // топик
            msg -> msg.value.contains("\"status\":\"SIGNED\""), // что ищем
            Duration.ofSeconds(30));                      // сколько ждать

    assertions.assertTrue(event.isPresent(), "Событие должно прийти за 30 сек");
}
```

Особенности:
- ловит только НОВЫЕ сообщения (после момента подписки) — старые не мешают;
- таймаут-основанный: не зависнет никогда;
- при неудаче в Allure прикрепляется список всех виденных сообщений — легко диагностировать.

Подключение: `-Dkafka.servers=172.18.38.9:9092`

### 6.5 gRPC: проверка внутренних сервисов

Перед долгим прогоном убедитесь, что бэкенд жив — за 5 секунд, а не за час странных падений.

```java
GrpcHealthCheck health = new GrpcHealthCheck();
health.checkAll(Duration.ofSeconds(5),
        GrpcConfig.Endpoint.METADATA,
        GrpcConfig.Endpoint.SEQUENCE);
// Если хоть один недоступен — AssertionError со списком всех проблем сразу.
```

Известные сервисы (порты = port-forward'ам из CI): `METADATA:12099`,
`SEQUENCE:9090`, `NOBLE_REGISTRY:12089`, `TRANSACT_MANAGER:13000`.
Хост/порт переопределяются: `-Dgrpc.metadata.host=... -Dgrpc.metadata.port=...`

Когда появятся `.proto` файлы сервисов, на этих же каналах строятся полноценные вызовы
(заготовка уже готова — см. javadoc `GrpcChannelFactory`).

### 6.6 Telegram-уведомления

Настраиваются один раз — дальше всё автоматом:

- ✅ PASSED / ❌ FAILED по каждому тесту;
- сводка каждые 10 тестов (total/passed/failed + % успеха).

```bash
mvn test \
  -Dtelegram.bot-token=123456:ABC-your-token \
  -Dtelegram.chat-id=-1001234567890
# или переменными окружения TELEGRAM_BOT_TOKEN / TELEGRAM_CHAT_ID
```

Токен бота выдаёт @BotFather, ID чата можно узнать через @userinfobot.
Не заданы — уведомления тихо отключены, тесты работают как обычно.

### 6.7 Jira TMS: автообновление статусов тест-кейсов

Связывает тесты с тест-кейсами в Jira через аннотацию `@TmsLink`:

```java
@Test
@TmsLink("EVS-T-101")            // ← код тест-кейса в TMS
public void myTest() { ... }
```

```bash
mvn test \
  -Dtms.url=https://jira.company.local \
  -Dtms.token=ВАШ_BEARER_TOKEN \
  -Dtms.test-run-id=1370 \
  -Dtms.project-id=10501 \
  -Dtms.user-key=JIRAUSER16914
```

Гарантии безопасности:
- без конфигурации интеграция полностью выключена;
- недоступность Jira **никогда** не роняет тесты (только warning в лог);
- обновляются только тесты с `@TmsLink`.

---

## 7. Отчёты Allure

После прогона соберите и откройте отчёт:

```bash
mvn allure:serve       # соберёт и откроет в браузере (локально проще всего)
```

Что искать в отчёте:

- **Behaviors** — тесты, сгруппированные по `@Epic/@Feature/@Story`;
- шаги внутри теста — каждый вызов `steps.xxx()` со своими вложенными шагами;
- вложения (attachments): скриншоты при падении, GraphQL запрос/ответ, SQL, Kafka-сообщения;
- при визуальном расхождении — картинки эталон/факт/diff.

---

## 8. Шпаргалка: все настройки проекта

Все настройки задаются системным свойством (`-Dключ=значение`) ИЛИ переменной
окружения (ВЕРХНИЙ_РЕГИСТР_С_ПОДЧЁРКИВАНИЯМИ). Свойство имеет приоритет.

### Основной прогон

| Свойство | Значения | По умолчанию |
|----------|----------|--------------|
| `evs.ui.type` | EVS_TEST_LKS, EVS_UAT_LKA… | из профиля |
| профиль Maven | test / uat / int / dev | dev |
| профиль браузера | chrome / firefox / yandex | chrome |

### Подписание

| Свойство | По умолчанию | Описание |
|----------|--------------|----------|
| `evs.sign.mode` | `ui` | `ui` — настоящий плагин (рабочий путь), `api` — эмуляция |
| `evs.sign.task-id` | — | ОБЯЗАТЕЛЕН для api-режима: ID задачи Camunda |
| `service.graphql-mesh.url` | `http://localhost:20266` | URL graphql-mesh |

### Telegram

| Свойство | Переменная окружения |
|----------|---------------------|
| `telegram.bot-token` | `TELEGRAM_BOT_TOKEN` |
| `telegram.chat-id` | `TELEGRAM_CHAT_ID` |

### Jira TMS

| Свойство | Переменная окружения |
|----------|---------------------|
| `tms.url` | `TMS_URL` |
| `tms.token` | `TMS_TOKEN` |
| `tms.test-run-id` | `TMS_TEST_RUN_ID` |
| `tms.project-id` | `TMS_PROJECT_ID` |
| `tms.user-key` | `TMS_USER_KEY` |
| `tms.enabled=false` — принудительно выключить | `TMS_ENABLED` |

### Визуальная регрессия

| Свойство | По умолчанию | Описание |
|----------|--------------|----------|
| `evs.visual.threshold` | `1.0` | допустимый % отличий пикселей |
| `evs.visual.update` | `false` | `true` = перезаписать эталоны |

### База данных

| Свойство | Описание |
|----------|----------|
| `db.url` | JDBC URL Phoenix Query Server |
| `db.user` / `db.password` | опционально |
| `db.enabled=false` | принудительно выключить |

### Kafka

| Свойство | По умолчанию |
|----------|--------------|
| `kafka.servers` | `localhost:9092` |
| `kafka.poll-interval-ms` | `500` |

### gRPC

| Свойство | Дефолт порта |
|----------|--------------|
| `grpc.metadata.host/port` | localhost:12099 |
| `grpc.sequence.host/port` | localhost:9090 |
| `grpc.noble-registry.host/port` | localhost:12089 |
| `grpc.transact-manager.host/port` | localhost:13000 |

---

## 9. Частые ошибки и их решения

| Симптом | Причина | Решение |
|---------|---------|---------|
| `UnsupportedClassVersionError` | Java не 11 | Проверьте JAVA_HOME и SDK в IDEA |
| Тест падает на авторизации | Нет credentials.properties | Скопируйте у коллеги, заполните |
| `Archive user credentials not available` | Нет evs.user2.* в credentials | Добавьте ключи в properties |
| Всё падает с таймаутами | Стенд недоступен / не тот контур | Проверьте `evs.ui.type` и VPN/доступ |
| `Database is not configured. Set -Ddb.url` | Не задан db.url | Добавьте `-Ddb.url="jdbc:phoenix:thin:url=..."` |
| Visual: `reference not found` | Нет эталона | Первый прогон с `-Devs.visual.update=true` |
| Тест «виснет» на waitForMessage | Неправильный топик/условие | Увеличьте таймаут, посмотрите сообщения в Allure |
| `gRPC endpoint unreachable` | Не поднят port-forward | В CI — сам; локально — `kubectl port-forward svc/<имя> <порт>:<порт>` |
| Telegram тишина | Не заданы bot-token/chat-id | Это норма; задайте свойства для включения |

---

## 10. Чек-лист перед pull request

- [ ] `mvn test-compile` проходит без ошибок;
- [ ] Тест наследует `UITestBase` (UI) или `BaseTest` (не-UI);
- [ ] Есть `@Test` + `@Tag` + `@Story` + `@Description`;
- [ ] `@AllureId` уникален, формат `ПРЕФИКС-NUM`;
- [ ] Логика: тест → шаги → страницы. Никаких селекторов в тесте (кроме коротких проверок);
- [ ] Пароли не в коде (только credentials.properties / env);
- [ ] SQL — только с плейсхолдерами `?`;
- [ ] Никаких `Thread.sleep()` — явные ожидания Selenide/Playwright;
- [ ] Проверки через `assertions.*`;
- [ ] Новый тест не помечен `@Disabled` без веской причины и задачи в Jira.

---

*Вопросы по фреймворку — к команде автоматизации. Этот документ живёт вместе с кодом:
нашли неточность — поправьте в том же PR.*
