package com.bft.helpers.forms;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.asserts.SoftAssert;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FormValidator extends FormElements {

    public FormValidator(WebDriver driver, SoftAssert softAssert) {
        super(driver, softAssert);
    }

    public void verifyInsurerBlockInCreationMode() {
        softAssert.assertTrue(isElementEnabled(getOrgNameField(), "Наименование организации"),
                "Поле 'Полное или сокращенное наименование' должно быть доступно в режиме создания");
        softAssert.assertTrue(isElementEnabled(getPfrRegNumberField(), "Рег. номер в ПФР"),
                "Поле 'Рег. номер в ПФР' должно быть доступно в режиме создания");
        softAssert.assertTrue(isElementEnabled(getInnField(), "ИНН"),
                "Поле 'ИНН' должно быть доступно в режиме создания");
        softAssert.assertTrue(isElementEnabled(getKppField(), "КПП"),
                "Поле 'КПП' должно быть доступно в режиме создания");
    }

    public void verifyInsuredPersonData(InsuredPerson expectedPerson, int index) {
        List<WebElement> persons = getInsuredPersonsList();

        if (persons.size() > index) {
            WebElement personRow = persons.get(index);
            String personText = personRow.getText();

            softAssert.assertTrue(personText.contains(expectedPerson.getLastName()),
                    String.format("Фамилия '%s' должна отображаться для ЗЛ №%d",
                            expectedPerson.getLastName(), index + 1));

            softAssert.assertTrue(personText.contains(expectedPerson.getFirstName()),
                    String.format("Имя '%s' должно отображаться для ЗЛ №%d",
                            expectedPerson.getFirstName(), index + 1));

            softAssert.assertTrue(personText.contains(expectedPerson.getMiddleName()),
                    String.format("Отчество '%s' должно отображаться для ЗЛ №%d",
                            expectedPerson.getMiddleName(), index + 1));
        } else {
            softAssert.fail(String.format("ЗЛ с индексом %d не найден в списке", index));
        }
    }

    public void verifyInsuredPersonsCount(int expectedCount) {
        List<WebElement> persons = getInsuredPersonsList();
        softAssert.assertEquals(persons.size(), expectedCount,
                String.format("Количество застрахованных лиц должно быть %d, но найдено %d",
                        expectedCount, persons.size()));
    }

    public void verifyFillingDateField() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String actualDate = getElementValue(getFillingDateField(), "Дата заполнения");
        softAssert.assertEquals(actualDate, currentDate, "Дата заполнения должна быть текущей");
    }

    public void verifyEditModeFields() {
        verifyCommonInfoBlock();
        verifyInfoTypeBlock();
        verifyInsurerBlockInCreationMode();
    }

    public void verifyCommonInfoBlock() {
        softAssert.assertTrue(isElementEnabled(getReportPeriodSelect(), "Отчетный период"),
                "Поле 'Отчетный период' должно быть доступно");
        softAssert.assertTrue(isElementEnabled(getCorrectionNumberField(), "Номер корректировки"),
                "Поле 'Номер корректировки' должно быть доступно");
    }

    public void verifyInfoTypeBlock() {
        softAssert.assertTrue(isElementEnabled(getInfoTypeSelect(), "Тип сведений"),
                "Поле 'Тип сведений' должно быть доступно");
    }

    public void verifyReadMode() {
        softAssert.assertTrue(isElementEnabled(getSignAndSendButton(), "Подписать и отправить"),
                "Кнопка 'Подписать и отправить' должна быть активна в режиме чтения");
        verifyFieldsAreDisabledInReadMode();
    }

    public void verifyFieldsAreDisabledInReadMode() {
        softAssert.assertFalse(isElementEnabled(getOrgNameField(), "Наименование организации"),
                "Поле наименования должно быть недоступно в режиме чтения");
        softAssert.assertFalse(isElementEnabled(getPfrRegNumberField(), "Рег. номер в ПФР"),
                "Поле рег. номера должно быть недоступно в режиме чтения");
        softAssert.assertFalse(isElementEnabled(getInnField(), "ИНН"),
                "Поле ИНН должно быть недоступно в режиме чтения");
        softAssert.assertFalse(isElementEnabled(getKppField(), "КПП"),
                "Поле КПП должно быть недоступно в режиме чтения");
    }

    private List<WebElement> getInsuredPersonsList() {
        return driver.findElements(By.cssSelector("table tbody tr"));
    }
}