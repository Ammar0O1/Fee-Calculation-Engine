package com.gateway.fee.api.error;

import com.gateway.fee.domain.exception.DuplicateRuleException;
import com.gateway.fee.domain.exception.NoMatchingRuleException;
import com.gateway.fee.domain.exception.RuleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/*
 Unit tests for GlobalExceptionHandler.

 Calls each handler method directly with a sample exception and a mock
 request, then verifies the returned ResponseEntity has the correct HTTP
 status and a fully-populated ErrorResponse body.

 No Spring context, no web server , pure unit test of the handler logic.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // a fake request so the handler can read a path from getRequestURI()
    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(path);
        return req;
    }

    @Test
    void shouldReturn422ForNoMatchingRule() {
        NoMatchingRuleException ex = new NoMatchingRuleException("no rule matched");

        ResponseEntity<ErrorResponse> response =
                handler.handleNoMatchingRuleException(ex, request("/api/fees/calculate"));

        // status is 422
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        // body is populated
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(422);
        assertThat(body.error()).isEqualTo("Unprocessable Entity");
        assertThat(body.message()).isEqualTo("no rule matched");
        assertThat(body.path()).isEqualTo("/api/fees/calculate");
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldReturn409ForDuplicateRule() {
        DuplicateRuleException ex = new DuplicateRuleException("Conflict");

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateRuleException(ex, request("/api/fees/calculate"));

        // status is 409
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // body is populated
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.error()).isEqualTo("Conflict");
        assertThat(body.message()).isEqualTo("Conflict");
        assertThat(body.path()).isEqualTo("/api/fees/calculate");
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldReturn404ForRuleNotFound() {
        RuleNotFoundException ex = new RuleNotFoundException("Not Found");

        ResponseEntity<ErrorResponse> response =
                handler.handleRuleNotFoundException(ex, request("/api/fees/calculate"));

        // status is 404
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // body is populated correctly
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).isEqualTo("Not Found");
        assertThat(body.path()).isEqualTo("/api/fees/calculate");
        assertThat(body.timestamp()).isNotNull();
    }
}