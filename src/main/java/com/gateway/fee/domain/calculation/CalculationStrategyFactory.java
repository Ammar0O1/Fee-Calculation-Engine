package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.CalculationMode;

public class CalculationStrategyFactory {
    public FeeCalculationStrategy getStrategy(CalculationMode mode) {
        return switch (mode) {
            case FLAT -> new FlatFeeStrategy();
            case PERCENTAGE -> new PercentageFeeStrategy();
            case HYBRID -> new HybridFeeStrategy();
            case TIERED_FLAT -> new FlatTierFeeStrategy();
            case TIERED_MARGINAL -> new MarginalTierFeeStrategy();
        };
    }
}
