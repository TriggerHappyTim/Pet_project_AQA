# AI_TESTING_CONTEXT — контекст для ИИ-ассистента

Файл для ИИ-ассистента (OpenCode, Cursor, Claude Code, Copilot и т.п.), который помогает
писать автотесты в этом репозитории. Человеческие правила и Definition of Done —
в [CONTRIBUTING.md](CONTRIBUTING.md); этот файл не дублирует их, а даёт ИИ стек,
структуру, ограничения и эталонные примеры кода.

> **Как пользоваться:** приложите этот файл к запросу (или укажите `@AI_TESTING_CONTEXT.md`)
> и опишите сценарий. Если правило ниже противоречит `CONTRIBUTING.md` или реальному коду —
> прав `CONTRIBUTING.md` и код.

## 1. Стек тестирования

| Слой | Технология |
|---|---|
| Язык / сборка | Java 11, Maven (`com.example:EVS-autotest`), без `mvnw` |
| Тест-фреймворк | JUnit 5 (Jupiter 5.10.2): `@Test`, `@ParameterizedTest`, `@MethodSource`, `@Tag` |
| UI | Playwright 1.49.0 через собственную обёртку `com.bft.pw` (`PwSession`, `PwElement`, `PwWait`, `Condition`, `Selenide`) — **не Selenide как зависимость** |
| Отчёты | Allure 2.22.2 (`allure-junit5`, `allure-maven`) |
| HTTP | REST Assured 5.3.1 |
| Данные | JavaFaker 1.0.2, билдеры `com.bft.testdata.*` |
| Прочее | Lombok 1.18.28, Jackson/Gson, BouncyCastle + Apache Santuario (xmldsig ГОСТ), Phoenix, Kafka, gRPC |
| Аннотации окружения | `@DisabledByIssue`, `@DisabledByInfrastructure`, `@DisabledOnEnvironment` |

**Ассерты.** Два уровня:

1. `assertions` из `BaseTest` (`com.bft.test.TestAssertions`) — **мягкие** проверки.
   Падения копятся, `assertAll()` вызывается автоматически после каждого теста.
   Для UI используйте `assertions.assertVisible(element, "описание")` и т.п.
2. JUnit `Assertions.*` — для fail-fast проверок в чистой логике (например, XML/XSD),
   где продолжать выполнение бессмысленно.

**Моки.** В проекте нет практики мокать HTTP/UI: тесты интеграционные, ходят на реальные
стенды. Для учётных данных есть fallback `MockCredentialProvider`, но он не заменяет
`credentials.properties`. Mockito транзитивно доступен через Spring Boot Test, но в тестах
не используется — **не добавляйте моки без явной задачи**.

## 2. Структура тестов

```
src/test/java/com/bft/
├── LK_Insurence/        # тесты ЛК Страхователя (SZV_M, EFS_1, ODV_1, ... и кросс-типовые)
├── LK_Archive/          # тесты ЛК Архивной организации
├── steps/               # бизнес-шаги: AuthSteps, SzvReportsSteps, ReportSignSteps, ...
├── ui/pages/            # Page Objects
├── ui/component/        # UI-компоненты
├── ui/core/             # SmartElement и ожидания
├── pw/                  # обёртка Playwright (движок UI)
├── test/base/           # BaseTest (без UI), UITestBase (UI)
├── test/template/       # NewFeatureTestTemplate.java
├── testdata/            # билдеры тестовых данных
├── service/             # GraphQL mesh, СУБД, Kafka, gRPC, подписание
├── security/            # TestUsers, CredentialManager
└── integration/tms/     # Jira TMS
```

Фикстуры:

- XML отчётов — `src/test/resources/application/<тип>/...`;
- XSD-схемы — `src/test/resources/scheme/evs/*.xsd`;
- выбирайте фикстуру через enum `ReportXmlResource`, а не строкой пути;
- тип отчёта — через enum `ReportFormType`.

**Имена:**

- Новый тест-класс — `<Feature>Test.java` в пакете своего ЛК/типа отчёта.
  Legacy-классы (`Efs1`, `SzvM`) не переименовывать без отдельной задачи.
