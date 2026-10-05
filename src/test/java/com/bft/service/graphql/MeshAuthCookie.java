package com.bft.service.graphql;

/**
 * Статический держатель session-cookie oauth2-прокси mesh
 * ({@code _ui_mesh_oauth2_proxy_0/1}), извлечённого из браузерной сессии после
 * авторизации в mesh-прокси. Используется в прямом режиме (port-forward) как
 * альтернатива токену: mesh может валидировать сессионный cookie вместо Bearer.
 */
public final class MeshAuthCookie {

    private static volatile String cookieHeader;

    private MeshAuthCookie() {
    }

    public static void set(String c) {
        cookieHeader = c;
    }

    public static String get() {
        return cookieHeader;
    }

    public static boolean has() {
        return cookieHeader != null && !cookieHeader.isBlank();
    }
}
