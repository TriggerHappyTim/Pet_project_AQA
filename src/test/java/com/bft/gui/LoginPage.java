package com.bft.gui;

import com.bft.security.masking.SecureLogger;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.bft.enums.UIType;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;


public class LoginPage {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    public LoginPage open(UIType uiType) {

        Selenide.open(uiType.value);
        return this;
    }

    public LoginPage authorize(String login, String password) {
        logger.info("Выполняем авторизацию пользователя: {}", login);

        /*$x(String.format("//*[@id= '%s')", login)).setValue(login);*/
        /*$("input.loginInput#login").setValue(login);*/
        $x("//div[@class = 'field loginControl']//input[@class = 'loginInput']").setValue(login);
        /*$("input.loginInput#pass").setValue(password);*/
        $x("//div[@class = 'field loginControl']//input[@class = 'loginInput secondInput']").setValue(password);
        /*$("button.loginButton").click();*/
        $x("//button[contains(@class,'button is-')]").click();
        waitForLoading();
        return this;
    }

    public LoginPage authorizeEPGU(String login, String password) {
        logger.info("Выполняем авторизацию через ЕПГУ для пользователя: {}", login);

        $x("//a[starts-with(@href,'/esia')]").click();
        $("input#login").shouldBe(Condition.enabled,Duration.ofSeconds(70));
        $("input#login").setValue(login);
        $("input#password").setValue(password);
        $x("//button[contains(text(), 'Войти')]").shouldBe(Condition.enabled,Duration.ofSeconds(70)).click();
        return this;
    }

    public LoginPage selectUserCardEPGU(String usercard) {
        $x("//*[@class = 'selectUserCardName' and text() = '" + usercard + "']").shouldBe(Condition.visible, Duration.ofSeconds(60)).click();
        return this;
    }

    private void waitForLoading() {
        $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(60));
    }

    public LoginPage logOut() {
        if (
                $x("//div[@class= 'ps-alert-top']").isDisplayed())
        {$x("//i[@class= 'ps-icon-x ps-alert-close']").click();}

        // если сообщение об ошибке запроса сервера,закрыть ее
        $x("//div[contains(@class, 'user-name')]").click();
        $x("//button[contains(@class, 'logout')]").click();
        $x("//div[contains(text(), 'Войдите в систему')]").shouldBe(Condition.visible, Duration.ofSeconds(60));

        return this;
    }
}
