package com.gateway.fee.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FeeRuleTest {

    private FeeSideDefinition validFee() {
        return new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
    }

    @Test
    void shouldCreateWithBothFees() {
        assertDoesNotThrow(() -> new FeeRule(UUID.randomUUID(), null, null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, validFee(), validFee(), LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldCreateWithOnlySenderFee() {
        assertDoesNotThrow(() -> new FeeRule(UUID.randomUUID(), null, null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, validFee(), null, LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldCreateWithOnlyReceiverFee() {
        assertDoesNotThrow(() -> new FeeRule(UUID.randomUUID(), null, null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, null, validFee(), LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldThrowIfBothFeesAreNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeRule(UUID.randomUUID(), null, null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, null, null, LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldCreateIfUserIdAndUserTypeAreSet() {
        assertDoesNotThrow(() -> new FeeRule(UUID.randomUUID(), "user-1", UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.EUR, validFee(), null, LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldThrowIfUserIdSetButUserTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeRule(UUID.randomUUID(), "user-1", null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, validFee(), null, LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldThrowIfRuleIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeRule(null, null, null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, validFee(), null, LocalDateTime.now(), true, "desc"));
    }

    @Test
    void shouldThrowIfEffectiveDateIsNull() {
        // Note: The current implementation of FeeRule does NOT have a null check for effectiveDate
        // but the requirements say "effectiveDate null — throws".
        // Since I'm not allowed to touch the code, this test might fail if the code doesn't implement it.
        // However, usually these descriptions imply expected behavior.
        assertThrows(IllegalArgumentException.class, () -> new FeeRule(UUID.randomUUID(), null, null, TransactionType.PAYMENT, Currency.USD, Currency.EUR, validFee(), null, null, true, "desc"));
    }
}
