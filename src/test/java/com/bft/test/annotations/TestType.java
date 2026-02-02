package com.bft.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания типа теста
 * Используется для категоризации и фильтрации тестов в отчетах
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestType {

    /**
     * Тип теста
     */
    Type value();

    /**
     * Дополнительное описание
     */
    String description() default "";

    /**
     * Типы тестов
     */
    enum Type {
        UI("User Interface"),
        API("Application Programming Interface"),
        INTEGRATION("Integration"),
        PERFORMANCE("Performance"),
        SECURITY("Security"),
        DATABASE("Database"),
        MOBILE("Mobile"),
        CROSS_BROWSER("Cross Browser"),
        REGRESSION("Regression"),
        SMOKE("Smoke"),
        ACCEPTANCE("Acceptance");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}