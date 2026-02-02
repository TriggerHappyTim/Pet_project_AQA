package com.bft.helpers;

import com.bft.helpers.forms.FormElements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.asserts.SoftAssert;

public class DataFiller extends FormElements {

    public DataFiller(WebDriver driver, SoftAssert softAssert) {
        super(driver, softAssert);
    }

    public void fillInsurerData(String orgName, String pfrRegNumber, String inn, String kpp) {
        fillField(orgNameField, orgName, "Наименование организации");
        fillField(pfrRegNumberField, pfrRegNumber, "Рег. номер в ПФР");
        fillField(innField, inn, "ИНН");
        fillField(kppField, kpp, "КПП");
    }

    public void fillOrgName(String orgName) {
        fillField(orgNameField, orgName, "Наименование организации");
    }

    public void fillPfrRegNumber(String pfrRegNumber) {
        fillField(pfrRegNumberField, pfrRegNumber, "Рег. номер в ПФР");
    }

    public void fillInn(String inn) {
        fillField(innField, inn, "ИНН");
    }

    public void fillKpp(String kpp) {
        fillField(kppField, kpp, "КПП");
    }

    public void fillCommonData(String reportPeriod, String correctionNumber) {
        selectReportPeriod(reportPeriod);
        fillCorrectionNumber(correctionNumber);
    }

    public void selectReportPeriod(String reportPeriod) {
        selectByVisibleText(reportPeriodSelect, reportPeriod, "Отчетный период");
    }

    public void fillCorrectionNumber(String correctionNumber) {
        fillField(correctionNumberField, correctionNumber, "Номер корректировки");
    }

    public void selectInfoType(String infoType) {
        selectByVisibleText(infoTypeSelect, infoType, "Тип сведений");
    }

    public com.bft.helpers.forms.InsuredPerson addInsuredPerson(String lastName, String firstName, String middleName,
                                                     String snils, String inn) {
        clickElement(insuredPersonsTab, "Вкладка 'Застрахованные лица'");
        clickElement(addPersonButton, "Добавить ЗЛ");

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
        clickElement(insuredPersonsTab, "Вкладка 'Застрахованные лица'");
    }

    public void saveForm() {
        clickElement(saveButton, "Сохранить форму");
    }

    public void editForm() {
        clickElement(editButton, "Редактировать форму");
    }

    public void signAndSendForm() {
        clickElement(signAndSendButton, "Подписать и отправить");
    }
}