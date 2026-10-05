# Contributing to Pet_project_AQA

Правила для всех, кто пишет и ревьюит автотесты. Цель — стабильные зелёные прогоны
и предсказуемое масштабирование на ~9 ЛК.

## Быстрый старт

```bash
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

## Quality gates (обязательны, блокируют CI)

| Гейт | Команда | Что проверяет |
|---|---|---|
| Checkstyle | `mvn -B checkstyle:check` | wildcard-импорты, пустые catch; 0 нарушений |
| Hard-waits ratchet | `bash scripts/check-hard-waits.sh` | новые `Thread.sleep` / `sleep()` в тестах |

- Checkstyle **привязан к фазе `validate`** и валит сборку при нарушениях. Нарушений сейчас 0 — держим планку.
- Те же гейты автоматически гоняются в **GitLab CI** (job `quality`, стадия `.pre`, блокирующий)
  и в **GitHub Actions** (`.github/workflows/quality.yml`).
- Hard waits запрещены в тестовом коде, кроме инфраструктуры ожиданий (`com.bft.pw.*`, `SmartWaits`).
  Текущие вхождения заморожены в `scripts/hard-waits-baseline.txt`; файл можно только **уменьшать**.
  Если hard wait временно необходим — обнови baseline (`scripts/check-hard-waits.sh --update`)
  и обоснуй это в MR.

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
   - Тип: `web`, `smoke`, `regress`, `xml-upload`, `report-sign`, `negative`, `manual`, `example`
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

## Добавление нового ЛК

Пошаговый чек-лист и конвенции — в [docs/LK_GUIDE.md](docs/LK_GUIDE.md).

## Ветки и коммиты

- Текущая рабочая ветка — `migration/playwright`; синхронизация с GitLab `master` — fast-forward.
- Коммиты: короткие и по делу (`test: …`, `ci: …`, `build: …`, `docs: …`, `fix: …`).
- Не коммитим артефакты: `target/`, `allure-*`, `*.log`, `*.patch` (уже в `.gitignore`).
- Секреты — только через CI/CD variables или локальные env, не в репозиторий.
