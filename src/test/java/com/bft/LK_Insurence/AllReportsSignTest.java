package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.steps.ReportSignSteps;
import com.bft.steps.ReportValidationSteps;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.bft.pw.Selenide.$x;
import static com.bft.pw.Selenide.refresh;

/**
 * Сквозной E2E-прогон подписания ВСЕХ типов отчётов ЕВС (ЛК Страхователя) единым потоком:
 * загрузка XML → API-подписание (xmldsig, GraphQL mesh :20266) → проверка статуса по ИД процесса.
 *
 * <p>Подписание универсально (CommonReportSignatureService определяет тип отчёта по itemId),
 * различаются только тип отчёта в UI и XML-фикстура. Код типа отчёта для агрегата
 * (ReportSignSteps.reportTypeCode) задаётся свойством {@code evs.sign.report-type-code}
 * (см. {@code ReportTypeDto}: 1=СЗВ-М, 2=СЗВ-ТД, 3=СЗВ-СТАЖ, 4=СЗВ-ИСХ, 5=ОДВ-1,
 * 6=СЗВ-КОРР, 7=СЗВ-К, 8=ЕФС-1, 9=СЗВ-DSO, 10=4-ФСС).
 *
 * <p>Запуск на стенде:
 * <pre>
 * mvn test -Dtest=AllReportsSignTest \
 *   -Dservice.graphql-mesh.url=http://localhost:20266 \
 *   -Devs.sign.mode=api -Devs.sign.transport=graphql -Devs.sign.xmldsig=true \
 *   -Devs.sign.variant=common -Devs.sign.pem-dir=src/test/resources/certs -Dheadless=true
 * </pre>
 */
@Tag("lk-insurer")
@Tag("signing")
@Tag("all-reports")
@Tag("web")
@Epic("Формы отчетности")
@Feature("Подписание всех типов отчётов ЕВС")
public class AllReportsSignTest extends UITestBase {

    private final ReportValidationSteps validationSteps = new ReportValidationSteps();
    private final ReportSignSteps signSteps = new ReportSignSteps();

    static Stream<Arguments> reportCases() {
        return Stream.of(
                Arguments.of("СЗВ-М",     ReportFormType.SZVM,    ReportXmlResource.SZV_M,          "1"),
                Arguments.of("СЗВ-ТД",    ReportFormType.SZVTD,   ReportXmlResource.SZV_TD_TYPE1,   "2"),
                Arguments.of("СЗВ-СТАЖ",  ReportFormType.SZVSTAZH, ReportXmlResource.SZV_STAZH_AF2, "3"),
                Arguments.of("СЗВ-ИСХ",   ReportFormType.SZVISH,  ReportXmlResource.SZV_ISH_AF2,   "4"),
                Arguments.of("ОДВ-1",     ReportFormType.ODV1,    ReportXmlResource.SZV_ODV_1_AF2, "5"),
                Arguments.of("СЗВ-КОРР",  ReportFormType.SZVKORR, ReportXmlResource.SZV_KORR_AF2,  "6"),
                Arguments.of("СЗВ-К",     ReportFormType.SZVK,    ReportXmlResource.SZV_K_AF2,     "7"),
                Arguments.of("ЕФС-1",     ReportFormType.EFS1,    ReportXmlResource.EFS_FULL_ECP,  "8"),
                Arguments.of("СЗВ-DSO",   ReportFormType.SZVDSO,  ReportXmlResource.SZV_DSO_AF2,   "9")
                // 4-ФСС (10) — нет XML-фикстуры в репозитории; см. disabled-тест ниже (TODO).
        );
    }

