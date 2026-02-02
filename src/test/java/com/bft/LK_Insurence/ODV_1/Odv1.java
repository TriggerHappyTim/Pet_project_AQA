package com.bft.LK_Insurence.ODV_1;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта ОДВ-1 (Опись документов, представляемых страхователем)
 * 
 * Покрывает сценарий загрузки отчёта ОДВ-1 через XML файл.
 */
@Epic("Формы отчетности")
@Feature("ОДВ-1 Reports")
public class Odv1 extends BaseTest {
    
    @Test(groups = {"web", "odv", "smoke", "xml-upload"}, 
          description = "Загрузка отчёта ОДВ-1 через XML файл")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта ОДВ-1 через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void odv_1_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.ODV1); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_ODV_1_AF2); //отправка xml
    }
}
