package com.bft.LK_Insurence.SZV_KORR;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-КОРР (Сведения корректирующие)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-КОРР через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-КОРР Reports")
public class Szv_korr extends BaseTest {
    
    @Test(groups = {"web", "szv-korr", "smoke", "xml-upload"}, 
          description = "Загрузка отчёта СЗВ-КОРР через XML файл")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-КОРР через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_korr_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVKORR); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_KORR_AF2); //отправка xml
    }
}