- Метод — осмысленный camelCase (`uploadReportViaXml`). Snake_case (`efs_szv_td`) —
  legacy, новый код так не пишем.
- На русском — `@DisplayName`, `@Description`, сообщения ассертов, комментарии.

## 3. Конвенции (кратко)

Полный список из 10 пунктов и Definition of Done — в
[CONTRIBUTING.md](CONTRIBUTING.md#как-писать-тесты). Для ИИ обязательны:

1. **Слои:** тест → `steps/` → `ui/pages|pw`. Бизнес-логика не в тесте и не в Page Object.
2. **AAA:** UI-тесты пишутся через `arrangeActAssert(arrange, act, softAssert -> ..., "имя")`.
   Arrange — авторизация/подготовка, Act — действие, Assert — проверки.
3. **Ожидания — только умные:** `SmartWaits`, `PwWait`, `Condition`, polling.
   `Thread.sleep` / `sleep()` запрещены (гейт `scripts/check-hard-waits.sh`).
4. **Локаторы:** `data-testid` → role/text/label → CSS → XPath (крайний случай).
   Новые XPath запрещены ratchet-гейтом (`scripts/check-xpath.sh`).
5. **Независимость:** тест сам готовит и чистит данные; уникальность через timestamp/uuid;
   никаких статических мутабельных полей.
6. **Учётные данные:** только `TestUsers` / `CredentialManager`; пароли в код не коммитим.
7. **Теги обязательны:** ЛК (`lk-insurer`, `lk-archive`) + тип (`web`, `smoke`, `regression`,
   `xml-upload`, `report-sign`, `negative`, `manual`, `example`) + `flaky` только с тикетом.
8. **Мягкие проверки:** `assertions.*` из `BaseTest`; вручную `assertAll()` не вызывайте.
9. **Flaky:** не ретраить молча — `flaky` + issue + `@DisabledByIssue("TICKET-123")`
   или `@DisabledByInfrastructure`; `@Retry` — только из `scripts/retry-allowlist.txt`.
10. **Не дублировать** существующие сценарии; новый тест — по `NEW_TESTS_GUIDE.md`
    и шаблону `test/template/NewFeatureTestTemplate.java`.

**Покрытие.** Процент покрытия не измеряется и не является критерием. Критерий —
Definition of Done: независимый зелёный тест, теги, умные ожидания, отсутствие hard waits,
понятные ассерты и артефакты при падении.

**Allure.** На классе/методе: `@Epic`, `@Feature`, `@Story`, `@AllureId` (уникальный),
`@Description`, `@Severity`. Нетривиальные решения фиксируйте в журнале:
`AgentDecisionLog.decision("что", "почему")`, `step(...)`, `note(...)`.

## 4. Golden Example 1 — параметризованный UI E2E

`src/test/java/com/bft/LK_Insurence/ReportXmlUploadTest.java`

Почему эталон: компактный, один сценарий на все типы отчётов, данные — enum-ы,
AAA через `arrangeActAssert`, мягкая проверка, полный набор тегов и Allure-аннотаций,
никакой бизнес-логики в тесте.

```java
package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.steps.ReportValidationSteps;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Параметризованный smoke-тест загрузки отчётов через XML по всем типам отчётов.
 *
 * <p>Объединяет сценарий «авторизация → Отчеты → выбор типа отчёта → новый отчёт → загрузка XML»
 * для СЗВ-М, СЗВ-ТД, СЗВ-СТАЖ, СЗВ-К, СЗВ-КОРР, СЗВ-ИСХ, СЗВ-ДСО, ОДВ-1, ЕФС-1 в один тест с DataProvider.
 *
 * <p>Специфичные сценарии (ручное создание ЕФС-1 ТД/СТАЖ, пользователи Кривоносов/Бездомный)
 * остаются в соответствующих классах (например {@link com.bft.LK_Insurence.EFS_1.Efs1}).
 *
 * @see SzvReportsSteps
 * @see ReportFormType
 * @see ReportXmlResource
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("xml-upload")
@Tag("web")
@Tag("regression")
@Epic("Формы отчетности")
@Feature("Report XML Upload (unified)")
public class ReportXmlUploadTest extends UITestBase {

    private final ReportValidationSteps validationSteps = new ReportValidationSteps();

    /**
     * Data provider: report type for UI selection and report type with XML file path.
     *
     * @return stream of [reportFormType, reportXmlResource] for each report type with xml-upload scenario
     */
    static java.util.stream.Stream<Arguments> reportXmlUploadData() {
        return java.util.stream.Stream.of(
                Arguments.of(ReportFormType.SZVM, ReportXmlResource.SZV_M),
                Arguments.of(ReportFormType.SZVTD, ReportXmlResource.SZV_TD_TYPE1),
                Arguments.of(ReportFormType.SZVSTAZH, ReportXmlResource.SZV_STAZH_AF2),
                Arguments.of(ReportFormType.SZVK, ReportXmlResource.SZV_K_AF2),
                Arguments.of(ReportFormType.SZVKORR, ReportXmlResource.SZV_KORR_AF2),
                Arguments.of(ReportFormType.SZVISH, ReportXmlResource.SZV_ISH_AF2),
                Arguments.of(ReportFormType.SZVDSO, ReportXmlResource.SZV_DSO_AF2),
                Arguments.of(ReportFormType.ODV1, ReportXmlResource.SZV_ODV_1_AF2),
                Arguments.of(ReportFormType.EFS1, ReportXmlResource.EFS_FULL_ECP)
        );
    }

    /**
     * Upload report via XML for given report type.
     *
     * @param reportFormType report type for UI selection (e.g., SZVM, EFS1)
     * @param reportXmlResource resource with XML file path for upload
     */
    @ParameterizedTest(name = "XML Upload: {0}")
    @MethodSource("reportXmlUploadData")
    @AllureId("REPORT-XML-001")
    @Story("XML Upload (unified)")
    @Description("Параметризованный тест: авторизация → Отчеты → выбор типа отчёта → новый отчёт → загрузка XML")
    @Severity(SeverityLevel.CRITICAL)
    public void uploadReportViaXml(ReportFormType reportFormType, ReportXmlResource reportXmlResource) {
        arrangeActAssert(
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.authorizeEVS(UITypeSelector.getSelectedUIType());
            },
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.addReports();
                steps.selectReportType(reportFormType);
                steps.addNewReport();
                steps.sendXml(reportXmlResource);
            },
            softAssert -> {
                validationSteps.verifyUrlContains("/reports");
            },
            "XML Upload: " + reportFormType.getDisplayName()
        );
    }
}
```

## 5. Golden Example 2 — параметризованная проверка данных без UI

`src/test/java/com/bft/LK_Insurence/ReportFixtureIntegrityTest.java`

Почему эталон: чистый non-UI тест, данные вынесены в `ReportSpec`, параметризация через
`@MethodSource`, fail-fast ассерты JUnit с понятными сообщениями, проверяется и структура XML,
и XSD. Стенд не нужен — такой тест можно запускать первым.

```java
package com.bft.LK_Insurence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Tag;

/**
 * Проверка целостности XML-фикстур отчётов ЕВС (слой данных, без UI).
 *
 * <p>Для каждого из 9 типов отчётов сверяются:
 * <ul>
 *   <li>наличие фикстуры на classpath и её непустота;</li>
 *   <li>корректность XML (well-formed);</li>
 *   <li>ожидаемый корневой элемент и пространство имён (по реальным отчётам ПФР);</li>
 *   <li>наличие обязательных элементов — обязательные поля отчёта.</li>
 * </ul>
 *
 * <p>Список обязательных элементов получен из реальных отчётных XML — это ground truth того,
 * какой минимальный набор полей обязан содержать отчёт каждого типа. Для 8 из 9 типов
 * (кроме СЗВ-К) дополнительно выполняется полная XSD-валидация фикстуры по схемам ПФР из
 * {@code src/test/resources/scheme/evs/*.xsd} (замкнутый набор, собранный из AF.2.106d).
 */
@DisplayName("Целостность XML-фикстур отчётов ЕВС")
@Tag("lk-insurer")
@Tag("regression")
class ReportFixtureIntegrityTest {

    private static final String RESOURCE_PREFIX = "application";

    static final class ReportSpec {
        final String displayName;
        final String path;
        final String rootElement;
        final String namespace;
        final String schemaFile;
        final List<String> mandatoryElements;

        ReportSpec(String displayName, String path, String rootElement, String namespace,
                   String schemaFile, List<String> mandatoryElements) {
            this.displayName = displayName;
            this.path = path;
            this.rootElement = rootElement;
            this.namespace = namespace;
            this.schemaFile = schemaFile;
            this.mandatoryElements = mandatoryElements;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    static Stream<ReportSpec> reportSpecs() {
        return Stream.of(
                new ReportSpec("СЗВ-М", "szv-m/PFR_154_SZV-M.xml", "ЭДПФР",
                        "http://пф.рф/ВС/СЗВ-М/2017-01-01", "СЗВ-М_2017-01-01.xsd",
                        List.of("СЗВ-М", "ТипФормы", "Страхователь", "РегНомер", "ИНН", "КПП",
                                "НаименованиеКраткое", "ОтчетныйПериод", "Месяц", "КалендарныйГод",
                                "СписокЗЛ", "ЗЛ", "ФИО", "Фамилия", "Имя", "Отчество", "СНИЛС", "ДатаЗаполнения")),
                new ReportSpec("СЗВ-ТД", "szv-td/PFR_154_SZV-TD.xml", "ЭДПФР",
                        "http://пф.рф/СЗВ-ТД/2020-09-26", "СЗВ-ТД_2020-09-26.xsd",
                        List.of("СЗВ-ТД", "Работодатель", "РегНомер", "ИНН", "КПП",
                                "НаименованиеОрганизации", "ТрудоваяДеятельность", "ЗЛ", "ФИО", "СНИЛС",
                                "ДатаРождения", "Мероприятие", "UUID", "Дата", "Вид", "Должность",
                                "КодВФ", "Основание", "Номер")),
                new ReportSpec("СЗВ-СТАЖ", "Szv_Stazh/PFR_154_SZV-STAJ.xml", "ЭДПФР",
                        "http://пф.рф/СЗВ-СТАЖ/2020-08-10", "СЗВ-СТАЖ_2020-08-10.xsd",
                        List.of("СЗВ-СТАЖ", "ОДВ-1", "Страхователь", "РегНомер", "ИНН", "КПП",
                                "Наименование", "ОтчетныйПериод", "Код", "Год", "КоличествоЗЛ",
                                "ЗЛ", "ФИО", "Фамилия", "Имя", "Отчество", "СНИЛС",
                                "СтажевыйПериод", "Период", "С", "По", "Тип")),
                new ReportSpec("СЗВ-ИСХ", "Szv_Ish/PFR_154_SZV-ISH.xml", "ЭДПФР",
                        "http://пф.рф/ВС/СЗВ-ИСХ/2018-11-20", "СЗВ-ИСХ_2018-11-20.xsd",
                        List.of("СЗВ-ИСХ", "ОДВ-1", "Страхователь", "РегНомер", "ИНН", "КПП",
                                "Наименование", "ОтчетныйПериод", "Код", "Год", "КоличествоЗЛ",
                                "ФИО", "Фамилия", "Имя", "Отчество", "СНИЛС",
                                "СтажевыйПериод", "Период", "С", "По", "Тип")),
                new ReportSpec("ОДВ-1", "odv_1/PFR_154_ODV-1.xml", "ЭДПФР",
                        "http://пф.рф/ВС/ОДВ-1/2017-12-25", "ОДВ-1_2017-12-25.xsd",
                        List.of("ОДВ-1", "Страхователь", "ОтчетныйПериод", "Год", "Код", "Документы",
                                "ПозицияСписка", "ПрофессияДолжность", "Подразделение", "ВсегоШтат",
                                "ВсегоФакт", "КоличествоШтат", "КоличествоФакт", "Основание",
                                "ОснованияДНП", "Руководитель", "ФИО")),
                new ReportSpec("СЗВ-КОРР", "Szv_Corr/PFR_154_SZV-KORR.xml", "ЭДПФР",
                        "http://пф.рф/СЗВ-КОРР/2020-08-10", "СЗВ-КОРР_2020-08-10.xsd",
                        List.of("СЗВ-КОРР", "Тип", "Страхователь", "РегНомер", "ИНН", "КПП",
                                "ОтчетныйПериод", "Код", "Год", "Наименование", "ДанныеЗЛ", "ЗЛ",
                                "ФИО", "Фамилия", "Имя", "Отчество", "СНИЛС", "Выплаты", "Суммы")),
                new ReportSpec("СЗВ-К", "szv_k/SFR_154_SZV-K.xml", "ЭДСФР",
                        "http://пф.рф/СЗВ-К/2026-01-01", null,
                        List.of("СЗВ-К", "ЛьготныйСтаж", "ВЛ", "ИС", "ТУ", "КодИсчисления",
                                "ВыработкаКалендарная", "ВсеДни", "ВсеМесяцы", "ВсеГоды", "Коэффициент",
                                "ОУТ", "ПозицияСписка", "ПрофессияДолжность", "ФИО", "Фамилия",
                                "Имя", "Отчество", "ДопСведенияИС", "Основание", "Руководитель",
                                "Должность", "Часы", "Минуты")),
                new ReportSpec("ЕФС-1", "EFS/PFR_154_EFS-1.xml", "ЭДПФР",
                        "http://пф.рф/ЕФС-1/2024-01-01", "ЕФС-1_2024-01-01.xsd",
                        List.of("ЕФС-1", "Страхователь", "РегНомер", "ИНН", "КПП", "ОГРН", "КодПоОКВЭД",
                                "ЗЛ", "СНИЛС", "ФИО", "Мероприятие", "Вид", "Дата", "КодВФпоОКЗ",
                                "UUID", "Должность")),
                new ReportSpec("СЗВ-ДСО", "Szv_Dso/PFR_154_SZV-DSO.xml", "ЭДПФР",
                        "http://пф.рф/СЗВ-ДСО/2022-07-08", "СЗВ-ДСО_2022-07-08.xsd",
                        List.of("СЗВ-ДСО", "Работодатель", "РегНомер", "ИНН", "КПП",
                                "НаименованиеОрганизации", "ТипСведений", "ПериодыДСО", "ДСОЛ",
                                "С", "По", "Налет", "СведенияОЗаработке", "ЗаМесяц", "Месяц",
                                "Сумма", "Итого", "ФИО", "Руководитель", "Должность"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("reportSpecs")
    @DisplayName("Фикстура корректна и содержит обязательные элементы")
    void fixtureIsWellFormedAndContainsMandatoryElements(ReportSpec spec) throws Exception {
        Document doc = parseFixture(spec);

        assertEquals(spec.rootElement, doc.getDocumentElement().getLocalName(),
                spec.displayName + ": корневой элемент");
        assertEquals(spec.namespace, doc.getDocumentElement().getNamespaceURI(),
                spec.displayName + ": пространство имён корневого элемента");

        for (String element : spec.mandatoryElements) {
            assertTrue(doc.getElementsByTagNameNS("*", element).getLength() > 0,
                    spec.displayName + ": обязательный элемент <" + element + "> отсутствует");
        }

        validateAgainstXsd(spec, doc);
    }

    private static void validateAgainstXsd(ReportSpec spec, Document doc) {
        if (spec.schemaFile == null) {
            return;
        }
        assertNotNull(ReportFixtureIntegrityTest.class.getClassLoader()
                .getResource("scheme/evs/" + spec.schemaFile),
                spec.displayName + ": XSD-схема не найдена на classpath: " + spec.schemaFile);
        try {
            Schema schema = buildSchema(spec.schemaFile);
            schema.newValidator().validate(new DOMSource(doc));
        } catch (Exception e) {
            throw new AssertionError(spec.displayName + ": XML-фикстура не проходит XSD-валидацию: "
                    + e.getMessage(), e);
        }
    }

    /** Компилирует корневую схему {@code schemaFile}, рекурсивно резолвя импорты через resolver. */
    private static Schema buildSchema(String schemaFile) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        List<javax.xml.transform.stream.StreamSource> ordered = new java.util.ArrayList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        visitSchema(schemaFile, ordered, visited);
        return schemaFactory.newSchema(ordered.toArray(new javax.xml.transform.stream.StreamSource[0]));
    }

    /** Post-order обход графа схем: зависимости до того, кто на них ссылается. */
    private static void visitSchema(String schemaFile, List<javax.xml.transform.stream.StreamSource> ordered,
                                     java.util.Set<String> visited) throws Exception {
        if (!visited.add(schemaFile)) {
            return;
        }
        java.nio.file.Path path = java.nio.file.Paths.get(
                ReportFixtureIntegrityTest.class.getResource("/scheme/evs/" + schemaFile).toURI());
        String content = new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("schemaLocation\\s*=\\s*\"([^\"]+\\.[Xx][Ss][Dd])\"")
                .matcher(content);
        while (m.find()) {
            String loc = m.group(1);
            String s = loc.lastIndexOf('/') >= 0 ? loc.substring(loc.lastIndexOf('/') + 1) : loc;
            if (!s.equals(schemaFile)) {
                visitSchema(s, ordered, visited);
            }
        }
        ordered.add(new javax.xml.transform.stream.StreamSource(path.toFile()));
    }

    private static Document parseFixture(ReportSpec spec) {
        URL resource = ReportFixtureIntegrityTest.class.getClassLoader()
                .getResource(RESOURCE_PREFIX + "/" + spec.path);
        assertNotNull(resource, spec.displayName + ": фикстура не найдена на classpath: " + spec.path);

        File file;
        try {
            file = new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(spec.displayName + ": некорректный URI фикстуры: " + resource, e);
        }

        assertTrue(file.canRead(), spec.displayName + ": фикстура не читается: " + file);
        try {
            assertTrue(Files.size(file.toPath()) > 0, spec.displayName + ": фикстура пуста: " + file);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception ignored) {
                // не все реализации поддерживают флаг
            }
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file);
        } catch (Exception e) {
            throw new AssertionError(spec.displayName + ": фикстура не является well-formed XML (" + file + "): "
                    + e.getMessage(), e);
        }
    }
}
```

## 6. Чек-лист перед выдачей кода ИИ

- [ ] Тест лежит в правильном пакете и называется `*Test`.
- [ ] Наследует `UITestBase` (UI) или `BaseTest`/обычный JUnit-класс (без UI).
- [ ] UI-сценарий собран через `arrangeActAssert`; логика — в `steps/`, не в тесте.
- [ ] Нет `Thread.sleep`, `waitForTimeout`, ручных пауз.
- [ ] Локаторы: `data-testid` / role / text / CSS; XPath не добавлен.
- [ ] Ассерты осмысленные: проверяется результат, а не факт отсутствия исключения.
- [ ] Данные уникальны и не зависят от порядка тестов; секретов в коде нет.
- [ ] Проставлены теги ЛК и типа; `@AllureId` уникален; есть `@DisplayName`/`@Description`.
- [ ] Для неочевидных решений добавлен `AgentDecisionLog`.
- [ ] `mvn -B -DskipTests test-compile` и `bash scripts/verify-quality.sh` проходят.

## 7. Антипаттерны

- `Thread.sleep`, `page.waitForTimeout`, «подождём 5 секунд» — запрещено.
- Новая копия существующего сценария «на всякий случай» — запрещено.
- Бизнес-шаги и селекторы прямо в тесте — только через `steps/` и Page Objects.
- `System.out.println` вместо логгера/Allure; `e.printStackTrace()`.
- Пароли, cookie, токены в коде, тест-данных или документации.
- `@Disabled` без `@DisabledByIssue` / `@DisabledByInfrastructure`.
- Ретраи «чтобы было зелёно» вместо разбора флейка.
- Моки внешних сервисов без явной задачи: тесты интеграционные.

## 8. Команды для самопроверки

```bash
mvn -B -DskipTests test-compile
mvn -B test -Dtest=ReportFixtureIntegrityTest
mvn -B test -Dtest="<НовыйТест>"
bash scripts/verify-quality.sh
mvn allure:serve
```

Если тест требует стенд — сначала проверьте контур: по умолчанию `-P uat`.
Для TEST: `-P test` (нестабилен для UI, см. [CONTRIBUTING.md](CONTRIBUTING.md#окружения-контуры)).
