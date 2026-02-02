package com.bft.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматизированных действий
 * Используется для логирования и отчетности действий в тестах
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutomationAction {

    /**
     * Название действия для отчетов
     */
    String value();

    /**
     * Тип действия
     */
    ActionType type() default ActionType.ACTION;

    /**
     * Описание действия
     */
    String description() default "";

    /**
     * Важность логирования
     */
    LogLevel logLevel() default LogLevel.INFO;

    /**
     * Нужно ли делать скриншот после действия
     */
    boolean takeScreenshot() default false;

    /**
     * Таймаут выполнения действия (в секундах)
     */
    int timeout() default 30;

    /**
     * Типы действий
     */
    enum ActionType {
        ACTION("Действие"),
        VERIFICATION("Проверка"),
        SETUP("Подготовка"),
        CLEANUP("Очистка"),
        NAVIGATION("Навигация"),
        INPUT("Ввод данных"),
        ASSERTION("Утверждение");

        private final String displayName;

        ActionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Уровни логирования
     */
    enum LogLevel {
        TRACE("Трассировка"),
        DEBUG("Отладка"),
        INFO("Информация"),
        WARN("Предупреждение"),
        ERROR("Ошибка");

        private final String displayName;

        LogLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}