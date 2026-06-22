package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.CalculationMode;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class FeeSideDefinitionResponse {
    private  CalculationMode calculationMode;
    private  BigDecimal flatAmount;
    private  BigDecimal percentage;
    private  List<TierBracketResponse> tiers;
    private  BigDecimal minCap;
    private  BigDecimal maxCap;
}
