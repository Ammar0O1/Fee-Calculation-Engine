package com.gateway.fee.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TierBracketTest {

    @Test
    void shouldCreateValidBracket() {
        assertDoesNotThrow(() -> new TierBracket(BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("0.02")));
    }

    @Test
    void shouldCreateValidLastBracket() {
        assertDoesNotThrow(() -> new TierBracket(new BigDecimal("5000"), null, new BigDecimal("0.01")));
    }

    @Test
    void shouldThrowIfFromAmountIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new TierBracket(null, new BigDecimal("1000"), new BigDecimal("0.01")));
    }

    @Test
    void shouldThrowIfFromAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new TierBracket(new BigDecimal("-1"), new BigDecimal("1000"), new BigDecimal("0.01")));
    }

    @Test
    void shouldCreateIfFromAmountIsZero() {
        assertDoesNotThrow(() -> new TierBracket(BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("0.01")));
    }

    @Test
    void shouldThrowIfToAmountIsLessThanFromAmount() {
        assertThrows(IllegalArgumentException.class, () -> new TierBracket(new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("0.01")));
    }

    @Test
    void shouldThrowIfToAmountEqualsFromAmount() {
        assertThrows(IllegalArgumentException.class, () -> new TierBracket(new BigDecimal("1000"), new BigDecimal("1000"), new BigDecimal("0.01")));
    }

    @Test
    void shouldThrowIfRateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new TierBracket(new BigDecimal("1000"), new BigDecimal("2000"), null));
    }

    @Test
    void shouldThrowIfRateIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new TierBracket(new BigDecimal("1000"), new BigDecimal("2000"), new BigDecimal("-0.01")));
    }

    @Test
    void shouldCreateIfRateIsZero() {
        assertDoesNotThrow(() -> new TierBracket(new BigDecimal("1000"), new BigDecimal("2000"), BigDecimal.ZERO));
    }
}
