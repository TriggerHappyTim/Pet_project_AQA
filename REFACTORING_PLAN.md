# REFACTORING_PLAN.md

План рефакторинга тестового проекта AQA (Java + TestNG + Selenide + Allure).

---

## 1. Текущая структура проекта

Maven-проект (Spring Boot, `src/main/java/com/bft/evs/Application.java`). Весь тестовый код в `src/test/java/com/bft/`.

```
src/test/java/com/bft/
├── ui/            # Новая реализация Page Objects (Selenide)
│   ├── pages/     #   BasePage, LoginPage, MainPage
│   ├── core/      #   BasePage, element/*, wait/*
│   └── component/ #   BaseComponent, Input, Select, Checkbox, Table, ...
├── steps/         # Step-классы (SzvReportsSteps — фасад, делегирует в AuthSteps/ArchiveSteps/Report*Steps)
├── utils/         # FormStructureParser, FormFieldMetadata, DebugUtils, CryptoProPluginVerifier
├── LK_Insurence/  # Тесты отчётов: SZV_M, SZV_K, SZV_KORR, SZV_STAJ, SZV_DSO, SZV_ISH, SZV_TD, EFS_1, ODV_1
├── LK_Archive/    # ArchivesTest
├── test/
│   ├── base/      # ApiTestBase, DataDrivenTestBase
│   ├── helpers/   # LoginHelper, PageObjectHelper, AssertionHelper, SmartWaits
│   ├── negative/  # NegativeTestCases, ApiNegativeTestCases
│   ├── examples/  # Примеры (ImprovedTestExamples, AdvancedReportingExample)
│   ├── retry/     # Retry, RetryAnalyzer
│   ├── logging/   # TestLogger
│   └── annotations/ # Allure/TestNG аннотации
├── config/        # TestConfig, TestStrategyType, UITestStrategy
├── helpers/       # TestConfig, ConfigReader, DataFiller, StandardWaits, forms/*
├── security/      # CredentialManager, TestUsers, провайдеры, masking/*
├── strategy/      # TestExecutionStrategy, BaseTestExecutionStrategy, ...
├── testdata/      # TestDataBuilder, UserTestDataBuilder, ReportTestDataBuilder
├── enums/         # ReportFormType, ReportXmlResource, UIType, TimeoutConstants, ...
├── browser/factory/ # Chrome/Firefox/YandexBrowserFactory
├── integration/   # Zephyr, Jira, Allure интеграции
└── monitoring/    # FlakyTestDetector, PerformanceMonitor
```

---

## 2. Основные проблемы

### Проблема 1 (КРИТИЧЕСКАЯ). Отсутствует базовый класс `UITestBase`
Минимум 15 тестовых классов (`Szv_*`, `Odv1`, `Efs1`, `ArchivesTest`, `NegativeTestCases`, `CryptoProCertificateTest`, `ReportXmlUploadTest`) и `DataDrivenTestBase` наследуются от `com.bft.test.base.UITestBase`, но **файла нет ни в `src/test/java`, ни в скомпилированных классах `target/`**. Документация (README, ARCHITECTURE.md) описывает его, но класс потерян/не создан. **Проект не компилируется.**

### Проблема 2. Идентичный XML-upload тест скопирован в 6+ классах
Метод `szv_*_xml()` полностью одинаковый в `Szv_staj`, `Szv_korr`, `Szv_dso`, `Szv_ish`, `Szv_k`, `Szv_td`, `Szv_m`:
```java
steps.authorizeEVS(UITypeSelector.getSelectedUIType());
steps.addReports();
steps.selectReportType(ReportFormType.X);
steps.addNewReport();
steps.sendXml(ReportXmlResource.Y);
```
Отличается только тип отчёта и XML-ресурс. Классический случай для параметризации через `DataProvider`.

### Проблема 3. Дубли тестовых методов внутри одного класса
- В `ArchivesTest` методы `processRequestInEVS()` (ARCH-003) и `setRegisterRequests()` (ARCH-004) **идентичны на 100%** (одна и та же цепочка шагов).
- В `SzvReportsSteps` дублируются пары методов: `searchRequest()` / `searchRequest1()` (отличаются только источником номера запроса), перегруженные `authorizeArhivEVS()`, `chooseEFS()`, `submitAndSend()` — копипаст с незначительными отличиями.

