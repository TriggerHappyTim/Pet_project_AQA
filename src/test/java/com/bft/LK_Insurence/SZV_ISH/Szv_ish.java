package com.bft.LK_Insurence.SZV_ISH;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UITypeSelector;
import com.bft.security.TestUsers;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-ИСХ (Сведения исходные)
 * 
 * Покрывает сценарии загрузки через XML и ручного заполнения отчёта СЗВ-ИСХ.
 * 
 * <p>Вход в систему: Госуслуги (ЕПГУ), пользователь Кривоносов А.П.,
 * организация, начинающаяся на «ОРГАНИЗАЦИЯ -154» (evs.user1.organization).
 * 
 * @see TestUsers#KRIVONOSOV_ALEXANDER
 */
@Epic("Формы отчетности")
@Feature("СЗВ-ИСХ Reports")
public class Szv_ish extends BaseTest {
    
    @Test(groups = {"web", "szv-ish", "regression", "manual-creation"}, 
          description = "Ручное заполнение отчёта СЗВ-ИСХ")
    @Story("Manual Creation")
    @Description("Тест проверяет ручное создание отчёта СЗВ-ИСХ по аналогии с ЕФС-1")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_ish_manual() {
        SzvReportsSteps steps = new SzvReportsSteps();
        
        steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        steps.addReports();
        steps.selectReportType(ReportType.SZVISH);
        steps.addNewReport();
        steps.addGeneralInfoISH();
        steps.createContinue();
        steps.goToZL();
        steps.addZL();
        steps.fillZL();
        steps.fillZLINN();
        steps.saveZL();
    }
    
    @Test(groups = {"web", "szv-ish", "smoke", "xml-upload"}, 
          testName = "#2 Загрузка отчёта СЗВ-ИСХ через XML файл",
          description = "Загрузка отчёта СЗВ-ИСХ через XML файл")
    @AllureId("SZVISH-002")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-ИСХ через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_ish_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER); //Вход через Госуслуги, орг. «ОРГАНИЗАЦИЯ -154*»
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVISH); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_ISH_AF2); //отправка xml
    }
}
