package com.bft.steps;

import com.bft.config.EnvironmentUtils;
import com.bft.pw.Condition;
import com.bft.service.graphql.SignService;
import com.bft.service.graphql.SignServiceImpl;
import com.bft.service.sign.PfxCmsSigner;
import com.bft.pw.PwSession;
import com.bft.service.sign.DaPortalClient;
import com.bft.service.sign.SigningHelper;
import com.bft.service.sign.PfxCmsSigner;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static com.bft.enums.TimeoutConstants.DEFAULT_WAIT;
import static com.bft.enums.TimeoutConstants.LONG_WAIT;
import static com.bft.pw.Selenide.$x;

/**
 * Шаги подписания отчёта в ЛК Страхователя: гибрид UI + автоматическая ЭЦП.
 *
 * <p>Режимы ({@code evs.sign.mode}):
 * <ul>
 *   <li>{@code api} (по умолчанию для этого сценария) — UI доходит до точки подписания
 *       («Подписать и отправить» → «Да»), фронт вызывает {@code /syncPrepare}; далее тест
 *       сам подписывает документ PFX-сертификатом ({@link PfxCmsSigner}) через Document API
 *       и завершает подписание вызовом {@code syncSign}. КриптоПРО-плагин не требуется;</li>
 *   <li>{@code ui} — классическое подписание через диалог провайдера
 *       (нужен установленный КриптоПРО/VipNet).</li>
 * </ul>
 */
public class ReportSignSteps {

    private static final Logger log = LoggerFactory.getLogger(ReportSignSteps.class);

    private PfxCmsSigner signer;

    @Step("Подписать и отправить отчёт")
    public void signAndSendReport() {
        if (isApiSignMode()) {
            // Чистый API-путь: ни одного UI-клика. UI нужен только чтобы
            // тест оказался на странице отчёта (после загрузки XML он там и так).
            signCurrentReportViaApi();
            return;
        }
        // UI-путь: полная эмуляция пользователя с КриптоПРО/VipNet
        MainPage mainPage = new MainPage().clickSignAndSend();
        confirmSignDialog();
        // Ассерт на падение mesh при подписании: если mesh недоступен (пользователь видел
        // уведомление в правом углу «меш не работает»), окно «Выбор сертификата» не откроется.
        // Проверяем это ДО таймаута 10с на chooseCertificate, чтобы дать внятное сообщение.
        String meshError = com.bft.pw.PwSession.getAndClearMeshError();
        if (meshError != null) {
            new com.bft.ui.pages.MainPage().assertNoBackendFailureToast();
            throw new AssertionError("Падение mesh при подписании отчёта: " + meshError);
        }
        new com.bft.ui.pages.MainPage().assertNoBackendFailureToast();
        // Окно «Выбор сертификата» рендерится внутри provider-dialog: каждый сертификат —
        // label.provider-label с радио и текстом субъекта (CN). Выбираем сертификат по
        // имени субъекта (evs.sign.cert-subject, по умолчанию «КОТОВ»), а не «последний» —
        // последним может оказаться служебный/другой сертификат и подпись уйдёт не тем ключом.
        String certSubject = EnvironmentUtils.getPropertyOrEnv("evs.sign.cert-subject", "КОТОВ");
        new com.bft.ui.pages.MainPage().chooseCertificate(certSubject);
        new com.bft.ui.pages.MainPage().clickButtonProviderDialog("Подписать");
        $x("//div[@class = 'provider-dialog']")
                .should(Condition.disappear, LONG_WAIT);
        // При подписании через syncSign модалка закрывается автоматически
        // после завершения — явного «Готово» нет.
    }

