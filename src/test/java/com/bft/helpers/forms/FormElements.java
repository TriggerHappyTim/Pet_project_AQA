package com.bft.helpers.forms;

import com.bft.helpers.TestConfig;
import com.bft.ui.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.asserts.SoftAssert;

public class FormElements extends BasePage {

    @FindBy(id = TestConfig.Selectors.ORG_NAME_FIELD)
    private WebElement orgNameField;

    @FindBy(id = TestConfig.Selectors.PFR_REG_NUMBER_FIELD)
    private WebElement pfrRegNumberField;

    @FindBy(id = TestConfig.Selectors.INN_FIELD)
    private WebElement innField;

    @FindBy(id = TestConfig.Selectors.KPP_FIELD)
    private WebElement kppField;

    @FindBy(id = TestConfig.Selectors.FILLING_DATE_FIELD)
    private WebElement fillingDateField;

    @FindBy(id = TestConfig.Selectors.REPORT_PERIOD_SELECT)
    private WebElement reportPeriodSelect;

    @FindBy(id = TestConfig.Selectors.CORRECTION_NUMBER_FIELD)
    private WebElement correctionNumberField;

    @FindBy(id = TestConfig.Selectors.INFO_TYPE_SELECT)
    private WebElement infoTypeSelect;

    @FindBy(xpath = TestConfig.Selectors.INSURED_PERSONS_TAB)
    private WebElement insuredPersonsTab;

    @FindBy(id = TestConfig.Selectors.ADD_PERSON_BUTTON)
    private WebElement addPersonButton;

    @FindBy(id = TestConfig.Selectors.SAVE_BUTTON)
    private WebElement saveButton;

    @FindBy(id = TestConfig.Selectors.EDIT_BUTTON)
    private WebElement editButton;

    @FindBy(id = TestConfig.Selectors.SIGN_AND_SEND_BUTTON)
    private WebElement signAndSendButton;

    public WebElement getOrgNameField() { return orgNameField; }
    public WebElement getPfrRegNumberField() { return pfrRegNumberField; }
    public WebElement getInnField() { return innField; }
    public WebElement getKppField() { return kppField; }
    public WebElement getFillingDateField() { return fillingDateField; }
    public WebElement getReportPeriodSelect() { return reportPeriodSelect; }
    public WebElement getCorrectionNumberField() { return correctionNumberField; }
    public WebElement getInfoTypeSelect() { return infoTypeSelect; }
    public WebElement getInsuredPersonsTab() { return insuredPersonsTab; }
    public WebElement getAddPersonButton() { return addPersonButton; }
    public WebElement getSaveButton() { return saveButton; }
    public WebElement getEditButton() { return editButton; }
    public WebElement getSignAndSendButton() { return signAndSendButton; }

    public FormElements(WebDriver driver, SoftAssert softAssert) {
        super(driver, softAssert);
    }
}