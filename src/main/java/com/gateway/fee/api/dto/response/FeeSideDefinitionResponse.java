package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.CalculationMode;
import java.util.List;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeSideDefinitionResponse {
    private final CalculationMode calculationMode;
    private final BigDecimal flatAmount;
    private final BigDecimal percentage;
    private final List<TierBracketResponse> tiers;
    private final BigDecimal minCap;
    private final BigDecimal maxCap;
}
