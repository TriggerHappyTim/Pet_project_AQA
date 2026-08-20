package com.bft.pw;

import java.time.Duration;
import java.util.function.Function;

/**
 * Ожидание, имитирующее Selenium {@code FluentWait}/{@code Wait}.
 */
public class Wait<T> {

    private Duration timeout = Duration.ofSeconds(4);
    private Duration pollingInterval = Duration.ofMillis(100);

    public Wait<T> withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public Wait<T> pollingEvery(Duration interval) {
        this.pollingInterval = interval;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <V> V until(Function<? super T, V> isTrue) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Throwable lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                V result = isTrue.apply((T) PwSession.driver());
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
                Thread.sleep(pollingInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new TimeoutException("Wait condition not satisfied within " + timeout, lastError);
    }
}