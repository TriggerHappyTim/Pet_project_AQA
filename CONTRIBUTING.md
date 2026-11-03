# Contributing to Pet_project_AQA

Правила для всех, кто пишет и ревьюит автотесты. Цель — стабильные зелёные прогоны
и предсказуемое масштабирование на ~9 ЛК.

## Быстрый старт

```bash
# все quality-гейты разом (Checkstyle + hard-waits + XPath + теги)
bash scripts/verify-quality.sh

# компиляция + статический анализ
mvn -B -DskipTests test-compile

# все smoke-тесты
mvn -B test -Dgroups=smoke

# только UI smoke
mvn -B test -Dgroups="smoke & web"

# smoke конкретного ЛК (см. docs/LK_GUIDE.md)
mvn -B test -Dgroups="lk-insurer & smoke"

# один тест
mvn -B test -Dtest=ReportFixtureIntegrityTest
mvn -B test -Dtest="Efs1#efs_szv_td"
```

## Окружения (контуры)

Контур задаётся в одном месте — профилем в `pom.xml`: `test.environment` (метка `environment`
в отчётах, `TestConfig`, `@DisabledOnEnvironment`) плюс `evs/rpu/uos.ui.type` (URL для `UITypeSelector`).
Без `-P` активен `uat` (`activeByDefault`).

| Профиль | Контур | Статус |
|---|---|---|
| `-P uat` (по умолчанию) | UAT | ✅ **Рабочий контур** для UI-smoke |
| `-P test` | TEST | ⚠️ Нестабилен для UI: РС/регистрация ЕФС-1 (gRPC) не работают |
| `-P int` | пока UAT | ❌ INT-стенд не сконфигурирован: в `UIType` нет INT-значений, URL неизвестны. Профиль работает как `uat`, отчёт помечается `uat` (а не `int`) |

- Профили запуска: `smoke_web`, `regress_web`, `smoke_manual`, `regress_manual`, `all_tests`.
- Для UI-smoke используй **UAT** и явно указывай профиль:

```bash
mvn -B test -P uat -Dgroups="lk-insurer & smoke"
```

**Windows/PowerShell:** `-D…`-аргументы манглятся без `--%`. Пиши
`mvn -o test --% -Dtest=... -Dservice.graphql-mesh.url=...` (см. `docs/MEMORY.md`, разд. 11).

### Внешние свойства (локальный запуск)

Часть настроек приходит не из профилей, а из системных свойств `-D…`. На чистой машине
их нужно задать явно — иначе тест либо не запустится, либо пойдёт не туда:

| Свойство | Зачем | По умолчанию |
|---|---|---|
| `environment` | Контур для `TestConfig` и `@DisabledOnEnvironment` | `uat` (из `pom.xml`) |
| `evt.auth.bypass` | Обход ESIA-логина для локального фронта (`true`/`false`) | `false` — реальный вход |
| `evs.auth.bypass.cookie` | Session-cookie `_oauth2_proxy` для bypass | из `auth-bypass.properties` / yml фронта |
| `evs.auth.bypass.configFile` | Путь к `application-auth-dev.yml` локального фронта | `D:\evs-oi-ipu-usv-front\…` |

- `evt.auth.bypass=true` нужен **только** для локального фронта (`AuthBypassConfig`).
  На TEST/UAT логин идёт через ESIA, cookie берётся из `credentials.properties` / CI-переменных.
- Cookie и `userId` в репозиторий не коммитим: `auth-bypass.properties` — локальный файл.
- Свойства GraphQL-mesh, СЭДО, Kafka и подписания — в [docs/MEMORY.md](docs/MEMORY.md).

```bash
# локальный запуск с обходом ESIA
mvn test --% -Devt.auth.bypass=true -Devs.auth.bypass.cookie=<cookie> -Dtest=...
```

## Quality gates (обязательны, блокируют CI)

| Гейт | Команда | Что проверяет |
|---|---|---|
| Checkstyle | `mvn -B checkstyle:check` | wildcard-импорты, пустые catch; 0 нарушений |
| Hard-waits ratchet | `bash scripts/check-hard-waits.sh` | новые `Thread.sleep` / `sleep()` в тестах |
| XPath ratchet | `bash scripts/check-xpath.sh` | рост числа XPath-локаторов в тестах |
| Tag consistency | `bash scripts/check-tags.sh` | теги в документации = `<groups>` в `pom.xml` = `@Tag` в тестах; профиль не должен выбирать 0 тестов |
| Flaky/retry | `bash scripts/check-flaky.sh` | `flaky` без `@DisabledByIssue`/`@DisabledByInfrastructure`; `@Retry` вне `scripts/retry-allowlist.txt` |

- Checkstyle **привязан к фазе `validate`** и валит сборку при нарушениях. Нарушений сейчас 0 — держим планку.
- Те же гейты автоматически гоняются в **GitLab CI** (job `quality`, стадия `.pre`, блокирующий)
  и в **GitHub Actions** (`.github/workflows/quality.yml`).
