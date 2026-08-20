package com.bft.steps;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UIType;
import com.bft.security.TestUsers;
import org.springframework.stereotype.Component;

/**
 * Фасад (композиция) шагов для работы с отчётами СЗВ в системе EVS.
 *
 * <p>Сохраняет обратную совместимость с существующими тестами и делегирует
 * выполнение в профильные step-классы:
 * <ul>
 *   <li>{@link AuthSteps} — авторизация (ЕВС/РПУ/УОС, ЕПГУ) и выход</li>
 *   <li>{@link ArchiveSteps} — запросы в архивные организации</li>
 *   <li>{@link ReportNavigationSteps} — навигация по отчётам</li>
 *   <li>{@link ReportDataEntrySteps} — заполнение форм отчётов</li>
 * </ul>
 */
@Component
public class SzvReportsSteps {

    private final AuthSteps authSteps = new AuthSteps();
    private final ArchiveSteps archiveSteps = new ArchiveSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final ReportDataEntrySteps reportDataEntrySteps = new ReportDataEntrySteps();

    // ==================== Авторизация (AuthSteps) ====================

    public void authorizeEVS(UIType uiType, TestUsers user) {
        authSteps.authorizeEVS(uiType, user);
    }

    public void authorizeEVS(UIType uiType) {
        authSteps.authorizeEVS(uiType);
    }

    public void authorizeArhivEVS() {
        authSteps.authorizeArhivEVS();
    }

    public void authorizeArhivEVS(UIType uiType) {
        authSteps.authorizeArhivEVS(uiType);
    }

    public void authorizeArhivRPU(UIType uiType) {
        authSteps.authorizeArhivRPU(uiType);
    }

    public void logOut() {
        authSteps.logOut();
    }

    // ==================== Архивные запросы (ArchiveSteps) ====================

    public void addPerformer() {
        archiveSteps.addPerformer();
    }

    public void fillPerformer() {
        archiveSteps.fillPerformer();
    }

    public void reestrZaprosov() {
        archiveSteps.reestrZaprosov();
    }

    public void searchPerformer(String performerName) {
        archiveSteps.searchPerformer(performerName);
    }

    public void searchRequest1(String numberRequest) {
        archiveSteps.searchRequest1(numberRequest);
    }

    public void assignPerformer() {
        archiveSteps.assignPerformer();
    }

    public void answerRequest() {
        archiveSteps.answerRequest();
    }

    public void addPeriodWork() {
        archiveSteps.addPeriodWork();
    }

    public void addBusinessTrip() {
        archiveSteps.addBusinessTrip();
    }

    public void fillAnswer() {
        archiveSteps.fillAnswer();
    }

    public void submitSigning() {
        archiveSteps.submitSigning();
    }

    public void createZaprosRPU() {
        archiveSteps.createZaprosRPU();
    }

    public void genegalInfo() {
        archiveSteps.genegalInfo();
    }

    public void infoCitizen() {
        archiveSteps.infoCitizen();
    }

    public void organizationData() {
        archiveSteps.organizationData();
    }

    public void totalPeriod() {
        archiveSteps.totalPeriod();
    }

    public void typeFormEmployment() {
        archiveSteps.typeFormEmployment();
    }

    public void requestSubjectPeriodWork() {
        archiveSteps.requestSubjectPeriodWork();
    }

    public void saveRequest() {
        archiveSteps.saveRequest();
    }

    public void signRequest() {
        archiveSteps.signRequest();
    }

    public void selectCertificate() {
        archiveSteps.selectCertificate();
    }

    public void get_nomber_send_request() {
        archiveSteps.get_nomber_send_request();
    }

    // ==================== Навигация по отчётам (ReportNavigationSteps) ====================

    public void addReports() {
        reportNavSteps.addReports();
    }

    public void selectReportType(ReportFormType reportFormType) {
        reportNavSteps.selectReportType(reportFormType);
    }

    public void addNewReport() {
        reportNavSteps.addNewReport();
    }

    public void createContinue() {
        reportNavSteps.createContinue();
    }

    public void goToZL() {
        reportNavSteps.goToZL();
    }

    public void sidebar() {
        reportNavSteps.sidebar();
    }

    // ==================== Заполнение форм отчётов (ReportDataEntrySteps) ====================

    public void addGeneralInfo() {
        reportDataEntrySteps.addGeneralInfo();
    }

    public void addGeneralInfoISH() {
        reportDataEntrySteps.addGeneralInfoISH();
    }

    public void fillBasisSectionISH() {
        reportDataEntrySteps.fillBasisSectionISH();
    }

    public void addGeneralInfoTD() {
        reportDataEntrySteps.addGeneralInfoTD();
    }

    public void addGeneralInfoEFS() {
        reportDataEntrySteps.addGeneralInfoEFS();
    }

    public void addZL() {
        reportDataEntrySteps.addZL();
    }

    public void addEvent() {
        reportDataEntrySteps.addEvent();
    }

    public void addEventEFS() {
        reportDataEntrySteps.addEventEFS();
    }

    public void fillZL() {
        reportDataEntrySteps.fillZL();
    }

    public void fillZLEFS() {
        reportDataEntrySteps.fillZLEFS();
    }

    public void fillZLINN() {
        reportDataEntrySteps.fillZLINN();
    }

    public void fillZLBirth() {
        reportDataEntrySteps.fillZLBirth();
    }

    public void saveZL() {
        reportDataEntrySteps.saveZL();
    }

    public void saveEvent() {
        reportDataEntrySteps.saveEvent();
    }

    public void addSTAJ() {
        reportDataEntrySteps.addSTAJ();
    }

    public void submitAndSend() {
        reportDataEntrySteps.submitAndSend();
    }

    public void submitAndSend(String providerName) {
        reportDataEntrySteps.submitAndSend(providerName);
    }

    public void sendXml(ReportXmlResource reportXmlResource) {
        reportDataEntrySteps.sendXml(reportXmlResource);
    }
}