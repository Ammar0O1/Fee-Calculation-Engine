package com.gateway.fee.domain.exception;

public class InvalidRuleException extends RuntimeException {
    public InvalidRuleException(String message) {
        super(message);
    }
}