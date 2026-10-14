# AGENTS.md

Инструкции для AI-агентов (OpenCode, Claude Code, Copilot и др.), работающих в этом репозитории.
Правила ниже дополняют [CONTRIBUTING.md](CONTRIBUTING.md) — при конфликте приоритет у него и у реального кода.

## О проекте

**EVS Testing Framework** — автотесты системы ЕВС (русскоязычный UI): ЛК Страхователя и ЛК Архивной организации.

- Стек: **Java 11, Maven 3.6+, JUnit 5.10.2, Playwright 1.49, Allure 2.22.2**, Lombok, JavaFaker.
- Артефакт: `com.example:EVS-autotest`. Рабочая ветка: `migration/playwright`.
- Весь код — в `src/test/java/com/bft`; `src/main` практически пуст (только Spring Boot `Application`).
- `com.bft.pw` — собственная Selenide-подобная обёртка над Playwright (`PwSession`, `PwElement`, `PwWait`, `Condition`).
  Selenide как зависимость не подключён и добавлять его не нужно.

> ⚠️ `README.md` и `ARCHITECTURE.md` местами устарели (упоминают Selenide, старую структуру).
> Источник истины — код, [CONTRIBUTING.md](CONTRIBUTING.md) и [docs/LK_GUIDE.md](docs/LK_GUIDE.md).

## Быстрые команды

```bash
mvn -B -DskipTests test-compile                      # компиляция (всегда перед коммитом)
mvn -B test -Dtest=SzvMXmlUploadSignTest             # один класс
mvn -B test -Dtest="Efs1#efs_szv_td"                 # один метод
mvn -B test -Dgroups=smoke                           # smoke (все)
mvn -B test -Dgroups="smoke & web"                   # UI smoke
mvn -B test -Dgroups="lk-insurer & smoke"            # smoke ЛК Страхователя
mvn -B test -DforkCount=2 ...                        # параллельный пилот (форки JVM)
mvn allure:serve                                     # Allure-отчёт (откроется в браузере)
```

- Профили запуска из `pom.xml`: `smoke_web`, `regress_web`, `smoke_manual`, `regress_manual`, `all_tests`.
- Профили контуров: `-Ptest` (по умолчанию) / `-Puat` / `-Pint` — задают `evs.ui.type`, `rpu.ui.type`, `uos.ui.type`.
- Иногда полезно: `-Dheadless=true`.

**Windows/PowerShell:** `-D…`-аргументы манглятся без `--%`. Пишите:
`mvn -o test --% -Dtest=... -Dservice.graphql-mesh.url=...` (см. `docs/MEMORY.md`, раздел 11).

## Quality gates (обязательны, блокируют CI)

```bash
bash scripts/verify-quality.sh   # оба гейта разом (нужен Git Bash на Windows)
```

1. **Checkstyle** — привязан к фазе `validate`, `config/checkstyle/checkstyle.xml`, тестовые исходники включены.
   Сейчас 0 нарушений — держим планку. `mvn -B -DskipTests validate`.
2. **Hard-waits ratchet** — `Thread.sleep` / `sleep()` в тестах запрещены (кроме `com.bft.pw.*` и `SmartWaits`).
   `scripts/hard-waits-baseline.txt` можно только **уменьшать**; `--update` — лишь с обоснованием в MR.

Те же проверки гоняются в GitHub Actions (`.github/workflows/quality.yml`) и GitLab CI (job `quality`, стадия `.pre`).

## Структура (реальная)

