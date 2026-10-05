package com.bft.service.graphql;

import com.bft.service.sign.DaPortalClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реализация {@link SignService}: реальное подписание документов ЕВС через
 * сервисы бэкенда (CommonReportSignatureService, PrintingReportSignatureService,
 * ArchivalResponseSignatureService).
 *
 * <p>Поток: prepare (GraphQL) → скачивание XML из DA → подпись (XMLDSIG,
 * см. {@code SigningHelper}) → загрузка подписанного файла в DA → process
 * (GraphQL, передача signedXmlFileRef). КриптоПРО-плагин не требуется.
 */
public class SignServiceImpl implements SignService {

    private static final Logger log = LoggerFactory.getLogger(SignServiceImpl.class);

    private final GraphQLClient graphQLClient;
    private final SignRequest signRequest;

    /** Когда mesh :20266 недоступен напрямую, GraphQL шлётся same-origin через браузерную сессию. */
    private final boolean graphqlViaBrowser =
            "true".equalsIgnoreCase(System.getProperty("evs.sign.graphql-via-browser", "false"));
    private DaPortalClient daClient;
    private static boolean meshAuthed = false;

    public SignServiceImpl() {
        this(new GraphQLClient());
    }

    public SignServiceImpl(GraphQLClient graphQLClient) {
        this.graphQLClient = graphQLClient;
        this.signRequest = new SignRequest();
    }

    /**
     * Маршрутизация GraphQL: напрямую в mesh (default) либо через JVM с SSO-cookie из
     * браузерной сессии (когда {@code evs.sign.graphql-via-browser=true} — обходит
     * отсутствие port-forward mesh в этой песочнице; mesh-URL берётся из
     * {@code evs.sign.graphql-via-browser-url}, по умолчанию тот же mesh).
     */
    private String executeGraphQL(String query) {
        // admin-cli-токен живёт ~30с; берём свежий перед каждым GraphQL-вызовом,
        // чтобы processSignatureCommon не уходил с просроченным токеном (UNAUTHENTICATED).
        try {
            KeycloakTokenProvider.invalidate();
        } catch (Exception ignore) {
        }
        if (graphqlViaBrowser) {
            if (daClient == null) {
                daClient = new DaPortalClient();
            }
            String basic = System.getProperty("evs.sign.graphql-basic");
            String meshUrl = System.getProperty("evs.sign.graphql-via-browser-url", GraphQLClient.resolveMeshUrl());
            if (basic != null && !basic.isEmpty() && basic.contains(":") && !meshAuthed) {
                daClient.authenticateToMesh(meshUrl.replaceAll("/graphql.*$", ""),
                        basic.substring(0, basic.indexOf(':')),
                        basic.substring(basic.indexOf(':') + 1));
                meshAuthed = true;
            }
            return daClient.executeGraphQL(query);
        }
        return graphQLClient.executeMutation(query);
    }

    // ===================== Отчёты ЛК Страхователя (signDoc.object.xml) =====================

    private static final String PREPARE_OP =
            "ruGovPfrEcpEvsDaIpuService_CommonReportSignatureService_GetPrepareDocRouter";
    private static final String PROCESS_OP =
            "ruGovPfrEcpEvsDaIpuService_CommonReportSignatureService_GetSignatureDoneRouter";
    private static final String PREPARE_OP_PRINTING =
            "ruGovPfrEcpEvsDaIpuService_PrintingReportSignatureService_GetPrepareDocRouter";
    private static final String PROCESS_OP_PRINTING =
            "ruGovPfrEcpEvsDaIpuService_PrintingReportSignatureService_GetSignatureDoneRouter";
    private static final String PROCESS_OP_ARCHIVE =
            "ruGovPfrEcpEvsDsArchiveInteractionService_ArchivalResponseSignatureService_GetSignatureDoneRouter";

    /** common (GitLab, prepareDocCommon/processSignatureCommon) либо printing (initPrepareDoc+checkPrepareDoc+processSignatureDone). */
    private final boolean printingVariant =
            "printing".equalsIgnoreCase(System.getProperty("evs.sign.variant", "common"));
    private static final int PREPARE_POLL_MAX = 30;
    private static final long PREPARE_POLL_DELAY_MS = 1000;

    private final ObjectMapper json = new ObjectMapper();

