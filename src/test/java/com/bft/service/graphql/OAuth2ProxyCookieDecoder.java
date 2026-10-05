package com.bft.service.graphql;

import com.bft.config.EnvironmentUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Decrypts oauth2-proxy session cookie (graphql-mesh / oauth2-proxy.dev.ecp) to extract the
 * keycloak access_token and reuse it as Bearer for direct (port-forward) mesh calls.
 *
 * <p>oauth2-proxy scheme: cookie-value = base64url( nonce(12) || AES-GCM ciphertext ),
 * key = SHA-256(client-secret). Mirrors oauth2-proxy/pkg/encryption.
 */
public final class OAuth2ProxyCookieDecoder {

    private static final int NONCE_SIZE = 12;
    private static final int TAG_BITS = 128;

    private OAuth2ProxyCookieDecoder() {
    }

    public static String extractAccessToken(String cookieValue, String secret) {
        org.slf4j.LoggerFactory.getLogger(OAuth2ProxyCookieDecoder.class).info(
                "extractAccessToken: cookieValue.len={}, secret.len={}",
                cookieValue == null ? -1 : cookieValue.length(), secret == null ? -1 : secret.length());
        if (cookieValue == null || secret == null) {
            return null;
        }
        byte[] decoded = base64UrlDecode(cookieValue);
        if (decoded == null || decoded.length <= NONCE_SIZE) {
            org.slf4j.LoggerFactory.getLogger(OAuth2ProxyCookieDecoder.class).warn(
                    "extractAccessToken: base64-decode не удался (decoded={})", decoded == null ? "null" : decoded.length);
            return null;
        }
        java.util.List<byte[]> keys = candidateKeys(secret);
        for (int i = 0; i < keys.size(); i++) {
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKeySpec ks = new SecretKeySpec(keys.get(i), "AES");
                GCMParameterSpec gcm = new GCMParameterSpec(TAG_BITS, decoded, 0, NONCE_SIZE);
                cipher.init(Cipher.DECRYPT_MODE, ks, gcm);
                byte[] plain = cipher.doFinal(decoded, NONCE_SIZE, decoded.length - NONCE_SIZE);
                String json = new String(plain, StandardCharsets.UTF_8);
                String token = findField(json, "access_token");
                if (token != null) {
                    org.slf4j.LoggerFactory.getLogger(OAuth2ProxyCookieDecoder.class)
                            .info("OAuth2ProxyCookieDecoder: ключ #{}/{} подошёл, access_token извлечён",
                                    i, keys.size());
                    return token;
                }
                if (json.startsWith("{") && json.length() > 5) {
                    org.slf4j.LoggerFactory.getLogger(OAuth2ProxyCookieDecoder.class)
                            .info("OAuth2ProxyCookieDecoder: ключ #{} дал JSON без access_token (первые 120: {})",
                                    i, json.substring(0, Math.min(120, json.length())).replace("\n", " "));
                }
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(OAuth2ProxyCookieDecoder.class)
                        .debug("OAuth2ProxyCookieDecoder: ключ #{} не подошёл: {}", i, e.getMessage());
            }
        }
        org.slf4j.LoggerFactory.getLogger(OAuth2ProxyCookieDecoder.class)
                .warn("OAuth2ProxyCookieDecoder: ни один ключ не дал access_token");
        return null;
    }

    private static java.util.List<byte[]> candidateKeys(String secret) {
        java.util.List<byte[]> out = new java.util.ArrayList<>();
        try {
            out.add(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
        }
        try {
            byte[] b64 = Base64.getUrlDecoder().decode(secret);
            if (b64.length == 32) {
                out.add(b64);
            }
        } catch (Exception ignored) {
        }
        try {
            byte[] b64s = Base64.getDecoder().decode(secret);
            if (b64s.length == 32) {
                out.add(b64s);
            }
        } catch (Exception ignored) {
        }
        // сырой секрет, обрезанный/дополненный до 32 байт
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        byte[] k = new byte[32];
        System.arraycopy(raw, 0, k, 0, Math.min(32, raw.length));
        out.add(k);
        return out;
    }

    public static String defaultSecret() {
        return EnvironmentUtils.getPropertyOrEnv("evs.sign.oauth2-cookie-secret", "");
    }

    public static String decodeClaims(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return "<not-jwt>";
            }
            String payload = parts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return "iss=" + findField(json, "iss") + ", aud=" + findField(json, "aud")
                    + ", azp=" + findField(json, "azp") + ", sub=" + findField(json, "sub")
                    + ", exp=" + findField(json, "exp") + ", typ=" + findField(json, "typ");
        } catch (Exception e) {
            return "<decode-error: " + e.getMessage() + ">";
        }
    }

    private static byte[] base64UrlDecode(String s) {
        try {
            return Base64.getUrlDecoder().decode(s);
        } catch (Exception e) {
            try {
                return Base64.getDecoder().decode(s);
            } catch (Exception e2) {
                try {
                    String fixed = s.replace('-', '+').replace('_', '/');
                    while (fixed.length() % 4 != 0) {
                        fixed += "=";
                    }
                    return Base64.getDecoder().decode(fixed);
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    private static String findField(String json, String field) {
        String needle = "\"" + field + "\":";
        int idx = json.indexOf(needle);
        if (idx < 0) {
            return null;
        }
        int colon = idx + needle.length();
        while (colon < json.length() && json.charAt(colon) != '"') {
            colon++;
        }
        if (colon >= json.length()) {
            return null;
        }
        int start = colon + 1;
        int end = json.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return json.substring(start, end);
    }
}