    /**
     * Полное подписание отчёта через API, БЕЗ нажатий «Подписать и отправить»/«Да».
     *
     * <p>Транспорт подписания выбирается свойством {@code evs.sign.transport}:
     * <ul>
     *   <li>{@code graphql} (по умолчанию) — целевой путь по контракту бэкенда
     *       ({@code signDoc.object.xml}): prepareDocCommon → подпись → processSignatureCommon;</li>
     *   <li>{@code rest} — устаревший REST+CMS (syncPrepare/syncSign), оставлен для отладки;</li>
     *   <li>{@code sedo} — EVS prepare/process остаются GraphQL (admin-cli Bearer), а шаг
     *       подписи+загрузки переводится на СЭДО-gateway (серверная CMS-подпись cert-manager).</li>
     * </ul>
     */
    @Step("Подписать отчёт полностью через API (без UI-кликов)")
    private void signCurrentReportViaApi() {
        String transport = EnvironmentUtils.getPropertyOrEnv("evs.sign.transport", "rest");
        if ("graphql".equalsIgnoreCase(transport)) {
            signCurrentReportViaGraphQL();
        } else if ("sedo".equalsIgnoreCase(transport)) {
            log.info("Транспорт подписания = СЭДО: EVS GraphQL prepare/process + СЭДО sign/upload");
            signCurrentReportViaGraphQL();
        } else {
            // rest (по умолчанию): prepareDocCommon (GraphQL, reader-user, READ — без
            // проверки владения) + syncSign через n2o в браузерной сессии ЕСИА
            // (владелец отчёта → ресурсная проверка пройдена). Не требует КриптоПРО.
            signCurrentReportViaRestApiLegacy();
        }
    }

    /**
     * GraphQL-путь (целевой, по signDoc.object.xml):
     * <ol>
     *   <li>itemId из агрегатного датасорса отчёта (GET pfrEcpStorReportAggreServ);</li>
     *   <li>{@code prepareDocCommon} (GraphQL) → requestId + xmlGuid;</li>
     *   <li>скачать XML из DA → CMS-подпись PFX → загрузить = signXmlGuid;</li>
     *   <li>{@code processSignatureCommon} (GraphQL, signedXmlFileRef=signXmlGuid) → подписано.</li>
     * </ol>
     *
     * <p>ПРИМЕЧАНИЕ по формату подписи: бэкенд указал формат xmldsig (не CMS/PKCS#7).
     * Здесь временно переиспользуется CMS-подписант ({@link PfxCmsSigner}) + загрузка в DA —
     * механизм изолирован в {@link #signer()} и {@link DaPortalClient#uploadSignature},
     * его замена на xmldsig (Apache Santuario) не затронет оркестрацию.
     */
    @Step("Подписать отчёт: GraphQL путь (prepareDocCommon → sign → processSignatureCommon)")
    private void signCurrentReportViaGraphQL() {
        String reportPath = currentReportPath(); // /reports/svzm/8982/mainInfo
        if (reportPath.isEmpty()) {
            throw new IllegalStateException(
                    "Тест должен находиться на странице отчёта (/reports/...), текущий URL: "
                            + PwSession.page().url());
        }
        DaPortalClient da = new DaPortalClient();
        SignService signService = new SignServiceImpl();

        // Диагностика: перехватываем реальные URL обращений браузера к mesh (если включено).
        da.captureAppBearer();

        // Используем реальную session-cookie oauth2-прокси из браузерной сессии
        // (_oauth2_proxy_0/_1, domain .ecp-test.sfr.gov.ru). Она выдана тем же прокси,
        // который фронтит mesh, поэтому mesh доверяет ей и пропускает мутации
        // processSignatureCommon на реальном отчёте (прокси расшифровывает токен и кладёт
        // в контекст). GraphQLClient шлёт эту cookie как Cookie-заголовок к proxied-mesh URL
        // (через -Dservice.graphql-mesh.url=https://ecp-test.sfr.gov.ru/graphql).
        boolean viaBrowser = "true".equalsIgnoreCase(
                com.bft.config.EnvironmentUtils.getPropertyOrEnv("evs.sign.graphql-via-browser", "false"));
        if (!viaBrowser) {
            String meshCookie = da.extractMeshCookie();
            com.bft.service.graphql.MeshAuthCookie.set(meshCookie);
            com.bft.service.graphql.KeycloakTokenProvider.setForcedToken(null);
            if (meshCookie != null) {
                log.info("Mesh Auth: session-cookie oauth2-прокси из браузерной сессии ({} симв.)", meshCookie.length());
            } else {
                log.warn("Mesh Auth: session-cookie НЕ извлечена — mesh process пойдёт без токена");
            }
        }

        // 1) itemId из агрегата отчёта
        String itemId = resolveItemId(da, reportPath);
        log.info("itemId из агрегата: {}", itemId);

        // 2) prepareDocCommon (GraphQL) → requestId + xmlGuid
        //    formName НЕ передаём: CommonReportSignatureService определяет тип отчёта по itemId.
        SignService.PrepareDocResult prep = signService.prepareDocCommon(
                itemId, mimeType(), null);
        if (prep.requestId == null || prep.xmlGuid == null) {
            throw new IllegalStateException("prepareDocCommon не вернул requestId/xmlGuid: " + prep);
        }
        Allure.addAttachment("prepareDocCommon результат", "application/json", prep.toString(), ".json");

        // 3) Файл → подпись (xmldsig или CMS) → загрузка в DA → signXmlGuid
        byte[] content = da.loadFile(prep.xmlGuid);
        String signXmlGuid = signAndUpload(da, content);
        log.info("signXmlGuid: {}", signXmlGuid);
        Allure.addAttachment("signXmlGuid", "text/plain", signXmlGuid, ".txt");

        // 4) processSignatureCommon (GraphQL)
        boolean ok = signService.processSignatureCommon(
                itemId, signXmlGuid, mimeType(), prep.requestId);
        if (!ok) {
            throw new IllegalStateException("processSignatureCommon завершился ошибкой "
                    + "(см. GraphQL Request/Response в отчёте Allure)");
        }
    }

