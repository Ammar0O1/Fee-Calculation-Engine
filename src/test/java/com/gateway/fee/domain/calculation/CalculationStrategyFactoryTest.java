package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationStrategyFactoryTest {

    private final CalculationStrategyFactory factory = new CalculationStrategyFactory();

    @Test
    void shouldReturnFlatStrategyForFlatMode() {
        FeeCalculationStrategy strategy = factory.getStrategy(CalculationMode.FLAT);
        assertThat(strategy).isInstanceOf(FlatFeeStrategy.class);
    }

    @Test
    void shouldReturnPercentageStrategyForPercentageMode() {
        FeeCalculationStrategy strategy = factory.getStrategy(CalculationMode.PERCENTAGE);
        assertThat(strategy).isInstanceOf(PercentageFeeStrategy.class);
    }

    @Test
    void shouldReturnHybridStrategyForHybridMode() {
        FeeCalculationStrategy strategy = factory.getStrategy(CalculationMode.HYBRID);
        assertThat(strategy).isInstanceOf(HybridFeeStrategy.class);
    }

    @Test
    void shouldReturnFlatTierStrategyForFlatTierMode() {
        FeeCalculationStrategy strategy = factory.getStrategy(CalculationMode.TIERED_FLAT);
        assertThat(strategy).isInstanceOf(FlatTierFeeStrategy.class);
    }

    @Test
    void shouldReturnMarginalTierStrategyForMarginalTierMode() {
        FeeCalculationStrategy strategy = factory.getStrategy(CalculationMode.TIERED_MARGINAL);
        assertThat(strategy).isInstanceOf(MarginalTierFeeStrategy.class);
    }
}