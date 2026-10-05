package com.bft.service.graphql;

import com.bft.config.EnvironmentUtils;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Получает access-токен Keycloak для прямых (без OAuth2-прокси) обращений к graphql-mesh.
 *
 * <p>При port-forward к mesh мы обходим oauth2-proxy, который обычно сам подставляет токен
 * в заголовок upstream-запроса. Mesh-бэкенд для мутаций (process/GetSignatureDoneRouter)
 * требует токен доступа в заголовке — иначе отвечает {@code UNAUTHENTICATED}.
 *
 * <p>Координаты realm/client взяты из редиректа прокси
 * (https://keycloak.dev.ecp/realms/geop/...&client_id=ui-mesh-java-debug-client...):
 * <ul>
 *   <li>realm — {@code geop}</li>
 *   <li>client_id — {@code ui-mesh-java-debug-client}</li>
 * </ul>
 *
 * <p>Токен запрашивается через Resource Owner Password Credentials (grant_type=password)
 * пользователем из {@code -Devs.sign.graphql-basic=user:pass} (по умолчанию reader-user).
 * Кэшируется до истечения {@code expires_in}.
 */
public final class KeycloakTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(KeycloakTokenProvider.class);

    private static final String DEFAULT_TOKEN_URL =
            "https://keycloak.dev.ecp/realms/geop/protocol/openid-connect/token";
    private static final String DEFAULT_CLIENT_ID = "ui-mesh-java-debug-client";

    private static volatile String cachedToken;
    private static volatile Instant cachedExpiry = Instant.EPOCH;

    /** Внешний (например, извлечённый из браузерной сессии) токен — приоритетнее Keycloak ROPC. */
    private static volatile String forcedToken;

    private KeycloakTokenProvider() {
    }

    /** Подменяет токен внешним (browser session). Если null — снова используется Keycloak ROPC. */
    public static void setForcedToken(String token) {
        forcedToken = (token != null && !token.isBlank()) ? token : null;
    }

    public static String getAccessToken() {
        if (forcedToken != null) {
            return forcedToken;
        }
        if (cachedToken != null && Instant.now().isBefore(cachedExpiry.minusSeconds(30))) {
            return cachedToken;
        }
        synchronized (KeycloakTokenProvider.class) {
            if (cachedToken != null && Instant.now().isBefore(cachedExpiry.minusSeconds(30))) {
                return cachedToken;
            }
            cachedToken = fetchToken();
            return cachedToken;
        }
    }

    /** Сбрасывает кэш — следующий {@link #getAccessToken()} запросит свежий токен.
     *  Нужно, т.к. admin-cli-токен живёт всего ~30с, и между prepare и processSignatureCommon
     *  в E2E проходит больше времени, чем TTL. */
    public static void invalidate() {
        cachedToken = null;
        cachedExpiry = Instant.EPOCH;
    }

    private static String fetchToken() {
        String tokenUrl = EnvironmentUtils.getPropertyOrEnv(
                "evs.sign.graphql-token-url", DEFAULT_TOKEN_URL);
        String basic = EnvironmentUtils.getPropertyOrEnv("evs.sign.graphql-basic", "reader-user:reader-user");
        String[] up = basic.split(":", 2);
        String user = up[0];
        String pass = up.length > 1 ? up[1] : "";

        // Кандидаты client_id (публичные / с включённым password-grant), проверяем по порядку.
        String configured = EnvironmentUtils.getPropertyOrEnv("evs.sign.graphql-client-id", "");
        java.util.List<String> clients = new java.util.ArrayList<>();
        if (!configured.isEmpty()) {
            clients.add(configured);
        }
        clients.addAll(java.util.Arrays.asList(
                "ui-mesh-java-debug-client", "admin-cli", "security-admin-console",
                "account", "mesh", "graphql-mesh", "evs-frontend", "ecp-frontend", "evs"));

        for (String clientId : clients) {
            try {
                io.restassured.response.Response tokResp = RestAssured.given()
                        .relaxedHTTPSValidation()
                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type", "password")
                        .formParam("client_id", clientId)
                        .formParam("username", user)
                        .formParam("password", pass)
                        .formParam("scope", "openid email profile")
                        .post(tokenUrl)
                        .then().extract().response();
                int tokStatus = tokResp.getStatusCode();
                String body = tokResp.getBody().asString();
                if (tokStatus == 200) {
                    com.fasterxml.jackson.databind.ObjectMapper om =
                            new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = om.readTree(body);
                    String accessToken = node.path("access_token").asText(null);
                    int expiresIn = node.path("expires_in").asInt(0);
                    if (accessToken != null && !accessToken.isBlank()) {
                        cachedExpiry = Instant.now().plusSeconds(expiresIn > 0 ? expiresIn : 300);
                        log.info("Keycloak: токен получен (client_id={}, expires_in={}s)", clientId, expiresIn);
                        log.info("Keycloak: claims полученного токена: {}", decodeClaims(accessToken));
                        // mesh ждёт токен для своего client (ui-mesh-java-debug-client):
                        // если audience не совпадает — пробуем token-exchange.
                        String target = DEFAULT_CLIENT_ID;
                        if (!target.equals(clientId)) {
                            String exchanged = tryTokenExchange(accessToken, target);
                            if (exchanged != null) {
                                cachedExpiry = Instant.now().plusSeconds(expiresIn > 0 ? expiresIn : 300);
                                log.info("Keycloak: выполнен token-exchange -> {} (claims: {})",
                                        target, decodeClaims(exchanged));
                                return exchanged;
                            }
                            log.warn("Keycloak: token-exchange в {} не удался — используем исходный токен", target);
                        }
                        return accessToken;
                    }
                } else {
                    log.warn("Keycloak: client_id={} -> HTTP {} ({})",
                            clientId, tokStatus, truncate(body));
                }
            } catch (Exception e) {
                log.warn("Keycloak: client_id={} -> ошибка: {}", clientId, e.getMessage());
            }
        }
        throw new IllegalStateException("Keycloak: не удалось получить токен ни для одного client_id");
    }

    private static String tryTokenExchange(String subjectToken, String targetClient) {
        String tokenUrl = EnvironmentUtils.getPropertyOrEnv(
                "evs.sign.graphql-token-url", DEFAULT_TOKEN_URL);
        // Пробуем обмен как привилегированным client (admin-cli — владелец subject-токена),
        // запрашивая аудиторию targetClient. Это даёт mesh-токен с нужным audience
        // (иначе processSignatureCommon падает с UNAUTHENTICATED для реального itemId).
        for (String exchangeClient : new String[]{"admin-cli", targetClient}) {
            try {
                String body = RestAssured.given()
                        .relaxedHTTPSValidation()
                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                        .formParam("client_id", exchangeClient)
                        .formParam("subject_token", subjectToken)
                        .formParam("subject_token_type", "urn:ietf:params:oauth:token-type:access_token")
                        .formParam("audience", targetClient)
                        .formParam("scope", "openid email profile")
                        .post(tokenUrl)
                        .then().extract().response().getBody().asString();
                com.fasterxml.jackson.databind.ObjectMapper om =
                        new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = om.readTree(body);
                String t = node.path("access_token").asText(null);
                if (t != null && !t.isBlank()) {
                    return t;
                }
                log.warn("Keycloak: token-exchange (client={}) ответ без access_token: {}",
                        exchangeClient, truncate(body));
            } catch (Exception e) {
                log.warn("Keycloak: token-exchange (client={}) ошибка: {}", exchangeClient, e.getMessage());
            }
        }
        return null;
    }

    public static String decodeClaimsPublic(String jwt) {
        return decodeClaims(jwt);
    }

    private static String decodeClaims(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return "<не JWT>";
            }
            String payload = parts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(payload);
            com.fasterxml.jackson.databind.ObjectMapper om =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(decoded);
            return "iss=" + node.path("iss").asText() + ", aud=" + node.path("aud").asText()
                    + ", azp=" + node.path("azp").asText() + ", sub=" + node.path("sub").asText()
                    + ", exp=" + node.path("exp").asText() + ", typ=" + node.path("typ").asText();
        } catch (Exception e) {
            return "<ошибка декода: " + e.getMessage() + ">";
        }
    }

    private static String truncate(String s) {
        return s != null && s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
