package com.bft.LK_Insurence.SZV_STAJ;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-СТАЖ (Сведения о страховом стаже)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-СТАЖ через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-СТАЖ Reports")
public class Szv_staj extends BaseTest {
    
    @Test(groups = {"web", "szv-staj", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта СЗВ-СТАЖ через XML файл",
          description = "Загрузка отчёта СЗВ-СТАЖ через XML файл")
    @AllureId("SZVSTAJ-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-СТАЖ через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_staj_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVSTAZH); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_STAZH_AF2); //отправка xml
    }
}
