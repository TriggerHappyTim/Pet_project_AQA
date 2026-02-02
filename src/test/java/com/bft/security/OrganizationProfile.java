package com.bft.security;

/**
 * Профиль организации для авторизации в системе EVS
 * 
 * <p>Содержит информацию об организации, к которой привязан тестовый пользователь.
 * Используется для выбора карточки организации после авторизации через ЕПГУ.
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * {@code
 * OrganizationProfile org = new OrganizationProfile(
 *     "ОРГАНИЗАЦИЯ -1546025669",
 *     "Кривоносов Александр Петрович"
 * );
 * 
 * loginPage.selectUserCardEPGU(org.getOrganizationName());
 * }
 * </pre>
 */
public class OrganizationProfile {
    
    private final String organizationName;
    private final String ownerFullName;
    
    /**
     * Создать профиль организации
     * 
     * @param organizationName название организации (например, "ОРГАНИЗАЦИЯ -1546025669")
     * @param ownerFullName ФИО владельца или представителя организации
     */
    public OrganizationProfile(String organizationName, String ownerFullName) {
        if (organizationName == null || organizationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Название организации не может быть пустым");
        }
        if (ownerFullName == null || ownerFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("ФИО владельца не может быть пустым");
        }
        
        this.organizationName = organizationName;
        this.ownerFullName = ownerFullName;
    }
    
    /**
     * Получить название организации
     * 
     * @return название организации
     */
    public String getOrganizationName() {
        return organizationName;
    }
    
    /**
     * Получить ФИО владельца организации
     * 
     * @return ФИО владельца
     */
    public String getOwnerFullName() {
        return ownerFullName;
    }
    
    /**
     * Строковое представление профиля организации
     * 
     * @return строка вида "ОРГАНИЗАЦИЯ -1546025669 (Кривоносов Александр Петрович)"
     */
    @Override
    public String toString() {
        return organizationName + " (" + ownerFullName + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        OrganizationProfile that = (OrganizationProfile) o;
        return organizationName.equals(that.organizationName);
    }
    
    @Override
    public int hashCode() {
        return organizationName.hashCode();
    }
}