- **GitHub Actions тесты не запускает** — только гейты: там нет доступа к стендам и секретам.
  Зелёная галочка GitHub означает «стиль, ожидания и теги в порядке», а не «тесты прошли».
- **Тесты выполняет GitLab CI** (job `maven test`: `mvn clean test` с профилем контура и `-Dtest=…`)
  и локальный запуск. Важно: job `maven test` объявлен с `allow_failure: true`, поэтому его падение
  **не блокирует** пайплайн — результат смотри в логе job и в Allure, а не только по статусу пайплайна.
- Hard waits запрещены в тестовом коде, кроме инфраструктуры ожиданий (`com.bft.pw.*`, `SmartWaits`).
  Текущие вхождения заморожены в `scripts/hard-waits-baseline.txt`; файл можно только **уменьшать**.
  Если hard wait временно необходим — обнови baseline (`scripts/check-hard-waits.sh --update`)
  и обоснуй это в MR.
- **XPath-локаторы** заморожены в `scripts/xpath-baseline.txt`; файл можно только **уменьшать**.
  Новые локаторы: `data-testid` → role/text → CSS (`:has-text`, `:text-is`); XPath — крайний случай
  (см. `docs/PW_GUIDE.md`). Обновлять baseline (`--update`) допустимо только при **снижении** общего числа.
- **Теги** сверяются скриптом `scripts/check-tags.sh`: каждый тег из `<groups>` в `pom.xml` должен быть
  описан в `AGENTS.md` и использован хотя бы в одном `@Tag`, а каждый профиль — выбирать хотя бы один класс.
  Канонический тег регресса — `regression` (не `regress`), он же используется в профилях `regress_web` / `regress_manual`.

## Как писать тесты

1. **JUnit 5 + теги.** Базовые классы: `BaseTest` (без UI) и `UITestBase` (UI, Playwright-сессия на тест).
   *Проверка: гейт `scripts/check-tags.sh`.*
2. **Слои:** `Test` (что проверяем) → `Steps` (бизнес-шаги) → `Pages/Pw` (взаимодействие с UI).
   Бизнес-логика — в steps, а не в page-объектах и не в тестах. *Проверка: ревью.*
3. **Ожидания — только умные:** `SmartWaits`, `PwWait`, `Condition`, polling по состоянию.
   Никаких `Thread.sleep`/`sleep()`. *Проверка: гейт `scripts/check-hard-waits.sh`.*
4. **Независимость.** Каждый тест сам готовит данные и не зависит от порядка выполнения.
   Данные — через билдеры `com.bft.testdata.*`, уникальные (timestamp/uuid), с очисткой.
   *Проверка: ревью + параллельный прогон.*
5. **Учётные записи** — через `TestUsers` / `CredentialManager`; пароли в код не коммитим. *Проверка: ревью.*
6. **Локаторы** — приоритет: `data-testid` > role/text/label > CSS > XPath. XPath — только когда иначе нельзя.
   *Проверка: гейт `scripts/check-xpath.sh` (ratchet) + ревью приоритета.*
7. **Теги обязательны:**
   - ЛК: `lk-insurer`, `lk-archive`, … (см. `docs/LK_GUIDE.md`)
   - Тип: `web`, `smoke`, `regression`, `xml-upload`, `report-sign`, `negative`, `manual`, `example`
   - Проблемные: `flaky` (только с тикетом)
   *Проверка: гейт `scripts/check-tags.sh`.*
8. **Мягкие проверки** — `assertions` из `BaseTest` (автоматический `assertAll` после теста). *Проверка: ревью.*
9. **Flaky — не ретраить молча:** тег `flaky` + issue + `@DisabledByIssue("TICKET-123")`;
   инфраструктура — `@DisabledByInfrastructure`; `@Retry` — только из `scripts/retry-allowlist.txt`.
   *Проверка: гейт `scripts/check-flaky.sh`.*
10. **Имена и новые тесты.** Осмысленные имена и `@DisplayName`/`@Description` на русском;
    новый тест — по `NEW_TESTS_GUIDE.md` и шаблону `NewFeatureTestTemplate.java`, новый ЛК —
    по `docs/LK_GUIDE.md`; не дублировать существующие сценарии. *Проверка: ревью.*

## Definition of Done для теста

У каждого пункта указан способ проверки: **гейт** (машина) или **ревью** (человек).

- [ ] Независим: проходит в любом порядке и параллельно с другими — **ревью** (+ прогон с `-DforkCount=2`)
- [ ] Сам создаёт и чистит тестовые данные; данные уникальны — **ревью**
- [ ] Зелёный локально (`mvn test -Dtest=...`) — **прогон**; в GitLab CI job `maven test` — **лог/Allure**
      (job объявлен с `allow_failure: true`, поэтому падение не блокирует пайплайн — смотри результат, а не статус)
