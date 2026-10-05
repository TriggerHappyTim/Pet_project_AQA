package com.bft.service.sign;

import com.bft.config.EnvironmentUtils;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Общий помощник подписания: скачивает/принимает документ, подписывает его
 * (XMLDSIG enveloped либо устаревший CMS/PKCS#7) и загружает подпись в DA,
 * возвращая guid подписанного документа (signXmlGuid / signedXmlFileRef).
 *
 * <p>Выделен из {@code ReportSignSteps}, чтобы переиспользоваться и в
 * {@code ArchiveSteps} (подписание архивных ответов через
 * ArchivalResponseSignatureService).
 *
 * <p>Режим подписи — свойство {@code evs.sign.xmldsig} (по умолчанию {@code true}):
 * <ul>
 *   <li>{@code true}  — XMLDSIG (enveloped) через {@link XmlDsigSigner};</li>
 *   <li>{@code false} — устаревший CMS/PKCS#7 через {@link PfxCmsSigner}.</li>
 * </ul>
 */
public final class SigningHelper {

    private static final Logger log = LoggerFactory.getLogger(SigningHelper.class);

    private SigningHelper() {
    }

    /**
     * Подписание через СЭДО-gateway (серверная CMS-подпись cert-manager):
     * СЭДО подписывает файл, извлекаем сертификат из CMS и загружаем в DA
     * тем же контрактом, что и локальный CMS (см. {@link DaPortalClient#uploadSedoSignature}).
     *
     * <p>Используется, когда {@code evs.sign.transport=sedo}. EVS prepare/process
     * (GraphQL) остаются без изменений — заменяется только шаг подписи+загрузки.
     */
    public static String signXmlAndUploadViaSedo(DaPortalClient da, byte[] content) {
        com.bft.service.sedo.SedoGatewayClient sedo = new com.bft.service.sedo.SedoGatewayClient();
        Allure.addAttachment("СЭДО: baseUrl", "text/plain", sedo.baseUrl(), ".txt");
        byte[] cms = sedo.signFile(content, "report.xml");
        Allure.addAttachment("СЭДО: CMS-подпись (cert-manager)", "application/pkcs7-signature",
                new java.io.ByteArrayInputStream(cms), ".sig");
        try {
            boolean valid = sedo.isSignatureValid(content, "report.xml");
            Allure.addAttachment("СЭДО: проверка подписи", "text/plain",
                    "valid=" + valid, ".txt");
            if (!valid) {
                log.warn("СЭДО: проверка подписи вернула valid=false (продолжаем загрузку в DA)");
            }
        } catch (Exception ve) {
            log.warn("СЭДО: проверку подписи не удалось выполнить: {}", ve.getMessage());
        }
        return da.uploadSedoSignature(cms);
    }

    public static String signXmlAndUpload(DaPortalClient da, byte[] content) {
        if (isSedoTransport()) {
            return signXmlAndUploadViaSedo(da, content);
        }
        if (isXmlDsigMode()) {
            XmlDsigSigner xs = xmlDsigSigner();
            byte[] signedXml = xs.signEnveloped(content);
            Allure.addAttachment("XMLDSIG подпись (" + xs.getSignerAlgorithm() + ", enveloped)",
                    "text/xml", new ByteArrayInputStream(signedXml), ".xml");
            // Намеренно БЕЗ mock-fallback: при ошибке загрузки в DA выбрасываем,
            // чтобы на прогоне был виден реальный ответ (контракт загрузки xmldsig
            // ещё не подтверждён бэкендом — см. docs/plan-evs-signing.md).
            return da.uploadXmlSignature(signedXml, xs);
        } else {
            PfxCmsSigner cms = cmsSigner();
            byte[] sig = cms.sign(content, true);
            Allure.addAttachment("CMS подпись (" + cms.getSignerAlgorithm() + ", detached)",
                    "text/plain", cms.getCertificateSubject(), ".txt");
            try {
                return da.uploadSignature(content, sig, cms);
            } catch (Exception uploadFail) {
                return uploadFallback(uploadFail, "CMS");
            }
        }
    }

    private static boolean isSedoTransport() {
        return "sedo".equalsIgnoreCase(
                EnvironmentUtils.getPropertyOrEnv("evs.sign.transport", "graphql"));
    }

    private static boolean isXmlDsigMode() {
        return !"false".equalsIgnoreCase(
                EnvironmentUtils.getPropertyOrEnv("evs.sign.xmldsig", "true"));
    }

    private static XmlDsigSigner xmlDsigSigner() {
        String pemDir = EnvironmentUtils.getPropertyOrEnv("evs.sign.pem-dir", "");
        String pfxPath = EnvironmentUtils.getPropertyOrEnv("evs.sign.pfx-path", "");
        if (!pemDir.isEmpty()) {
            String prefix = pemDir.endsWith("/") || pemDir.endsWith("\\") ? pemDir : pemDir + "/";
            return XmlDsigSigner.fromPem(
                    prefix + "krivonosov.cer.pem", prefix + "krivonosov.key.pem");
        } else if (!pfxPath.isEmpty()) {
            char[] password = EnvironmentUtils.getPropertyOrEnv("evs.sign.pfx-password", "").toCharArray();
            try (InputStream pfx = pfxPath.startsWith("classpath:")
                    ? SigningHelper.class.getClassLoader()
                        .getResourceAsStream(pfxPath.substring("classpath:".length()))
                    : new FileInputStream(pfxPath)) {
                if (pfx == null) {
                    throw new IllegalStateException("PFX не найден: " + pfxPath);
                }
                return new XmlDsigSigner(pfx, password);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Ошибка чтения PFX: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException(
                    "Для xmldsig нужен PEM (evs.sign.pem-dir) или PFX (evs.sign.pfx-path). "
                            + "Self-signed для xmldsig не реализован — задайте реквизиты ЕВС.");
        }
    }

    private static PfxCmsSigner cmsSigner() {
        String pemDir = EnvironmentUtils.getPropertyOrEnv("evs.sign.pem-dir", "");
        String pfxPath = EnvironmentUtils.getPropertyOrEnv("evs.sign.pfx-path", "");
        if (!pemDir.isEmpty()) {
            String prefix = pemDir.endsWith("/") || pemDir.endsWith("\\") ? pemDir : pemDir + "/";
            return PfxCmsSigner.fromPem(
                    prefix + "krivonosov.cer.pem", prefix + "krivonosov.key.pem");
        } else if (!pfxPath.isEmpty()) {
            char[] password = EnvironmentUtils.getPropertyOrEnv("evs.sign.pfx-password", "").toCharArray();
            try (InputStream pfx = pfxPath.startsWith("classpath:")
                    ? SigningHelper.class.getClassLoader()
                        .getResourceAsStream(pfxPath.substring("classpath:".length()))
                    : new FileInputStream(pfxPath)) {
                if (pfx == null) {
                    throw new IllegalStateException("PFX не найден: " + pfxPath);
                }
                return new PfxCmsSigner(pfx, password);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Ошибка чтения PFX: " + e.getMessage(), e);
            }
        } else {
            // Fallback: self-signed (не проходит проверку бэкенда, но полезен для отладки)
            return SelfSignedCertGenerator.generateWithRequisites(
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.cn", "Кривоносов Александр Петрович"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.surname", "Кривоносов"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.givenName", "Александр Петрович"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.country", "RU"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.state", "Москва"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.locality", "г Москва"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.street", "ул. Хуторская 2-я, д. 38а, СТР. 26"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.snils", "13390078549"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.innFl", "648821876972"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.org", "ОРГАНИЗАЦИЯ -1546025669"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.title", "Руководитель"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.ogrn", "1087746716606"),
                    EnvironmentUtils.getPropertyOrEnv("evs.sign.innLe", "7731595726")
            );
        }
    }

    private static String uploadFallback(Exception uploadFail, String kind) {
        log.warn("Загрузка подписи ({}) в DA не удалась ({}), используем mock-guid "
                + "как фронтовый fallback", kind, uploadFail.getMessage());
        Allure.addAttachment("DA upload fallback", "text/plain",
                "Ошибка: " + uploadFail.getMessage()
                        + "\n→ mock-guid file_guid_signed_mock_2rsfs323rgsdf", ".txt");
        return "file_guid_signed_mock_2rsfs323rgsdf";
    }
}
