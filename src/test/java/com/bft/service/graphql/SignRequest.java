package com.bft.service.graphql;

/**
 * Строит GraphQL-запросы для подписания документов ЕВС (ЛК Страхователя, архивные
 * ответы и пр.), используя сервисы подписи бэкенда (CommonReportSignatureService,
 * PrintingReportSignatureService, ArchivalResponseSignatureService).
 *
 * <p>Формат подписи — XMLDSIG (enveloped), требование бэкенда. Подписанный файл
 * загружается в DA, а полученный guid передаётся в операцию {@code *GetSignatureDoneRouter}
 * как {@code signedXmlFileRef}.
 */
public class SignRequest {

    // ===================== Архивные ответы (downloadRequest.object.xml) =====================
    // Источник: evs-ov-archive-interaction-front .../requestRegisters/downloadRequest.object.xml
    // Сервис: ruGovPfrEcpEvsDsArchiveInteractionService_ArchivalResponseSignatureService.
    // prepare (initPrepareDoc + checkPrepareDoc) делает фронт; тест завершает подпись
    // операцией processSignatureDone. formType захардкожен во фронте архива = "1608".

    private static final String PROCESS_SIGNATURE_ARCHIVE_TEMPLATE =
            "query GetSignatureDoneArchive {" +
            "  ruGovPfrEcpEvsDsArchiveInteractionService_ArchivalResponseSignatureService_GetSignatureDoneRouter(" +
            "    input: {" +
            "      itemId: {value: \"%s\"}" +
            "      requestId: {value: \"%s\"}" +
            "      signedXmlFileRef: {value: \"%s\"}" +
            "      formType: {value: \"1608\"}" +
            "      mimeTypes: [MIME_TYPE_PROTO_PDF]" +
            "    }" +
            "  ) { result {value} statusMessage {value} }" +
            "}";

    /**
     * Завершение подписания архивного ответа (ArchivalResponseSignatureService).
     *
     * <p>Передаётся ссылка на УЖЕ подписанный файл ({@code signedXmlFileRef}).
     * Подготовка (initPrepareDoc) выполняется фронтом при клике «Подписать»,
     * test получает requestId/xmlGuid через перехват {@code /syncPrepare}.
     *
     * @param itemId        id архивного ответа
     * @param requestId     requestId из initPrepareDoc (перехвачен из /syncPrepare)
     * @param signXmlGuid   fileGuid подписанного файла (signedXmlFileRef)
     * @return GraphQL-запрос
     */
    public String buildProcessSignatureArchive(String itemId, String requestId, String signXmlGuid) {
        return String.format(PROCESS_SIGNATURE_ARCHIVE_TEMPLATE, itemId, requestId, signXmlGuid);
    }

    // ===================== Отчёты ЛК Страхователя (signDoc.object.xml) =====================
    // Источник: evs-oi-ipu-usv-front .../objects/signDoc.object.xml
    // Операции — GraphQL-запросы на сервисе ruGovPfrEcpEvsDaIpuService.
    // ВАЖНО: mimeTypes — ENUM, передаётся БЕЗ кавычек: [MIME_TYPE_PROTO_PDF].

    // prepareDocInsurer (CommonReportSignatureService): вход itemId + mimeTypes (XML); formName НЕ передаётся
    // (тип отчёта определяется бэкендом по itemId). formName добавляется только если задан явно.

    private static final String PROCESS_SIGNATURE_COMMON_TEMPLATE =
            "query ProcessSignatureCommon {" +
            "  ruGovPfrEcpEvsDaIpuService_CommonReportSignatureService_GetSignatureDoneRouter(" +
            "    input: {" +
            "      itemId: {value: \"%s\"}" +
            "      signedXmlFileRef: {value: \"%s\"}" +
            "      mimeTypes: [%s]" +
            "      requestId: {value: \"%s\"}" +
            "    }" +
            "  ) {" +
            "    result {value}" +
            "    statusMessage {value}" +
            "  }" +
            "}";

