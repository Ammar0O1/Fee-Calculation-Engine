package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.FeeSideDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PercentageFeeStrategyTest {

    private final PercentageFeeStrategy strategy = new PercentageFeeStrategy();

    private FeeSideDefinition feeWithPercentage(String percentage) {
        return new FeeSideDefinition(
                CalculationMode.PERCENTAGE,
                null, new BigDecimal(percentage), null, null, null
        );
    }

    @Test
    void shouldCalculatePercentageOfTransactionAmount() {
        BigDecimal result = strategy.calculate(new BigDecimal("10000"), feeWithPercentage("0.015"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("150"));
    }

    @Test
    void shouldCalculatePercentageForSmallAmount() {
        BigDecimal result = strategy.calculate(new BigDecimal("100"), feeWithPercentage("0.015"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("1.5"));
    }

    @Test
    void shouldKeepRawPrecisionBeforeRounding() {
        BigDecimal result = strategy.calculate(new BigDecimal("333.33"), feeWithPercentage("0.015"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("4.99995"));
    }

    @Test
    void shouldReturnZeroForZeroAmount() {
        BigDecimal result = strategy.calculate(new BigDecimal("0"), feeWithPercentage("0.015"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}