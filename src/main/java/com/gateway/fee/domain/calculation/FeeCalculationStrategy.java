package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.FeeSideDefinition;
import java.math.BigDecimal;

public interface FeeCalculationStrategy {
    BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee);
}
