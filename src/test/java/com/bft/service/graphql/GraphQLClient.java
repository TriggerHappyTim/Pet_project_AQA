package com.bft.service.graphql;

import com.bft.config.EnvironmentUtils;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraphQL client for executing mutations and queries against graphql-mesh-java service.
 *
 * <p>URL resolution order:
 * <ol>
 *   <li>System property: {@code -Dservice.graphql-mesh.url=http://...}</li>
 *   <li>Environment variable: {@code SERVICE_GRAPHQL_MESH_URL=http://...}</li>
 *   <li>Default: {@code http://localhost:20266}</li>
 * </ol>
 *
 * <p>Example:
 * <pre>{@code
 * GraphQLClient client = new GraphQLClient();
 * String response = client.executeMutation("{ me { id } }");
 * }</pre>
 */
public class GraphQLClient {

    private static final Logger log = LoggerFactory.getLogger(GraphQLClient.class);
    private static final String DEFAULT_URL = "http://localhost:20266";
    private static final String PROPERTY_KEY = "service.graphql-mesh.url";
    private static final String ENV_KEY = "SERVICE_GRAPHQL_MESH_URL";
    private static final int REQUEST_TIMEOUT_MS = 30_000;

    private final String meshUrl;

    public GraphQLClient() {
        this.meshUrl = resolveUrl();
        log.info("GraphQL client initialized with URL: {}", meshUrl);
    }

    public GraphQLClient(String meshUrl) {
        this.meshUrl = meshUrl;
    }

    /**
     * Resolves the graphql-mesh endpoint URL the same way the default constructor does.
     * Reused by InfrastructureCheck to avoid duplicated resolution logic.
     */
    public static String resolveMeshUrl() {
        return System.getProperty(PROPERTY_KEY,
                System.getenv(ENV_KEY) != null ? System.getenv(ENV_KEY) : DEFAULT_URL);
    }

    /** HttpClient с отключённой проверкой TLS-сертификата (аналог relaxedHTTPSValidation
     *  у rest-assured) — нужен для proxied-mesh URL с self-signed/внутренним сертификатом. */
    private static java.net.http.HttpClient trustingClient() {
        try {
            javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
            ctx.init(null, new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] c, String a) {
                        }
                        public void checkServerTrusted(java.security.cert.X509Certificate[] c, String a) {
                        }
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            }, new java.security.SecureRandom());
            return java.net.http.HttpClient.newBuilder()
                    .sslContext(ctx)
                    .build();
        } catch (Exception e) {
            log.warn("GraphQL: не удалось создать trusting HttpClient, используем обычный: {}", e.getMessage());
            return java.net.http.HttpClient.newHttpClient();
        }
    }

    /**
     * Executes a GraphQL mutation or query.
     *
     * @param query the GraphQL query/mutation string
     * @return the response body as string
     */
    @io.qameta.allure.Step("Execute GraphQL mutation")
    public String executeMutation(String query) {
        return executeMutation(query, endpointUrl(), null);
    }

    /**
     * Выполняет GraphQL с явным URL и необязательным Cookie-заголовком (для прохождения
     * SSO-аутентификации mesh из JVM, когда прямой доступ к mesh ограничен сетью, а
     * SSO-cookie извлечён из браузерной сессии).
     */
    public String executeMutation(String query, String url, String cookieHeader) {
        log.debug("Executing GraphQL mutation: {} -> {}", truncate(query, 200), url);

        String body = buildBody(query);
        java.net.http.HttpClient hc = trustingClient();
        java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .timeout(java.time.Duration.ofMillis(REQUEST_TIMEOUT_MS))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                        body, java.nio.charset.StandardCharsets.UTF_8));

        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            rb.header("Cookie", cookieHeader);
            try {
                java.net.URI u = java.net.URI.create(url);
                String origin = u.getScheme() + "://" + u.getHost();
                rb.header("Origin", origin);
                rb.header("X-Requested-With", "XMLHttpRequest");
            } catch (Exception ignore) {
            }
            log.info("GraphQL: отправлены cookies (via-browser)");
        }

        // Прямой режим (без OAuth2-прокси/cookie): mesh требует авторизацию.
        boolean authAdded = false;
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            // Приоритет — реальная session-cookie oauth2-прокси, извлечённая из браузерной
            // сессии (имя _oauth2_proxy_0/_1, domain .ecp-test.sfr.gov.ru). Mesh доверяет ей
            // (прокси расшифровывает токен и кладёт в контекст) — это единственный путь,
            // проходящий ресурсную проверку для мутаций processSignatureCommon на реальном отчёте.
            String meshCookie = com.bft.service.graphql.MeshAuthCookie.get();
            if (meshCookie != null && !meshCookie.isBlank()) {
                rb.header("Cookie", meshCookie);
                authAdded = true;
                log.info("GraphQL: добавлена mesh session-cookie (Cookie) — прямой режим");
            }
            // Резерв — Keycloak Bearer (admin-cli): проходит для query, но для мутаций на
            // реальном отчёте падает с UNAUTHENTICATED (audience=ui-mesh-java-debug-client).
            if (!authAdded) {
                try {
                    String jwt = KeycloakTokenProvider.getAccessToken();
                    rb.header("X-Forwarded-Access-Token", jwt);
                    authAdded = true;
                    log.info("GraphQL: добавлен X-Forwarded-Access-Token (резерв, без Authorization)");
                } catch (Exception tokEx) {
                    log.error("GraphQL: не удалось получить токен Keycloak: {}", tokEx.getMessage());
                }
            }
            // Резерв 1: app-токен из браузера (если Keycloak недоступен).
            if (!authAdded) {
                String appToken = MeshAuthToken.get();
                if (appToken != null && !appToken.isBlank()) {
                    rb.header("X-Forwarded-Access-Token", appToken);
                    authAdded = true;
                    log.info("GraphQL: добавлен app-токен (X-Forwarded-Access-Token без Authorization)");
                }
            }
            // Резерв 2: только если Bearer недоступен — шлём session-cookie прокси.
            if (!authAdded) {
                String meshCookie2 = com.bft.service.graphql.MeshAuthCookie.get();
                if (meshCookie != null && !meshCookie.isBlank()) {
                    rb.header("Cookie", meshCookie);
                    authAdded = true;
                    log.info("GraphQL: добавлен mesh session-cookie (Cookie) — Bearer недоступен");
                }
            }
        }

        if ("true".equalsIgnoreCase(System.getProperty("evs.sign.graphql-debug-headers", "false"))) {
            log.info("GraphQL: заголовки (java.net.http.HttpClient) — Authorization и X-Forwarded установлены выше");
        }

        java.net.http.HttpResponse<String> resp;
        try {
            resp = hc.send(rb.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("GraphQL request error: " + e.getMessage(), e);
        }
        int statusCode = resp.statusCode();
        String respBody = resp.body();

        if (statusCode >= 300 && statusCode < 400) {
            log.error("GraphQL редирект {} -> Location: {}", statusCode,
                    resp.headers().firstValue("Location").orElse("<нет>"));
        }
        log.debug("GraphQL response [{}]: {}", statusCode, truncate(respBody, 500));

        if (statusCode >= 400) {
            log.error("GraphQL request failed with status {}: {}", statusCode, respBody);
            throw new RuntimeException("GraphQL request failed with HTTP " + statusCode + ": " + respBody);
        }

        Allure.addAttachment("GraphQL Request", "application/json", query, ".graphql");
        Allure.addAttachment("GraphQL Response", "application/json", respBody, ".json");
        return respBody;
    }

    /**
     * Executes a GraphQL mutation and extracts the result status.
     *
     * @param query the GraphQL mutation string
     * @return true if the mutation was successful (no errors in response)
     */
    public boolean executeMutationAndCheck(String query) {
        String response = executeMutation(query);
        return !response.contains("\"errors\"");
    }

    private String buildBody(String query) {
        return "{\"query\": " + escapeJson(query) + "}";
    }

    /**
     * Appends the /graphql path unless the configured URL already points to it.
     * Accepts: http://host:20266, http://host:20266/, http://host:20266/graphql
     */
    private String endpointUrl() {
        String base = meshUrl.endsWith("/") ? meshUrl.substring(0, meshUrl.length() - 1) : meshUrl;
        return base.endsWith("/graphql") ? base : base + "/graphql";
    }

    private String escapeJson(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 16);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private String resolveUrl() {
        String url = System.getProperty(PROPERTY_KEY);
        if (url != null && !url.isEmpty()) {
            log.debug("Resolved GraphQL URL from system property: {}", url);
            return url;
        }

        url = System.getenv(ENV_KEY);
        if (url != null && !url.isEmpty()) {
            log.debug("Resolved GraphQL URL from environment variable: {}", url);
            return url;
        }

        log.debug("Using default GraphQL URL: {}", DEFAULT_URL);
        return DEFAULT_URL;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "null";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