    @Override
    @Step("Prepare document (GraphQL prepareDocCommon): itemId={itemId}, form={formName}")
    public PrepareDocResult prepareDocCommon(String itemId, String mimeType, String formName) {
        if (printingVariant) {
            return prepareDocPrinting(itemId);
        }
        String query = signRequest.buildPrepareDocCommon(itemId, mimeType, formName);
        log.debug("prepareDocCommon query: {}", query);

        String response = executeGraphQL(query); // это query, но шлём POST /graphql
        JsonNode root = parseOrThrow(response, PREPARE_OP);
        JsonNode op = root.path("data").path(PREPARE_OP);

        String requestId = op.path("requestId").path("value").asText(null);
        String xmlGuid = null;
        String fileName = null;
        String mimeTypeOut = null;
        JsonNode documents = op.path("documents");
        if (documents.isArray()) {
            for (JsonNode doc : documents) {
                String mt = doc.path("mimeType").path("value").asText(null);
                String id = doc.path("id").path("value").asText(null);
                if (id == null) continue;
                // Приоритет — XML-документ (его и подписываем); иначе берём первый.
                if ("XML".equalsIgnoreCase(mt)) {
                    xmlGuid = id;
                    fileName = doc.path("fileName").path("value").asText(null);
                    mimeTypeOut = mt;
                    break;
                }
                if (xmlGuid == null) {
                    xmlGuid = id;
                    fileName = doc.path("fileName").path("value").asText(null);
                    mimeTypeOut = mt;
                }
            }
        }
        log.info("prepareDocCommon: requestId={}, xmlGuid={}, fileName={}", requestId, xmlGuid, fileName);
        return new PrepareDocResult(requestId, xmlGuid, fileName, mimeTypeOut);
    }

    @Override
    @Step("Process signature (GraphQL processSignatureCommon): itemId={itemId}")
    public boolean processSignatureCommon(String itemId, String signXmlGuid,
                                         String mimeType, String requestId) {
        if (printingVariant) {
            return processSignaturePrinting(itemId, signXmlGuid, requestId);
        }
        String query = signRequest.buildProcessSignatureCommon(itemId, signXmlGuid, mimeType, requestId);
        log.debug("processSignatureCommon query: {}", query);

        String response = executeGraphQL(query); // query, но POST /graphql
        JsonNode root = parseOrThrow(response, PROCESS_OP);
        JsonNode op = root.path("data").path(PROCESS_OP);

        String result = op.path("result").path("value").asText(null);
        String statusMessage = op.path("statusMessage").path("value").asText(null);
        log.info("processSignatureCommon: result={}, statusMessage={}", result, statusMessage);

        boolean ok = result != null && !result.isBlank() && !"Error".equalsIgnoreCase(result);
        if (!ok) {
            log.error("processSignatureCommon вернул ошибку: result={}, statusMessage={}",
                    result, statusMessage);
        }
        return ok;
    }

    @Override
    @Step("Process signature (GraphQL processSignatureArchive, архив): itemId={itemId}")
    public boolean processSignatureArchive(String itemId, String signXmlGuid, String requestId) {
        String query = signRequest.buildProcessSignatureArchive(itemId, requestId, signXmlGuid);
        log.debug("processSignatureArchive query: {}", query);

        String response = executeGraphQL(query); // query, но POST /graphql
        JsonNode root = parseOrThrow(response, PROCESS_OP_ARCHIVE);
        JsonNode op = root.path("data").path(PROCESS_OP_ARCHIVE);

        String result = op.path("result").path("value").asText(null);
        String statusMessage = op.path("statusMessage").path("value").asText(null);
        log.info("processSignatureArchive: result={}, statusMessage={}", result, statusMessage);

        boolean ok = result != null && !result.isBlank() && !"Error".equalsIgnoreCase(result);
        if (!ok) {
            log.error("processSignatureArchive вернул ошибку: result={}, statusMessage={}",
                    result, statusMessage);
        }
        return ok;
    }

    // ----- Печатная/локальная вариация (initPrepareDoc + checkPrepareDoc + processSignatureDone) -----

