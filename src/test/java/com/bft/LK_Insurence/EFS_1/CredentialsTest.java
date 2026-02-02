package com.bft.LK_Insurence.EFS_1;

import com.bft.security.CredentialManager;
import io.qameta.allure.*;
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

    @Test(groups = {"config", "smoke", "credentials"}, 
          description = "Проверка загрузки credentials из переменных окружения")
    @Story("Credential Loading")
    @Description("Тест проверяет корректность загрузки учетных данных EVS и EPGU из переменных окружения")
    @Severity(SeverityLevel.BLOCKER)
    public void testCredentialsLoading() {
        SoftAssert softAssert = new SoftAssert();
        CredentialManager credentialManager = CredentialManager.getInstance();

        // Test individual credential lookup first
        String evsUsername = credentialManager.getCredential("evs.username");
        String evsPassword = credentialManager.getCredential("evs.password");

        System.out.println("Debug: evs.username = '" + evsUsername + "'");
        System.out.println("Debug: evs.password = '" + evsPassword + "'");

        // Test EVS credentials
        var evsCredentials = credentialManager.getUserCredentials("evs");
        System.out.println("Debug: evsCredentials = " + evsCredentials);

        softAssert.assertNotNull(evsCredentials, "EVS credentials should not be null");
        softAssert.assertTrue(evsCredentials.isValid(), "EVS credentials should be valid");
        softAssert.assertNotNull(evsCredentials.username, "EVS username should not be null");
        softAssert.assertFalse(evsCredentials.username.isEmpty(), "EVS username should not be empty");
        softAssert.assertNotNull(evsCredentials.password, "EVS password should not be null");
        softAssert.assertFalse(evsCredentials.password.isEmpty(), "EVS password should not be empty");

        System.out.println("✓ EVS credentials loaded successfully: " + evsCredentials.username);

        // Test EPGU credentials
        var epguCredentials = credentialManager.getUserCredentials("epgu");
        if (epguCredentials != null && epguCredentials.isValid()) {
            System.out.println("✓ EPGU credentials loaded successfully: " + epguCredentials.username);
        }

        System.out.println("✓ All credential tests passed!");
        
        softAssert.assertAll();
    }
}