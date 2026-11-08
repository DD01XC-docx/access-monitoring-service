package com.dd01xc.service.service.exception;

import java.time.Duration;

public class AccountLockedException extends RuntimeException {
    private final Duration retryAfter;

    public AccountLockedException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
