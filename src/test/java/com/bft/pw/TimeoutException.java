package com.bft.pw;

/**
 * Исключение таймаута, совместимое с {@code org.openqa.selenium.TimeoutException}.
 */
public class TimeoutException extends RuntimeException {

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}