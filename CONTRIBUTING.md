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

- Checkstyle **привязан к фазе `validate`** и валит сборку при нарушениях. Нарушений сейчас 0 — держим планку.
- Те же гейты автоматически гоняются в **GitLab CI** (job `quality`, стадия `.pre`, блокирующий)
  и в **GitHub Actions** (`.github/workflows/quality.yml`).
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
2. **Слои:** `Test` (что проверяем) → `Steps` (бизнес-шаги) → `Pages/Pw` (взаимодействие с UI).
   Бизнес-логика — в steps, а не в page-объектах и не в тестах.
3. **Ожидания — только умные:** `SmartWaits`, `PwWait`, `Condition`, polling по состоянию.
   Никаких `Thread.sleep`/`sleep()` (см. гейт выше).
4. **Независимость.** Каждый тест сам готовит данные и не зависит от порядка выполнения.
   Данные — через билдеры `com.bft.testdata.*`, уникальные (timestamp/uuid), с очисткой.
5. **Учётные записи** — через `TestUsers` / `CredentialManager`; пароли в код не коммитим.
6. **Локаторы** — приоритет: `data-testid` > role/text/label > CSS > XPath. XPath — только когда иначе нельзя.
7. **Теги обязательны:**
   - ЛК: `lk-insurer`, `lk-archive`, … (см. `docs/LK_GUIDE.md`)
   - Тип: `web`, `smoke`, `regression`, `xml-upload`, `report-sign`, `negative`, `manual`, `example`
   - Проблемные: `flaky` (только с тикетом)
8. **Мягкие проверки** — `assertions` из `BaseTest` (автоматический `assertAll` после теста).

## Definition of Done для теста

- [ ] Независим: проходит в любом порядке и параллельно с другими
- [ ] Сам создаёт и чистит тестовые данные; данные уникальны
- [ ] Зелёный локально (`mvn test -Dtest=...`) и в CI
- [ ] Нет hard waits; ожидания — умные
- [ ] Есть теги ЛК и типа (`lk-*`, `web`/`smoke`/…)
- [ ] Осмысленное имя и `@DisplayName`/`@Description`
- [ ] Checkstyle без нарушений, hard-waits ratchet не растёт
- [ ] При падении достаточно артефактов (скриншот/trace — уже встроено)
- [ ] Ревью пройдено; дубликаты существующих сценариев не созданы

## Флейки и quarantine

- Новый флейк — это **баг в тесте или в продукте**, а не повод для ретраев.
- Помечай нестабильный тест тегом `flaky`, заводи issue и указывай его в
  `@DisabledByIssue("TICKET-123")` (есть готовые JUnit-расширения).
- Инфраструктурные проблемы — `@DisabledByInfrastructure`.
- `FlakyTestDetector` помогает собирать статистику; не игнорируй его отчёты.
  Подключается точечно: `@ExtendWith(FlakyTestDetector.class)` на проблемном классе.

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
