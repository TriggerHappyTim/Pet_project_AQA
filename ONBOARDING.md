# ONBOARDING — с чего начать в EVS Testing Framework

Короткая инструкция для нового инженера: как поднять окружение, запустить первые тесты
и не наступить на известные грабли. Подробные правила — в [CONTRIBUTING.md](CONTRIBUTING.md),
контекст для ИИ-ассистента — в [AI_TESTING_CONTEXT.md](AI_TESTING_CONTEXT.md).

## 1. Что это за проект

**EVS Testing Framework** — репозиторий автотестов системы ЕВС (русскоязычный UI):
ЛК Страхователя и ЛК Архивной организации.

- Java 11, Maven, JUnit 5, Playwright (через собственную обёртку `com.bft.pw`),
  Allure, REST Assured, Lombok, JavaFaker.
- `src/main` практически пуст (только Spring Boot `Application`) — **это не серверное
  приложение, dev-сервера здесь нет**. «Dev-режим» = запуск одного теста с видимым браузером.
- Все тесты лежат в `src/test/java/com/bft`, фикстуры — в `src/test/resources`.
- Основная рабочая ветка — `migration/playwright`.

## 2. Что установить

| Инструмент | Версия / примечание |
|---|---|
| JDK | **11** (Temurin 11 рекомендуется). Именно JDK 11 должен быть в `JAVA_HOME`: Lombok 1.18.28 и JAXB-стек на новых JDK могут не собраться. |
| Maven | 3.6+ (wrapper `mvnw` в репозитории нет) |
| Git | любая актуальная версия |
| Браузеры | ставятся Playwright отдельной командой (см. ниже) |
| Git Bash | нужен для `scripts/*.sh` на Windows |

Проверьте, что Maven видит правильную Java:

```bash
mvn -v
# Ожидаемо: Java version: 11.x, vendor: Eclipse Adoptium (или другой JDK 11)
```

Если `mvn -v` показывает другую версию — задайте `JAVA_HOME`.

**PowerShell:**

```powershell
$env:JAVA_HOME = "C:\Users\<вы>\jdk-11"
mvn -v
```

**Git Bash:**

```bash
export JAVA_HOME="/c/Users/<вы>/jdk-11"
mvn -v
```

### Доступ к зависимостям

В `pom.xml` подключены корпоративные Maven-репозитории `gitlab.bft.local`. Если зависимости
не скачиваются (401/403/timeout):

- нужен доступ к корпоративной сети/VPN;
- попросите у команды `settings.xml` с credentials для `gitlab-ps` / `gitlab-maven`
  и запускайте Maven с `-s <путь>/settings.xml`.

## 3. Первичная установка

```bash
git clone <url-репозитория>
cd Pet_project_AQA

# 1) Компиляция тестов (быстрая проверка, что окружение собралось)
mvn -B -DskipTests test-compile

# 2) Браузеры Playwright — один раз на машину
mvn -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

# 3) Локальные учётные данные (файл в .gitignore, в репозиторий не попадает)
cp src/test/resources/credentials.properties.example src/test/resources/credentials.properties
# заполните логины/пароли реальными значениями

# 4) (опционально) локальные переменные окружения
cp .env.example .env
```

> **Секреты.** В репозитории уже есть исторические логины в `application-uat.yml` /
> `application-test.yml`. Не добавляйте туда новые секреты и не копируйте их в документацию.
> Новые учётные данные — только в `credentials.properties` (локально) или в GitLab CI/CD Variables.

## 4. Переменные окружения и учётные данные

Полный шаблон — [.env.example](.env.example). Минимум для UI-smoke на UAT:

```dotenv
BROWSER=chrome
HEADLESS=false
ENVIRONMENT=uat
```

Важно:

- **Maven не читает `.env` автоматически.** Используйте `set -a; source .env; set +a`
  в Git Bash, `$env:ИМЯ="значение"` в PowerShell или передавайте свойства через `-D...`.
- Приоритет: `-D`-свойство → переменная окружения → значение по умолчанию.
- Логины/пароли (`evs.username`, `evs.password`, `evs.user1.*` и т.д.) читаются
  из `src/test/resources/credentials.properties` или из `-Devs.username=...`.
  Ключи с точками специально не вынесены в `.env`.
- Контур задаётся профилем Maven: `-P uat` (по умолчанию), `-P test`, `-P int`
  (пока работает как UAT — INT-стенд не сконфигурирован).
- Подписание отчётов требует GraphQL mesh на `localhost:20266` (port-forward) и ключей
  `EVS_SIGN_PEM_DIR` / `EVS_SIGN_PFX_PATH`. Полный разбор — [docs/MEMORY.md](docs/MEMORY.md).

## 5. «Dev-режим»: первый запуск

Сначала — быстрый тест без UI (проверяет фикстуры и XSD, стенд не нужен):

```bash
mvn -B test -Dtest=ReportFixtureIntegrityTest
```

Затем — один UI-тест с видимым браузером. Локально браузер и так видимый (`HEADLESS=false`
по умолчанию), но лучше задать явно:

