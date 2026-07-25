package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.FeeSideDefinition;
import java.math.BigDecimal;
// def of class : returns a fixed fee regardless of transaction amount

public class FlatFeeStrategy implements FeeCalculationStrategy {

    @Override
    public BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee) {
        return fee.getFlatAmount();
    }
}
