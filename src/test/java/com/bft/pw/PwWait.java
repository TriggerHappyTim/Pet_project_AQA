package com.bft.pw;

import java.time.Duration;
import java.util.function.Function;

/**
 * Ожидание, имитирующее Selenium {@code WebDriverWait}.
 * Опрошивает условие через текущий {@link PwDriver}.
 */
public class PwWait {

    private Duration timeout;
    private Duration polling;

    public PwWait(Duration timeout, Duration polling) {
        this.timeout = timeout;
        this.polling = polling;
    }

    public PwWait withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public PwWait pollingEvery(Duration polling) {
        this.polling = polling;
        return this;
    }

    public <V> V until(Function<PwDriver, V> isTrue) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Throwable lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                V result = isTrue.apply(PwSession.driver());
                if (result != null) {
                    if (result instanceof Boolean) {
                        if ((Boolean) result) {
                            return result;
                        }
                    } else {
                        return result;
                    }
                }
            } catch (Throwable t) {
                lastError = t;
            }
            try {
                Thread.sleep(polling.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new TimeoutException("Wait condition not satisfied within " + timeout, lastError);
    }
}