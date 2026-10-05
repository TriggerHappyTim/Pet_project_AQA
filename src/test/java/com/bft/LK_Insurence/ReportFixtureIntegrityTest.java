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