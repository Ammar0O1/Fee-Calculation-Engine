package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.FeeSideDefinition;
import java.math.BigDecimal;
// def of class : returns a percentage of the transaction amount

public class PercentageFeeStrategy implements  FeeCalculationStrategy {
    @Override
    public BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee) {
        return fee.getPercentage().multiply(amount);
    }
}