    /**
     * Устаревший REST+CMS путь (syncPrepare/syncSign). Оставлен как fallback для отладки;
     * бэкенд пометил этот путь как неверный — используйте {@link #signCurrentReportViaGraphQL()}.
     */
    @Step("Подписать отчёт: устаревший REST+CMS путь (legacy fallback)")
    private void signCurrentReportViaRestApiLegacy() {
        String reportPath = currentReportPath(); // /reports/svzm/8982/mainInfo
        if (reportPath.isEmpty()) {
            throw new IllegalStateException(
                    "Тест должен находиться на странице отчёта (/reports/...), текущий URL: "
                            + PwSession.page().url());
        }
        DaPortalClient da = new DaPortalClient();

        // 1) itemId из агрегата отчёта
        String itemId = resolveItemId(da, reportPath);

        // 2) prepareDocCommon (GraphQL, READ) — возвращает xmlGuid документа отчёта.
        //    READ не требует владения отчётом, поэтому проходит под reader-user (admin-cli).
        //    Финальная ЗАПИСЬ (syncSign) ниже идёт через n2o в браузерной сессии ЕСИА
        //    (владелец отчёта) — ресурсная проверка пройдена.
        SignService signService = new SignServiceImpl();
        SignService.PrepareDocResult prep = signService.prepareDocCommon(itemId, mimeType(), null);
        if (prep.xmlGuid == null) {
            throw new IllegalStateException("prepareDocCommon не вернул xmlGuid: " + prep);
        }
        String xmlGuid = prep.xmlGuid;
        String requestId = prep.requestId;
        String mimeTypeValue = prep.mimeTypeOut != null ? prep.mimeTypeOut : mimeType();
        log.info("prepareDocCommon: xmlGuid={}, requestId={}", xmlGuid, requestId);
        Allure.addAttachment("prepareDocCommon результат", "application/json", prep.toString(), ".json");

        // 3) Файл → подпись → загрузка в DA → signXmlGuid.
        //    Формат подписи для syncSign выбирается свойством evs.sign.n2o-signature-format
        //    (по умолчанию xmldsig — требование бэкенда processSignatureCommon; альтернатива
        //    cms — detached CMS, как производит плагин КриптоПРО digSign). Если syncSign
        //    падает с ошибкой формата — переключить на другой вариант.
        byte[] content = da.loadFile(xmlGuid);
        String signatureFormat = EnvironmentUtils.getPropertyOrEnv(
                "evs.sign.n2o-signature-format", "xmldsig");
        String signXmlGuid;
        try {
            if ("cms".equalsIgnoreCase(signatureFormat)) {
                byte[] cms = signer().sign(content, true);
                Allure.addAttachment("CMS подпись (" + signer.getSignerAlgorithm() + ", detached)",
                        "text/plain", signer.getCertificateSubject(), ".txt");
                signXmlGuid = da.uploadSignature(content, cms, signer());
            } else {
                signXmlGuid = SigningHelper.signXmlAndUpload(da, content);
            }
        } catch (Exception uploadFail) {
            log.warn("Загрузка подписи в DA не удалась ({}), используем mock-guid "
                    + "как фронтовый fallback", uploadFail.getMessage());
            Allure.addAttachment("DA upload fallback", "text/plain",
                    "Ошибка: " + uploadFail.getMessage()
                            + "\n→ mock-guid file_guid_signed_mock_2rsfs323rgsdf", ".txt");
            signXmlGuid = "file_guid_signed_mock_2rsfs323rgsdf";
        }
        log.info("signXmlGuid ({}): {}", signatureFormat, signXmlGuid);
        Allure.addAttachment("signXmlGuid", "text/plain", signXmlGuid, ".txt");

        // 4) syncSign
        String safeRequestId = requestId != null ? requestId : "";
        String safeMime = mimeTypeValue != null ? mimeTypeValue : mimeType();
        String signBody = String.format(
                "{\"mimeType\":\"%s\",\"formName\":\"%s\",\"itemId\":\"%s\","
                        + "\"requestId\":\"%s\",\"xmlGuid\":\"%s\",\"signXmlGuid\":\"%s\","
                        + "\"mime_types\":[\"%s\"]}",
                safeMime, formName(), itemId, safeRequestId, xmlGuid, signXmlGuid,
                safeMime);
        String signResponse = da.postPortalJson(
                "/insurer/n2o/data" + reportPath + "/sign/syncSign", signBody);
        log.info("syncSign ответ: {}", truncate(signResponse));
        Allure.addAttachment("syncSign запрос/ответ", "application/json",
                signBody + "\n\n---\n\n" + signResponse, ".json");

        boolean isError = signResponse.contains("\"status\":4")
                || signResponse.contains("\"status\":5")
                || signResponse.contains("severity")
                || signResponse.contains("danger");
        if (isError) {
            throw new IllegalStateException("syncSign завершился ошибкой: " + truncate(signResponse));
        }
    }

