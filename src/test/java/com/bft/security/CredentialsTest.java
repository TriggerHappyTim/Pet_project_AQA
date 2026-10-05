package com.bft.security;

import com.bft.security.CredentialManager;
import com.bft.test.base.BaseTest;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Тесты для проверки загрузки credentials из переменных окружения
 * 
 * Проверяет корректность работы CredentialManager и загрузки учетных данных
 * для EVS и EPGU систем.
 */
@Tag("smoke")
@Epic("Configuration")
@Feature("Credentials Management")
public class CredentialsTest extends BaseTest {

    @Test
    @AllureId("CRED-001")
    @Story("Credential Loading")
    @Description("Тест проверяет корректность загрузки учетных данных EVS и EPGU из переменных окружения")
    @Severity(SeverityLevel.BLOCKER)
    public void testCredentialsLoading() {
        CredentialManager credentialManager = CredentialManager.getInstance();

        String evsUsername = credentialManager.getCredential("evs.username");
        String evsPassword = credentialManager.getCredential("evs.password");

        logger.debug("evs.username = '{}'", evsUsername);
        logger.debug("evs.password = '***'");

        var evsCredentials = credentialManager.getUserCredentials("evs");
        logger.debug("evsCredentials = {}", evsCredentials);

        assertions.assertNotNull(evsCredentials, "EVS credentials should not be null");
        assertions.assertTrue(evsCredentials.isValid(), "EVS credentials should be valid");
        assertions.assertNotNull(evsCredentials.username, "EVS username should not be null");
        assertions.assertFalse(evsCredentials.username.isEmpty(), "EVS username should not be empty");
        assertions.assertNotNull(evsCredentials.password, "EVS password should not be null");
        assertions.assertFalse(evsCredentials.password.isEmpty(), "EVS password should not be empty");
        assertions.assertNotNull(evsPassword, "evs.password credential should be loadable");

        logger.info("EVS credentials loaded successfully: {}", evsCredentials.username);

        var epguCredentials = credentialManager.getUserCredentials("epgu");
        if (epguCredentials != null && epguCredentials.isValid()) {
            logger.info("EPGU credentials loaded successfully: {}", epguCredentials.username);
        }

        logger.info("All credential tests passed!");
    }
}