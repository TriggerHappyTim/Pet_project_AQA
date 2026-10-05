package com.bft.service.graphql;

/**
 * Интерфейс операций подписания документов ЕВС.
 *
 * <p>Реализация {@link SignServiceImpl} выполняет реальное подписание через
 * сервисы бэкенда (CommonReportSignatureService, PrintingReportSignatureService,
 * ArchivalResponseSignatureService): prepare → подпись (XMLDSIG) → загрузка в DA →
 * process. UI-подписание (через плагин КриптоПРО) остаётся отдельным сценарием.
 */
public interface SignService {

    /**
     * Завершение подписания архивного ответа (ArchivalResponseSignatureService).
     *
     * <p>Передаётся guid УЖЕ подписанного файла, загруженного в DA
     * ({@code signedXmlFileRef}).
     *
     * @param itemId        id архивного ответа
     * @param signXmlGuid   fileGuid подписанного файла (signedXmlFileRef)
     * @param requestId     requestId из initPrepareDoc (перехвачен из /syncPrepare)
     * @return true, если бэкенд вернул result без ошибок
     */
    boolean processSignatureArchive(String itemId, String signXmlGuid, String requestId);

    /**
     * Подготовка отчёта к подписанию (ЛК Страхователя, путь без печати).
     *
     * <p>Соответствует операции {@code prepareDocCommon} объекта signDoc.
     * Возвращает requestId и список документов; для подписи берётся XML-документ.
     *
     * @param itemId   id отчёта
     * @param mimeType enum без кавычек, напр. MIME_TYPE_PROTO_PDF
     * @param formName имя формы, напр. СЗВ-М
     * @return результат подготовки (requestId + xmlGuid)
     */
    PrepareDocResult prepareDocCommon(String itemId, String mimeType, String formName);

    /**
     * Завершение подписания отчёта (ЛК Страхователя, путь без печати).
     *
     * <p>Соответствует операции {@code processSignatureCommon} объекта signDoc.
     *
     * @param itemId        id отчёта
     * @param signXmlGuid   fileGuid подписанного файла (signedXmlFileRef)
     * @param mimeType      enum без кавычек
     * @param requestId     requestId из {@link #prepareDocCommon}
     * @return true, если бэкенд вернул result без ошибок
     */
    boolean processSignatureCommon(String itemId, String signXmlGuid,
                                  String mimeType, String requestId);

    /** Результат {@link #prepareDocCommon}: id запроса и документы для подписи. */
    class PrepareDocResult {
        public final String requestId;
        public final String xmlGuid;
        public final String fileName;
        public final String mimeTypeOut;

        public PrepareDocResult(String requestId, String xmlGuid,
                               String fileName, String mimeTypeOut) {
            this.requestId = requestId;
            this.xmlGuid = xmlGuid;
            this.fileName = fileName;
            this.mimeTypeOut = mimeTypeOut;
        }

        @Override
        public String toString() {
            return "PrepareDocResult{requestId=" + requestId
                    + ", xmlGuid=" + xmlGuid
                    + ", fileName=" + fileName
                    + ", mimeTypeOut=" + mimeTypeOut + "}";
        }
    }
}
