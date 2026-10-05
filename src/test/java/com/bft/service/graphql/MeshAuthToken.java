package com.bft.service.graphql;

/**
 * Статический держатель access-токена приложения EVS, извлечённого из браузерной
 * сессии после логина (SSO Keycloak). Используется в прямом режиме обращения к
 * graphql-mesh (port-forward), где мы обошли oauth2-прокси и должны сами подставить
 * токен в заголовок запроса.
 */
public final class MeshAuthToken {

    private static volatile String token;

    private MeshAuthToken() {
    }

    public static void set(String t) {
        token = t;
    }

    public static String get() {
        return token;
    }

    public static boolean has() {
        return token != null && !token.isBlank();
    }
}
