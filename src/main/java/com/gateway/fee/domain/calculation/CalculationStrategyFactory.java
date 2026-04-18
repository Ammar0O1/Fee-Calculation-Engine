package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.CalculationMode;

public class CalculationStrategyFactory {
    public FeeCalculationStrategy getStrategy(CalculationMode mode) {
        switch (mode) {
            case FLAT: return new FlatFeeStrategy();
            case PERCENTAGE: return new PercentageFeeStrategy();
            case HYBRID: return new HybridFeeStrategy();
            case TIERED_FLAT: return new FlatTierFeeStrategy();
            case TIERED_MARGINAL: return new MarginalTierFeeStrategy();
            default: throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }
}
