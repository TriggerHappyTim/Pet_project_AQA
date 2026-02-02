package com.bft.testdata;

import com.github.javafaker.Faker;

import java.util.Locale;

/**
 * Базовый класс для создания тестовых данных с использованием Builder pattern
 * 
 * <p>Предоставляет базовую функциональность для создания тестовых данных:
 * <ul>
 *   <li>Интеграция с Faker для генерации реалистичных данных</li>
 *   <li>Базовые методы для работы с локализацией</li>
 *   <li>Общие утилиты для генерации данных</li>
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * User user = UserTestDataBuilder.createDefault()
 *     .withFirstName("Иван")
 *     .withLastName("Иванов")
 *     .withEmail("ivan@example.com")
 *     .build();
 * 
 * User randomUser = UserTestDataBuilder.createRandom().build();
 * }</pre>
 * 
 * @param <T> тип объекта, который будет построен
 * @param <B> тип builder класса (для fluent API)
 * @author QA Automation Team
 * @version 2.0
 * @since 2.0
 */
public abstract class TestDataBuilder<T, B extends TestDataBuilder<T, B>> {
    
    /**
     * Экземпляр Faker для генерации случайных данных
     * Использует русскую локаль для генерации данных на русском языке
     */
    protected static final Faker faker = new Faker(new Locale("ru"));
    
    /**
     * Строит финальный объект на основе настроек builder
     * 
     * @return построенный объект типа T
     */
    public abstract T build();
    
    /**
     * Возвращает текущий экземпляр builder для fluent API
     * 
     * @return текущий экземпляр builder
     */
    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }
    
    /**
     * Генерирует случайное целое число в указанном диапазоне
     * 
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @return случайное целое число
     */
    protected int randomInt(int min, int max) {
        return faker.number().numberBetween(min, max + 1);
    }
    
    /**
     * Генерирует случайное число с плавающей точкой в указанном диапазоне
     * 
     * @param min минимальное значение
     * @param max максимальное значение
     * @return случайное число с плавающей точкой
     */
    protected double randomDouble(double min, double max) {
        return faker.number().randomDouble(2, (long) min, (long) max);
    }
    
    /**
     * Генерирует случайную строку указанной длины
     * 
     * @param length длина строки
     * @return случайная строка
     */
    protected String randomString(int length) {
        return faker.lorem().characters(length);
    }
    
    /**
     * Генерирует случайный email адрес
     * 
     * @return случайный email адрес
     */
    protected String randomEmail() {
        return faker.internet().emailAddress();
    }
    
    /**
     * Генерирует случайный телефонный номер в российском формате
     * 
     * @return случайный телефонный номер (формат: +7-XXX-XXX-XX-XX)
     */
    protected String randomPhone() {
        return "+7-" + faker.number().numberBetween(900, 999) + "-" +
               faker.number().numberBetween(100, 999) + "-" +
               faker.number().numberBetween(10, 99) + "-" +
               faker.number().numberBetween(10, 99);
    }
}
