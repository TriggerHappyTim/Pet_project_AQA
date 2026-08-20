package com.bft.security;

import com.bft.security.CredentialManager;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * Тесты для проверки загрузки credentials из переменных окружения
 * 
 * Проверяет корректность работы CredentialManager и загрузки учетных данных
 * для EVS и EPGU систем.
 */
@Epic("Configuration")
@Feature("Credentials Management")
public class CredentialsTest {

    private static final Logger log = LoggerFactory.getLogger(CredentialsTest.class);

    @Test(groups = {"config", "smoke", "credentials"}, 
          testName = "#1 Проверка загрузки credentials из переменных окружения",
          description = "Проверка загрузки credentials из переменных окружения")
    @AllureId("CRED-001")
    @Story("Credential Loading")
    @Description("Тест проверяет корректность загрузки учетных данных EVS и EPGU из переменных окружения")
    @Severity(SeverityLevel.BLOCKER)
    public void testCredentialsLoading() {
        SoftAssert softAssert = new SoftAssert();
        CredentialManager credentialManager = CredentialManager.getInstance();

        // Test individual credential lookup first
        String evsUsername = credentialManager.getCredential("evs.username");
        String evsPassword = credentialManager.getCredential("evs.password");

        log.debug("evs.username = '{}'", evsUsername);
        log.debug("evs.password = '***'");

        var evsCredentials = credentialManager.getUserCredentials("evs");
        log.debug("evsCredentials = {}", evsCredentials);

        softAssert.assertNotNull(evsCredentials, "EVS credentials should not be null");
        softAssert.assertTrue(evsCredentials.isValid(), "EVS credentials should be valid");
        softAssert.assertNotNull(evsCredentials.username, "EVS username should not be null");
        softAssert.assertFalse(evsCredentials.username.isEmpty(), "EVS username should not be empty");
        softAssert.assertNotNull(evsCredentials.password, "EVS password should not be null");
        softAssert.assertFalse(evsCredentials.password.isEmpty(), "EVS password should not be empty");
        softAssert.assertNotNull(evsPassword, "evs.password credential should be loadable");

        log.info("✓ EVS credentials loaded successfully: {}", evsCredentials.username);

        var epguCredentials = credentialManager.getUserCredentials("epgu");
        if (epguCredentials != null && epguCredentials.isValid()) {
            log.info("✓ EPGU credentials loaded successfully: {}", epguCredentials.username);
        }

        log.info("✓ All credential tests passed!");
        
        softAssert.assertAll();
    }
}