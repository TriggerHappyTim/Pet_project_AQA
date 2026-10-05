package com.bft.service.sedo;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Проверка авторизации СЭДО БЕЗ сети: минтуем JWT (HS256) и валидируем его
 * подпись тем же secret, что шлюз (security.secret). Если HMAC совпадает —
 * шлюз примет такой Bearer. Это единственная часть СЭДО-интеграции,
 * которую можно прогнать локально без port-forward.
 */
public class SedoGatewayClientAuthTest {

    private static final ObjectMapper OM = new ObjectMapper();

    @Test
    public void tokenIsValidHs256() throws Exception {
        SedoGatewayClient sedo = new SedoGatewayClient();
        String token = sedo.getToken();
        LoggerFactory.getLogger(SedoGatewayClientAuthTest.class).info("СЭДО JWT: {}", token);

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalStateException("JWT должен содержать 3 части");
        }
        String headerJson = new String(b64urlDecode(parts[0]), StandardCharsets.UTF_8);
        String payloadJson = new String(b64urlDecode(parts[1]), StandardCharsets.UTF_8);
        LoggerFactory.getLogger(SedoGatewayClientAuthTest.class).info("header: {}", headerJson);
        LoggerFactory.getLogger(SedoGatewayClientAuthTest.class).info("payload: {}", payloadJson);

        JsonNode header = OM.readTree(headerJson);
        if (!"HS256".equals(header.path("alg").asText())) {
            throw new IllegalStateException("Неожиданный alg: " + header.path("alg"));
        }

        // Реплицируем подпись шлюза и сравниваем
        String secret = sedoSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] expectedSig = mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
        byte[] actualSig = b64urlDecode(parts[2]);
        if (!java.util.Arrays.equals(expectedSig, actualSig)) {
            throw new IllegalStateException("HS256-подпись JWT НЕ совпадает с secret — шлюз отвергнет");
        }
        LoggerFactory.getLogger(SedoGatewayClientAuthTest.class)
                .info("OK: HS256-подпись валидна, subject={}", OM.readTree(payloadJson).path("sub").asText());
    }

    private static String sedoSecret() {
        // дублируем дефолт из SedoGatewayClient (тест читает тот же, если не переопределён)
        String fromEnv = System.getProperty("evs.sedo.secret");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return "Xs18DmMnO+9dTMePAIRchpBWbEB2dg9+YGMs6f8sdz/XdI09Ppa/uIsUO9Ao2sZ7QQSJSrfOqK9O88iXeS0fYg";
    }

    private static byte[] b64urlDecode(String s) {
        return Base64.getUrlDecoder().decode(s);
    }
}
