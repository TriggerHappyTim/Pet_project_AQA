package com.bft.security;

import com.bft.security.EnvironmentCredentialProvider.UserCredentials;
import io.qameta.allure.AllureId;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit-тесты для enum TestUsers
 * 
 * Проверяет корректность работы с различными тестовыми пользователями,
 * получение credentials и организаций.
 */
public class TestUsersTest {
    
    private CredentialManager credentialManager;
    
    @BeforeMethod
    public void setUp() {
        // Сбросить синглтон и создать новый экземпляр
        CredentialManager.reset();
        credentialManager = CredentialManager.getInstance();
    }
    
    @AfterMethod
    public void tearDown() {
        CredentialManager.reset();
    }
    
    @Test(groups = {"unit", "security"})
    public void testKrivonosovUserCredentials() {
        // Given
        TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
        
        // When
        String username = user.getUsername();
        String organization = user.getOrganization();
        
        // Then
        assertNotNull(username, "Username должен быть задан");
        assertNotNull(organization, "Organization должна быть задана");
        assertEquals(organization, "ОРГАНИЗАЦИЯ -1546025669", "Неверная организация для Кривоносова");
        assertEquals(user.getFullName(), "Кривоносов Александр Петрович", "Неверное ФИО");
    }
    
    @Test(groups = {"unit", "security"}, 
          testName = "#3 testBezdomniyUserCredentials",
          description = "Проверка credentials пользователя Бездомный")
    @AllureId("USERS-003")
    public void testBezdomniyUserCredentials() {
        // Given
        TestUsers user = TestUsers.BEZDOMNIY_IVAN;
        
        // When
        String username = user.getUsername();
        String organization = user.getOrganization();
        
        // Then
        assertNotNull(username, "Username должен быть задан");
        assertNotNull(organization, "Organization должна быть задана");
        assertEquals(organization, "ОРГАНИЗАЦИЯ -2036470831", "Неверная организация для Бездомного");
        assertEquals(user.getFullName(), "Бездомный Иван Николаевич", "Неверное ФИО");
    }
    
    @Test(groups = {"unit", "security"}, 
          testName = "#8 testBabkinaUserCredentials",
          description = "Проверка credentials пользователя Бабкина")
    @AllureId("USERS-008")
    public void testBabkinaUserCredentials() {
        // Given
        TestUsers user = TestUsers.BABKINA_VERA;
        
        // When
        String username = user.getUsername();
        String organization = user.getOrganization();
        
        // Then
        assertNotNull(username, "Username должен быть задан");
        assertNotNull(organization, "Organization должна быть задана");
        assertEquals(organization, "ОРГАНИЗАЦИЯ -292426768", "Неверная организация для Бабкиной");
        assertEquals(user.getFullName(), "Бабкина Вера Васильевна", "Неверное ФИО");
    }
    
    @Test(groups = {"unit", "security"})
    public void testDefaultUserCredentials() {
        // Given
        TestUsers user = TestUsers.DEFAULT_USER;
        
        // When
        String username = user.getUsername();
        String credentialPrefix = user.getCredentialPrefix();
        
        // Then
        assertNotNull(username, "Username должен быть задан");
        assertEquals(credentialPrefix, "evs", "Неверный префикс для DEFAULT_USER");
        assertEquals(user.getFullName(), "Тестовый пользователь по умолчанию", "Неверное ФИО");
    }
    
    @Test(groups = {"unit", "security"}, 
          testName = "#6 testGetCredentialsObject",
          description = "Проверка получения объекта credentials")
    @AllureId("USERS-006")
    public void testGetCredentialsObject() {
        // Given
        TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
        
        // When
        UserCredentials credentials = user.getCredentials();
        
        // Then
        assertNotNull(credentials, "Credentials не должны быть null");
        assertNotNull(credentials.username, "Username должен быть задан");
        assertNotNull(credentials.password, "Password должен быть задан");
        assertTrue(credentials.isValid(), "Credentials должны быть валидны");
    }
    
    @Test(groups = {"unit", "security"})
    public void testHasCredentials() {
        // Given
        TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
        
        // When
        boolean hasCredentials = user.hasCredentials();
        
        // Then
        assertTrue(hasCredentials, "У пользователя должны быть все обязательные credentials");
    }
    
    @Test(groups = {"unit", "security"}, 
          testName = "#4 testCredentialPrefix",
          description = "Проверка префиксов credentials")
    @AllureId("USERS-004")
    public void testCredentialPrefix() {
        // Given & When & Then
        assertEquals(TestUsers.KRIVONOSOV_ALEXANDER.getCredentialPrefix(), "evs.user1");
        assertEquals(TestUsers.BEZDOMNIY_IVAN.getCredentialPrefix(), "evs.user2");
        assertEquals(TestUsers.BABKINA_VERA.getCredentialPrefix(), "evs.user3");
        assertEquals(TestUsers.DEFAULT_USER.getCredentialPrefix(), "evs");
    }
    
    @Test(groups = {"unit", "security"}, 
          testName = "#9 testToString",
          description = "Проверка метода toString()")
    @AllureId("USERS-009")
    public void testToString() {
        // Given
        TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
        
        // When
        String result = user.toString();
        
        // Then
        assertNotNull(result, "toString() не должен возвращать null");
        assertTrue(result.contains(user.getFullName()), "toString() должен содержать ФИО");
    }
    
    @Test(groups = {"unit", "security"})
    public void testAllUsersHaveUniqueCredentialPrefix() {
        // Given
        TestUsers[] allUsers = TestUsers.values();
        
        // When & Then
        for (int i = 0; i < allUsers.length; i++) {
            for (int j = i + 1; j < allUsers.length; j++) {
                assertNotEquals(
                    allUsers[i].getCredentialPrefix(), 
                    allUsers[j].getCredentialPrefix(),
                    String.format("Пользователи %s и %s имеют одинаковый credential prefix", 
                        allUsers[i], allUsers[j])
                );
            }
        }
    }
    
    @Test(groups = {"unit", "security"}, 
          testName = "#1 testAllUsersHaveFullName",
          description = "Проверка наличия ФИО у всех пользователей")
    @AllureId("USERS-001")
    public void testAllUsersHaveFullName() {
        // Given
        TestUsers[] allUsers = TestUsers.values();
        
        // When & Then
        for (TestUsers user : allUsers) {
            assertNotNull(user.getFullName(), "У пользователя " + user + " должно быть ФИО");
            assertFalse(user.getFullName().trim().isEmpty(), "ФИО не должно быть пустым");
        }
    }
}