### Проблема 4. Несколько Page Objects с одинаковыми именами
Существуют три `BasePage` (`gui`, `ui/pages`, `ui/core`) и два `LoginPage` (`gui`, `ui/pages`). Старый пакет `gui/` дублирует функциональность нового пакета `ui/`, при этом `steps/SzvReportsSteps` импортирует **старый** `com.bft.gui.LoginPage` и новый `ui.pages.MainPage` одновременно — смешение поколений Page Objects в одном классе.

### Проблема 5. Дублирующиеся конфигурации и константы
Два класса `TestConfig`: `com.bft.config.TestConfig` (231 строка, окружение/браузеры) и `com.bft.helpers.TestConfig` (59 строк, константы тестовых данных, в т.ч. вложенный `InsuredPerson`). Данные застрахованного лица дублируются также в `helpers/forms/InsuredPerson`, `testdata/UserTestDataBuilder`, `helpers/DataFiller`. Расширение данных — в трёх местах.

### Проблема 6 (доп.). `SzvReportsSteps` — «божественный объект» с мёртвым кодом
1316 строк, смешивает авторизацию (EVS/RPU/UOS), архивные запросы, формы всех отчётов, заглушки (`isOnZLPage()`, `hasErrorMessages()`, `isContinueButtonEnabled()`, `getCurrentZLFieldValue()`) и незадействованные методы (`changeInfoCitizen`, `infoSeparateDivision`, `attachmentsRequest`, `addGeneralInfoISH/TD/EFS`, `addEvent`/`addEventEFS` и др. — часть вызывается, часть нет).

---

## 3. План рефакторинга

Шаги упорядочены по приоритету и зависимости. Каждый шаг заканчивается запуском компиляции/тестов.

### Шаг 1. Восстановить компилируемость проекта
- **Задача:** Вернуть `UITestBase` (или отказаться от наследования).
- **Вариант А (предпочтительный):** Восстановить файл `src/test/java/com/bft/test/base/UITestBase.java` по описанию в `ARCHITECTURE.md` / `README.md` (базовый класс UI-тестов с Arrange-Act-Assert, soft assertions, `@BeforeMethod`/`@AfterMethod`, интеграция с Allure). `DataDrivenTestBase` уже ссылается на него — он должен наследовать `UITestBase`.
- **Вариант Б:** Если класс был удалён намеренно — заменить `extends UITestBase` на `extends DataDrivenTestBase` (или удалить наследование вовсе).
- **Критерий:** `mvn test-compile` собирается без ошибок.

### Шаг 2. Параметризовать XML-upload тесты
- **Задача:** Заменить 7 идентичных `szv_*_xml()` методов одним параметризованным тестом.
- **Что изменить:**
  - Создать единый тестовый класс `SZV_XmlUploadTest` (например, в `LK_Insurence/`) с `@DataProvider`, отдающим пары `(ReportFormType, ReportXmlResource)` для СЗВ-М, СЗВ-К, СЗВ-КОРР, СЗВ-СТАЖ, СЗВ-ДСО, СЗВ-ИСХ, СЗВ-ТД.
  - Удалить методы `szv_*_xml()` из классов `Szv_m`, `Szv_k`, `Szv_korr`, `Szv_staj`, `Szv_dso`, `Szv_ish`, `Szv_td` (сами классы остаются для не-XML сценариев).
- **Критерий:** В Allure/TestNG остаются отдельные записи на каждый отчёт (testName из данных).