    @ParameterizedTest(name = "Подписание отчёта: {0}")
    @MethodSource("reportCases")
    @Story("XML Upload + Подписание (все типы)")
    @Description("Сквозной цикл для типа отчёта: загрузка XML, подписание (API/xmldsig), проверка статуса по ИД процесса")
    @Severity(SeverityLevel.BLOCKER)
    public void signEveryReportType(String displayName, ReportFormType formType,
                                   ReportXmlResource xml, String reportTypeCode) {
        SzvReportsSteps steps = new SzvReportsSteps();

        // ---------- Arrange ----------
        steps.authorizeEVS(UITypeSelector.getSelectedUIType());

        // ---------- Act 1. Загрузка XML ----------
        steps.addReports();
        steps.selectReportType(formType);
        steps.addNewReport();                 // диалог «Создать новый»
        steps.sendXml(xml);

        // Код типа отчёта для агрегата (ReportSignSteps.reportTypeCode)
        System.setProperty("evs.sign.report-type-code", reportTypeCode);

        // ---------- Assert 1. Нет бизнес-ошибок после загрузки ----------
        // После загрузки XML открывается карточка отчёта /reports/{type}/{id}/... — не таблица списка.
        validationSteps.verifyUrlContains("/reports");
        assertions.assertFalse(
                $x("//*[contains(text(), 'Ошибка проверки по XSD')]").exists(),
                "Не должно быть ошибки проверки по XSD схеме для " + displayName);

        // ---------- Act 2. Подписание (API + xmldsig, GraphQL mesh) ----------
        signSteps.signAndSendReport();

        // Статус и кнопки актуализируются только после refresh
        refresh();

        // ---------- Assert 2. Отчёт подписан, кнопка неактивна ----------
        String processId = new com.bft.ui.pages.MainPage().getProcessIdValue();
        assertions.assertNotNull(processId,
                "Отчёту " + displayName + " должен быть присвоен Идентификатор процесса");
        if (processId != null) {
            logger.info("ИД процесса подписанного отчёта {}: {}", displayName, processId);
            Allure.addAttachment("ИД процесса", "text/plain", displayName + " : " + processId, ".txt");
            assertions.assertTrue(processId.matches(
                            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"),
                    "Идентификатор должен быть в формате UUID");
        }

        assertions.assertTrue(
                new com.bft.ui.pages.MainPage().isSignAndSendDisabled(),
                "После подписания кнопка «Подписать и отправить» должна стать неактивной");

        // ---------- Act 3. Поиск в таблице по UUID ----------
        new com.bft.ui.pages.MainPage().searchReportsByProcessId(processId);

        // ---------- Assert 3. Статус в таблице ----------
        String status = new com.bft.ui.pages.MainPage().getReportStatusByProcessId(processId);
        assertions.assertFalse(status.isEmpty(),
                "По ИД процесса " + displayName + " должна найтись ровно одна строка со статусом");
        assertions.assertTrue(status.matches("(?i).*(отправлен|подписан|принят).*"),
                "Статус отчёта " + displayName + " должен подтверждать отправку/подписание. Фактический: '"
                        + status + "'");
    }

    /**
     * TODO: 4-ФСС (reportTypeCode=10) — в репозитории пока нет XML-фикстуры.
     * Когда появится файл, добавить соответствующий {@link ReportXmlResource} и включить
     * в {@link #reportCases()}. Здесь оставлен отключённый шаблон для быстрого подключения.
     */
    @Tag("disabled")
    @org.junit.jupiter.api.Disabled("Нет XML-фикстуры 4-ФСС (reportTypeCode=10)")
    @Test
    @Story("XML Upload + Подписание")
    @Description("Заглушка для 4-ФСС: требуется XML-фикстура")
    public void signF4Fss_pendingFixture() {
        // steps.authorizeEVS(...); steps.addReports();
        // steps.selectReportType(ReportFormType.F4FSS); steps.addNewReport();
        // steps.sendXml(ReportXmlResource.<F4FSS>); // добавить ресурс
        // System.setProperty("evs.sign.report-type-code", "10");
        // signSteps.signAndSendReport(); ...
        org.junit.jupiter.api.Assumptions.assumeTrue(false, "4-ФСС: ожидается XML-фикстура");
    }
}
