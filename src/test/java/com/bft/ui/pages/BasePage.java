package com.bft.ui.pages;

import com.bft.pw.PwDriver;
import com.bft.pw.PwElement;
import org.testng.asserts.SoftAssert;

public class BasePage {
    protected PwDriver driver;
    protected SoftAssert softAssert;

    public BasePage(PwDriver driver, SoftAssert softAssert) {
        this.driver = driver;
        this.softAssert = softAssert;
    }

    protected void fillField(PwElement field, String value, String fieldName) {
        if (field != null && field.isDisplayed()) {
            field.clear();
            field.setValue(value);
            softAssert.assertEquals(field.getAttribute("value"), value,
                    "Поле '" + fieldName + "' должно содержать значение: " + value);
        } else {
            softAssert.fail("Поле '" + fieldName + "' не найдено или не отображается");
        }
    }

    protected void selectByVisibleText(PwElement selectElement, String text, String fieldName) {
        if (selectElement != null && selectElement.isEnabled()) {
            selectElement.selectOption(text);
            Object selectedText = selectElement.executeJavaScript(
                    "return arguments[0].options[arguments[0].selectedIndex].text;", selectElement);
            softAssert.assertEquals(selectedText, text,
                    "В поле '" + fieldName + "' должно быть выбрано: " + text);
        } else {
            softAssert.fail("Элемент выбора '" + fieldName + "' не доступен");
        }
    }

    protected void clickElement(PwElement element, String elementName) {
        if (element != null && element.isEnabled()) {
            element.click();
        } else {
            softAssert.fail("Элемент '" + elementName + "' не доступен для клика");
        }
    }

    protected boolean isElementEnabled(PwElement element, String elementName) {
        if (element != null) {
            return element.isEnabled();
        }
        softAssert.fail("Элемент '" + elementName + "' не найден");
        return false;
    }

    protected String getElementValue(PwElement element, String elementName) {
        if (element != null) {
            return element.getAttribute("value");
        }
        softAssert.fail("Элемент '" + elementName + "' не найден");
        return "";
    }

    protected String getElementText(PwElement element, String elementName) {
        if (element != null) {
            return element.getText();
        }
        softAssert.fail("Элемент '" + elementName + "' не найден");
        return "";
    }
}