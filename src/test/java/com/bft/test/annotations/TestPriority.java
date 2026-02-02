package com.bft.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания приоритета/важности теста
 * Влияет на порядок выполнения и отчетность
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestPriority {

    /**
     * Приоритет теста
     */
    Priority value();

    /**
     * Причины выбора приоритета
     */
    String reason() default "";

    /**
     * Уровни приоритета
     */
    enum Priority {
        CRITICAL("Критический - блокирует релиз", 1),
        HIGH("Высокий - важная функциональность", 2),
        MEDIUM("Средний - второстепенная функциональность", 3),
        LOW("Низкий - косметические улучшения", 4),
        TRIVIAL("Тривиальный - незначительные изменения", 5);

        private final String description;
        private final int level;

        Priority(String description, int level) {
            this.description = description;
            this.level = level;
        }

        public String getDescription() {
            return description;
        }

        public int getLevel() {
            return level;
        }

        public boolean isHigherThan(Priority other) {
            return this.level < other.level;
        }
    }
}