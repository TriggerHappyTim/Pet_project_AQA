package com.bft.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для связи теста с требованиями/спецификациями
 * Позволяет отслеживать покрытие требований тестами
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirement {

    /**
     * ID требования в системе управления требованиями
     */
    String id();

    /**
     * Название требования
     */
    String name();

    /**
     * Источник требования (документ, система)
     */
    String source() default "";

    /**
     * Версия требования
     */
    String version() default "1.0";

    /**
     * Автор требования
     */
    String author() default "";

    /**
     * Дата создания требования
     */
    String created() default "";

    /**
     * Статус требования
     */
    Status status() default Status.ACTIVE;

    /**
     * Статусы требований
     */
    enum Status {
        ACTIVE("Активное"),
        DEPRECATED("Устаревшее"),
        IMPLEMENTED("Реализовано"),
        VERIFIED("Проверено");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}