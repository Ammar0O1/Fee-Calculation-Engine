package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.FeeSideDefinition;
import com.gateway.fee.domain.model.TierBracket;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MarginalTierFeeStrategyTest {

    private final MarginalTierFeeStrategy strategy = new MarginalTierFeeStrategy();

    private final List<TierBracket> tiers = List.of(
            new TierBracket(new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0.02")),
            new TierBracket(new BigDecimal("1000"), new BigDecimal("5000"), new BigDecimal("0.015")),
            new TierBracket(new BigDecimal("5000"), null, new BigDecimal("0.01"))
    );

    private final FeeSideDefinition fee = new FeeSideDefinition(
            CalculationMode.TIERED_MARGINAL,
            null, null, tiers, null, null
    );

    @Test
    void shouldUseSingleBracketWhenAmountFitsInFirstBracket() {
        BigDecimal result = strategy.calculate(new BigDecimal("500"), fee);
        // 500 × 0.02 = 10
        assertThat(result).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void shouldSplitAcrossTwoBrackets() {
        BigDecimal result = strategy.calculate(new BigDecimal("3000"), fee);
        // (1000 × 0.02) + (2000 × 0.015) = 20 + 30 = 50
        assertThat(result).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void shouldSplitAcrossAllBrackets() {
        BigDecimal result = strategy.calculate(new BigDecimal("8000"), fee);
        // (1000 × 0.02) + (4000 × 0.015) + (3000 × 0.01) = 20 + 60 + 30 = 110
        assertThat(result).isEqualByComparingTo(new BigDecimal("110"));
    }

    @Test
    void shouldEqualPercentageWhenSingleBracket() {
        FeeSideDefinition singleBracket = new FeeSideDefinition(
                CalculationMode.TIERED_MARGINAL, null, null,
                List.of(new TierBracket(new BigDecimal("0"), null, new BigDecimal("0.015"))),
                null, null
        );
        BigDecimal result = strategy.calculate(new BigDecimal("1000"), singleBracket);
        // 1000 × 0.015 = 15 (same as percentage)
        assertThat(result).isEqualByComparingTo(new BigDecimal("15"));
    }
}