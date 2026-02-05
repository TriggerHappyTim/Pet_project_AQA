package com.bft.LK_Insurence.SZV_K;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-К (Сведения корректирующие по взносам)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-К через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-К Reports")
public class Szv_k extends BaseTest {
    
    @Test(groups = {"web", "szv-k", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта СЗВ-К через XML файл",
          description = "Загрузка отчёта СЗВ-К через XML файл")
    @AllureId("SZVK-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-К через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_k_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVK); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_K_AF2); //отправка xml
    }
}
