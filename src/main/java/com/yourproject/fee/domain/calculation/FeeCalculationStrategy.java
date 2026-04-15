package com.yourproject.fee.domain.calculation;

import com.yourproject.fee.domain.model.FeeSideDefinition;
import java.math.BigDecimal;

public interface FeeCalculationStrategy {
    BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee);
}
