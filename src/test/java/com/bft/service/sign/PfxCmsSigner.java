package com.bft.service.sign;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Создание attached CMS/PKCS#7 подписи файла PFX-сертификатом.
 *
 * <p>Заменяет связку «КриптоПРО-плагин в браузере»: тест сам подписывает документ
 * и загружает подпись в Document API (см. {@link DaPortalClient}).
 *
 * <p>Поддерживает RSA/ECDSA (стандартный JCA) и ГОСТ Р 34.10-2012 (через
 * BouncyCastle provider) — определяется по алгоритму ключа сертификата.
 */
public class PfxCmsSigner {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final Collection<X509Certificate> chain;
    private final String signerAlg;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PfxCmsSigner.class);

    private char[] keyPassword;

    /** Создание из готовой пары ключ+сертификат (например, self-signed для тестов). */
    public static PfxCmsSigner of(KeyPair keyPair, X509Certificate certificate) {
        PfxCmsSigner signer = new PfxCmsSigner(keyPair.getPrivate(), certificate);
        return signer;
    }

    /**
     * Загружает подписанта из PEM-файлов сертификата и приватного ключа.
     *
     * <p>Это предпочтительный способ: PEM-файлы не имеют ГОСТ-шифрования PFX,
     * BC читает их напрямую.
     *
     * @param certPemPath путь к .cer.pem (classpath: или файловая система)
     * @param keyPemPath  путь к .key.pem (PKCS#8, PEM)
     */
    public static PfxCmsSigner fromPem(String certPemPath, String keyPemPath) {
        try {
            byte[] certBytes = readResource(certPemPath);
            byte[] keyBytes = readResource(keyPemPath);

            // Парсим сертификат через CertificateFactory (стандартный JCE)
            java.security.cert.CertificateFactory cf =
                    java.security.cert.CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(certBytes));

            // Парсим приватный ключ через BC PEMParser
            org.bouncycastle.openssl.PEMParser pemParser = new org.bouncycastle.openssl.PEMParser(
                    new java.io.InputStreamReader(
                            new java.io.ByteArrayInputStream(keyBytes),
                            java.nio.charset.StandardCharsets.UTF_8));
            Object pemObject = pemParser.readObject();
            pemParser.close();

            org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter converter =
                    new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME);

            java.security.PrivateKey privateKey;
            if (pemObject instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                privateKey = converter.getPrivateKey(
                        (org.bouncycastle.asn1.pkcs.PrivateKeyInfo) pemObject);
            } else if (pemObject instanceof org.bouncycastle.openssl.PEMKeyPair) {
                privateKey = converter.getKeyPair(
                        (org.bouncycastle.openssl.PEMKeyPair) pemObject).getPrivate();
            } else {
                throw new IllegalStateException("Неизвестный тип ключа: "
                        + (pemObject == null ? "null" : pemObject.getClass().getName()));
            }

            return new PfxCmsSigner(privateKey, certificate);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Не удалось загрузить PEM-сертификат/ключ: " + e.getMessage(), e);
        }
    }

    /** Убирает PEM-заголовки/футеры и декодирует base64. */
    private static byte[] stripPemHeaders(byte[] pemBytes) {
        String text = new String(pemBytes, java.nio.charset.StandardCharsets.UTF_8);
        String base64 = text.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("[\\r\\n\\s]", "");
        return java.util.Base64.getDecoder().decode(base64);
    }

    private static byte[] readResource(String path) throws Exception {
        InputStream is;
        if (path.startsWith("classpath:")) {
            is = PfxCmsSigner.class.getClassLoader()
                    .getResourceAsStream(path.substring("classpath:".length()));
        } else {
            is = new java.io.FileInputStream(path);
        }
        if (is == null) {
            throw new IllegalStateException("Файл не найден: " + path);
        }
        try (is) {
            return is.readAllBytes();
        }
    }

    private PfxCmsSigner(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.chain = java.util.List.of(certificate);
        this.signerAlg = detectAlgorithm(certificate);
        log.info("Подписант готов: subject={}, алгоритм={}",
                certificate.getSubjectX500Principal().getName(), signerAlg);
    }

    public PfxCmsSigner(InputStream pfxStream, char[] password) {
        try {
            final byte[] pfxBytes = pfxStream.readAllBytes();
            // BC-провайдер обязателен: PFX СФР использует ГОСТ-PBE алгоритмы
            // (pbeWithGost28147...), которые стандартный JCA не парсит
            // ("expecting the object identifier for a HmacSHA key derivation function").
            KeyStore ks = null;
            Exception lastFailure = null;
            // Каскад: BC+пароль → BC+null → BC+пустая строка
            for (char[] pwd : new char[][]{password, null, new char[0]}) {
                try (InputStream attempt = new java.io.ByteArrayInputStream(pfxBytes)) {
                    KeyStore candidate = KeyStore.getInstance("PKCS12",
                            BouncyCastleProvider.PROVIDER_NAME);
                    candidate.load(attempt, pwd);
                    ks = candidate;
                    keyPassword = pwd;
                    break;
                } catch (Exception e) {
                    lastFailure = e;
                    log.debug("PFX load failed (pwd={}): {}", pwd == null ? "null" : "chars",
                            e.toString());
                }
            }
            if (ks == null) {
                throw new IllegalStateException("Все варианты загрузки PFX провалились: "
                        + lastFailure, lastFailure);
            }
            String alias = ks.aliases().nextElement();
            this.privateKey = (PrivateKey) ks.getKey(alias, keyPassword);
            java.security.cert.Certificate[] chainCerts = ks.getCertificateChain(alias);
            if (chainCerts == null || chainCerts.length == 0) {
                throw new IllegalStateException("В PFX нет цепочки сертификатов (alias=" + alias + ")");
            }
            this.certificate = (X509Certificate) chainCerts[0];
            List<X509Certificate> certs = new ArrayList<>();
            for (java.security.cert.Certificate c : chainCerts) {
                certs.add((X509Certificate) c);
            }
            this.chain = certs;
            this.signerAlg = detectAlgorithm(certificate);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить PFX-сертификат: "
                    + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private static java.io.ByteArrayInputStream replay(java.io.ByteArrayInputStream in) {
        java.io.ByteArrayInputStream copy = new java.io.ByteArrayInputStream(in.readAllBytes());
        in.reset();
        return copy;
    }

    /**
     * Подписывает байты файла CMS-подписью.
     *
     * @param content  подписываемые данные (байты файла из DA)
     * @param detached true = отсоединённая подпись (контент НЕ внутри CMS) —
     *                 именно такой вариант ожидает бэкенд при проверке против
     *                 файла в хранилище; false = attached (контент внутри)
     * @return DER-кодированная CMSSignedData
     */
    public byte[] sign(byte[] content, boolean detached) {
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner signer = new JcaContentSignerBuilder(signerAlg)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(privateKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder()
                                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build())
                    .build(signer, certificate));
            gen.addCertificates(new JcaCertStore(chain));
            CMSSignedData signed = gen.generate(new CMSProcessableByteArray(content), !detached);
            return signed.getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка создания CMS-подписи (" + signerAlg + "): " + e.getMessage(), e);
        }
    }

    /** Attached-вариант (совместимость со старыми вызовами). */
    public byte[] signAttached(byte[] content) {
        return sign(content, false);
    }

    public String getSignerAlgorithm() {
        return signerAlg;
    }

    /** DER-кодировка сертификата (для передачи в DA metadata.certificate в base64). */
    public byte[] getCertificateEncoded() throws java.security.cert.CertificateEncodingException {
        return certificate.getEncoded();
    }

    public String getCertificateSubject() {
        return certificate.getSubjectX500Principal().getName();
    }

    /**
     * Подбор имени алгоритма подписи по типу ключа/OID сертификата.
     * СФР использует ГОСТ Р 34.10-2012; обычные стенды — RSA.
     */
    private static String detectAlgorithm(X509Certificate cert) {
        String oid = cert.getSigAlgOID();
        String keyAlg = cert.getPublicKey().getAlgorithm();
        if (oid.contains("1.2.643.7") || keyAlg.toUpperCase().contains("GOST")) {
            // ГОСТ Р 34.10-2012: 256 или 512
            return oid.endsWith("1.2.643.7.1.1.1.2") || keyAlg.contains("512")
                    ? "GOST3411-2012-512withGOST3410-2012-512"
                    : "GOST3411-2012-256withGOST3410-2012-256";
        }
        if ("EC".equals(keyAlg)) {
            return "SHA256withECDSA";
        }
        if ("Ed25519".equals(keyAlg)) {
            return "Ed25519";
        }
        return "SHA256withRSA";
    }
}
