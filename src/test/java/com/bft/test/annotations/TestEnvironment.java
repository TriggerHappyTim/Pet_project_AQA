package com.bft.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания требований к тестовому окружению
 * Используется для проверки совместимости перед запуском теста
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestEnvironment {

    /**
     * Требуемые браузеры
     */
    Browser[] browsers() default {};

    /**
     * Требуемые операционные системы
     */
    OS[] operatingSystems() default {};

    /**
     * Минимальная версия Java
     */
    String minJavaVersion() default "";

    /**
     * Требуемые системные свойства
     */
    String[] requiredProperties() default {};

    /**
     * Требуемые переменные окружения
     */
    String[] requiredEnvironmentVariables() default {};

    /**
     * Требуемые разрешения/права
     */
    String[] requiredPermissions() default {};

    /**
     * Поддерживаемые браузеры
     */
    enum Browser {
        CHROME("Google Chrome"),
        FIREFOX("Mozilla Firefox"),
        EDGE("Microsoft Edge"),
        SAFARI("Apple Safari"),
        OPERA("Opera"),
        YANDEX("Yandex Browser"),
        IE("Internet Explorer");

        private final String displayName;

        Browser(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Поддерживаемые ОС
     */
    enum OS {
        WINDOWS("Windows"),
        LINUX("Linux"),
        MACOS("macOS"),
        UNIX("Unix");

        private final String displayName;

        OS(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}