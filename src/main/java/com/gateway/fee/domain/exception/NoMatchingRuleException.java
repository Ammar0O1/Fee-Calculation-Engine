package com.gateway.fee.domain.exception;

public class NoMatchingRuleException extends RuntimeException {
    public NoMatchingRuleException(String message) {
        super(message);
    }
}