**Статус: ✅ ВЫПОЛНЕНО (2026-08-19).** Поправка к плану: унифицированный класс уже существовал — `LK_Insurence/ReportXmlUploadTest` с `@DataProvider` на все 9 типов отчётов (СЗВ-М/ТД/СТАЖ/К/КОРР/ИСХ/ДСО, ОДВ-1, ЕФС-1). Отдельный `SZV_XmlUploadTest` создавать не потребовалось.
Выполнено:
- Удалены классы, в которых оставался только XML-дубль: `Szv_staj`, `Szv_k`, `Szv_korr`, `Szv_dso`, `Odv1` (покрыты `ReportXmlUploadTest`).
- Удалены дубли xml-тестов: `Szv_m.szv_m_xml()`, `Szv_td.szv_td_xml()`, `Efs1.efs_1_xml()` (default-пользователь). Очищены неиспользуемые импорты, обновлены Javadoc.
- **Сохранены** user-specific xml-тесты (не дубли, разный пользователь/организация): `Szv_ish.szv_ish_xml()` (KRIVONOSOV), `Efs1.efs_1_xml_krivonosov()`, `Efs1.efs_1_xml_babkina()`.
- `mvn test-compile` проходит (EXIT 0).

### Шаг 3. Удалить дубли тестовых методов
- **Задача:** Устранить дубликаты внутри `ArchivesTest` и `SzvReportsSteps`.
- **Что изменить:**
  - `ArchivesTest`: удалить `setRegisterRequests()` (ARCH-004) или сделать его вызовом общего приватного метода `processRequestFlow()`; оставить одну аннотацию-описание.
  - `SzvReportsSteps`: убрать `searchRequest()`, оставив `searchRequest1(String)`; слить перегруженные `authorizeArhivEVS()`, `chooseEFS()`, `submitAndSend()` — одна реализация с параметром по умолчанию.
- **Критерий:** в классе не остаётся двух методов с одинаковой логикой.

**Статус: ✅ ВЫПОЛНЕНО (2026-08-19).**
Выполнено:
- `ArchivesTest`: удалён 100% дубль `setRegisterRequests()` (ARCH-004) — цепочка шагов идентична `processRequestInEVS()` (ARCH-003), которая оставлена.
- `SzvReportsSteps`: удалён мёртвый дубль `searchRequest()` (не вызывался нигде) — оставлен параметризованный `searchRequest1(String)`.
- Поправка к плану: перегрузки `authorizeArhivEVS()`, `chooseEFS()`, `submitAndSend()` оказались не копипастом, а чистыми делегациями (одна реализация, тело не дублируется) — оставлены без изменений.
- `mvn test-compile` проходит (EXIT 0).

### Шаг 4. Унифицировать Page Objects
- **Задача:** Оставить одну реализацию `BasePage` и `LoginPage`, удалить пакет `gui/`.
- **Что изменить:**
  - Определить единственный источник правды: пакет `ui/` (новая реализация).
  - `ui/core/BasePage` (477 строк) vs `ui/pages/BasePage` (62 строки) — перенести общую логику в `ui/core/BasePage`, а `ui/pages/BasePage` сделать тонким наследником (или наоборот).
  - `gui/BasePage`, `gui/LoginPage`, `gui/CryptoProDemoPage` — удалить после переноса недостающих методов в `ui/`.
  - Заменить в `SzvReportsSteps` импорт `com.bft.gui.LoginPage` → `com.bft.ui.pages.LoginPage`.
- **Критерий:** `rg "com.bft.gui"` в `src/test/java` не находит вхождений.

**Статус: ✅ ВЫПОЛНЕНО (2026-08-19).**
Выполнено:
- Пакет `gui/` удалён целиком (`gui/BasePage`, `gui/LoginPage`, `gui/CryptoProDemoPage`).
- `gui/BasePage` оказался сиротой (никто не наследовал); `gui/CryptoProDemoPage` уже наследовал `ui/core/BasePage` — перенесён как `ui/pages/CryptoProDemoPage` (изменён только package, тело без изменений).
- Импорты `com.bft.gui.LoginPage` → `com.bft.ui.pages.LoginPage` в `steps/SzvReportsSteps` и `steps/AuthSteps` (методы `open/authorize/authorizeEPGU/selectUserCardEPGU/logOut` полностью совместимы).
- Импорты `com.bft.gui.CryptoProDemoPage` → `com.bft.ui.pages.CryptoProDemoPage` в `strategy/CryptoProValidationStrategy` и `LK_Insurence/CryptoProCertificateTest`.
- Обновлены закомментированные упоминания `gui` в файлах-примерах (`ExampleTest`, `ImprovedTestExamples`, `AdvancedReportingExample`).
- Критерий достигнут: `rg "com.bft.gui"` в `src/test/java` — 0 вхождений. `mvn test-compile` проходит (EXIT 0).
- **Отложено (не входило в критерий):** слияние `ui/core/BasePage` (Selenide, generic) и `ui/pages/BasePage` (WebDriver, `DataFiller` наследует её) — разные API и потребители; вынесено в Шаг 7 (мёртвый код) при необходимости.