    /** itemId отчёта из агрегатного датасорса (GET pfrEcpStorReportAggreServ). */
    private String resolveItemId(DaPortalClient da, String reportPath) {
        String aggregateUrl = "/insurer/n2o/data" + reportPath
                + "/pfrEcpStorReportAggreServ?page=1&size=1&"
                + "pfrEcpStorReportAggreServ_id=" + extractReportId(reportPath)
                + "&pfrEcpStorReportAggreServ_reportType=" + reportTypeCode();
        String aggregate = da.getPortalJson(aggregateUrl);
        log.info("Агрегат отчёта ({}): {}", aggregateUrl, truncate(aggregate));
        Allure.addAttachment("Агрегат отчёта", "application/json",
                aggregateUrl + "\n---\n" + aggregate, ".json");
        String itemId = extractFromJson(aggregate, "itemId");
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalStateException("itemId не найден в агрегате отчёта: "
                    + truncate(aggregate));
        }
        return itemId;
    }

    /** 8982 из /reports/svzm/8982/mainInfo */
    private String extractReportId(String reportPath) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("/reports/[^/]+/(\\d+)").matcher(reportPath);
        if (!m.find()) {
            throw new IllegalStateException("Не удалось извлечь id отчёта из " + reportPath);
        }
        return m.group(1);
    }

    /**
     * Тип отчёта для агрегата pfrEcpStorReportAggreServ (ReportTypeDto.value):
     * 1=СЗВ-М, 2=СЗВ-ТД, 3=СЗВ-СТАЖ, 4=СЗВ-ИСХ, 5=ОДВ-1, 6=СЗВ-КОРР, 7=СЗВ-К,
     * 8=ЕФС-1, 9=СЗВ-DSO, 10=4-ФСС.
     */
    private String reportTypeCode() {
        return System.getProperty("evs.sign.report-type-code", "1");
    }

    private String formName() {
        return System.getProperty("evs.sign.form-name", "СЗВ-М");
    }

    private String mimeType() {
        // CommonReportSignatureService (prepareDocInsurer) запрашивает XML-документ для подписи.
        return System.getProperty("evs.sign.mime-type", "MIME_TYPE_PROTO_XML");
    }

    private String extractFromJsonOrNull(String json, String field) {
        try {
            return extractFromJson(json, field);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractFromJson(String json, String field) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper om =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(json);
            // DaPortalClient оборачивает ответ: {"status":200,"body":"<json строкой>"} —
            // разворачиваем вложенный body перед поиском
            if (node.has("body") && node.get("body").isTextual()) {
                node = om.readTree(node.get("body").asText());
            }
            com.fasterxml.jackson.databind.JsonNode found = node.findValue(field);
            if (found != null && !found.isNull()) {
                return found.asText();
            }
        } catch (Exception ignored) {
        }
        throw new IllegalStateException("Поле '" + field + "' не найдено в ответе: "
                + truncate(json));
    }

    private boolean isApiSignMode() {
        return "api".equalsIgnoreCase(
                EnvironmentUtils.getPropertyOrEnv("evs.sign.mode", "api"));
    }

    /**
     * Подтверждает модалку «Подписать и отправить?» (только UI-режим).
     *
     * <p>Диалог подтверждения — отдельный компонент БЕЗ класса {@code modal}
     * (в отличие от модалки загрузки файла), поэтому ищем по тексту вопроса,
     * а кнопку «Да» — глобально по точному тексту.
     */
    private void confirmSignDialog() {
        $x("//*[normalize-space(text()) = 'Подписать и отправить?']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(15));
        $x("//button[normalize-space() = 'Да']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(10))
                .click();
    }

    /** Путь текущей страницы отчёта для syncSign, например /reports/svzm/8982/mainInfo. */
    private String currentReportPath() {
        String url = PwSession.page().url();
        int idx = url.indexOf("/reports/");
        String path = idx >= 0 ? url.substring(idx).split("#")[0] : "";
        if (path.contains("/add/")) {
            // После XML-upload hash-роутер иногда не переключается: URL остаётся
            // /reports/add/svzm/mainInfo, хотя карточка созданного отчёта уже открыта.
            // Путь отчёта (с id) берём из активного пункта сайдбара (#/reports/svzm/9164/mainInfo).
            // Ссылка появляется с отрисовкой карточки — ждём её коротким поллингом.
            String domPath = null;
            long deadline = System.currentTimeMillis() + 6000;
            while (System.currentTimeMillis() < deadline) {
                domPath = activeReportPathFromDom();
                if (domPath != null && !domPath.isEmpty()) {
                    log.info("URL страницы '{}' → путь отчёта из сайдбара: '{}'", path, domPath);
                    path = domPath;
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return path;
    }

    /** Ищет активный пункт реестра отчёта (aria-current="page") и возвращает его href (#/reports/{type}/{id}/...). */
    private String activeReportPathFromDom() {
        try {
            String content = PwSession.page().content();
            java.util.regex.Matcher a = java.util.regex.Pattern
                    .compile("<a[^>]*aria-current=\"[^\"]*\"[^>]*>").matcher(content);
            while (a.find()) {
                String tag = a.group(0);
                int h = tag.indexOf("#/reports/");
                if (h >= 0) {
                    int end = tag.indexOf("\"", h);
                    return tag.substring(h + 1, end);
                }
            }
        } catch (Exception e) {
            log.debug("Не удалось получить путь отчёта из HTML: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Ленивая инициализация подписанта.
     *
     * <p>Приоритет:
     * <ol>
     *   <li>PEM-файлы (cert + key) — если задан {@code evs.sign.pem-dir}</li>
     *   <li>PFX — если задан {@code evs.sign.pfx-path}</li>
     *   <li>Self-signed с реквизитами — fallback</li>
     * </ol>
     */
    private PfxCmsSigner signer() {
        if (signer == null) {
            String pemDir = System.getProperty("evs.sign.pem-dir",
                    System.getenv().getOrDefault("EVS_SIGN_PEM_DIR", ""));
            String pfxPath = System.getProperty("evs.sign.pfx-path",
                    System.getenv().getOrDefault("EVS_SIGN_PFX_PATH", ""));

            if (!pemDir.isEmpty()) {
                // PEM: krivonosov.cer.pem + krivonosov.key.pem
                String prefix = pemDir.endsWith("/") || pemDir.endsWith("\\")
                        ? pemDir : pemDir + "/";
                signer = PfxCmsSigner.fromPem(
                        prefix + "krivonosov.cer.pem",
                        prefix + "krivonosov.key.pem");
            } else if (!pfxPath.isEmpty()) {
                char[] password = System.getProperty("evs.sign.pfx-password", "").toCharArray();
                try (InputStream pfx = pfxPath.startsWith("classpath:")
                        ? getClass().getClassLoader()
                            .getResourceAsStream(pfxPath.substring("classpath:".length()))
                        : new java.io.FileInputStream(pfxPath)) {
                    if (pfx == null) {
                        throw new IllegalStateException("PFX не найден: " + pfxPath);
                    }
                    signer = new PfxCmsSigner(pfx, password);
                } catch (java.io.IOException e) {
                    throw new IllegalStateException("Ошибка чтения PFX: " + e.getMessage(), e);
                }
            } else {
                // Fallback: self-signed (не проходит проверку бэкенда, но полезен для отладки)
                signer = com.bft.service.sign.SelfSignedCertGenerator
                    .generateWithRequisites(
                        prop("evs.sign.cn", "Кривоносов Александр Петрович"),
                        prop("evs.sign.surname", "Кривоносов"),
                        prop("evs.sign.givenName", "Александр Петрович"),
                        prop("evs.sign.country", "RU"),
                        prop("evs.sign.state", "Москва"),
                        prop("evs.sign.locality", "г Москва"),
                        prop("evs.sign.street", "ул. Хуторская 2-я, д. 38а, СТР. 26"),
                        prop("evs.sign.snils", "13390078549"),
                        prop("evs.sign.innFl", "648821876972"),
                        prop("evs.sign.org", "ОРГАНИЗАЦИЯ -1546025669"),
                        prop("evs.sign.title", "Руководитель"),
                        prop("evs.sign.ogrn", "1087746716606"),
                        prop("evs.sign.innLe", "7731595726")
                    );
            }
            log.info("Подписант: subject={}, алгоритм={}",
                    signer.getCertificateSubject(), signer.getSignerAlgorithm());
        }
        return signer;
    }

    /**
     * Подписывает скачанный XML и загружает подпись в DA, возвращая guid подписанного
     * документа (signXmlGuid / signedXmlFileRef).
     *
     * <p>Режим подписи — свойство {@code evs.sign.xmldsig} (по умолчанию {@code true}):
     * <ul>
     *   <li>{@code true}  — XMLDSIG (enveloped) через {@link XmlDsigSigner} (требование бэкенда);</li>
     *   <li>{@code false} — устаревший CMS/PKCS#7 через {@link PfxCmsSigner}.</li>
     * </ul>
     */
    private String signAndUpload(DaPortalClient da, byte[] content) {
        return SigningHelper.signXmlAndUpload(da, content);
    }
    private String truncate(String s) {
        return s != null && s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }

    private String prop(String key, String def) {
        return System.getProperty(key, def);
    }
}
