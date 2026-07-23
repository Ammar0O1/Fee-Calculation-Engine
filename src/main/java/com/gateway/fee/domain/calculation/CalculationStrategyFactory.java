package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.CalculationMode;
//def of class: when you need to pick a strategy you use this class
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
