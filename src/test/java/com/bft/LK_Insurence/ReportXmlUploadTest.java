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
@Tag("smoke")
@Tag("xml-upload")
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