### Шаг 5. Объединить TestConfig
- **Задача:** Один класс конфигурации.
- **Что изменить:**
  - `com.bft.config.TestConfig` оставить как источник окружения/браузера.
  - Константы тестовых данных из `com.bft.helpers.TestConfig` перенести в `testdata/` (например, `TestDataBuilder` или отдельный `TestDataConstants`).
  - Обновить импорты в `SzvReportsSteps` и других классах с `com.bft.helpers.TestConfig` → новый класс.
  - Устранить дублирование `InsuredPerson`: использовать `com.bft.helpers.forms.InsuredPerson` как модель, а константы ФИО/СНИЛС/ИНН — в одном месте.
- **Критерий:** `rg "class TestConfig"` находит ровно одно объявление.

**Статус: ✅ ВЫПОЛНЕНО (2026-08-19).**
Выполнено:
- Создан `src/test/java/com/bft/testdata/TestDataConstants.java` (package `com.bft.testdata`): перенесены все константы из `com.bft.helpers.TestConfig` — данные страхователя, вложенный `InsuredPerson` (константы + фабрики `createPerson1/2`, модель остаётся `com.bft.helpers.forms.InsuredPerson`), вложенный `Selectors`.
- `helpers/DataFiller`: `import static com.bft.helpers.TestConfig.Selectors.*` → `com.bft.testdata.TestDataConstants.Selectors.*`.
- `steps/SzvReportsSteps`: `import com.bft.helpers.TestConfig` → `com.bft.testdata.TestDataConstants`; все `TestConfig.InsuredPerson.*` → `TestDataConstants.InsuredPerson.*` (включая упоминания в Javadoc).
- Удалён `com.bft.helpers.TestConfig` (все константы в одном месте — `TestDataConstants`).
- `com.bft.config.TestConfig` (окружение/браузер) не используется в активном коде, но оставлен как единственный класс `TestConfig` — соответствует критерию.
- Критерий достигнут: `rg "class TestConfig"` — 1 объявление (`config/TestConfig`). `mvn test-compile` проходит (EXIT 0).

### Шаг 6. Разбить `SzvReportsSteps`
- **Задача:** Уменьшить связанность, разделить по доменам.
- **Что изменить:**
  - `AuthSteps` — перенести `authorizeEVS/authorizeArhivEVS/authorizeRPU/authorizeUOS`, `logOut`, `performEpguAuth`, `isUserLoggedIn`.
  - `ArchiveSteps` — перенести `addPerformer`, `fillPerformer`, `reestrZaprosov`, `searchPerformer`, `searchRequest1`, `assignPerformer`, `answerRequest`, `addPeriodWork`, `addBusinessTrip`, `fillAnswer`, `submitSigning`, `createZaprosRPU`, `genegalInfo`, `infoCitizen`, `organizationData`, `totalPeriod`, `typeFormEmployment`, `requestSubjectPeriodWork`, `saveRequest`, `signRequest`, `selectCertificate`, `get_nomber_send_request`.
  - `ReportDataEntrySteps` / `ReportNavigationSteps` / `ReportValidationSteps` — уже существуют, распределить в них методы работы с отчётами.
  - Удалить заглушки `isOnZLPage()`, `hasErrorMessages()`, `isContinueButtonEnabled()`, `getCurrentZLFieldValue()`.
- **Критерий:** `SzvReportsSteps` становится композицией (или удаляется), размер каждого steps-класса < 300 строк.

