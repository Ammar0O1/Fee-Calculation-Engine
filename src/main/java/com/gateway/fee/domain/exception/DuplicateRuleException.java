package com.gateway.fee.domain.exception;

public class DuplicateRuleException extends RuntimeException {
    public DuplicateRuleException(String message) {

        super(message);
    }
}
