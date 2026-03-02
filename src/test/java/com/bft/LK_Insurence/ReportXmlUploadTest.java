package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
@Epic("Формы отчетности")
@Feature("Report XML Upload (unified)")
public class ReportXmlUploadTest extends UITestBase {

    /**
     * Провайдер данных: тип отчёта для выбора в UI и тип отчёта с путём к XML-файлу.
     *
     * @return массив [reportFormType, reportXmlResource] для каждого типа отчёта с xml-upload сценарием
     */
    @DataProvider(name = "reportXmlUpload")
    public static Object[][] reportXmlUploadData() {
        return new Object[][]{
                {ReportFormType.SZVM, ReportXmlResource.SZV_M},
                {ReportFormType.SZVTD, ReportXmlResource.SZV_TD_TYPE1},
                {ReportFormType.SZVSTAZH, ReportXmlResource.SZV_STAZH_AF2},
                {ReportFormType.SZVK, ReportXmlResource.SZV_K_AF2},
                {ReportFormType.SZVKORR, ReportXmlResource.SZV_KORR_AF2},
                {ReportFormType.SZVISH, ReportXmlResource.SZV_ISH_AF2},
                {ReportFormType.SZVDSO, ReportXmlResource.SZV_DSO_AF2},
                {ReportFormType.ODV1, ReportXmlResource.SZV_ODV_1_AF2},
                {ReportFormType.EFS1, ReportXmlResource.EFS_FULL_ECP},
        };
    }

    /**
     * Загрузка отчёта через XML для заданного типа отчёта.
     *
     * @param reportFormType тип отчёта для выбора в UI (например СЗВ-М, ЕФС-1)
     * @param reportXmlResource ресурс с путём к XML-файлу для загрузки
     */
    @Test(
            groups = {"web", "smoke", "xml-upload", "report-xml-unified"},
            dataProvider = "reportXmlUpload",
            description = "Загрузка отчёта через XML (параметризованный тест по типам отчётов)")
    @AllureId("REPORT-XML-001")
    @Story("XML Upload (unified)")
    @Description("Параметризованный тест: авторизация → Отчеты → выбор типа отчёта → новый отчёт → загрузка XML")
    @Severity(SeverityLevel.CRITICAL)
    public void uploadReportViaXml(ReportFormType reportFormType, ReportXmlResource reportXmlResource) {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(reportFormType);
        steps.addNewReport();
        steps.sendXml(reportXmlResource);
    }
}
