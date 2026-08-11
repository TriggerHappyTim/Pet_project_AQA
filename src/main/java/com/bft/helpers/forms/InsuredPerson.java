package com.bft.helpers.forms;

/**
 * Модель застрахованного лица
 * 
 * Представляет данные о застрахованном лице: ФИО, СНИЛС, ИНН.
 * Используется для хранения и передачи данных о сотруднике.
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class InsuredPerson {
    
    private final String lastName;
    private final String firstName;
    private final String middleName;
    private final String snils;
    private final String inn;
    
    /**
     * Конструктор
     * 
     * @param lastName фамилия
     * @param firstName имя
     * @param middleName отчество
     * @param snils СНИЛС
     * @param inn ИНН (может быть null)
     */
    public InsuredPerson(String lastName, String firstName, String middleName, String snils, String inn) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.snils = snils;
        this.inn = inn;
    }
    
    /**
     * Получает фамилию
     * 
     * @return фамилия
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Получает имя
     * 
     * @return имя
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Получает отчество
     * 
     * @return отчество
     */
    public String getMiddleName() {
        return middleName;
    }
    
    /**
     * Получает СНИЛС
     * 
     * @return СНИЛС
     */
    public String getSnils() {
        return snils;
    }
    
    /**
     * Получает ИНН
     * 
     * @return ИНН или null если не указан
     */
    public String getInn() {
        return inn;
    }
    
    /**
     * Возвращает полное ФИО
     * 
     * @return строка в формате "Фамилия Имя Отчество"
     */
    public String getFullName() {
        return lastName + " " + firstName + " " + middleName;
    }
    
    @Override
    public String toString() {
        return "InsuredPerson{" +
                "lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", snils='" + snils + '\'' +
                ", inn='" + inn + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        InsuredPerson that = (InsuredPerson) o;
        
        if (!lastName.equals(that.lastName)) return false;
        if (!firstName.equals(that.firstName)) return false;
        if (!middleName.equals(that.middleName)) return false;
        if (!snils.equals(that.snils)) return false;
        return inn != null ? inn.equals(that.inn) : that.inn == null;
    }
    
    @Override
    public int hashCode() {
        int result = lastName.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + middleName.hashCode();
        result = 31 * result + snils.hashCode();
        result = 31 * result + (inn != null ? inn.hashCode() : 0);
        return result;
    }
}