    @Step("Prepare document (printing variant: initPrepareDoc + poll checkPrepareDoc): itemId={itemId}")
    private PrepareDocResult prepareDocPrinting(String itemId) {
        String initQuery = signRequest.buildPrepareDocPrintingInit(itemId);
        String initResp = executeGraphQL(initQuery);
        JsonNode initRoot = parseOrThrow(initResp, PREPARE_OP_PRINTING);
        String requestId = initRoot.path("data").path(PREPARE_OP_PRINTING)
                .path("requestId").path("value").asText(null);
        log.info("prepareDocPrinting init: requestId={}", requestId);

        String xmlGuid = null, fileName = null, mimeTypeOut = null;
        for (int attempt = 1; attempt <= PREPARE_POLL_MAX; attempt++) {
            String checkQuery = signRequest.buildPrepareDocPrintingCheck(requestId);
            String checkResp = executeGraphQL(checkQuery);
            JsonNode checkRoot = parseOrThrow(checkResp, PREPARE_OP_PRINTING);
            JsonNode op = checkRoot.path("data").path(PREPARE_OP_PRINTING);
            String status = op.path("status").path("value").asText(null);
            log.info("prepareDocPrinting poll {}/{}: status={}", attempt, PREPARE_POLL_MAX, status);

            JsonNode documents = op.path("documents");
            if (documents.isArray() && documents.size() > 0) {
                PrepareDocResult r = extractXmlDoc(documents);
                xmlGuid = r.xmlGuid; fileName = r.fileName; mimeTypeOut = r.mimeTypeOut;
                break;
            }
            if ("Error".equalsIgnoreCase(status)) {
                throw new IllegalStateException("prepareDocPrinting: статус Error при опросе, requestId=" + requestId);
            }
            try {
                Thread.sleep(PREPARE_POLL_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Прервано ожидание prepareDocPrinting", e);
            }
        }
        if (xmlGuid == null) {
            throw new IllegalStateException("prepareDocPrinting: документы не появились после "
                    + PREPARE_POLL_MAX + " попыток, requestId=" + requestId);
        }
        return new PrepareDocResult(requestId, xmlGuid, fileName, mimeTypeOut);
    }

    @Step("Process signature (printing variant: processSignatureDone): itemId={itemId}")
    private boolean processSignaturePrinting(String itemId, String signXmlGuid, String requestId) {
        String query = signRequest.buildProcessSignaturePrinting(itemId, requestId, signXmlGuid);
        log.debug("processSignaturePrinting query: {}", query);

        String response = executeGraphQL(query);
        JsonNode root = parseOrThrow(response, PROCESS_OP_PRINTING);
        JsonNode op = root.path("data").path(PROCESS_OP_PRINTING);

        String result = op.path("result").path("value").asText(null);
        String statusMessage = op.path("statusMessage").path("value").asText(null);
        log.info("processSignaturePrinting: result={}, statusMessage={}", result, statusMessage);

        boolean ok = result != null && !result.isBlank() && !"Error".equalsIgnoreCase(result);
        if (!ok) {
            log.error("processSignaturePrinting вернул ошибку: result={}, statusMessage={}",
                    result, statusMessage);
        }
        return ok;
    }

    private PrepareDocResult extractXmlDoc(JsonNode documents) {
        String xmlGuid = null, fileName = null, mimeTypeOut = null;
        for (JsonNode doc : documents) {
            String mt = doc.path("mimeType").path("value").asText(null);
            String id = doc.path("id").path("value").asText(null);
            if (id == null) continue;
            if ("XML".equalsIgnoreCase(mt)) {
                xmlGuid = id;
                fileName = doc.path("fileName").path("value").asText(null);
                mimeTypeOut = mt;
                break;
            }
            if (xmlGuid == null) {
                xmlGuid = id;
                fileName = doc.path("fileName").path("value").asText(null);
                mimeTypeOut = mt;
            }
        }
        return new PrepareDocResult(null, xmlGuid, fileName, mimeTypeOut);
    }

    private JsonNode parseOrThrow(String response, String operation) {
        try {
            JsonNode root = json.readTree(response);
            if (root.has("errors") && root.get("errors").isArray() && root.get("errors").size() > 0) {
                throw new IllegalStateException("GraphQL errors в ответе " + operation + ": "
                        + root.get("errors").toString());
            }
            if (!root.path("data").has(operation)) {
                throw new IllegalStateException("В ответе нет поля data." + operation
                        + ". Ответ: " + truncate(response, 500));
            }
            return root;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось распарсить ответ GraphQL ("
                    + operation + "): " + truncate(response, 500), e);
        }
    }

    private String truncate(String s, int max) {
        return s == null ? "null" : (s.length() <= max ? s : s.substring(0, max) + "...");
    }
}
