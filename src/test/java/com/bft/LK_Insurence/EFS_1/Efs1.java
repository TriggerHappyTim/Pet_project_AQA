package com.bft.LK_Insurence.EFS_1;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.security.TestUsers;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта ЕФС-1 (Единая форма сведений)
 * 
 * Покрывает различные сценарии создания и отправки отчётов ЕФС-1:
 * - Загрузка через XML файл
 * - Ручное создание ТД (трудовая деятельность)
 * - Ручное создание СТАЖ
 * 
 * Поддерживает авторизацию от разных пользователей через {@link TestUsers}
 */
@Epic("Формы отчетности")
@Feature("EFS-1 Reports")
public class Efs1 extends BaseTest {
    
    @Test(groups = {"web", "efs", "smoke", "xml-upload"}, 
          description = "Загрузка отчёта ЕФС-1 через XML файл")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта ЕФС-1 через XML файл с ЭЦП")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.EFS1); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.EFS_FULL_ECP); //отправка xml
        /*steps.chooseCriptoProvider(); //Выбор провайдера
        steps.selectCertificate(); //Выбор сертификата*/
    }

    @Test(groups = {"web", "efs", "regression", "manual-creation"}, 
          description = "Ручное создание отчёта ЕФС-1 ТД")
    @Story("Manual Creation - ТД")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД (трудовая деятельность)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_TEST_LKS); //Авторизация в ЕВС -154
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport(); // Создание нового черновика
        steps.addGeneralInfoEFS(); // Заполнение общих сведений
        steps.createContinue(); //Продолжить
        steps.sidebar(); // Выбор раздела 1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ
        steps.addZL(); //Добавление ЗЛ
        steps.fillZLEFS(); //Заполнение ЗЛ
        steps.addEventEFS();
        steps.saveEvent();
        steps.saveZL();
    }

    @Test(groups = {"web", "efs", "regression", "manual-creation"}, 
          description = "Ручное создание отчёта ЕФС-1 СТАЖ")
    @Story("Manual Creation - СТАЖ")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ (страховой стаж)")
    @Severity(SeverityLevel.NORMAL)
    public void efs_szv_staj() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС -154
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport(); // Создание нового черновика
        steps.addGeneralInfoEFS(); // Заполнение общих сведений
        steps.createContinue(); //Продолжить
        steps.sidebar(); // Выбор раздела 1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ
        steps.addZL(); //Добавление ЗЛ
        steps.fillZLEFS(); //Заполнение ЗЛ
        steps.addSTAJ();
        /*steps.addEventEFS();
        steps.saveEvent();*/ //переделать для стаж
        steps.saveZL();
    }
    
    // ========== Тесты с конкретными пользователями ==========
    
    @Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"}, 
          description = "Загрузка отчёта ЕФС-1 через XML от пользователя Кривоносов")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Кривоносов Александр Петрович (ОРГАНИЗАЦИЯ -1546025669)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_krivonosov() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.sendXml(ReportType.EFS_FULL_ECP);
    }
    
    @Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"}, 
          description = "Загрузка отчёта ЕФС-1 через XML от пользователя Бездомный")
    @Story("XML Upload - Бездомный И.Н.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Бездомный Иван Николаевич (ОРГАНИЗАЦИЯ -2036470831)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_bezdomniy() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.BEZDOMNIY_IVAN);
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.sendXml(ReportType.EFS_FULL_ECP);
    }
    
    @Test(groups = {"web", "efs", "regression", "manual-creation", "user-specific"}, 
          description = "Ручное создание отчёта ЕФС-1 ТД от пользователя Кривоносов")
    @Story("Manual Creation - ТД - Кривоносов А.П.")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td_krivonosov() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_TEST_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.addGeneralInfoEFS();
        steps.createContinue();
        steps.sidebar();
        steps.addZL();
        steps.fillZLEFS();
        steps.addEventEFS();
        steps.saveEvent();
        steps.saveZL();
    }
}
