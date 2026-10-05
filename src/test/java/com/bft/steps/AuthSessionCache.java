package com.bft.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Кэш live-сессии oauth2_proxy: сохраняет session-cookie (_oauth2_proxy/_oauth2_proxy_0/_oauth2_proxy_1)
 * после успешного реального ESIA-логина и позволяет подставлять их в последующих прогонах без обращения
 * к флаковому контуру sso-test.sfr.gov.ru (стабильные ежедневные прогоны).
 *
 * <p>Путь к файлу: {@code -Devs.auth.session-file=...} (по умолчанию {@code target/evs-auth-session.properties}).
 * <p>Запас по сроку действия: {@code -Devs.auth.session-margin-hours=N} (по умолчанию 6 часов).
 *
 * <p>Значение cookie oauth2_proxy имеет вид {@code email|user|expiry|signature}, поэтому срок действия
 * извлекается из 10-значного pipe-сегмента. Если срок не распознан — сессия считается живой только
 * если сохранена менее 12 часов назад.
 */
public final class AuthSessionCache {

    private static final String FILE_KEY = "evs.auth.session-file";
    private static final String DEFAULT_FILE = "target/evs-auth-session.properties";
    private static final String MARGIN_KEY = "evs.auth.session-margin-hours";
    private static final int DEFAULT_MARGIN_HOURS = 6;
    private static final long FALLBACK_MAX_AGE_SECONDS = 12 * 3600L;
    private static final String SAVED_AT = "saved-at";

    private static final String[] COOKIE_NAMES = {
            "_oauth2_proxy", "_oauth2_proxy_0", "_oauth2_proxy_1"
    };

    public static Map<String, String> load() {
        File f = file();
        if (!f.isFile()) {
            return null;
        }
        Properties p = new Properties();
        try (InputStream is = new FileInputStream(f)) {
            p.load(is);
        } catch (IOException e) {
            return null;
        }
        Map<String, String> m = new LinkedHashMap<>();
        for (String name : COOKIE_NAMES) {
            String v = p.getProperty(name);
            if (v != null && !v.isEmpty()) {
                m.put(name, v);
            }
        }
        String saved = p.getProperty(SAVED_AT);
        if (saved != null && !saved.isEmpty()) {
            m.put(SAVED_AT, saved);
        }
        return m.isEmpty() ? null : m;
    }

    public static void save(Map<String, String> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return;
        }
        try {
            File f = file();
            File parent = f.getAbsoluteFile().getParentFile();
            if (parent != null) {
                Files.createDirectories(parent.toPath());
            }
            Properties p = new Properties();
            for (Map.Entry<String, String> e : cookies.entrySet()) {
                if (e.getValue() != null) {
                    p.setProperty(e.getKey(), e.getValue());
                }
            }
            p.setProperty(SAVED_AT, String.valueOf(System.currentTimeMillis() / 1000));
            try (OutputStream os = new FileOutputStream(f)) {
                p.store(os, "EVS session cache (oauth2_proxy cookies)");
            }
        } catch (Exception ignored) {
            // кэш — не критичен
        }
    }

    /**
     * @return {@code true}, если сессия достаточно свежая для использования.
     */
    public static boolean isFresh(Map<String, String> cached) {
        if (cached == null) {
            return false;
        }
        long now = System.currentTimeMillis() / 1000;
        String main = cached.get("_oauth2_proxy");
        String alt = main == null ? cached.get("_oauth2_proxy_0") : null;
        Long expiry = parseExpiry(main != null ? main : alt);
        if (expiry == null) {
            String saved = cached.get(SAVED_AT);
            if (saved == null) {
                return false;
            }
            try {
                long savedAt = Long.parseLong(saved.trim());
                return now - savedAt < FALLBACK_MAX_AGE_SECONDS;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return expiry - now > marginHours() * 3600L;
    }

    /**
     * Извлекает срок действия (Unix, сек) из значения cookie формата email|user|expiry|signature.
     */
    private static Long parseExpiry(String value) {
        if (value == null) {
            return null;
        }
        for (String seg : value.split("\\|")) {
            if (seg.length() == 10 && seg.matches("\\d{10}")) {
                long t = Long.parseLong(seg);
                if (t > 1_600_000_000L) {
                    return t;
                }
            }
        }
        return null;
    }

    private static File file() {
        String path = System.getProperty(FILE_KEY);
        if (path == null || path.isEmpty()) {
            path = DEFAULT_FILE;
        }
        return new File(path);
    }

    private static int marginHours() {
        try {
            return Integer.parseInt(System.getProperty(MARGIN_KEY, String.valueOf(DEFAULT_MARGIN_HOURS)));
        } catch (NumberFormatException e) {
            return DEFAULT_MARGIN_HOURS;
        }
    }

    private AuthSessionCache() {
    }
}