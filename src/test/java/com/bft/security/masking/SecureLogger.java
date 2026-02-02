package com.bft.security.masking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Безопасный логгер с автоматической маскировкой чувствительных данных
 * Все сообщения автоматически проверяются на наличие чувствительных данных
 */
public class SecureLogger {

    private final Logger delegate;
    private final DataMasker masker;

    private SecureLogger(Class<?> clazz) {
        this.delegate = LoggerFactory.getLogger(clazz);
        this.masker = new CompositeDataMasker();
    }

    private SecureLogger(String name) {
        this.delegate = LoggerFactory.getLogger(name);
        this.masker = new CompositeDataMasker();
    }

    /**
     * Создать безопасный логгер для класса
     */
    public static SecureLogger getLogger(Class<?> clazz) {
        return new SecureLogger(clazz);
    }

    /**
     * Создать безопасный логгер по имени
     */
    public static SecureLogger getLogger(String name) {
        return new SecureLogger(name);
    }

    /**
     * Замаскировать сообщение перед логированием
     */
    private String maskMessage(String message, Object... args) {
        if (message == null) {
            return null;
        }

        // Сначала форматируем сообщение с аргументами
        String formattedMessage = formatMessage(message, args);

        // Затем маскируем чувствительные данные
        if (masker.containsSensitiveData(formattedMessage)) {
            formattedMessage = masker.mask(formattedMessage);
        }

        return formattedMessage;
    }

    /**
     * Форматировать сообщение с аргументами
     */
    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        try {
            return String.format(message, args);
        } catch (Exception e) {
            // Если форматирование не удалось, возвращаем оригинальное сообщение
            return message;
        }
    }

    // Методы логирования с автоматической маскировкой

    public void trace(String message, Object... args) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(maskMessage(message, args));
        }
    }

    public void debug(String message, Object... args) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(maskMessage(message, args));
        }
    }

    public void info(String message, Object... args) {
        if (delegate.isInfoEnabled()) {
            delegate.info(maskMessage(message, args));
        }
    }

    public void warn(String message, Object... args) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(maskMessage(message, args));
        }
    }

    public void error(String message, Object... args) {
        if (delegate.isErrorEnabled()) {
            delegate.error(maskMessage(message, args));
        }
    }

    public void error(String message, Throwable throwable, Object... args) {
        if (delegate.isErrorEnabled()) {
            delegate.error(maskMessage(message, args), throwable);
        }
    }

    // Методы для логирования без маскировки (для внутренних нужд)

    public void traceUnsafe(String message, Object... args) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(message, args);
        }
    }

    public void debugUnsafe(String message, Object... args) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(message, args);
        }
    }

    public void infoUnsafe(String message, Object... args) {
        if (delegate.isInfoEnabled()) {
            delegate.info(message, args);
        }
    }

    public void warnUnsafe(String message, Object... args) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(message, args);
        }
    }

    public void errorUnsafe(String message, Object... args) {
        if (delegate.isErrorEnabled()) {
            delegate.error(message, args);
        }
    }

    public void errorUnsafe(String message, Throwable throwable, Object... args) {
        if (delegate.isErrorEnabled()) {
            delegate.error(message, args, throwable);
        }
    }

    // Проверки уровня логирования

    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    /**
     * Получить доступ к базовому логгеру (для специальных случаев)
     */
    public Logger getDelegate() {
        return delegate;
    }
}