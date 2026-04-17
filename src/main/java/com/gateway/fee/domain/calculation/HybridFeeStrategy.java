package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.FeeSideDefinition;
import java.math.BigDecimal;
// def of class : returns a flat amount plus a percentage of the transaction amount

public class HybridFeeStrategy  implements FeeCalculationStrategy {
    @Override
    public BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee) {
        return fee.getPercentage().multiply(amount).add(fee.getFlatAmount());
    }
}