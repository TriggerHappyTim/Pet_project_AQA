package com.bft.steps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Конфигурация обхода ESIA-авторизации, повторяющая механизм локального запуска фронта
 * (application-auth-dev.yml: ps.auth.cookie + ps.auth.user-id).
 *
 * <p>При включении ({@code -Devt.auth.bypass=true}) в браузерный контекст подставляется
 * session-cookie oauth2_proxy из локального профиля фронта, и реальный ESIA-логин не выполняется.
 *
 * <p>Источники значения cookie (в порядке приоритета):
 * <ol>
 *   <li>системное свойство {@code -Devt.auth.bypass.cookie=...}</li>
 *   <li>файл {@code auth-bypass.properties} в test-resources</li>
 *   <li>файл yml локального фронта (путь из {@code -Devt.auth.bypass.configFile=...},
 *       по умолчанию D:\evs-oi-ipu-usv-front\...\application-auth-dev.yml)</li>
 * </ol>
 */
public final class AuthBypassConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = AuthBypassConfig.class.getClassLoader()
                .getResourceAsStream("auth-bypass.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException ignored) {
            // properties не обязателен, есть fallback на yml фронта
        }
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty("evt.auth.bypass", "false"))
                || Boolean.parseBoolean(System.getProperty("evs.auth.bypass",
                        props.getProperty("evs.auth.bypass", "false")));
    }

    public static String cookieName() {
        return System.getProperty("evs.auth.bypass.cookieName",
                props.getProperty("cookieName", "_oauth2_proxy"));
    }

    public static String cookieValue() {
        String v = System.getProperty("evs.auth.bypass.cookie");
        if (v != null && !v.isEmpty()) {
            return v;
        }
        v = props.getProperty("cookie");
        if (v != null && !v.isEmpty()) {
            return v;
        }
        return readYmlValue("cookie");
    }

    public static String userId() {
        String v = System.getProperty("evs.auth.bypass.userId");
        if (v != null && !v.isEmpty()) {
            return v;
        }
        v = props.getProperty("userId");
        if (v != null && !v.isEmpty()) {
            return v;
        }
        return readYmlValue("user-id");
    }

    public static String domainFor(String baseUrl) {
        String d = System.getProperty("evs.auth.bypass.domain", props.getProperty("domain"));
        if (d != null && !d.isEmpty()) {
            return d;
        }
        try {
            return new java.net.URI(baseUrl).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private static String readYmlValue(String key) {
        String path = System.getProperty("evs.auth.bypass.configFile",
                props.getProperty("configFile",
                        "D:\\evs-oi-ipu-usv-front\\src\\main\\resources\\application-auth-dev.yml"));
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String line;
            String prefix = key + ":";
            while ((line = r.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith(prefix)) {
                    return trimmed.substring(prefix.length()).trim();
                }
            }
        } catch (IOException ignored) {
            // файл недоступен — значение не получить
        }
        return null;
    }

    private AuthBypassConfig() {
    }
}
