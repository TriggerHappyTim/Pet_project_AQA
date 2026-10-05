package com.bft.service.sedo;

import com.bft.config.EnvironmentUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Клиент СЭДО-gateway REST (проект {@code D:\sedo-gateway-rest}).
 *
 * <p>Назначение: «перевести подписание на СЭДО» — авторизация через HS256-JWT (секрет из
 * {@code security.secret}) и серверная подпись/проверка через cert-manager, доставка пакетов в DA.
 *
 * <p>Авторизация: шлюз выпускает JWT (HS256, subject=client_id) в {@code POST auth}, но секрет HS256
 * нам известен (application.yml), поэтому JWT минтуется локально — без обращения к Keycloak/ESIA/браузеру.
 *
 * <p>Контракт (см. SedoController/DebugUtilsController):
 * <ul>
 *   <li>{@code POST auth} (form-urlencoded: client_id, request_id, timestamp, secret=CMS) → access_token.</li>
 *   <li>{@code POST debug/sign} (multipart, part "file") → CMS-подписанный файл (серверный cert-manager).</li>
 *   <li>{@code POST debug/verify-signature} (multipart, part "file") → VerifySignatureResultDto (JSON).</li>
 *   <li>{@code POST push} (multipart "file" + заголовки Content-MD5, Document-Type) → PackageResponseDto.</li>
 *   <li>{@code GET pckg/{package_id}}, {@code GET pckg/last} и т.п. — статусы пакетов.</li>
 * </ul>
 */
public final class SedoGatewayClient {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final String DEFAULT_SECRET =
            "Xs18DmMnO+9dTMePAIRchpBWbEB2dg9+YGMs6f8sdz/XdI09Ppa/uIsUO9Ao2sZ7QQSJSrfOqK9O88iXeS0fYg";
    private static final String DEFAULT_CLIENT_ID = "f143baec-28f6-44ce-9206-abb9140b8f89";

    private final String baseUrl;
    private final String secret;
    private final String clientId;
    private String cachedToken;

    public SedoGatewayClient() {
        this.baseUrl = EnvironmentUtils.getPropertyOrEnv("evs.sedo.url", "http://localhost:28602");
        this.secret = EnvironmentUtils.getPropertyOrEnv("evs.sedo.secret", DEFAULT_SECRET);
        this.clientId = EnvironmentUtils.getPropertyOrEnv("evs.sedo.client-id", DEFAULT_CLIENT_ID);
    }

    public String baseUrl() {
        return baseUrl;
    }

    // ===================== Авторизация (JWT HS256, минтуем сами) =====================

