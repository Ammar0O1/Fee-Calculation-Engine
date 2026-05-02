package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.FeeSideDefinition;
import com.gateway.fee.domain.model.TierBracket;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FlatTierFeeStrategyTest {

    private final FlatTierFeeStrategy strategy = new FlatTierFeeStrategy();

    private final List<TierBracket> tiers = List.of(
            new TierBracket(new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0.02")),
            new TierBracket(new BigDecimal("1000"), new BigDecimal("5000"), new BigDecimal("0.015")),
            new TierBracket(new BigDecimal("5000"), null, new BigDecimal("0.01"))
    );

    private final FeeSideDefinition fee = new FeeSideDefinition(
            CalculationMode.TIERED_FLAT,
            null, null, tiers, null, null
    );

    @Test
    void shouldUseFirstBracketRateForAmountInFirstBracket() {
        BigDecimal result = strategy.calculate(new BigDecimal("500"), fee);
        // 500 × 0.02 = 10
        assertThat(result).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void shouldUseSecondBracketRateAtBoundary() {
        BigDecimal result = strategy.calculate(new BigDecimal("1000"), fee);
        // 1000 falls into [1000-5000]: 1000 × 0.015 = 15
        assertThat(result).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void shouldUseSecondBracketRateForAmountInSecondBracket() {
        BigDecimal result = strategy.calculate(new BigDecimal("3000"), fee);
        // 3000 × 0.015 = 45
        assertThat(result).isEqualByComparingTo(new BigDecimal("45"));
    }

    @Test
    void shouldUseLastBracketRateForLargeAmount() {
        BigDecimal result = strategy.calculate(new BigDecimal("50000"), fee);
        // 50000 × 0.01 = 500
        assertThat(result).isEqualByComparingTo(new BigDecimal("500"));
    }
}