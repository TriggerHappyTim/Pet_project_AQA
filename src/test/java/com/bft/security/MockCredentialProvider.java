package com.bft.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock провайдер учетных данных для тестирования
 * Содержит безопасные тестовые данные, которые можно использовать в логах
 */
public class MockCredentialProvider implements CredentialProvider {

    /** Префикс и значения для Кривоносов Александр Петрович (evs.user1) */
    private static final String EVS_USER1_PREFIX = "evs.user1";
    private static final String EVS_USER1_USERNAME = "ci_evs_user1";
    private static final String EVS_USER1_PASSWORD = "ci_placeholder_password";
    private static final String EVS_USER1_ORGANIZATION = "ORG-CI-USER1";
    private static final String EVS_USER1_FULLNAME = "Кривоносов Александр Петрович";

    private final Map<String, String> mockCredentials;

    public MockCredentialProvider() {
        mockCredentials = new HashMap<>();
        initializeMockData();
    }

    private void initializeMockData() {
        // Тестовые пользовательские данные
        mockCredentials.put("test.username", "testuser");
        mockCredentials.put("test.password", "testpass123");
        mockCredentials.put("test.email", "test@example.com");

        mockCredentials.put("admin.username", "admin");
        mockCredentials.put("admin.password", "admin123");
        mockCredentials.put("admin.email", "admin@example.com");

        // Тестовые API данные
        mockCredentials.put("api.key", "test_api_key_12345");
        mockCredentials.put("api.secret", "test_api_secret_67890");
        mockCredentials.put("api.token", "test_jwt_token_abcdef");

        // Тестовые database данные
        mockCredentials.put("db.url", "jdbc:h2:mem:testdb");
        mockCredentials.put("db.username", "testdbuser");
        mockCredentials.put("db.password", "testdbpass");
        mockCredentials.put("db.driver", "org.h2.Driver");

        // CryptoPro тестовые данные
        mockCredentials.put("cryptopro.test.username", "test@example.com");
        mockCredentials.put("cryptopro.test.password", "testpass");

        // EPGU тестовые данные (замена реальных credentials)
        mockCredentials.put("epgu.username", "testuser@example.com");
        mockCredentials.put("epgu.password", "testpass123");

        // RPU тестовые данные
        mockCredentials.put("rpu.username", "testuser_rpu");
        mockCredentials.put("rpu.password", "testpass_rpu123");

        // UOS тестовые данные
        mockCredentials.put("uos.username", "testuser_uos");
        mockCredentials.put("uos.password", "testpass_uos123");

        // EVS тестовые данные (DEFAULT_USER, префикс evs)
        mockCredentials.put("evs.username", "testuser_evs");
        mockCredentials.put("evs.password", "testpass_evs123");
        mockCredentials.put("evs.organization", "ORG-EVS-DEFAULT");
        mockCredentials.put("evs.email", "testuser_evs@example.com");

        // EVS user1 (Кривоносов) — fallback для CI/локального запуска.
        // Для реального логина задайте в GitLab CI/CD Variables или credentials.properties: evs.user1.username, evs.user1.password, evs.user1.organization.
        mockCredentials.put(EVS_USER1_PREFIX + ".username", EVS_USER1_USERNAME);
        mockCredentials.put(EVS_USER1_PREFIX + ".password", EVS_USER1_PASSWORD);
        mockCredentials.put(EVS_USER1_PREFIX + ".organization", EVS_USER1_ORGANIZATION);
        mockCredentials.put(EVS_USER1_PREFIX + ".fullname", EVS_USER1_FULLNAME);

        // EVS user2 (Бездомный) — fallback для CI
        mockCredentials.put("evs.user2.username", "ci_evs_user2");
        mockCredentials.put("evs.user2.password", "ci_placeholder_password");
        mockCredentials.put("evs.user2.organization", "ORG-CI-USER2");
        mockCredentials.put("evs.user2.fullname", "Бездомный Иван Николаевич");
    }

    @Override
    public String getCredential(String key) {
        return mockCredentials.get(key);
    }

    @Override
    public boolean hasCredential(String key) {
        return mockCredentials.containsKey(key);
    }

    @Override
    public ProviderType getType() {
        return ProviderType.MOCK;
    }

    /**
     * Добавить или обновить mock credential
     */
    public void setMockCredential(String key, String value) {
        mockCredentials.put(key, value);
    }

    /**
     * Очистить все mock credentials
     */
    public void clearCredentials() {
        mockCredentials.clear();
        initializeMockData();
    }

    /**
     * Проверить, что credential является mock данными
     */
    public boolean isMockCredential(String key) {
        return mockCredentials.containsKey(key);
    }
}