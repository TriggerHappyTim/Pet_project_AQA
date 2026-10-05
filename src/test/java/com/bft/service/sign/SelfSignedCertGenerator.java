package com.bft.service.sign;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Генератор самоподписанного тестового сертификата для автоматической ЭЦП.
 *
 * <p>Используется, когда нет выданного УЦ PFX: UAT-стенды часто принимают
 * любые криптографически валидные подписи без проверки цепочки доверия.
 * Если стенд отклоняет недоверенный сертификат — потребуется сертификат
 * тестового УЦ СФР.
 */
public final class SelfSignedCertGenerator {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SelfSignedCertGenerator() {
    }

    /** OID СНИЛС в сертификатах КриптоПРО/СФР */
    public static final ASN1ObjectIdentifier OID_SNILS =
            new ASN1ObjectIdentifier("1.2.643.3.131.1.1");
    /** OID ИНН юридического лица */
    public static final ASN1ObjectIdentifier OID_INN_LE =
            new ASN1ObjectIdentifier("1.2.643.100.4");
    /** OID ОГРН юридического лица */
    public static final ASN1ObjectIdentifier OID_OGRN =
            new ASN1ObjectIdentifier("1.2.643.100.1");
    /** OID ИНН физического лица */
    public static final ASN1ObjectIdentifier OID_INN_FL =
            new ASN1ObjectIdentifier("1.2.643.100.3");
    /** OID должность (title) — 2.5.4.12 */
    private static final ASN1ObjectIdentifier OID_TITLE =
            new ASN1ObjectIdentifier("2.5.4.12");

    /**
     * Генерирует RSA-2048 пару и самоподписанный сертификат (срок 1 год)
     * с полными реквизитами в Subject — бэкенд ЕВС сверяет их с профилем:
     * CN (ФИО подписанта), СНИЛС (1.2.643.3.131.1.1),
     * ИНН (1.2.643.100.4), КПП (1.2.643.100.111), ОГРН (1.2.643.100.1), O (организация).
     *
     * @return готовая связка для подписания
     */
    /**
     * Генерирует сертификат с полными реквизитами подписанта СФР.
     * Все поля соответствуют реестру ЕВС — бэкенд сверяет Subject с ним.
     */
    public static PfxCmsSigner generateWithRequisites(
            String cn, String surname, String givenName,
            String country, String state, String locality, String street,
            String snils, String innFl, String orgName, String title,
            String ogrn, String innLe) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048, new SecureRandom());
            KeyPair keyPair = kpg.generateKeyPair();

            X500NameBuilder nb = new X500NameBuilder(BCStyle.INSTANCE);
            if (cn != null) nb.addRDN(BCStyle.CN, cn);
            if (surname != null) nb.addRDN(BCStyle.SURNAME, surname);
            if (givenName != null) nb.addRDN(BCStyle.GIVENNAME, givenName);
            if (country != null) nb.addRDN(BCStyle.C, country);
            if (state != null) nb.addRDN(BCStyle.ST, state);
            if (locality != null) nb.addRDN(BCStyle.L, locality);
            if (street != null) nb.addRDN(BCStyle.STREET, street);
            if (orgName != null) nb.addRDN(BCStyle.O, orgName);
            if (title != null) nb.addRDN(OID_TITLE, title);
            if (snils != null) nb.addRDN(OID_SNILS, snils);
            if (innFl != null) nb.addRDN(OID_INN_FL, innFl);
            if (ogrn != null) nb.addRDN(OID_OGRN, ogrn);
            if (innLe != null) nb.addRDN(OID_INN_LE, innLe);
            X500Name subject = nb.build();

            Date notBefore = Date.from(OffsetDateTime.now().minusMinutes(5).toInstant());
            Date notAfter = Date.from(OffsetDateTime.now().plusYears(1).toInstant());

            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                    subject,
                    BigInteger.valueOf(System.currentTimeMillis()),
                    notBefore, notAfter,
                    subject,
                    keyPair.getPublic());

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(keyPair.getPrivate());

            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(builder.build(signer));

            return PfxCmsSigner.of(keyPair, certificate);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Не удалось сгенерировать самоподписанный сертификат: " + e.getMessage(), e);
        }
    }
}
