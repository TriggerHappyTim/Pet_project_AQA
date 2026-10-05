package com.bft.service.sign;

import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

/**
 * Создание XMLDSIG (xmldsig) подписи файла PFX/PEM-сертификатом.
 *
 * <p>Заменяет связку «КриптоПРО-плагин в браузере» для отчётов ЛК Страхователя:
 * тест сам подписывает XML и загружает подпись в Document API. Формат — xmldsig
 * (по требованию бэкенда, в отличие от CMS/PKCS#7 в {@link PfxCmsSigner}).
 *
 * <p>По умолчанию формируется <b>enveloped</b>-подпись (элемент {@code <Signature>}
 * добавляется внутрь подписываемого XML) — один файл, пригодный для передачи как
 * {@code signedXmlFileRef}. Поддерживается ГОСТ Р 34.10-2012 и RSA (авто-детект
 * по алгоритму ключа, как в {@link PfxCmsSigner}).
 *
 * <p>⚠️ КОНСТАНТЫ АЛГОРИТМОВ (ALGO_*) — требуют ПОДТВЕРЖДЕНИЯ БЭКЕНДОМ. Если бэкенд
 * ожидает другой canonicalization/digest/signature URI (особенно для ГОСТ), их
 * нужно поправить здесь; оркестрация от этого не пострадает.
 */
