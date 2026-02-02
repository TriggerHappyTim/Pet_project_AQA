package com.bft.helpers.forms;

import com.bft.helpers.TestConfig;
import com.bft.ui.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.asserts.SoftAssert;

public class FormElements extends BasePage {

    @FindBy(id = TestConfig.Selectors.ORG_NAME_FIELD)
    public WebElement orgNameField;

    @FindBy(id = TestConfig.Selectors.PFR_REG_NUMBER_FIELD)
    public WebElement pfrRegNumberField;

    @FindBy(id = TestConfig.Selectors.INN_FIELD)
    public WebElement innField;

    @FindBy(id = TestConfig.Selectors.KPP_FIELD)
    public WebElement kppField;

    @FindBy(id = TestConfig.Selectors.FILLING_DATE_FIELD)
    public WebElement fillingDateField;

    @FindBy(id = TestConfig.Selectors.REPORT_PERIOD_SELECT)
    public WebElement reportPeriodSelect;

    @FindBy(id = TestConfig.Selectors.CORRECTION_NUMBER_FIELD)
    public WebElement correctionNumberField;

    @FindBy(id = TestConfig.Selectors.INFO_TYPE_SELECT)
    public WebElement infoTypeSelect;

    @FindBy(xpath = TestConfig.Selectors.INSURED_PERSONS_TAB)
    public WebElement insuredPersonsTab;

    @FindBy(id = TestConfig.Selectors.ADD_PERSON_BUTTON)
    public WebElement addPersonButton;

    @FindBy(id = TestConfig.Selectors.SAVE_BUTTON)
    public WebElement saveButton;

    @FindBy(id = TestConfig.Selectors.EDIT_BUTTON)
    public WebElement editButton;

    @FindBy(id = TestConfig.Selectors.SIGN_AND_SEND_BUTTON)
    public WebElement signAndSendButton;

    public FormElements(WebDriver driver, SoftAssert softAssert) {
        super(driver, softAssert);
    }
}