    public String getToken() {
        if (cachedToken != null) {
            return cachedToken;
        }
        long now = Instant.now().getEpochSecond();
        long exp = now + Long.parseLong(EnvironmentUtils.getPropertyOrEnv("evs.sedo.token-expiry", "36000"));
        String header = b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = b64url("{\"sub\":\"" + clientId + "\",\"iat\":" + now + ",\"exp\":" + exp + "}");
        String signingInput = header + "." + payload;
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            cachedToken = signingInput + "." + b64url(sig);
            return cachedToken;
        } catch (Exception e) {
            throw new IllegalStateException("СЭДО: не удалось сформировать JWT: " + e.getMessage(), e);
        }
    }

    // ===================== Подпись/проверка (cert-manager, серверная) =====================

    /** CMS-подпись файла на стороне шлюза (cert-manager). Возвращает подписанные байты. */
    public byte[] signFile(byte[] content, String fileName) {
        return postMultipartBytes(baseUrl + "/debug/sign", "file", fileName, "application/xml", content);
    }

    /** Проверка подписи XML-файла (cert-manager). Возвращает JSON VerifySignatureResultDto. */
    public String verifySignature(byte[] content, String fileName) {
        return postMultipartJson(baseUrl + "/debug/verify-signature", "file", fileName, "application/xml", content);
    }

    /** Проверка подписи XML-файла → булев результат (из поля valid/result JSON). */
    public boolean isSignatureValid(byte[] content, String fileName) {
        try {
            JsonNode node = OM.readTree(verifySignature(content, fileName));
            JsonNode v = node.path("valid");
            if (!v.isMissingNode() && v.isBoolean()) {
                return v.asBoolean();
            }
            JsonNode r = node.path("result");
            if (!r.isMissingNode()) {
                return r.asBoolean();
            }
            return node.toString().toLowerCase().contains("\"valid\":true")
                    || node.toString().toLowerCase().contains("\"result\":true");
        } catch (Exception e) {
            throw new IllegalStateException("СЭДО: ошибка проверки подписи: " + e.getMessage(), e);
        }
    }

    // ===================== Доставка пакета (push → DA) =====================

    /** Загрузка подписанного пакета в DA. Возвращает JSON PackageResponseDto (package_id). */
    public String pushPackage(byte[] content, String fileName, String md5Base64, String documentType) {
        try {
            String md5 = md5Base64 != null ? md5Base64
                    : Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(content));
            HttpURLConnection con = (HttpURLConnection) new URI(baseUrl + "/push").toURL().openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(60000);
            con.setReadTimeout(120000);
            con.setDoOutput(true);
            con.setRequestProperty("Authorization", "Bearer " + getToken());
            con.setRequestProperty("Content-MD5", md5);
            con.setRequestProperty("Document-Type", documentType);
            String boundary = "----SedoBoundary" + System.nanoTime();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream os = con.getOutputStream()) {
                writePart(os, boundary, "file", fileName, "application/octet-stream", content);
                os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            String body = readBody(con, code);
            if (code >= 400) {
                throw new IllegalStateException("СЭДО /push HTTP " + code + ": " + body);
            }
            return body;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("СЭДО /push error: " + e.getMessage(), e);
        }
    }

    /** Статус пакета по id. */
    public String getPackage(String packageId) {
        return getJson(baseUrl + "/pckg/" + packageId);
    }

    /** Последний пакет (для проверки статуса после push). */
    public String getLastPackage() {
        return getJson(baseUrl + "/pckg/last");
    }

    // ===================== Утилиты =====================

    private String getJson(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URI(url).toURL().openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            con.setRequestProperty("Authorization", "Bearer " + getToken());
            int code = con.getResponseCode();
            String body = readBody(con, code);
            if (code >= 400) {
                throw new IllegalStateException("СЭДО GET " + url + " HTTP " + code + ": " + body);
            }
            return body;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("СЭДО GET error: " + e.getMessage(), e);
        }
    }

    private byte[] postMultipartBytes(String url, String part, String fileName, String contentType, byte[] content) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URI(url).toURL().openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(60000);
            con.setReadTimeout(120000);
            con.setDoOutput(true);
            con.setRequestProperty("Authorization", "Bearer " + getToken());
            String boundary = "----SedoBoundary" + System.nanoTime();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream os = con.getOutputStream()) {
                writePart(os, boundary, part, fileName, contentType, content);
                os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            if (code >= 400) {
                throw new IllegalStateException("СЭДО " + url + " HTTP " + code + ": " + readBody(con, code));
            }
            return readBytes(con);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("СЭДО " + url + " error: " + e.getMessage(), e);
        }
    }

    private String postMultipartJson(String url, String part, String fileName, String contentType, byte[] content) {
        return new String(postMultipartBytes(url, part, fileName, contentType, content), StandardCharsets.UTF_8);
    }

    private static void writePart(OutputStream os, String boundary, String part, String fileName,
                                  String contentType, byte[] content) throws Exception {
        os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        os.write(("Content-Disposition: form-data; name=\"" + part + "\"; filename=\""
                + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        os.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        os.write(content);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] readBytes(HttpURLConnection con) throws Exception {
        try (InputStream is = con.getInputStream()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }

    private static String readBody(HttpURLConnection con, int code) throws Exception {
        InputStream is = code < 400 ? con.getInputStream() : con.getErrorStream();
        if (is == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String b64url(String s) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String b64url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
