package com.bft.service.sign;

import com.bft.pw.PwSession;
import io.qameta.allure.Allure;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Клиент Document API (/da) портала ЕВС, выполняющий запросы ВНУТРИ браузерной
 * сессии Playwright (page.evaluate + fetch).
 *
 * <p>Авторизация и cookies наследуются от открытой страницы автоматически —
 * не нужно переносить их в rest-assured. Работает локально и в CI одинаково.
 *
 * <p>Цепочка подписания документа:
 * <ol>
 *   <li>{@link #loadFile(String)} — скачать исходный файл по fileGuid;</li>
 *   <li>подписать байты ({@link PfxCmsSigner});</li>
 *   <li>{@link #uploadSignature(byte[])} — загрузить CMS-подпись → новый fileGuid;</li>
 *   <li>передать этот guid как signXmlGuid в syncSign.</li>
 * </ol>
 *
 * <p>ВАЖНО про JS-выражения: каждое — ровно ОДНА функция {@code async arg => {...}}
 * с деструктуризацией полей внутри. Запись вида {@code async ({url}) => {...}},
 * обёрнутая в {@code (arg) => ...}, даёт функцию, возвращающую функцию —
 * Playwright сериализует её как null.
 */
public class DaPortalClient {

    private static final Logger log = LoggerFactory.getLogger(DaPortalClient.class);

    private static final String DA_BASE = "/da/sk/api";

    // JS: скачать файл и вернуть содержимое в base64
    private static final String JS_LOAD =
            "async arg => {"
            + " const url = arg.url;"
            + " const r = await fetch(url, { credentials: 'include' });"
            + " if (!r.ok) throw new Error('DA load HTTP ' + r.status);"
            + " const buf = await r.arrayBuffer();"
            + " const bytes = new Uint8Array(buf);"
            + " let bin = '';"
            + " for (let i = 0; i < bytes.length; i += 0x8000) {"
            + "   bin += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));"
            + " }"
            + " return btoa(bin);"
            + "}";

    // JS: загрузка multipart (fileSig + metadata), ответ — текст
    private static final String JS_UPLOAD_SIGNATURE =
            "async arg => {"
            + " const url = arg.url;"
            + " const sigBase64 = arg.sigBase64;"
            + " const metadata = arg.metadata;"
            + " const raw = atob(sigBase64);"
            + " const bytes = new Uint8Array(raw.length);"
            + " for (let i = 0; i < raw.length; i++) bytes[i] = raw.charCodeAt(i);"
            + " const fd = new FormData();"
            + " fd.append('fileSig', new Blob([bytes], {type: 'application/pkcs7-signature'}), 'signature.sig');"
            + " fd.append('metadata', metadata);"
            + " const r = await fetch(url, { method: 'POST', credentials: 'include', body: fd });"
            + " const text = await r.text();"
            + " if (!r.ok) throw new Error('DA signature upload HTTP ' + r.status + ': ' + text.substring(0, 300));"
            + " return text;"
            + "}";

    // JS: загрузка подписанного XML (xmldsig), поле файла и тип берутся из arg
    private static final String JS_UPLOAD_XML_SIGNATURE =
            "async arg => {"
            + " const url = arg.url;"
            + " const sigBase64 = arg.sigBase64;"
            + " const metadata = arg.metadata;"
            + " const field = arg.field || 'fileSig';"
            + " const raw = atob(sigBase64);"
            + " const bytes = new Uint8Array(raw.length);"
            + " for (let i = 0; i < raw.length; i++) bytes[i] = raw.charCodeAt(i);"
            + " const fd = new FormData();"
            + " fd.append(field, new Blob([bytes], {type: 'application/xml'}), 'signature.xml');"
            + " fd.append('metadata', metadata);"
            + " const r = await fetch(url, { method: 'POST', credentials: 'include', body: fd });"
            + " const text = await r.text();"
            + " if (!r.ok) throw new Error('DA xml-signature upload HTTP ' + r.status + ': ' + text.substring(0, 300));"
            + " return text;"
            + "}";

    // JS: POST JSON в контексте сессии. Используем XMLHttpRequest (а НЕ window.fetch),
    // т.к. фронт EVS монки-патчит window.fetch обёрткой, которая вызывает .json() на
    // ответе и падает с "Unexpected end of JSON input", если n2o-эндпоинт вернул
    // пустое тело (например condition_4_polling). XHR обёрткой не перехватывается.
    private static final String JS_POST_JSON =
            "async arg => {"
            + " return await new Promise((resolve, reject) => {"
            + "   const xhr = new XMLHttpRequest();"
            + "   xhr.open('POST', arg.url, true);"
            + "   xhr.withCredentials = true;"
            + "   xhr.setRequestHeader('Content-Type', 'application/json');"
            + "   xhr.onload = () => resolve(JSON.stringify({ status: xhr.status, body: xhr.responseText }));"
            + "   xhr.onerror = () => reject(new Error('XHR POST error ' + arg.url));"
            + "   xhr.send(arg.body);"
            + " });"
            + "}";

    // JS: GET JSON в контексте сессии (через XMLHttpRequest — см. обоснование выше).
    private static final String JS_GET_JSON =
            "async arg => {"
            + " return await new Promise((resolve, reject) => {"
            + "   const xhr = new XMLHttpRequest();"
            + "   xhr.open('GET', arg.url, true);"
            + "   xhr.withCredentials = true;"
            + "   xhr.setRequestHeader('Content-Type', 'application/json');"
            + "   xhr.onload = () => resolve(JSON.stringify({ status: xhr.status, body: xhr.responseText }));"
            + "   xhr.onerror = () => reject(new Error('XHR GET error ' + arg.url));"
            + "   xhr.send();"
            + " });"
            + "}";

    // JS: POST GraphQL (same-origin на stand host), возвращает {status, body}
    private static final String JS_GRAPHQL =
            "async arg => {"
            + " const r = await fetch(arg.url, {"
            + "   method: 'POST', credentials: 'include',"
            + "   headers: { 'Content-Type': 'application/json' }, body: arg.body });"
            + " const text = await r.text();"
            + " return JSON.stringify({ status: r.status, body: text });"
            + "}";

    /** Аутентифицированная страница mesh (same-origin для GraphQL-вызовов). */
    private static com.microsoft.playwright.Page meshGraphqlPage = null;
    private static String meshUser;
    private static String meshPass;

    private static boolean hasMeshSessionCookie(String meshHost) {
        for (com.microsoft.playwright.options.Cookie c : PwSession.context().cookies()) {
            if (c.name != null && c.name.contains("_oauth2_proxy") && !c.name.contains("_csrf")
                    && c.domain != null && c.domain.contains(meshHost)) {
                return true;
            }
        }
        return false;
    }

    /** Скачивает содержимое файла хранилища по guid (с retry: файл может появляться асинхронно). */
    public byte[] loadFile(String fileGuid) {
        String url = DA_BASE + "/store/load?fileGuid=" + fileGuid;
        IllegalStateException last = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                String base64 = evaluate(JS_LOAD,
                        java.util.Collections.singletonMap("url", url));
                byte[] content = java.util.Base64.getDecoder().decode(base64);
                log.info("DA: загружен файл {} ({} байт, попытка {})", fileGuid, content.length, attempt);
                Allure.addAttachment("DA исходный файл (" + fileGuid + ")",
                        "application/octet-stream",
                        new java.io.ByteArrayInputStream(content), ".bin");
                return content;
            } catch (IllegalStateException e) {
                last = e;
                if (!e.getMessage().contains("404")) {
                    throw e;
                }
                log.warn("DA: файл {} ещё не доступен (404), попытка {}/5", fileGuid, attempt);
                try { Thread.sleep(1500); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Ожидание файла прервано", ie);
                }
            }
        }
        throw last;
    }

    /**
     * Загружает CMS-подпись и возвращает sigGuid подписанного документа
     * (= signXmlGuid для syncSign).
     *
     * <p>Контракт (вскрыт по api-docs + зондированию):
     * POST /da/sk/api/store/signature, multipart:
     * <ul>
     *   <li>fileSig — binary CMS;</li>
     *   <li>metadata — JSON {@code UploadSignatureRequest}:
     *       fileName, algorithm, certificate (base64 DER), format="CMS",
     *       signDate (ISO), hashes:[{algorithm, hash}] — хэши ИСХОДНОГО файла.</li>
     * </ul>
     */
    public String uploadSignature(byte[] originalFile, byte[] cmsSignature, PfxCmsSigner signer) {
        String certificateBase64;
        String fileHashHex;
        try {
            certificateBase64 = java.util.Base64.getEncoder()
                    .encodeToString(signer.getCertificateEncoded());
            // Бэкенд сравнивает СТРОКОВО: ожидает HEX, а не base64
            // ("FileHash(hash=<наш base64>) != <их hex>" при равных байтах!)
            java.security.MessageDigest md =
                    java.security.MessageDigest.getInstance("MD5");
            byte[] digestBytes = md.digest(cmsSignature);
            StringBuilder hex = new StringBuilder();
            for (byte b : digestBytes) {
                hex.append(String.format("%02x", b));
            }
            fileHashHex = hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка подготовки metadata: " + e.getMessage(), e);
        }

        // Формат metadata из фронтового бандла (точное соответствие): format="cms" строчными
        String metadata = String.format(
                "{\"fileName\":\"signature.sig\",\"algorithm\":\"%s\","
                        + "\"certificate\":\"%s\",\"format\":\"cms\","
                        + "\"signDate\":\"%s\",\"hashes\":[{\"algorithm\":\"MD5\",\"hash\":\"%s\"}]}",
                escape(signer.getSignerAlgorithm()), certificateBase64,
                java.time.OffsetDateTime.now().toString(), fileHashHex);

        java.util.Map<String, String> arg = new java.util.HashMap<>();
        arg.put("url", DA_BASE + "/store/signature");
        arg.put("sigBase64", java.util.Base64.getEncoder().encodeToString(cmsSignature));
        arg.put("metadata", metadata);

        String response = evaluate(JS_UPLOAD_SIGNATURE, arg);
        log.info("DA: подпись загружена, ответ: {}", truncate(response));
        Allure.addAttachment("DA upload подписи", "application/json",
                metadata + "\n---\n" + response, ".json");
        return extractSigGuid(response);
    }

    /**
     * Загружает CMS-подпись, полученную от СЭДО-gateway ({@code /debug/sign},
     * серверная подпись cert-manager), в DA тем же контрактом, что и локальный CMS.
     *
     * <p>Сертификат и алгоритм извлекаются из самого CMS (BouncyCastle),
     * поэтому отдельный запрос к СЭДО за сертификатом не нужен.
     */
    public String uploadSedoSignature(byte[] cmsSignature) {
        String certificateBase64;
        String algorithm;
        try {
            CMSSignedData cms = new CMSSignedData(cmsSignature);
            SignerInformation si = cms.getSignerInfos().getSigners().iterator().next();
            Store<?> certStore = cms.getCertificates();
            java.util.Iterator<?> certIt = certStore.getMatches(si.getSID()).iterator();
            X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
            certificateBase64 = java.util.Base64.getEncoder().encodeToString(certHolder.getEncoded());
            algorithm = cmsAlgorithmName(si.getDigestAlgOID(), si.getEncryptionAlgOID());
        } catch (Exception e) {
            throw new IllegalStateException("СЭДО: не удалось извлечь сертификат/алгоритм из CMS: "
                    + e.getMessage(), e);
        }
        // Бэкенд сравнивает СТРОКОВО: ожидает HEX, а не base64 (см. uploadSignature)
        java.security.MessageDigest md;
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new IllegalStateException("MD5 недоступен: " + e.getMessage(), e);
        }
        byte[] digestBytes = md.digest(cmsSignature);
        StringBuilder hex = new StringBuilder();
        for (byte b : digestBytes) {
            hex.append(String.format("%02x", b));
        }
        String fileHashHex = hex.toString();

        String metadata = String.format(
                "{\"fileName\":\"signature.sig\",\"algorithm\":\"%s\","
                        + "\"certificate\":\"%s\",\"format\":\"cms\","
                        + "\"signDate\":\"%s\",\"hashes\":[{\"algorithm\":\"MD5\",\"hash\":\"%s\"}]}",
                escape(algorithm), certificateBase64,
                java.time.OffsetDateTime.now().toString(), fileHashHex);

        java.util.Map<String, String> arg = new java.util.HashMap<>();
        arg.put("url", DA_BASE + "/store/signature");
        arg.put("sigBase64", java.util.Base64.getEncoder().encodeToString(cmsSignature));
        arg.put("metadata", metadata);

        String response = evaluate(JS_UPLOAD_SIGNATURE, arg);
        log.info("DA: СЭДО-подпись загружена, ответ: {}", truncate(response));
        Allure.addAttachment("DA upload СЭДО-подписи", "application/json",
                metadata + "\n---\n" + response, ".json");
        return extractSigGuid(response);
    }

    private static final java.util.Map<String, String> CMS_DIGEST_NAMES =
            java.util.Map.of(
                    "1.2.643.7.1.1.2.2", "GOST3411_2012_256",
                    "1.2.643.7.1.1.2.3", "GOST3411_2012_512",
                    "1.2.643.2.2.9", "GOST3411",
                    "2.16.840.1.101.3.4.2.1", "SHA256",
                    "2.16.840.1.101.3.4.2.2", "SHA384",
                    "2.16.840.1.101.3.4.2.3", "SHA512",
                    "1.3.14.3.2.26", "SHA1");
    private static final java.util.Map<String, String> CMS_ENC_NAMES =
            java.util.Map.of(
                    "1.2.643.7.1.1.1.1", "ECGOST3410_2012_256",
                    "1.2.643.7.1.1.1.2", "ECGOST3410_2012_512",
                    "1.2.643.2.2.19", "GOST3410",
                    "1.2.840.113549.1.1.1", "RSA",
                    "1.2.840.10045.4.3.2", "ECDSA",
                    "1.2.840.10045.4.3.3", "ECDSA",
                    "1.2.840.10045.4.3.4", "ECDSA");

    private static String cmsAlgorithmName(String digestOid, String encOid) {
        String d = CMS_DIGEST_NAMES.getOrDefault(digestOid, digestOid);
        String e = CMS_ENC_NAMES.getOrDefault(encOid, encOid);
        return d + "with" + e;
    }

    /** GET JSON на портал (например агрегат отчёта) в контексте браузерной сессии. */
    public String getPortalJson(String url) {
        return evaluate(JS_GET_JSON, java.util.Collections.singletonMap("url", url));
    }

    /** POST JSON на портал (например syncSign) в контексте браузерной сессии. */
    public String postPortalJson(String url, String jsonBody) {
        java.util.Map<String, String> arg = new java.util.HashMap<>();
        arg.put("url", url);
        arg.put("body", jsonBody);
        return evaluate(JS_POST_JSON, arg);
    }

    /**
     * Выполняет GraphQL-запрос ВНУТРИ браузерной сессии (same-origin на stand host),
     * обходя необходимость port-forward mesh :20266. Используется, когда прямой доступ
     * к mesh недоступен (свойство {@code evs.sign.graphql-via-browser=true}).
     *
     * <p>Путь эндпоинта берётся из {@code -Devs.sign.graphql-path} (по умолчанию
     * {@code /graphql}, как в n2o-фронтах ЕВС).
     *
     * @param query GraphQL-запрос/мутация
     * @return тело ответа GraphQL (строка JSON)
     */
    /**
     * Выполняет GraphQL-запрос, имитируя работу человека в web-UI graphiql:
     * очищает редактор запроса, вставляет запрос, нажимает кнопку «Play» (Execute)
     * и считывает результат из панели ответа.
     * Использует уже аутентифицированную страницу {@link #meshGraphqlPage}
     * (прокси принимает запрос, т.к. graphiql шлёт его своим родным механизмом).
     */
    public String executeGraphQL(String query) {
        if (meshUser == null || meshPass == null) {
            String basic = System.getProperty("evs.sign.graphql-basic");
            if (basic != null && basic.contains(":")) {
                meshUser = basic.substring(0, basic.indexOf(':'));
                meshPass = basic.substring(basic.indexOf(':') + 1);
            }
        }
        String meshBase = System.getProperty("evs.sign.graphql-via-browser-url",
                "https://ui.mesh-java.uat.ecp/graphql").replaceAll("/graphql.*$", "");
        // переиспользуем ОДНУ аутентифицированную вкладку mesh (без постоянного переоткрытия)
        if (meshGraphqlPage == null) {
            authenticateToMesh(meshBase, meshUser, meshPass);
        }
        com.microsoft.playwright.Page page = meshGraphqlPage;
        page.bringToFront();
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
        } catch (Exception ignored) {
        }
        // если сессия слетела и graphiql перенаправил на keycloak — пройти заново
        if (page.url() != null && page.url().contains("keycloak")) {
            authenticateToMesh(meshBase, meshUser, meshPass);
            page = meshGraphqlPage;
            page.bringToFront();
        }
        // graphiql HTML-UI может быть недоступен (Envoy upstream error), поэтому шлём
        // запрос напрямую в /graphql тем же браузерным fetch (same-origin, cookie включена
        // автоматически). Ранее fetch уходил на keycloak из-за csrf-куки; теперь сессия
        // реальная (_ui_mesh_oauth2_proxy_0/1), и прокси должен принять запрос.
        String origin = String.valueOf(page.evaluate("() => location.origin"));
        java.util.Map<String, String> arg = new java.util.HashMap<>();
        arg.put("url", origin + "/graphql");
        arg.put("body", "{\"query\":" + escapeJson(query) + "}");
        Object rawObj;
        try {
            rawObj = page.evaluate(JS_GRAPHQL, arg);
        } catch (Exception e) {
            throw new IllegalStateException("GraphQL (fetch) не выполнен: " + e.getMessage(), e);
        }
        if (rawObj == null) {
            throw new IllegalStateException("GraphQL (fetch): браузер вернул null");
        }
        String raw = String.valueOf(rawObj);
        Allure.addAttachment("GraphQL (UI) Request", "application/json", query, ".graphql");
        Allure.addAttachment("GraphQL (UI) Raw", "application/json", raw, ".json");
        log.info("GraphQL (fetch) ответ: {}", truncate(raw));
        String txt;
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(raw);
            int status = node.path("status").asInt(200);
            txt = node.path("body").asText("");
            if (status >= 400) {
                throw new IllegalStateException("GraphQL (fetch) HTTP " + status + ": " + txt);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось распарсить ответ GraphQL (fetch): " + raw, e);
        }
        if (txt.isEmpty()) {
            throw new IllegalStateException("GraphQL (fetch): пустой ответ (возможно редирект на keycloak по /graphql)");
        }
        return txt;
    }

    private String evaluateOn(com.microsoft.playwright.Page page, String jsExpression, Object arg) {
        Exception last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                page.bringToFront();
                try {
                    page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
                } catch (Exception ignored) {
                }
                Object result = page.evaluate(jsExpression, arg);
                if (result == null) {
                    throw new IllegalStateException(
                            "Браузер вернул null (проверьте формат JS-выражения): " + jsExpression);
                }
                return String.valueOf(result);
            } catch (Exception e) {
                last = e;
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (!msg.contains("destroyed") && !msg.contains("navigation")) {
                    break;
                }
                log.warn("Mesh GraphQL evaluate: контекст разрушен (попытка {}/3), повтор...", attempt);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IllegalStateException(
                "Ошибка выполнения запроса через браузерную сессию: " + last.getMessage(), last);
    }

    /**
     * Извлекает ВСЕ cookies из активной браузерной сессии (Playwright context) в виде
     * HTTP-заголовка {@code Cookie}, чтобы JVM-клиент (rest-assured) мог аутентифицироваться
     * на mesh GraphQL тем же SSO-сеансом, что и тестовый браузер.
     */
    public String getAuthCookiesHeader() {
        try {
            java.util.List<com.microsoft.playwright.options.Cookie> cookies = PwSession.context().cookies();
            StringBuilder sb = new StringBuilder();
            for (com.microsoft.playwright.options.Cookie c : cookies) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(c.name).append("=").append(c.value);
            }
            log.info("Извлечено cookies из сессии браузера: {} шт", cookies.size());
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось извлечь cookies сессии: " + e.getMessage(), e);
        }
    }

    /**
     * Аутентифицирует браузерную сессию на mesh GraphQL, стоящем за OAuth2-прокси
     * (редирект {@code /oauth2/start?rd=/graphql}). Браузер теста уже в SSO-сессии
     * приложения, поэтому редирект возвращается с cookie {@code _oauth2_proxy}, которая
     * затем извлекается через {@link #getAuthCookiesHeader()} и шлётся JVM-клиентом.
     * Если OAuth2 прокси всё же показывает форму логина — заполняет {@code user}/{@code pass}.
     */
    public void authenticateToMesh(String baseUrl, String user, String pass) {
        try {
            String meshHost = new java.net.URI(baseUrl.replaceAll("/+$", "")).getHost();
            com.microsoft.playwright.Page meshPage = PwSession.context().newPage();
            meshGraphqlPage = meshPage;
            meshUser = user;
            meshPass = pass;
            String graphiql = baseUrl.replaceAll("/+$", "") + "/graphiql/index.html";
            log.info("Mesh auth: переход на {}", graphiql);
            meshPage.navigate(graphiql);
            // Полный цикл OAuth2: прокси может показать форму логина Keycloak и/или
            // страницу Authorize на самом прокси. Обрабатываем появляющиеся формы в цикле.
            boolean authed = false;
            for (int i = 0; i < 60; i++) {
                if (hasMeshSessionCookie(meshHost)) {
                    authed = true;
                    break;
                }
                String url = meshPage.url();
                try {
                    if (url != null && url.contains("keycloak")) {
                        if (!url.toLowerCase().contains("kc-login") || url.contains("client_id")) {
                            log.info("Mesh auth: Keycloak url: {}", url);
                        }
                        com.microsoft.playwright.Locator u = meshPage.locator(
                                "input[name='username'], #username, input[type='text']").first();
                        if (u.count() > 0 && u.isVisible()) {
                            u.fill(user);
                            meshPage.locator("input[name='password'], #password, input[type='password']").first().fill(pass);
                            meshPage.locator("button[type='submit'], input[type='submit'], #kc-login").first().click();
                            log.info("Mesh auth: форма Keycloak заполнена и отправлена ({})", user);
                        }
                    } else if (url != null && url.contains("oauth2")) {
                        // страница подтверждения прокси (Authorize/Allow)
                        com.microsoft.playwright.Locator allow = meshPage.locator(
                                "button:has-text('Authorize'), button:has-text('Allow'), "
                                + "button:has-text('Authorise'), input[type='submit'], #approve").first();
                        if (allow.count() > 0 && allow.isVisible()) {
                            allow.click();
                            log.info("Mesh auth: нажата кнопка Authorize на прокси");
                        }
                    }
                } catch (Exception stepEx) {
                    log.debug("Mesh auth: шаг цикла пропущен: {}", stepEx.getMessage());
                }
                Thread.sleep(1000);
            }
            if (!authed) {
                StringBuilder cookieNames = new StringBuilder();
                for (com.microsoft.playwright.options.Cookie c : PwSession.context().cookies()) {
                    cookieNames.append(c.name).append("(d=").append(c.domain).append("), ");
                }
                log.error("Mesh auth: cookie в context: [{}]", cookieNames);
                log.error("Mesh auth: финальный URL страницы mesh: {}", meshPage.url());
                meshPage.close();
                meshGraphqlPage = null;
                throw new IllegalStateException(
                        "Mesh auth: сессионная cookie _oauth2_proxy не получена за 120s (проверьте user/pass или SSO)");
            }
            try {
                meshPage.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
            } catch (Exception ignored) {
            }
            log.info("Mesh auth: успешно, сессия mesh установлена (хост {})", meshHost);
            for (com.microsoft.playwright.options.Cookie c : PwSession.context().cookies()) {
                if (c.name != null && c.name.contains("_oauth2_proxy") && !c.name.contains("_csrf")
                        && c.domain != null && c.domain.contains(meshHost)) {
                    log.info("Mesh auth cookie: name={} path={} secure={} httpOnly={} sameSite={} expires={}",
                            c.name, c.path, c.secure, c.httpOnly, c.sameSite, c.expires);
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка аутентификации на mesh: " + e.getMessage(), e);
        }
    }

    /** ПРОБА: GraphQL-запрос к mesh ИЗ аутентифицированной браузерной сессии (куки уходят автоматически). */
    public String probeMeshViaBrowser(String graphqlUrl) {
        if (meshGraphqlPage == null) {
            return "PROBE_FAIL meshGraphqlPage==null (не аутентифицированы)";
        }
        try {
            String js = "async (url) => {"
                    + "  try {"
                    + "    const r = await fetch(url, {method:'POST', credentials:'include',"
                    + "      headers:{'Content-Type':'application/json'},"
                    + "      body: JSON.stringify({query:'{ __typename }'})});"
                    + "    const t = await r.text();"
                    + "    return r.status + ' :: ' + t.substring(0, 300);"
                    + "  } catch (e) { return 'FETCH_ERR ' + e.message; }"
                    + "};";
            Object res = meshGraphqlPage.evaluate(js, graphqlUrl);
            return String.valueOf(res);
        } catch (Exception e) {
            return "PROBE_FAIL " + e.getMessage();
        }
    }

    /** ПРОБА: GraphQL-запрос к mesh из Java с явной Cookie (без браузера). */
    public String probeMeshViaJava(String url, String cookieName, String cookieValue) {
        try {
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) new java.net.URI(url).toURL().openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Cookie", cookieName + "=" + cookieValue);
            con.setDoOutput(true);
            String body = "{\"query\":\"{ __typename }\"}";
            try (java.io.OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            StringBuilder sb = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(
                    code < 400 ? con.getInputStream() : con.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String l;
                while ((l = br.readLine()) != null) sb.append(l);
            }
            return code + " :: " + sb.substring(0, Math.min(300, sb.length()));
        } catch (Exception e) {
            return "PROBE_FAIL " + e.getMessage();
        }
    }

    /** ПРОБА: GraphQL-мутация к mesh из Java с Bearer-токеном Keycloak. */
    public String probeMeshViaJavaBearer(String url, String bearer, String query) {
        try {
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) new java.net.URI(url).toURL().openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + bearer);
            con.setRequestProperty("X-Forwarded-Access-Token", bearer);
            con.setDoOutput(true);
            String escaped = query.replace("\\", "\\\\").replace("\"", "\\\"");
            String body = "{\"query\":\"" + escaped + "\"}";
            try (java.io.OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            StringBuilder sb = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(
                    code < 400 ? con.getInputStream() : con.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String l;
                while ((l = br.readLine()) != null) sb.append(l);
            }
            return code + " :: " + sb.substring(0, Math.min(400, sb.length()));
        } catch (Exception e) {
            return "PROBE_FAIL " + e.getMessage();
        }
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

    // ==================== Helpers ====================

    /**
     * Загружает XMLDSIG-подпись (enveloped подписанный XML) и возвращает guid подписанного
     * документа (= signedXmlFileRef для processSignatureCommon).
     *
     * <p>Контракт по ответу бэкенда (27.08): «просто загрузи через {@code api/sk/api/store}»,
     * поле {@code format} НЕ нужно. Поэтому endpoint — {@code /da/sk/api/store}
     * (свойство {@code -Devs.sign.xmldsig.upload-url}), multipart (поле файла —
     * {@code -Devs.sign.xmldsig.upload-field}, по умолчанию {@code fileSig}), metadata без format.
     * Точное имя поля/состав metadata могут уточниться на первом прогоне (см. Allure-вложение).
     *
     * @param signedXml подписанный XML (байты, с элементом {@code <Signature>})
     * @param signer    XMLDSIG-подписант (для certificate/algorithm в metadata)
     * @return guid подписанного документа
     */
    public String uploadXmlSignature(byte[] signedXml, XmlDsigSigner signer) {
        String certificateBase64;
        String fileHashHex;
        try {
            certificateBase64 = java.util.Base64.getEncoder().encodeToString(signer.getCertificateEncoded());
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digestBytes = md.digest(signedXml);
            StringBuilder hex = new StringBuilder();
            for (byte b : digestBytes) {
                hex.append(String.format("%02x", b));
            }
            fileHashHex = hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка подготовки metadata (XMLDSIG): " + e.getMessage(), e);
        }

        String uploadUrl = System.getProperty("evs.sign.xmldsig.upload-url", DA_BASE + "/store");
        String uploadField = System.getProperty("evs.sign.xmldsig.upload-field", "fileSig");
        String metadata = String.format(
                "{\"fileName\":\"signature.xml\",\"algorithm\":\"%s\","
                        + "\"certificate\":\"%s\","
                        + "\"signDate\":\"%s\",\"hashes\":[{\"algorithm\":\"MD5\",\"hash\":\"%s\"}]}",
                escape(signer.getSignerAlgorithm()), certificateBase64,
                java.time.OffsetDateTime.now().toString(), fileHashHex);

        java.util.Map<String, String> arg = new java.util.HashMap<>();
        arg.put("url", uploadUrl);
        arg.put("sigBase64", java.util.Base64.getEncoder().encodeToString(signedXml));
        arg.put("metadata", metadata);
        arg.put("field", uploadField);

        String response = evaluate(JS_UPLOAD_XML_SIGNATURE, arg);
        log.info("DA: XMLDSIG загружен, ответ: {}", truncate(response));
        Allure.addAttachment("DA upload XMLDSIG", "application/json",
                "uploadUrl=" + uploadUrl + "\nuploadField=" + uploadField
                        + "\nmetadata=" + metadata + "\n---\n" + response, ".json");
        return extractSigGuid(response);
    }

    private String extractSigGuid(String responseJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper om =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(responseJson);
            if (node.has("body") && node.get("body").isTextual()) {
                node = om.readTree(node.get("body").asText());
            }
            for (String field : new String[]{"sigGuid", "fileGuid", "newID"}) {
                if (node.has(field) && !node.get(field).isNull()
                        && !node.get(field).asText().isEmpty()) {
                    return node.get(field).asText();
                }
            }
            if (node.has("successful") && !node.get("successful").asBoolean()) {
                throw new IllegalStateException("DA отклонил подпись: "
                        + node.path("resultText").asText("") + " / "
                        + node.path("resultCode").asText(""));
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception ignored) {
        }
        throw new IllegalStateException(
                "Не удалось извлечь sigGuid из ответа DA: " + truncate(responseJson));
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }

    private String evaluate(String jsExpression, Object arg) {
        try {
            Object result = PwSession.page().evaluate(jsExpression, arg);
            if (result == null) {
                throw new IllegalStateException(
                        "Браузер вернул null (проверьте формат JS-выражения): " + jsExpression);
            }
            return String.valueOf(result);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Ошибка выполнения запроса через браузерную сессию: " + e.getMessage(), e);
        }
    }

    private String truncate(String s) {
        return s != null && s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }

    /**
     * Извлекает access-токен (JWT) приложения EVS из текущей браузерной сессии
     * (localStorage / sessionStorage / cookie). Используется в прямом режиме mesh,
     * чтобы подставить токен в заголовок GraphQL-запроса (вместо oauth2-прокси).
     *
     * @return JWT-строка или null, если не найдена
     */
    public String extractAppToken() {
        // 1) localStorage / sessionStorage / document.cookie (не HttpOnly)
        String js = "(() => {\n"
                + "  const cands = [];\n"
                + "  const push = (v) => { if (typeof v === 'string' && v.indexOf('eyJ') === 0) cands.push(v); };\n"
                + "  try { for (let i = 0; i < localStorage.length; i++) { push(localStorage.getItem(localStorage.key(i))); } } catch(e){}\n"
                + "  try { for (let i = 0; i < sessionStorage.length; i++) { push(sessionStorage.getItem(sessionStorage.key(i))); } } catch(e){}\n"
                + "  try { document.cookie.split(';').forEach(c => { const v = c.split('=')[1]; if (v) push(v.trim()); }); } catch(e){}\n"
                + "  return cands;\n"
                + "})()";
        try {
            Object res = com.bft.pw.PwSession.page().evaluate(js);
            if (res instanceof java.util.List) {
                for (Object o : (java.util.List<?>) res) {
                    String t = String.valueOf(o);
                    if (t.startsWith("eyJ")) {
                        log.info("extractAppToken: найден JWT ({} симв., начало: {})",
                                t.length(), t.substring(0, Math.min(24, t.length())));
                        return t;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("extractAppToken: js-чтение не удалось: {}", e.getMessage());
        }
        // 2) HttpOnly-cookie (доступны только через Playwright context, не через document.cookie)
        try {
            java.util.List<com.microsoft.playwright.options.Cookie> cookies =
                    com.bft.pw.PwSession.context().cookies();
            java.util.List<String> names = new java.util.ArrayList<>();
            for (com.microsoft.playwright.options.Cookie c : cookies) {
                names.add(c.name + "(d=" + c.domain + ",h=" + (c.httpOnly ? "H" : "-") + ",v.len="
                        + (c.value != null ? c.value.length() : 0) + ")");
                String v = c.value;
                if (v != null && v.startsWith("eyJ")) {
                    log.info("extractAppToken: найден JWT в HttpOnly-cookie {}={}... ({} симв.)",
                            c.name, v.substring(0, Math.min(24, v.length())), v.length());
                    return v;
                }
            }
            log.info("extractAppToken: cookie в context ({} шт): {}", cookies.size(), names);
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get("build/cookies_dump.txt"),
                        names, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            log.warn("extractAppToken: чтение cookie не удалось: {}", e.getMessage());
        }
        log.warn("extractAppToken: JWT в браузерной сессии не найден");
        return null;
    }

    /**
     * Считывает session-cookie oauth2-прокси mesh ({@code _ui_mesh_oauth2_proxy_0/1})
     * из Playwright-context (HttpOnly-куки недоступны через document.cookie, но доступны
     * через context().cookies()). Возвращает строку для заголовка Cookie или null.
     */
    /**
     * Возвращает RAW-значения session-cookie oauth2-прокси mesh (части `_ui_mesh_oauth2_proxy_0`
     * и `_1`, объединённые в порядке `_0`+`_1`) для последующей расшифровки access_token.
     * Объединение нужно, т.к. oauth2-прокси при превышении 4KB разбивает cookie на части.
     */
    public String extractMeshCookieRaw() {
        try {
            java.util.List<com.microsoft.playwright.options.Cookie> cookies =
                    com.bft.pw.PwSession.context().cookies();
            java.util.Map<Integer, String> parts = new java.util.TreeMap<>();
            for (com.microsoft.playwright.options.Cookie c : cookies) {
                if (c.name != null && c.name.startsWith("_ui_mesh_oauth2_proxy_") && c.value != null) {
                    String suffix = c.name.substring("_ui_mesh_oauth2_proxy_".length());
                    Integer idx = suffix.isEmpty() ? 0 : Integer.parseInt(suffix);
                    parts.put(idx, c.value);
                }
            }
            if (parts.isEmpty()) {
                log.warn("extractMeshCookieRaw: cookie _ui_mesh_oauth2_proxy_* не найден");
                return null;
            }
            String joined = String.join("", parts.values());
            log.info("extractMeshCookieRaw: собрано {} частей, {} симв. (для расшифровки access_token)",
                    parts.size(), joined.length());
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get("build/mesh_cookie_raw.txt"),
                        joined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (java.util.Map.Entry<Integer, String> e : parts.entrySet()) {
                    String v = e.getValue();
                    sb.append("part").append(e.getKey()).append(".len=").append(v.length())
                            .append(" first40=").append(v.substring(0, Math.min(40, v.length())))
                            .append(" last20=").append(v.substring(Math.max(0, v.length() - 20)))
                            .append("\n");
                }
                log.info("extractMeshCookieRaw DEBUG:\n{}joined.first40={}\njoined.last20={}", sb,
                        joined.substring(0, Math.min(40, joined.length())),
                        joined.substring(Math.max(0, joined.length() - 20)));
            } catch (Exception ignored) {
            }
            return joined;
        } catch (Exception e) {
            log.warn("extractMeshCookieRaw: ошибка: {}", e.getMessage());
            return null;
        }
    }

    // JS: собрать все JWT-подобные значения из localStorage/sessionStorage браузера
    private static final String JS_EXTRACT_JWTS =
            "async () => {"
            + " const cands = [];"
            + " const pick = (s) => { try { for (let i=0;i<s.length;i++){"
            + "   const k=s.key(i); const v=s.getItem(k);"
            + "   if (v && v.split('.').length===3 && v.length>200) cands.push(v);"
            + " } } catch(e){} };"
            + " pick(window.localStorage); pick(window.sessionStorage);"
            + " return cands;"
            + "}";

    /** Извлекает access-токены (JWT) из браузерной сессии (localStorage/sessionStorage).
     *  Реальный пользователь после ESIA авторизован для подписываемого отчёта — его
     *  токен проходит ресурсную проверку mesh, которую не проходит reader-user (admin-cli). */
    public java.util.List<String> extractBrowserJwts() {
        try {
            Object res = com.bft.pw.PwSession.page().evaluate(JS_EXTRACT_JWTS);
            java.util.List<String> out = new java.util.ArrayList<>();
            if (res instanceof java.util.List) {
                for (Object o : (java.util.List<?>) res) {
                    if (o instanceof String && !((String) o).isBlank()) {
                        out.add((String) o);
                    }
                }
            }
            log.info("extractBrowserJwts: найдено {} JWT в браузере", out.size());
            for (int i = 0; i < out.size(); i++) {
                log.info("browser JWT[{}]: {}", i, com.bft.service.graphql.KeycloakTokenProvider
                        .decodeClaimsPublic(out.get(i)));
            }
            return out;
        } catch (Exception e) {
            log.warn("extractBrowserJwts: ошибка: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    private static volatile String capturedBearer;
    private static java.util.List<String> capturedMeshUrls = new java.util.ArrayList<>();

    /** Перехватывает Authorization-заголовки из сетевых запросов браузера к mesh.
     *  Приложение EVS непрерывно опрашивает mesh своим токеном
     *  (аудитория ui-mesh-java-debug-client) — перехватываем его и используем для прямых мутаций. */
    public void captureAppBearer() {
        try {
            com.microsoft.playwright.Page page = com.bft.pw.PwSession.page();
            java.util.Set<String> urls = new java.util.TreeSet<>();
            page.onRequest(req -> {
                try {
                    String url = req.url();
                    urls.add(url);
                    java.util.Map<String, String> h = req.headers();
                    String a = h.get("authorization");
                    if (a == null) {
                        a = h.get("Authorization");
                    }
                    if (a != null && a.toLowerCase().startsWith("bearer ")) {
                        capturedBearer = a.substring(7).trim();
                        synchronized (capturedMeshUrls) {
                            capturedMeshUrls.add("AUTH " + url);
                        }
                    } else {
                        synchronized (capturedMeshUrls) {
                            capturedMeshUrls.add(url);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
            page.onRequestFinished(req -> {
                try {
                    java.nio.file.Files.write(java.nio.file.Paths.get("build/requrls.txt"),
                            urls, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }
            });
            log.info("captureAppBearer: подписан на сетевые запросы браузера");
        } catch (Exception e) {
            log.warn("captureAppBearer: ошибка: {}", e.getMessage());
        }
    }

    public String getCapturedBearer() {
        return capturedBearer;
    }

    public String getCapturedMeshUrls() {
        synchronized (capturedMeshUrls) {
            int n = capturedMeshUrls.size();
            int from = Math.max(0, n - 8);
            return String.join("\n", capturedMeshUrls.subList(from, n));
        }
    }

    /** ПРОБА: обращается к списку кандидат-URL через аутентифицированную браузерную сессию
     *  (cookie уходит автоматически) и возвращает статус+начало тела для каждого. */
    public java.util.List<String> probeUrls(java.util.List<String> urls) {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String url : urls) {
            try {
                String js = "async (u) => {"
                        + " try {"
                        + "   const r = await fetch(u, {method:'POST', credentials:'include',"
                        + "     headers:{'Content-Type':'application/json'},"
                        + "     body: JSON.stringify({query:'{ __typename }'})});"
                        + "   const t = await r.text();"
                        + "   return r.status + ' :: ' + t.substring(0, 200);"
                        + " } catch (e) { return 'FETCH_ERR ' + e.message; }"
                        + "};";
                Object res = com.bft.pw.PwSession.page().evaluate(js, url);
                out.add(url + " -> " + res);
                log.info("probeUrls: {} -> {}", url, res);
            } catch (Exception e) {
                out.add(url + " -> ERR " + e.getMessage());
            }
        }
        return out;
    }

    public String extractMeshCookie() {
        try {
            java.util.List<com.microsoft.playwright.options.Cookie> cookies =
                    com.bft.pw.PwSession.context().cookies();
            // Приоритет — основная session-cookie (_ui_mesh_oauth2_proxy_0): она меньше и достаточна
            // для авторизации. Полная пара (_0+_1) ~10KB и вызывает HTTP 413 (слишком большой header).
            java.util.List<String> primary = new java.util.ArrayList<>();
            java.util.List<String> all = new java.util.ArrayList<>();
            for (com.microsoft.playwright.options.Cookie c : cookies) {
                if (c.name != null && c.name.contains("_oauth2_proxy") && c.value != null) {
                    String part = c.name + "=" + c.value;
                    all.add(part);
                    if ("_ui_mesh_oauth2_proxy_0".equals(c.name)) {
                        primary.add(part);
                    }
                }
            }
            java.util.List<String> parts = primary.isEmpty() ? all : primary;
            if (parts.isEmpty()) {
                log.warn("extractMeshCookie: cookie _oauth2_proxy не найден в context");
                return null;
            }
            String header = String.join("; ", parts);
            log.info("extractMeshCookie: получен mesh-cookie ({} частей, {} симв.)", parts.size(), header.length());
            return header;
        } catch (Exception e) {
            log.warn("extractMeshCookie: ошибка: {}", e.getMessage());
            return null;
        }
    }
}