- [ ] Нет hard waits; ожидания — умные — **гейт** `scripts/check-hard-waits.sh`
- [ ] Есть теги ЛК и типа (`lk-*`, `web`/`smoke`/…) — **гейт** `scripts/check-tags.sh`
- [ ] Осмысленное имя и `@DisplayName`/`@Description` — **ревью**
- [ ] Checkstyle без нарушений, XPath не растёт — **гейт** `mvn -B checkstyle:check` + `scripts/check-xpath.sh`
- [ ] При падении достаточно артефактов (скриншот/trace — уже встроено) — **ревью**:
      единственный пункт, который нельзя проверить автоматически до самого падения
- [ ] Ревью пройдено; дубликаты существующих сценариев не созданы — **ревью**

## Флейки и quarantine

- Новый флейк — это **баг в тесте или в продукте**, а не повод для ретраев.
- Помечай нестабильный тест тегом `flaky`, заводи issue и указывай его в
  `@DisabledByIssue("TICKET-123")` (есть готовые JUnit-расширения).
- Инфраструктурные проблемы — `@DisabledByInfrastructure`.
- `FlakyTestDetector` помогает собирать статистику; не игнорируй его отчёты.
  Подключается точечно: `@ExtendWith(FlakyTestDetector.class)` на проблемном классе.
- Разборы сбоев и «что после этого изменили в правилах» копятся в [docs/FLAKY.md](docs/FLAKY.md).
  Запись добавляется в том же MR, что и отключение теста.

## Отключение тестов (`@Disabled`)

Отключай тест **осознанно** и всегда с причиной. «Тихое» `@Disabled` без объяснения недопустимо.
Используй готовые расширения из `com.bft.jupiter.annotation`:

| Ситуация | Аннотация |
|---|---|
| Баг/недоделка в продукте | `@DisabledByIssue("TICKET-123")` |
| Проблема стенда/инфраструктуры | `@DisabledByInfrastructure("<причина>")` |
| Тест не подходит конкретному контуру | `@DisabledOnEnvironment({"TEST"})` |

Правила:

- Причину описывай **на русском** и конкретно: что именно не работает, где (контур, сервис, TraceId).
- Тест с `@Disabled` должен оставаться **самодостаточным**: когда блокер снимут — он заработает без переписывания.
- Не оставляй `@Disabled` «на всякий случай» без тикета/причины.
- В `@DisabledByInfrastructure` указывай затронутый контур (обычно TEST) и что при этом работает на UAT.

Пример:

```java
@Test
@Tag("lk-insurer")
@Tag("smoke")
@DisabledByIssue("EVS-1234") // UAT: graphql-mesh INTERNAL на «Подписать» (TraceId …)
void signAndSend() { ... }
```

## Артефакты: что видно в отчёте

При падении в Allure уже есть скриншот, trace и лог. Чего не хватало — следа решений:
на каком контуре шёл тест, что он делал и почему. Этот след пишет `AgentDecisionLog`
во вложение `agent-decisions` (рядом со скриншотом) и в метки `environment`,
`agent.contour`, `agent.test`.

Что нужно писать явно (из кода это не выводится):

```java
AgentDecisionLog.decision("подписываем через API", "КриптоПРО на стенде недоступен");
AgentDecisionLog.step("скачали XML из архива");
AgentDecisionLog.note("стенд отвечал 502 — повторили один раз");
```

Автоматически в журнал попадают фазы `arrangeActAssert` и `performAction`/`performCheck`
из `BaseTest`, а также `TestLogger.step/check/action`. Тесты, не наследующие `BaseTest`,
журнал не получают — им нужно добавить `@ExtendWith(AgentDecisionExtension.class)`.

## Параллелизм (пилот)

Пока тесты идут последовательно. Пилотный запуск — форками JVM (каждый форк = свой браузер,
изоляция сессии уже на уровне теста):

```bash
# smoke ЛК Страхователя в 2 форка
mvn -B test -Dgroups="lk-insurer & smoke" -DforkCount=2
```

Перед расширением параллелизма убедись, что тесты не делят данные: уникальные данные
на тест, без статических мутабельных полей, cleanup после каждого теста.

## Добавление нового ЛК

Пошаговый чек-лист и конвенции — в [docs/LK_GUIDE.md](docs/LK_GUIDE.md).

## Ветки и коммиты

- Текущая рабочая ветка — `migration/playwright`; синхронизация с GitLab `master` — fast-forward.
- Коммиты: короткие и по делу (`test: …`, `ci: …`, `build: …`, `docs: …`, `fix: …`).
- Не коммитим артефакты: `target/`, `allure-*`, `*.log`, `*.patch` (уже в `.gitignore`).
- Секреты — только через CI/CD variables или локальные env, не в репозиторий.
