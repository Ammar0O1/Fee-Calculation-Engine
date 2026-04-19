package com.gateway.fee.domain.model;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class FeeSideDefinitionTest {

    @Test
    void shouldCreateFlatFee() {
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null));
    }

    @Test
    void shouldThrowIfFlatFeeAmountIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeSideDefinition(CalculationMode.FLAT, null, null, null, null, null));
    }

    @Test
    void shouldCreatePercentageFee() {
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.PERCENTAGE, null, new BigDecimal("0.015"), null, null, null));
    }

    @Test
    void shouldThrowIfPercentageIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeSideDefinition(CalculationMode.PERCENTAGE, null, null, null, null, null));
    }

    @Test
    void shouldCreateHybridFee() {
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.HYBRID, new BigDecimal("5"), new BigDecimal("0.01"), null, null, null));
    }

    @Test
    void shouldThrowIfHybridFlatAmountIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeSideDefinition(CalculationMode.HYBRID, null, new BigDecimal("0.01"), null, null, null));
    }

    @Test
    void shouldThrowIfHybridPercentageIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeSideDefinition(CalculationMode.HYBRID, new BigDecimal("5"), null, null, null, null));
    }

    @Test
    void shouldCreateTieredFlatFee() {
        List<TierBracket> tiers = List.of(new TierBracket(BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("0.02")));
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.TIERED_FLAT, null, null, tiers, null, null));
    }

    @Test
    void shouldThrowIfTieredFlatTiersIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new FeeSideDefinition(CalculationMode.TIERED_FLAT, null, null, null, null, null));
    }

    @Test
    void shouldCreateTieredMarginalFee() {
        List<TierBracket> tiers = List.of(new TierBracket(BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("0.02")));
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.TIERED_MARGINAL, null, null, tiers, null, null));
    }

    @Test
    void shouldCreateWithValidCaps() {
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, new BigDecimal("5"), new BigDecimal("50")));
    }

    @Test
    void shouldThrowIfMinCapGreaterThanMaxCap() {
        assertThrows(IllegalArgumentException.class, () -> new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, new BigDecimal("50"), new BigDecimal("5")));
    }

    @Test
    void shouldCreateWithOnlyMinCap() {
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, new BigDecimal("5"), null));
    }

    @Test
    void shouldCreateWithOnlyMaxCap() {
        assertDoesNotThrow(() -> new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, new BigDecimal("50")));
    }
}
