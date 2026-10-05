package com.bft.LK_Insurence.SZV_M;

import com.bft.service.graphql.OAuth2ProxyCookieDecoder;
import com.bft.service.sign.DaPortalClient;
import com.bft.test.base.UITestBase;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Изолированная проверка: авторизация в oauth2-прокси mesh (reader-user) и расшифровка
 * session-cookie для извлечения keycloak access_token. БЕЗ app-логина (ESIA) и БЕЗ E2E,
 * чтобы быстро проверить механизм аутентификации mesh.
 */
public class MeshCookieDecodeTest extends UITestBase {

    private String secret() {
        String p = System.getProperty("evs.sign.oauth2-cookie-secret");
        if (p != null && !p.isBlank()) {
            return p;
        }
        try {
            return new String(Files.readAllBytes(Paths.get("C:\\Users\\Tim\\mesh_cookie_secret.txt")));
        } catch (Exception e) {
            return "";
        }
    }

    @Test
    @Description("Авторизация в mesh-прокси + расшифровка cookie -> access_token")
    public void decodeMeshCookie() {
        DaPortalClient da = new DaPortalClient();
        da.authenticateToMesh("https://ui.mesh-java.uat.ecp", "reader-user", "reader-user");
        String raw = da.extractMeshCookieRaw();
        assertNotNull(raw, "mesh session-cookie не получен");

        // ПРОБА: бьём в живой port-forward localhost:20266 с кукой под разными именами/значениями.
        String meshUrl = System.getProperty("evs.sign.graphql-probe-url", "http://localhost:20266/graphql");
        int bar = raw.indexOf('|');
        String payload = bar >= 0 ? raw.substring(0, bar) : raw;
        org.slf4j.LoggerFactory.getLogger(MeshCookieDecodeTest.class)
                .info("PROBE _ui_mesh_oauth2_proxy_0 = {}", da.probeMeshViaJava(meshUrl, "_ui_mesh_oauth2_proxy_0", raw));
        org.slf4j.LoggerFactory.getLogger(MeshCookieDecodeTest.class)
                .info("PROBE _oauth2_proxy(full) = {}", da.probeMeshViaJava(meshUrl, "_oauth2_proxy", raw));
        org.slf4j.LoggerFactory.getLogger(MeshCookieDecodeTest.class)
                .info("PROBE _oauth2_proxy(payload) = {}", da.probeMeshViaJava(meshUrl, "_oauth2_proxy", payload));

        String token = OAuth2ProxyCookieDecoder.extractAccessToken(raw, secret());
        org.slf4j.LoggerFactory.getLogger(MeshCookieDecodeTest.class)
                .info("DECODED access_token present: {}", token != null);
        if (token != null) {
            org.slf4j.LoggerFactory.getLogger(MeshCookieDecodeTest.class)
                    .info("DECODED token claims: {}", OAuth2ProxyCookieDecoder.decodeClaims(token));
        }
        assertNotNull(token, "access_token не расшифрован из cookie прокси");
    }
}
