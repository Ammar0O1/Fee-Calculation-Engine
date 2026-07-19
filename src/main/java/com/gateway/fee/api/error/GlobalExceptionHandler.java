package com.gateway.fee.api.error;

import com.gateway.fee.domain.exception.DuplicateRuleException;
import com.gateway.fee.domain.exception.InvalidRuleException;
import com.gateway.fee.domain.exception.NoMatchingRuleException;
import com.gateway.fee.domain.exception.RuleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 422: no matching rule (business rule violation)
@ExceptionHandler(NoMatchingRuleException.class)
    public ResponseEntity<ErrorResponse> handleNoMatchingRuleException(NoMatchingRuleException ex, HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(422,"Unprocessable Entity", ex.getMessage(), LocalDateTime.now(),request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);

}
// 409: duplicate rule (conflict)
@ExceptionHandler(DuplicateRuleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRuleException(DuplicateRuleException ex, HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(409,"Conflict", ex.getMessage(), LocalDateTime.now(),request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}
// 404: not found
@ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRuleNotFoundException(RuleNotFoundException ex, HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
}

// 400: validation errors
@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(400,"Bad Request", ex.getMessage(), LocalDateTime.now(),request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400: invalid rule configuration (manual validation)
    @ExceptionHandler(InvalidRuleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRuleException(InvalidRuleException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(400, "Bad Request", ex.getMessage(), LocalDateTime.now(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
