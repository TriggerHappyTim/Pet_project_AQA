package com.bft.LK_Archive;

import com.bft.test.base.UITestBase;
import com.bft.enums.UIType;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Тесты для ЛК Архивной организации
 * 
 * <p>Назначенный пользователь для ЛК Архива: {@link com.bft.security.TestUsers#BEZDOMNIY_IVAN}
 * (Бездомный Иван Николаевич, ОРГАНИЗАЦИЯ -2036470831).
 * 
 * <p>Покрывает бизнес-процессы работы с архивными запросами:
 * <ul>
 *   <li>Создание запроса в РПУ</li>
 *   <li>Обработка запроса в ЕВС (назначение исполнителя, заполнение ответа, подписание)</li>
 *   <li>Добавление исполнителя в реестр</li>
 *   <li>Полный E2E поток: РПУ -> ЕВС</li>
 * </ul>
 */
@Tag("lk-archive")
@Tag("archive")
@Tag("web")
@Epic("ЛК Архивной организации")
@Feature("Реестр запросов в архивы")
public class ArchivesTest extends UITestBase {

    // ========== РПУ ==========

    @Test
    @AllureId("ARCH-001")
    @Story("Создание запроса в РПУ")
    @Description("Полный цикл создания запроса в РПУ: авторизация, заполнение формы (сведения о запросе, гражданине, организации, период работы, тема), сохранение, подписание, отправка")
    @Severity(SeverityLevel.CRITICAL)
    public void createRequestInRPU() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeArhivRPU(UITypeSelector.getSelectedRpuType());
        steps.createZaprosRPU();
        steps.genegalInfo();
        steps.infoCitizen();
        steps.organizationData();
        steps.totalPeriod();
        steps.typeFormEmployment();
        steps.requestSubjectPeriodWork();
        steps.saveRequest();
        steps.signRequest();
        steps.selectCertificate();
        steps.get_nomber_send_request();
    }

    // ========== ЕВС ==========

    @Test
    @AllureId("ARCH-002")
    @Story("Добавление исполнителя")
    @Description("Авторизация в ЕВС архивной организации, добавление нового исполнителя в реестр, заполнение формы (ФИО, телефон, подразделение), поиск через фильтры")
    @Severity(SeverityLevel.CRITICAL)
    public void addIspolnitel() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeArhivEVS(UITypeSelector.getSelectedUIType(UIType.EVS_UAT_LKA));
        steps.addPerformer();
        steps.fillPerformer();
        steps.searchPerformer("Котов Олег Олегович");
    }

    @Test
    @AllureId("ARCH-003")
    @Story("Обработка запроса в ЕВС")
    @Description("Авторизация в ЕВС, переход в реестр запросов, поиск запроса, назначение исполнителя, выбор ответа, заполнение периода работы и загранкомандировки, ввод ответа, подписание")
    @Severity(SeverityLevel.CRITICAL)
    public void processRequestInEVS() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeArhivEVS(UITypeSelector.getSelectedUIType(UIType.EVS_UAT_LKA));
        steps.reestrZaprosov();
        steps.searchRequest1("ЗСПО-124-000000307");
        steps.assignPerformer();
        steps.answerRequest();
        steps.addPeriodWork();
        steps.addBusinessTrip();
        steps.fillAnswer();
        steps.submitSigning();
        steps.selectCertificate();
    }

    // ========== E2E ==========

    @Test
    @AllureId("ARCH-005")
    @Story("Полный бизнес-процесс РПУ -> ЕВС")
    @Description("E2E сценарий: создание запроса в РПУ (заполнение всех полей, подписание, отправка), затем авторизация в ЕВС архивной организации для обработки запроса")
    @Severity(SeverityLevel.BLOCKER)
    public void createNewRequestFullFlow() {
        SzvReportsSteps steps = new SzvReportsSteps();

        // --- РПУ: создание и отправка запроса ---
        steps.authorizeArhivRPU(UITypeSelector.getSelectedRpuType());
        steps.createZaprosRPU();
        steps.genegalInfo();
        steps.infoCitizen();
        steps.organizationData();
        steps.totalPeriod();
        steps.typeFormEmployment();
        steps.requestSubjectPeriodWork();
        steps.saveRequest();
        steps.signRequest();
        steps.selectCertificate();
        steps.get_nomber_send_request();

        // --- Выход и переход в ЕВС ---
        steps.logOut();
        steps.authorizeArhivEVS(UITypeSelector.getSelectedUIType(UIType.EVS_UAT_LKA));
    }
}