**Статус: ✅ ВЫПОЛНЕНО (2026-08-19).**
Выполнено:
- `SzvReportsSteps` (1316 строк) превращён в тонкий фасад (272 строки) — композиция из 4 step-классов; потребители (`Szv_td`, `Szv_m`, `Szv_ish`, `ArchivesTest`, `ReportXmlUploadTest`, `Efs1`) не менялись, все методы сохранены.
- **`AuthSteps`** (189 строк): перенесены `authorizeEVS(UIType, TestUsers)` (объединена с существующей — обе использовали одни credentials через префикс; выбрана реализация с `performEpguAuth`/`isUserLoggedIn`), `authorizeEVS(UIType)`, `authorizeArhivEVS()`, `authorizeArhivEVS(UIType)`, `authorizeArhivRPU(UIType)`, `authorizeUOS(UIType)`, `logOut()` (сессия через статический `currentSessionKey`, как было в `SzvReportsSteps`).
- **`ArchiveSteps`** (263 строки): перенесены `addPerformer`, `fillPerformer`, `reestrZaprosov`, `searchPerformer` (заменён вариант `SzvReportsSteps` — «Исполнитель в архивной организации» + `clickMainButton`), `searchRequest1`, `assignPerformer`, `answerRequest`, `addPeriodWork`, `addBusinessTrip`, `fillAnswer`, `submitSigning`, `createZaprosRPU`, `genegalInfo`, `infoCitizen`, `changeInfoCitizen`, `infoSeparateDivision`, `organizationData`, `totalPeriod`, `typeFormEmployment`, `requestSubjectPeriodWork`, `attachmentsRequest`, `saveRequest`, `signRequest`, `selectCertificate`, `get_nomber_send_request` (+ поле `getActualResult`).
- **`ReportNavigationSteps`** (135 строк): перенесены `addReports`, `selectReportType`, `addNewReport`, `createContinue`, `goToZL`, `sidebar`, `chooseEFS`, `chooseEFS(String)`.
- **`ReportDataEntrySteps`** (296 строк): перенесены `addGeneralInfo`, `addGeneralInfoISH`, `fillBasisSectionISH`, `addGeneralInfoTD`, `addGeneralInfoEFS`, `addZL`, `addEvent`, `addEventEFS`, `fillZL`, `fillZLEFS`, `fillZLINN`, `fillZLBirth`, `saveZL`, `saveEvent`, `addSTAJ`, `saveAsDraft`, `submitAndSend()`, `submitAndSend(String)`, `chooseCriptoProvider`, `sendXml`.
- Удалены заглушки `isOnZLPage()`, `hasErrorMessages()`, `isContinueButtonEnabled()`, `getCurrentZLFieldValue()` (не вызывались нигде).
- Критерий достигнут: все steps-классы < 300 строк; `mvn test-compile` проходит (EXIT 0).

### Шаг 7. Вычистить мёртвый код
- **Задача:** Удалить неиспользуемые методы и комментарии-заглушки.
- **Что изменить:** методы `changeInfoCitizen`, `infoSeparateDivision`, `attachmentsRequest`, `chooseCriptoProvider` (если не вызывается), закомментированные блоки в `requestSubjectPeriodWork`, `addGeneralInfoTD` и т.п.
- **Критерий:** IDEA / `mvn` без warning'ов о неиспользуемых методах.

**Статус: ✅ ВЫПОЛНЕНО (2026-08-19).**
Выполнено:
- Удалены неиспользуемые методы (не вызывались ни одним тестом):
  - `ArchiveSteps`: `changeInfoCitizen`, `infoSeparateDivision`, `attachmentsRequest` (+ ставшие лишними импорты `ReportXmlResource`/`SecureLogger`).
  - `AuthSteps`: `authorizeUOS`.
  - `ReportNavigationSteps`: `chooseEFS`, `chooseEFS(String)`, заглушка `fillGeneralInfo` (+ импорт `$`).
  - `ReportDataEntrySteps`: `saveAsDraft`, `inputDatePeriodStaj` + JS-хелперы `setDateViaJsReact`/`inputDateByLabelContains`, placeholder-комментарий (импорты `Selenide`/`SelenideElement`/`Duration`).
  - `SzvReportsSteps` (фасад): удалены соответствующие делегации.
