package com.bft.service.sign;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.utils.Constants;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlDsigSignerSelfCheck {

    private static final Path CERT = Path.of("src/test/resources/certs/krivonosov.cer.pem");
    private static final Path KEY = Path.of("src/test/resources/certs/krivonosov.key.pem");

    @Test
    public void signAndVerifyGost() throws Exception {
        Init.init();
        XmlDsigSigner signer = XmlDsigSigner.fromPem(CERT.toString(), KEY.toString());
        System.out.println("[SELFCHECK] algorithm=" + signer.getSignerAlgorithm());

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<report><id>8982</id><form>СЗВ-М</form></report>";
        byte[] signed = signer.signEnveloped(xml.getBytes("UTF-8"));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(signed));

        String DS = Constants.SignatureSpecNS;
        Element sig = (Element) doc.getElementsByTagNameNS(DS, "Signature").item(0);
        Element signedInfo = (Element) sig.getElementsByTagNameNS(DS, "SignedInfo").item(0);
        Element sigValEl = (Element) sig.getElementsByTagNameNS(DS, "SignatureValue").item(0);
        Element dvEl = (Element) ((Element) sig.getElementsByTagNameNS(DS, "Reference").item(0))
                .getElementsByTagNameNS(DS, "DigestValue").item(0);

        byte[] siBytes = canonicalize(signedInfo);
        byte[] sigVal = Base64.getDecoder().decode(sigValEl.getTextContent().trim());

        X509Certificate cert = loadCert(CERT);
        Signature verifier = Signature.getInstance("ECGOST3410-2012-256",
                org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME);
        verifier.initVerify(cert.getPublicKey());
        verifier.update(siBytes);
        boolean sigValid = verifier.verify(sigVal);
        System.out.println("[SELFCHECK] sigValid=" + sigValid);
        assertTrue(sigValid, "ГОСТ-подпись SignedInfo должна валидироваться");

        // Reference digest check
        Document clone = dbf.newDocumentBuilder().newDocument();
        clone.appendChild(clone.importNode(doc.getDocumentElement(), true));
        NodeList sigs = clone.getElementsByTagNameNS(DS, "Signature");
        for (int i = 0; i < sigs.getLength(); i++) sigs.item(i).getParentNode().removeChild(sigs.item(i));
        byte[] refData = canonicalize(clone.getDocumentElement());
        java.security.MessageDigest md = java.security.MessageDigest
                .getInstance("GOST3411-2012-256", org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME);
        String refDigestB64 = Base64.getEncoder().encodeToString(md.digest(refData));
        boolean digestValid = refDigestB64.equals(dvEl.getTextContent().trim());
        System.out.println("[SELFCHECK] refDigestValid=" + digestValid);
        assertTrue(digestValid, "Reference-digest должен совпадать");
    }

    private static byte[] canonicalize(org.w3c.dom.Node n) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        Canonicalizer.getInstance("http://www.w3.org/2001/10/xml-exc-c14n#").canonicalizeSubtree(n, out);
        return out.toByteArray();
    }

    private static X509Certificate loadCert(Path p) throws Exception {
        String pem = new String(java.nio.file.Files.readAllBytes(p))
                .replaceAll("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----", "").replaceAll("\\s+", "");
        return (X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
                .generateCertificate(new java.io.ByteArrayInputStream(Base64.getDecoder().decode(pem)));
    }
}
