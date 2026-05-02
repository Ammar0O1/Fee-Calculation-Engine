package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.FeeSideDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HybridFeeStrategyTest {

    private final HybridFeeStrategy strategy = new HybridFeeStrategy();

    private final FeeSideDefinition fee = new FeeSideDefinition(
            CalculationMode.HYBRID,
            new BigDecimal("2"),
            new BigDecimal("0.005"),
            null, null, null
    );

    @Test
    void shouldAddFlatAndPercentageParts() {
        BigDecimal result = strategy.calculate(new BigDecimal("1000"), fee);
        // 2 + (1000 × 0.005) = 2 + 5 = 7
        assertThat(result).isEqualByComparingTo(new BigDecimal("7"));
    }

    @Test
    void shouldReturnFlatPartWhenAmountIsZero() {
        BigDecimal result = strategy.calculate(new BigDecimal("0"), fee);
        // 2 + (0 × 0.005) = 2
        assertThat(result).isEqualByComparingTo(new BigDecimal("2"));
    }
}