- **Сохранены** (вызываются/намеренно сохранены): `chooseCriptoProvider` (вызывается `submitAndSend(String)`), `submitAndSend()`/`submitAndSend(String)` (сохранены в шаге 3 как целостный поток подписания).
- Убраны закомментированные блоки кода: `organizationData`, `addBusinessTrip`, `addGeneralInfoTD`, `requestSubjectPeriodWork` (очищен ещё в шаге 6).
- Критерий достигнут: все steps-классы < 300 строк, `mvn test-compile` проходит (EXIT 0), удалённые имена методов не упоминаются в `src/`.

---

## 4. Файлы, которые нужно изменить

| Файл / пакет | Действие |
|---|---|
| `src/test/java/com/bft/test/base/UITestBase.java` | **Создать** (шаг 1) |
| `src/test/java/com/bft/test/base/DataDrivenTestBase.java` | Проверить после создания UITestBase (шаг 1) |
| `src/test/java/com/bft/LK_Insurence/SZV_*/Szv_*.java` (M, K, KORR, STAJ, DSO, ISH, TD) | Удалить дубли `szv_*_xml()` (шаг 2) |
| `src/test/java/com/bft/LK_Insurence/SZV_XmlUploadTest.java` | **Создать** параметризованный тест (шаг 2) |
| `src/test/java/com/bft/LK_Archive/ArchivesTest.java` | Удалить дубль `setRegisterRequests` (шаг 3) |
| `src/test/java/com/bft/steps/SzvReportsSteps.java` | Тонкий фасад (244 стр.), делегирует в AuthSteps/ArchiveSteps/Report*Steps; заглушки и мёртвые делегации удалены (шаги 6–7 ✅) |
| `src/test/java/com/bft/steps/AuthSteps.java` | Консолидирована вся авторизация; мёртвый код удалён (шаги 6–7 ✅) |
| `src/test/java/com/bft/steps/ArchiveSteps.java` | Принял методы архивных запросов; мёртвый код удалён (шаги 6–7 ✅) |
| `src/test/java/com/bft/steps/ReportNavigationSteps.java` | Принял навигацию по отчётам; мёртвый код удалён (шаги 6–7 ✅) |
| `src/test/java/com/bft/steps/ReportDataEntrySteps.java` | Принял заполнение форм отчётов; мёртвый код удалён (шаги 6–7 ✅) |
| `src/test/java/com/bft/gui/` (BasePage, LoginPage, CryptoProDemoPage) | **Удалён целиком** (шаг 4 ✅) |
| `src/test/java/com/bft/ui/pages/CryptoProDemoPage.java` | **Создан** — перенесён из `gui/` (шаг 4 ✅) |
| `src/test/java/com/bft/ui/pages/BasePage.java` | Оставить как наследник `ui/core/BasePage` или объединить (шаг 4, отложено — см. статус) |
| `src/test/java/com/bft/config/TestConfig.java` | Оставлен как единственный TestConfig (шаг 5 ✅) |
| `src/test/java/com/bft/helpers/TestConfig.java` | **Удалён**, константы перенесены в `testdata/TestDataConstants` (шаг 5 ✅) |
| `src/test/java/com/bft/testdata/TestDataConstants.java` | **Создан** — константы тестовых данных и селекторов (шаг 5 ✅) |
| `src/test/resources/docs/*.md`, `README.md`, `ARCHITECTURE.md` | Обновлено описание структуры после рефакторинга (ui/README.md, roadmap.md, ARCHITECTURE_ANALYSIS.md — шаг 7 ✅) |

---

## 5. Риски и рекомендации

- **Шаг 1 — критический блокер:** без `UITestBase` ничего не собирается и не запускается; выполнять в первую очередь.
- **Совместимость:** `ui/`-классы компонентов (`ui/component/*`, `ui/core/element/*`) уже содержат большую часть логики — используйте их, а не старый `gui/`.
- **Поэтапность:** выполнять шаги по одному, после каждого — `mvn test-compile` и прогон smoke-группы (см. `validate-tests.sh`).
- **Не менять бизнес-логику тестов:** рефакторинг только структурный; ожидания и шаги остаются прежними.
