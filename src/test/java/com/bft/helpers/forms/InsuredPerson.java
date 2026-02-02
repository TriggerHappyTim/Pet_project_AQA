package com.bft.helpers.forms;

public class InsuredPerson {
    private String lastName;
    private String firstName;
    private String middleName;
    private String snils;
    private String inn;

    public InsuredPerson(String lastName, String firstName, String middleName, String snils, String inn) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.snils = snils;
        this.inn = inn;
    }

    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getSnils() { return snils; }
    public String getInn() { return inn; }

    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setSnils(String snils) { this.snils = snils; }
    public void setInn(String inn) { this.inn = inn; }

    @Override
    public String toString() {
        return String.format("InsuredPerson{lastName='%s', firstName='%s', middleName='%s', snils='%s', inn='%s'}",
                lastName, firstName, middleName, snils, inn);
    }
}