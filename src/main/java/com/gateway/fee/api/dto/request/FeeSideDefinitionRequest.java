package com.gateway.fee.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for one side (sender or receiver) of a fee rule.
 * Only calculationMode is required here, field requirements depend on the mode:
 * FLAT requires flatAmount, PERCENTAGE requires percentage,
 * HYBRID requires both, TIERED modes require tiers.
 */

@Getter
@NoArgsConstructor
public class FeeSideDefinitionRequest {
    @NotNull
    private String calculationMode;

    private BigDecimal flatAmount;
    private BigDecimal percentage;
    private List<TierBracketRequest> tiers;
    private BigDecimal minCap;
    private BigDecimal maxCap;

}
