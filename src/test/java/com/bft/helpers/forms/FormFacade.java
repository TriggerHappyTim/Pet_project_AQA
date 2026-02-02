package com.bft.helpers.forms;

import com.bft.helpers.DataFiller;
import com.bft.helpers.TestConfig;
import org.openqa.selenium.WebDriver;
import org.testng.asserts.SoftAssert;

public class FormFacade {
    private WebDriver driver;
    private SoftAssert softAssert;
    private DataFiller dataFiller;
    private FormValidator validator;

    public FormFacade(WebDriver driver, SoftAssert softAssert) {
        this.driver = driver;
        this.softAssert = softAssert;
        this.dataFiller = new DataFiller(driver, softAssert);
        this.validator = new FormValidator(driver, softAssert);
    }

    public void createFullSZVMForm() {
        dataFiller.fillInsurerData(
                TestConfig.ORG_NAME,
                TestConfig.PFR_REG_NUMBER,
                TestConfig.INN,
                TestConfig.KPP
        );

        dataFiller.fillCommonData(
                TestConfig.REPORT_PERIOD,
                TestConfig.CORRECTION_NUMBER
        );

        dataFiller.selectInfoType(TestConfig.INFO_TYPE);

        validator.verifyInsurerBlockInCreationMode();
        validator.verifyFillingDateField();
    }

    public InsuredPerson addInsuredPersonAndVerify(String lastName, String firstName, String middleName,
                                                              String snils, String inn, int expectedIndex) {
        InsuredPerson person = dataFiller.addInsuredPerson(lastName, firstName, middleName, snils, inn);
        validator.verifyInsuredPersonData(person, expectedIndex);
        return person;
    }

    public void saveFormAndVerifyReadMode() {
        dataFiller.saveForm();
        validator.verifyReadMode();
    }

    public void editFormAndVerify() {
        dataFiller.editForm();
        validator.verifyEditModeFields();
    }

    public DataFiller getDataFiller() {
        return dataFiller;
    }

    public FormValidator getValidator() {
        return validator;
    }
}