public class XmlDsigSigner {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        Init.init();
    }

    private static final Logger log = LoggerFactory.getLogger(XmlDsigSigner.class);

    // ===== ALGO-константы: подтвердить у бэкенда (см. описание класса) =====
    private static final String CANON_EXCL = "http://www.w3.org/2001/10/xml-exc-c14n#";
    private static final String SIG_GOST = "http://www.w3.org/2001/04/xmldsig-more#gostr34102012-gostr34112012-256";
    private static final String DIGEST_GOST = "http://www.w3.org/2001/04/xmldsig-more#gostr34112012-256";
    private static final String SIG_RSA = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    private static final String DIGEST_RSA = "http://www.w3.org/2001/04/xmlenc#sha256";

    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final String signatureMethod;
    private final String digestMethod;
    private final String signerAlg;

    private XmlDsigSigner(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.signerAlg = detectAlgorithm(certificate);
        boolean gost = signerAlg.startsWith("GOST");
        this.signatureMethod = gost ? SIG_GOST : SIG_RSA;
        this.digestMethod = gost ? DIGEST_GOST : DIGEST_RSA;
        Provider bc = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        log.info("XMLDSIG-подписант готов: subject={}, алгоритм={}, sigMethod={}",
                certificate.getSubjectX500Principal().getName(), signerAlg, signatureMethod);
    }

    public static XmlDsigSigner of(PrivateKey privateKey, X509Certificate certificate) {
        return new XmlDsigSigner(privateKey, certificate);
    }

    public static XmlDsigSigner fromPem(String certPemPath, String keyPemPath) {
        try {
            byte[] certBytes = readResource(certPemPath);
            byte[] keyBytes = readResource(keyPemPath);
            java.security.cert.CertificateFactory cf =
                    java.security.cert.CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(certBytes));

            org.bouncycastle.openssl.PEMParser pemParser = new org.bouncycastle.openssl.PEMParser(
                    new java.io.InputStreamReader(
                            new ByteArrayInputStream(keyBytes), java.nio.charset.StandardCharsets.UTF_8));
            Object pemObject = pemParser.readObject();
            pemParser.close();
            org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter converter =
                    new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME);
            PrivateKey privateKey;
            if (pemObject instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                privateKey = converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) pemObject);
            } else if (pemObject instanceof org.bouncycastle.openssl.PEMKeyPair) {
                privateKey = converter.getKeyPair((org.bouncycastle.openssl.PEMKeyPair) pemObject).getPrivate();
            } else {
                throw new IllegalStateException("Неизвестный тип ключа: "
                        + (pemObject == null ? "null" : pemObject.getClass().getName()));
            }
            return new XmlDsigSigner(privateKey, certificate);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить PEM для XMLDSIG: " + e.getMessage(), e);
        }
    }

    public XmlDsigSigner(InputStream pfxStream, char[] password) {
        this(loadFromPfx(pfxStream, password));
    }

    private XmlDsigSigner(PfxHolder holder) {
        this(holder.privateKey, holder.certificate);
    }

    private static class PfxHolder {
        final PrivateKey privateKey;
        final X509Certificate certificate;
        PfxHolder(PrivateKey k, X509Certificate c) { this.privateKey = k; this.certificate = c; }
    }

    private static PfxHolder loadFromPfx(InputStream pfxStream, char[] password) {
        try {
            final byte[] pfxBytes = pfxStream.readAllBytes();
            Exception lastFailure = null;
            java.security.KeyStore ks = null;
            char[] used = password;
            for (char[] pwd : new char[][]{password, null, new char[0]}) {
                try (InputStream attempt = new ByteArrayInputStream(pfxBytes)) {
                    java.security.KeyStore candidate =
                            java.security.KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
                    candidate.load(attempt, pwd);
                    ks = candidate;
                    used = pwd;
                    break;
                } catch (Exception e) {
                    lastFailure = e;
                }
            }
            if (ks == null) {
                throw new IllegalStateException("Все варианты загрузки PFX провалились: " + lastFailure, lastFailure);
            }
            String alias = ks.aliases().nextElement();
            PrivateKey key = (PrivateKey) ks.getKey(alias, used);
            X509Certificate cert = (X509Certificate) ks.getCertificateChain(alias)[0];
            return new PfxHolder(key, cert);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить PFX для XMLDSIG: " + e.getMessage(), e);
        }
    }

    /**
     * Подписывает XML enveloped-подписью (подпись внутри документа).
     *
     * @param xml подписываемый XML (байты из DA)
     * @return подписанный XML (байты), пригодный для загрузки как signedXmlFileRef
     */
    public byte[] signEnveloped(byte[] xml) {
        try {
            if (signerAlg.startsWith("GOST")) {
                return signEnvelopedGost(xml);
            }
            return signEnvelopedSantuario(xml);
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка создания XMLDSIG-подписи (" + signerAlg + "): " + e.getMessage(), e);
        }
    }

    /**
     * Enveloped xmldsig через штатный Santuario (RSA/ECDSA). Для ГОСТ используется
     * {@link #signEnvelopedGost}, т.к. xmlsec:3.0.4 не содержит реализаций ГОСТ-алгоритмов.
     */
    private byte[] signEnvelopedSantuario(byte[] xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml));

        XMLSignature sig = new XMLSignature(doc, "", signatureMethod, CANON_EXCL);
        Transforms transforms = new Transforms(doc);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        sig.addDocument("", transforms, digestMethod);

        sig.addKeyInfo(certificate);

        Element root = doc.getDocumentElement();
        root.appendChild(sig.getElement());

        sig.sign(privateKey);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(baos));
        byte[] result = baos.toByteArray();
        log.info("XMLDSIG (enveloped, Santuario) сформирован: {} байт", result.length);
        return result;
    }

    /**
     * Enveloped xmldsig для ГОСТ Р 34.10-2012 (256), формируемый вручную по RFC 6986,
     * т.к. xmlsec:3.0.4 не содержит ГОСТ-реализаций.
     *
     * <p>Использует только canonicalizer exc-c14n из Santuario + BouncyCastle для
     * дайджеста (GOST3411-2012-256) и подписи (ECGOST3410-2012-256). Значение подписи —
     * конкатенация r||s (по 32 байта big-endian), как требует RFC 6986 (BC JCA отдаёт
     * ровно такой формат — проверено).
     */
    private byte[] signEnvelopedGost(byte[] xml) throws Exception {
        String DS = org.apache.xml.security.utils.Constants.SignatureSpecNS; // http://www.w3.org/2000/09/xmldsig#
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml));
        Element docElem = doc.getDocumentElement();

        Element sig = doc.createElementNS(DS, "ds:Signature");
        Element signedInfo = doc.createElementNS(DS, "ds:SignedInfo");

        Element cm = doc.createElementNS(DS, "ds:CanonicalizationMethod");
        cm.setAttribute("Algorithm", CANON_EXCL);
        signedInfo.appendChild(cm);

        Element sm = doc.createElementNS(DS, "ds:SignatureMethod");
        sm.setAttribute("Algorithm", SIG_GOST);
        signedInfo.appendChild(sm);

        Element ref = doc.createElementNS(DS, "ds:Reference");
        ref.setAttribute("URI", "");
        Element transforms = doc.createElementNS(DS, "ds:Transforms");
        Element t1 = doc.createElementNS(DS, "ds:Transform");
        t1.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        Element t2 = doc.createElementNS(DS, "ds:Transform");
        t2.setAttribute("Algorithm", CANON_EXCL);
        transforms.appendChild(t1);
        transforms.appendChild(t2);
        Element dm = doc.createElementNS(DS, "ds:DigestMethod");
        dm.setAttribute("Algorithm", DIGEST_GOST);
        Element dv = doc.createElementNS(DS, "ds:DigestValue");
        ref.appendChild(transforms);
        ref.appendChild(dm);
        ref.appendChild(dv);
        signedInfo.appendChild(ref);

        Element sigVal = doc.createElementNS(DS, "ds:SignatureValue");
        Element keyInfo = doc.createElementNS(DS, "ds:KeyInfo");
        Element x509 = doc.createElementNS(DS, "ds:X509Data");
        Element x509cert = doc.createElementNS(DS, "ds:X509Certificate");
        x509cert.setTextContent(
                java.util.Base64.getEncoder().encodeToString(certificate.getEncoded()));
        x509.appendChild(x509cert);
        keyInfo.appendChild(x509);

        sig.appendChild(signedInfo);
        sig.appendChild(sigVal);
        sig.appendChild(keyInfo);
        docElem.appendChild(sig);

        // --- Reference digest: документ минус <Signature>, exc-c14n, GOST3411-2012-256 ---
        byte[] refData = canonicalizeSubtree(removeSignature(doc));
        byte[] digest = java.security.MessageDigest
                .getInstance("GOST3411-2012-256", BouncyCastleProvider.PROVIDER_NAME)
                .digest(refData);
        dv.setTextContent(java.util.Base64.getEncoder().encodeToString(digest));

        // --- SignedInfo: exc-c14n, подпись ECGOST3410-2012-256 (BC -> 64 байта r||s) ---
        byte[] siBytes = canonicalizeSubtree(signedInfo);
        java.security.Signature bcSig = java.security.Signature
                .getInstance("ECGOST3410-2012-256", BouncyCastleProvider.PROVIDER_NAME);
        bcSig.initSign(privateKey);
        bcSig.update(siBytes);
        byte[] sigBytes = bcSig.sign();
        sigVal.setTextContent(java.util.Base64.getEncoder().encodeToString(sigBytes));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(baos));
        byte[] result = baos.toByteArray();
        log.info("XMLDSIG (enveloped, ГОСТ-ручной) сформирован: {} байт", result.length);
        return result;
    }

    private static byte[] canonicalizeSubtree(org.w3c.dom.Node node) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        org.apache.xml.security.c14n.Canonicalizer.getInstance(CANON_EXCL)
                .canonicalizeSubtree(node, out);
        return out.toByteArray();
    }

    /** Глубокая копия документа без элемента <ds:Signature> (для enveloped-дайджеста). */
    private static org.w3c.dom.Document removeSignature(org.w3c.dom.Document doc) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document clone = dbf.newDocumentBuilder().newDocument();
        org.w3c.dom.Node imported = clone.importNode(doc.getDocumentElement(), true);
        clone.appendChild(imported);
        String DS = org.apache.xml.security.utils.Constants.SignatureSpecNS;
        org.w3c.dom.NodeList sigs = clone.getElementsByTagNameNS(DS, "Signature");
        for (int i = 0; i < sigs.getLength(); i++) {
            org.w3c.dom.Node s = sigs.item(i);
            s.getParentNode().removeChild(s);
        }
        return clone;
    }

    public String getSignerAlgorithm() {
        return signerAlg;
    }

    public byte[] getCertificateEncoded() throws java.security.cert.CertificateEncodingException {
        return certificate.getEncoded();
    }

    public String getCertificateSubject() {
        return certificate.getSubjectX500Principal().getName();
    }

    private static byte[] readResource(String path) throws Exception {
        InputStream is;
        if (path.startsWith("classpath:")) {
            is = XmlDsigSigner.class.getClassLoader()
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

    private static String detectAlgorithm(X509Certificate cert) {
        String oid = cert.getSigAlgOID();
        String keyAlg = cert.getPublicKey().getAlgorithm();
        if (oid.contains("1.2.643.7") || keyAlg.toUpperCase().contains("GOST")) {
            return oid.endsWith("1.2.643.7.1.1.1.2") || keyAlg.contains("512")
                    ? "GOST-512" : "GOST-256";
        }
        if ("EC".equals(keyAlg)) {
            return "EC";
        }
        return "RSA";
    }
}