    /**
     * Подготовка документа к подписанию (CommonReportSignatureService, путь страхователя).
     *
     * <p>По контракту {@code signDoc.object.xml} (операция {@code prepareDocInsurer}) входные
     * поля — {@code itemId} и {@code mimeTypes}; {@code formName} НЕ используется (тип отчёта
     * определяется бэкендом по itemId). Поле {@code formName} добавляется в запрос только если
     * задано явно (для совместимости с иными версиями схемы).
     *
     * @param itemId   id отчёта (из агрегата, строкой)
     * @param mimeType enum без кавычек, напр. {@code MIME_TYPE_PROTO_XML}
     * @param formName имя формы; если null/пусто — не передаётся
     * @return GraphQL-запрос
     */
    public String buildPrepareDocCommon(String itemId, String mimeType, String formName) {
        StringBuilder q = new StringBuilder();
        q.append("query PrepareDocCommon {");
        q.append(" ruGovPfrEcpEvsDaIpuService_CommonReportSignatureService_GetPrepareDocRouter(");
        q.append(" input: {");
        q.append(" itemId: {value: \"").append(itemId).append("\"}");
        q.append(" mimeTypes: [").append(mimeType).append("]");
        if (formName != null && !formName.isBlank()) {
            q.append(" formName: {value: \"").append(formName).append("\"}");
        }
        q.append(" }");
        q.append(" ) { requestId {value} documents { id {value} mimeType {value} fileName {value} } }");
        q.append("}");
        return q.toString();
    }

    /**
     * Завершение подписания (без печати): передаётся ссылка на УЖЕ подписанный файл.
     *
     * @param itemId        id отчёта
     * @param signXmlGuid   fileGuid подписанного файла (signedXmlFileRef)
     * @param mimeType      enum без кавычек
     * @param requestId     requestId из {@link #buildPrepareDocCommon}
     * @return GraphQL-запрос
     */
    public String buildProcessSignatureCommon(String itemId, String signXmlGuid,
                                              String mimeType, String requestId) {
        return String.format(PROCESS_SIGNATURE_COMMON_TEMPLATE,
                itemId, signXmlGuid, mimeType, requestId);
    }

    // ===================== Локальная/печатная вариация signDoc.object.xml =====================
    // Совпадает с тем, что прописано в page.xml СЗВ-М (initPrepareDoc + checkPrepareDoc + processSignatureDone,
    // сервис PrintingReportSignatureService). Prepare здесь АСИНХРОННЫЙ: init возвращает requestId+status,
    // затем опрос checkPrepareDoc, пока не появятся documents.

    private static final String PREPARE_DOC_PRINTING_INIT_TEMPLATE =
            "query GetPrepareDocRouterInit {" +
            "  ruGovPfrEcpEvsDaIpuService_PrintingReportSignatureService_GetPrepareDocRouter(" +
            "    input: { itemId: {value: \"%s\"}, mimeTypes: [MIME_TYPE_PROTO_PDF] }" +
            "  ) { requestId {value} status {value} }" +
            "}";

    private static final String PREPARE_DOC_PRINTING_CHECK_TEMPLATE =
            "query GetPrepareDocRouterPoll {" +
            "  ruGovPfrEcpEvsDaIpuService_PrintingReportSignatureService_GetPrepareDocRouter(" +
            "    input: { requestId: {value: \"%s\"} }" +
            "  ) { requestId {value} status {value} documents { mimeType {value} id {value} } }" +
            "}";

    private static final String PROCESS_SIGNATURE_PRINTING_TEMPLATE =
            "query GetSignatureDone {" +
            "  ruGovPfrEcpEvsDaIpuService_PrintingReportSignatureService_GetSignatureDoneRouter(" +
            "    input: {" +
            "      itemId: {value: \"%s\"}," +
            "      requestId: {value: \"%s\"}," +
            "      mimeTypes: [MIME_TYPE_PROTO_PDF]," +
            "      signedXmlFileRef: {value: \"%s\"}" +
            "    }" +
            "  ) { result {value} statusMessage {value} }" +
            "}";

    public String buildPrepareDocPrintingInit(String itemId) {
        return String.format(PREPARE_DOC_PRINTING_INIT_TEMPLATE, itemId);
    }

    public String buildPrepareDocPrintingCheck(String requestId) {
        return String.format(PREPARE_DOC_PRINTING_CHECK_TEMPLATE, requestId);
    }

    public String buildProcessSignaturePrinting(String itemId, String requestId, String signXmlGuid) {
        return String.format(PROCESS_SIGNATURE_PRINTING_TEMPLATE, itemId, requestId, signXmlGuid);
    }
}
