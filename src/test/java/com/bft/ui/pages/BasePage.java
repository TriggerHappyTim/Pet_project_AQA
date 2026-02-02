package com.bft.ui.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.asserts.SoftAssert;

public class BasePage {
    protected WebDriver driver;
    protected SoftAssert softAssert;

    public BasePage(WebDriver driver, SoftAssert softAssert) {
        this.driver = driver;
        this.softAssert = softAssert;
    }

    protected void fillField(WebElement field, String value, String fieldName) {
        if (field != null && field.isDisplayed()) {
            field.clear();
            field.sendKeys(value);
            softAssert.assertEquals(field.getAttribute("value"), value,
                    "Поле '" + fieldName + "' должно содержать значение: " + value);
        } else {
            softAssert.fail("Поле '" + fieldName + "' не найдено или не отображается");
        }
    }

    protected void selectByVisibleText(WebElement selectElement, String text, String fieldName) {
        if (selectElement != null && selectElement.isEnabled()) {
            Select select = new Select(selectElement);
            select.selectByVisibleText(text);
            String selectedText = select.getFirstSelectedOption().getText();
            softAssert.assertEquals(selectedText, text,
                    "В поле '" + fieldName + "' должно быть выбрано: " + text);
        } else {
            softAssert.fail("Элемент выбора '" + fieldName + "' не доступен");
        }
    }

    protected void clickElement(WebElement element, String elementName) {
        if (element != null && element.isEnabled()) {
            element.click();
        } else {
            softAssert.fail("Элемент '" + elementName + "' не доступен для клика");
        }
    }

    protected boolean isElementEnabled(WebElement element, String elementName) {
        if (element != null) {
            return element.isEnabled();
        }
        softAssert.fail("Элемент '" + elementName + "' не найден");
        return false;
    }

    protected String getElementValue(WebElement element, String elementName) {
        if (element != null) {
            return element.getAttribute("value");
        }
        softAssert.fail("Элемент '" + elementName + "' не найден");
        return "";
    }

    protected String getElementText(WebElement element, String elementName) {
        if (element != null) {
            return element.getText();
        }
        softAssert.fail("Элемент '" + elementName + "' не найден");
        return "";
    }
}