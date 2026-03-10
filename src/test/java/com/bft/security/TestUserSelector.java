package com.bft.security;

/**
 * Утилита для выбора тестового пользователя из system property
 * 
 * <p>Используется для динамического выбора учётной записи через Maven profiles
 * или переменные окружения в CI/CD pipeline.
 * 
 * <p><b>Использование в CI/CD:</b>
 * <pre>
 * {@code
 * # В GitLab CI/CD выбираем USER_ACCOUNT: user_krivonosov
 * # Maven активирует profile:
 * mvn test -P user_krivonosov
 * 
 * # Profile устанавливает system property:
 * <systemPropertyVariables>
 *   <test.user>KRIVONOSOV_ALEXANDER</test.user>
 * </systemPropertyVariables>
 * 
 * # В тесте получаем пользователя:
 * TestUsers user = TestUserSelector.getSelectedUser();
 * steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
 * }
 * </pre>
 * 
 * <p><b>Использование в локальных тестах:</b>
 * <pre>
 * {@code
 * // Явно указываем пользователя в тесте
 * TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
 * 
 * // Или через system property
 * System.setProperty("test.user", "BEZDOMNIY_IVAN");
 * TestUsers user = TestUserSelector.getSelectedUser();
 * }
 * </pre>
 * 
 * @see TestUsers
 */
public class TestUserSelector {
    
    /**
     * System property для выбора пользователя
     */
    public static final String TEST_USER_PROPERTY = "test.user";
    
    /**
     * Пользователь по умолчанию (если не указан в system property)
     */
    public static final TestUsers DEFAULT_USER = TestUsers.KRIVONOSOV_ALEXANDER;
    
    /**
     * Получить выбранного пользователя из system property
     * 
     * <p>Ищет system property "test.user" и возвращает соответствующий enum.
     * Если property не установлен, возвращает {@link #DEFAULT_USER}.
     * 
     * <p><b>Примеры значений property:</b>
     * <ul>
     *   <li>"KRIVONOSOV_ALEXANDER" → TestUsers.KRIVONOSOV_ALEXANDER</li>
     *   <li>"BABKINA_VERA" → TestUsers.BABKINA_VERA (ЛК Страхователя)</li>
     *   <li>"BEZDOMNIY_IVAN" → TestUsers.BEZDOMNIY_IVAN (ЛК Архива)</li>
     *   <li>"DEFAULT_USER" → TestUsers.DEFAULT_USER</li>
     * </ul>
     * 
     * @return выбранный TestUsers или DEFAULT_USER
     * @throws IllegalArgumentException если указано неизвестное имя пользователя
     */
    public static TestUsers getSelectedUser() {
        String userProperty = System.getProperty(TEST_USER_PROPERTY);
        
        if (userProperty == null || userProperty.trim().isEmpty()) {
            return DEFAULT_USER;
        }
        
        try {
            return TestUsers.valueOf(userProperty.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Неизвестный пользователь в system property '%s': '%s'. " +
                    "Доступные значения: KRIVONOSOV_ALEXANDER, BABKINA_VERA, BEZDOMNIY_IVAN, DEFAULT_USER",
                    TEST_USER_PROPERTY, userProperty),
                e
            );
        }
    }
    
    /**
     * Получить выбранного пользователя с fallback на указанного по умолчанию
     * 
     * @param fallbackUser пользователь по умолчанию, если system property не установлен
     * @return выбранный TestUsers или fallbackUser
     */
    public static TestUsers getSelectedUser(TestUsers fallbackUser) {
        String userProperty = System.getProperty(TEST_USER_PROPERTY);
        
        if (userProperty == null || userProperty.trim().isEmpty()) {
            return fallbackUser;
        }
        
        try {
            return TestUsers.valueOf(userProperty.trim());
        } catch (IllegalArgumentException e) {
            return fallbackUser;
        }
    }
    
    /**
     * Проверить, установлен ли пользователь через system property
     * 
     * @return true, если system property "test.user" установлен
     */
    public static boolean isUserSelected() {
        String userProperty = System.getProperty(TEST_USER_PROPERTY);
        return userProperty != null && !userProperty.trim().isEmpty();
    }
    
    /**
     * Установить пользователя через system property (для тестов)
     * 
     * @param user пользователь для установки
     */
    public static void setSelectedUser(TestUsers user) {
        System.setProperty(TEST_USER_PROPERTY, user.name());
    }
    
    /**
     * Очистить system property (для тестов)
     */
    public static void clearSelectedUser() {
        System.clearProperty(TEST_USER_PROPERTY);
    }
}
