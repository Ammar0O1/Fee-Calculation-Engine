package com.gateway.fee.infrastructure.dto;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.TierBracket;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FeeSideDefinitionDto {
    private  CalculationMode calculationMode;
    private  BigDecimal flatAmount;
    private  BigDecimal percentage;
    private  List<TierBracketDto> tiers;
    private  BigDecimal minCap;
    private  BigDecimal maxCap;
}