```bash
mvn -B test -Dtest="ReportXmlUploadTest" -Dselenide.headless=false
```

Один конкретный метод:

```bash
mvn -B test -Dtest="Efs1#efs_szv_td" -Dselenide.headless=false
```

В IDE (IntelliJ IDEA) можно запускать тест-класс/метод кнопкой Run — JUnit 5 и Allure
подхватываются из `pom.xml`. Для UI-теста убедитесь, что в Run Configuration заданы
нужные `-D`-свойства и `credentials.properties` существует.

> В старых документах встречается `-Dheadless=true`. Фактически `TestConfig` читает
> свойство **`selenide.headless`** (или переменную `HEADLESS`).

### Allure-отчёт

```bash
mvn -B test -Dtest=ReportFixtureIntegrityTest
mvn allure:serve
```

Результаты пишутся в `target/allure-results` (так настроено в `pom.xml`). При падении UI-теста
в отчёт автоматически попадают скриншот, Playwright trace (если `EVS_TRACE=true`) и журнал
решений `agent-decisions`.

## 6. Как запускать тесты

```bash
# компиляция + Checkstyle (фаза validate)
mvn -B -DskipTests test-compile

# все smoke
mvn -B test -Dgroups=smoke

# UI smoke
mvn -B test -Dgroups="smoke & web"

# smoke ЛК Страхователя на рабочем контуре UAT
mvn -B test -P uat -Dgroups="lk-insurer & smoke"

# один класс / один метод
mvn -B test -Dtest=ReportFixtureIntegrityTest
mvn -B test -Dtest="Efs1#efs_szv_td"

# параллельный пилот (2 форка JVM)
mvn -B test -P uat -Dgroups="lk-insurer & smoke" -DforkCount=2
```

Профили запуска из `pom.xml`: `smoke_web`, `regress_web`, `smoke_manual`, `regress_manual`,
`all_tests`. Профили контура: `uat` (по умолчанию), `test`, `int`.

> **Windows/PowerShell:** `-D…`-аргументы манглятся без `--%`. Пишите:
> `mvn -o test --% -Dtest=... -Dservice.graphql-mesh.url=...`

## 7. Перед коммитом

```bash
# все quality-гейты разом (нужен Git Bash)
bash scripts/verify-quality.sh

# минимум — компиляция
mvn -B -DskipTests test-compile
```

Гейты блокируют CI: Checkstyle (0 нарушений), hard-waits ratchet, XPath ratchet,
согласованность тегов, правила flaky/retry. Подробности — в
[CONTRIBUTING.md](CONTRIBUTING.md#quality-gates-обязательны-блокируют-ci).

Коммиты короткие и по теме: `test: …`, `ci: …`, `build: …`, `docs: …`, `fix: …`.
Документация и тесты — на русском языке.

## 8. Куда смотреть дальше

| Документ | Зачем |
|---|---|
| [CONTRIBUTING.md](CONTRIBUTING.md) | 10 конвенций, Definition of Done, quality gates |
| [AI_TESTING_CONTEXT.md](AI_TESTING_CONTEXT.md) | контекст и golden examples для ИИ-ассистента |
| [NEW_TESTS_GUIDE.md](NEW_TESTS_GUIDE.md) | пошагово: как добавить новый тест |
| [BEGINNERS_GUIDE.md](BEGINNERS_GUIDE.md) | учебник «с нуля до первого теста» |
| [docs/LK_GUIDE.md](docs/LK_GUIDE.md) | конвенции ЛК и масштабирование на 9 кабинетов |
| [docs/MEMORY.md](docs/MEMORY.md) | подписание, GraphQL mesh, порт-форварды, доступы |
| [docs/FLAKY.md](docs/FLAKY.md) | журнал разборов флейков |
| [COMPONENTS_GUIDE.md](COMPONENTS_GUIDE.md) | UI-компоненты и локаторы |

## 9. Частые проблемы

| Симптом | Причина / решение |
|---|---|
| `Unsupported class file major version`, падения Lombok | Maven запущен не на JDK 11. Проверьте `mvn -v`, `JAVA_HOME`. |
| `Executable doesn't exist` / браузер не найден | Выполните `mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"`. |
| Зависимости не скачиваются, 401/403 | Нет доступа к `gitlab.bft.local` или не передан `settings.xml`. |
| `Credentials ... не найдены` | Нет `src/test/resources/credentials.properties` или пустые ключи `evs.*`. |
| Тест падает на авторизации ESIA | Проверьте логин/пароль и контур (`-P uat`); для локального фронта — `evt.auth.bypass` (см. CONTRIBUTING). |
| GraphQL mesh `:20266` недоступен | Нужен `kubectl port-forward` или запуск в сети кластера; см. docs/MEMORY.md. |
| Пустой Allure | Смотрите `target/allure-results`; `mvn allure:serve` запускайте после прогона тестов. |
| UI-тесты нестабильны на TEST | Используйте UAT: `-P uat`; ограничения TEST описаны в docs/LK_GUIDE.md. |
| PowerShell ломает `-D...` | Добавьте `--%` сразу после `mvn` (см. раздел 6). |
