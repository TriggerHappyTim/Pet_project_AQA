package com.bft.helpers;

import com.bft.ui.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.asserts.SoftAssert;

import static com.bft.testdata.TestDataConstants.Selectors.*;

public class DataFiller extends BasePage {

    public DataFiller(WebDriver driver, SoftAssert softAssert) {
        super(driver, softAssert);
    }

    public void fillInsurerData(String orgName, String pfrRegNumber, String inn, String kpp) {
        fillField(getOrgNameField(), orgName, "Наименование организации");
        fillField(getPfrRegNumberField(), pfrRegNumber, "Рег. номер в ПФР");
        fillField(getInnField(), inn, "ИНН");
        fillField(getKppField(), kpp, "КПП");
    }

    private WebElement getOrgNameField() {
        return driver.findElement(By.name(ORG_NAME_FIELD));
    }

    private WebElement getPfrRegNumberField() {
        return driver.findElement(By.name(PFR_REG_NUMBER_FIELD));
    }

    private WebElement getInnField() {
        return driver.findElement(By.name(INN_FIELD));
    }

    private WebElement getKppField() {
        return driver.findElement(By.name(KPP_FIELD));
    }

    private WebElement getReportPeriodSelect() {
        return driver.findElement(By.name(REPORT_PERIOD_SELECT));
    }

    private WebElement getCorrectionNumberField() {
        return driver.findElement(By.name(CORRECTION_NUMBER_FIELD));
    }

    private WebElement getInfoTypeSelect() {
        return driver.findElement(By.name(INFO_TYPE_SELECT));
    }

    private WebElement getInsuredPersonsTab() {
        return driver.findElement(By.xpath(INSURED_PERSONS_TAB));
    }

    private WebElement getAddPersonButton() {
        return driver.findElement(By.name(ADD_PERSON_BUTTON));
    }

    private WebElement getSaveButton() {
        return driver.findElement(By.name(SAVE_BUTTON));
    }

    private WebElement getEditButton() {
        return driver.findElement(By.name(EDIT_BUTTON));
    }

    private WebElement getSignAndSendButton() {
        return driver.findElement(By.name(SIGN_AND_SEND_BUTTON));
    }

    public void fillOrgName(String orgName) {
        fillField(getOrgNameField(), orgName, "Наименование организации");
    }

    public void fillPfrRegNumber(String pfrRegNumber) {
        fillField(getPfrRegNumberField(), pfrRegNumber, "Рег. номер в ПФР");
    }

    public void fillInn(String inn) {
        fillField(getInnField(), inn, "ИНН");
    }

    public void fillKpp(String kpp) {
        fillField(getKppField(), kpp, "КПП");
    }

    public void fillCommonData(String reportPeriod, String correctionNumber) {
        selectReportPeriod(reportPeriod);
        fillCorrectionNumber(correctionNumber);
    }

    public void selectReportPeriod(String reportPeriod) {
        selectByVisibleText(getReportPeriodSelect(), reportPeriod, "Отчетный период");
    }

    public void fillCorrectionNumber(String correctionNumber) {
        fillField(getCorrectionNumberField(), correctionNumber, "Номер корректировки");
    }

    public void selectInfoType(String infoType) {
        selectByVisibleText(getInfoTypeSelect(), infoType, "Тип сведений");
    }

    public com.bft.helpers.forms.InsuredPerson addInsuredPerson(String lastName, String firstName, String middleName,
                                                     String snils, String inn) {
        clickElement(getInsuredPersonsTab(), "Вкладка 'Застрахованные лица'");
        clickElement(getAddPersonButton(), "Добавить ЗЛ");

        WebElement lastNameField = driver.findElement(By.name("lastName"));
        WebElement firstNameField = driver.findElement(By.name("firstName"));
        WebElement middleNameField = driver.findElement(By.name("middleName"));
        WebElement snilsField = driver.findElement(By.name("snils"));
        WebElement innField = driver.findElement(By.name("personInn"));

        fillField(lastNameField, lastName, "Фамилия ЗЛ");
        fillField(firstNameField, firstName, "Имя ЗЛ");
        fillField(middleNameField, middleName, "Отчество ЗЛ");
        fillField(snilsField, snils, "СНИЛС ЗЛ");

        if (inn != null && !inn.isEmpty()) {
            fillField(innField, inn, "ИНН ЗЛ");
        }

        clickElement(driver.findElement(By.id("savePersonBtn")), "Сохранить ЗЛ");

        return new com.bft.helpers.forms.InsuredPerson(lastName, firstName, middleName, snils, inn);
    }

    public void openInsuredPersonsTab() {
        clickElement(getInsuredPersonsTab(), "Вкладка 'Застрахованные лица'");
    }

    public void saveForm() {
        clickElement(getSaveButton(), "Сохранить форму");
    }

    public void editForm() {
        clickElement(getEditButton(), "Редактировать форму");
    }

    public void signAndSendForm() {
        clickElement(getSignAndSendButton(), "Подписать и отправить");
    }
}