| Путь | Содержимое |
|---|---|
| `test/base/` | `BaseTest` (без UI), `UITestBase` (UI, Playwright-сессия на тест) |
| `test/` | `TestAssertions`, аннотации, retry, шаблон `template/NewFeatureTestTemplate.java` |
| `jupiter/extension/` | `@DisabledByIssue`, `@DisabledByInfrastructure`, `@DisabledOnEnvironment`, скриншоты при падении |
| `LK_Insurence/` | Тесты ЛК Страхователя: подпакеты по типам отчётов (`SZV_M`, `EFS_1`, `ODV_1`, …) |
| `LK_Archive/` | Тесты ЛК Архивной организации |
| `steps/` | Бизнес-шаги (`AuthSteps`, `ReportSignSteps`, `SzvReportsSteps`, `ArchiveSteps`, …) |
| `ui/pages/`, `ui/component/`, `ui/core/` | Page Objects, 14 UI-компонентов, SmartElement/ожидания |
| `pw/` | Обёртка над Playwright (движок UI-тестов) |
| `service/` | Подписание (GraphQL mesh, СЭДО, PFX/xmldsig ГОСТ), БД (Phoenix), Kafka, gRPC |
| `security/` | `TestUsers`, `CredentialManager`, маскировка логов |
| `testdata/` | Билдеры данных (`UserTestDataBuilder`, `ReportTestDataBuilder`) |
| `monitoring/` | `PerformanceMonitor`, `FlakyTestDetector` |
| `integration/tms/` | Jira TMS (активен). `integration/zephyr/*` — неактивный скелет, не трогать без задачи |

## Конвенции кода и тестов

1. **Слои:** `Test` (что проверяем) → `Steps` (бизнес-логика) → `Page/Pw` (взаимодействие с UI).
   Бизнес-логику не писать в тестах и page-объектах.
2. **Теги обязательны**: ЛК — `lk-insurer`, `lk-archive`; тип — `web`, `smoke`, `regress`, `xml-upload`,
   `report-sign`, `negative`, `manual`, `example`; проблемные — `flaky` (только с тикетом).
3. **Ожидания — только умные**: `SmartWaits`, `PwWait`, `Condition`, polling по состоянию. Никаких `sleep`.
4. **Локаторы**, по приоритету: `data-testid` → role/text/label → CSS → XPath (XPath — крайний случай).
5. **Тестовые данные**: builders из `com.bft.testdata.*`, уникальные (timestamp/uuid), с cleanup.
   Тест не зависит от порядка выполнения и других тестов.
6. **Учётные данные**: только `TestUsers` / `CredentialManager` / env. Секреты в репозиторий не коммитить.
7. **Мягкие проверки** — через `assertions` из `BaseTest` (авто-`assertAll` после теста).
8. **Flaky** — не ретраить молча: тег `flaky` + issue + `@DisabledByIssue("TICKET-123")`,
   инфраструктура — `@DisabledByInfrastructure`. `@Retry` — только осознанно.
9. **Имена и описания** — осмысленные, `@DisplayName`/`@Description` на русском.
10. Не создавать дубликаты существующих сценариев; новый ЛК — по чек-листу `docs/LK_GUIDE.md`;
    новые тесты — по `NEW_TESTS_GUIDE.md` и шаблону `NewFeatureTestTemplate.java`.

## Ловушки окружения

- **Артефакты не коммитить**: `target/`, `allure-*`, `*.log`, `*.patch` (уже в `.gitignore`).
- Playwright-браузеры ставятся один раз:
  `mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"`.
- Подписание отчётов (GraphQL mesh, СЭДО, xmldsig ГОСТ, port-forward, все свойства и рабочие команды) —
  подробно в [docs/MEMORY.md](docs/MEMORY.md). Перед изменениями в `service/graphql`, `service/sign`,
  `service/sedo`, `steps/ReportSignSteps` — прочитать его обязательно.
- Коммиты короткие: `test: …`, `ci: …`, `build: …`, `docs: …`, `fix: …`.
- Документация и тесты — на русском языке.

## Полезные документы

- [CONTRIBUTING.md](CONTRIBUTING.md) — правила, DoD теста, quality gates
- [NEW_TESTS_GUIDE.md](NEW_TESTS_GUIDE.md) — как добавить тест
- [BEGINNERS_GUIDE.md](BEGINNERS_GUIDE.md) — учебник «с нуля до первого теста»
- [docs/LK_GUIDE.md](docs/LK_GUIDE.md) — конвенции ЛК (масштабирование до 9 кабинетов)
- [docs/MEMORY.md](docs/MEMORY.md) — подписание, доступы, статус E2E
- [COMPONENTS_GUIDE.md](COMPONENTS_GUIDE.md) — UI-компоненты
