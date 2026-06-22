package com.gateway.fee.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for a single tier bracket within a tiered fee definition.
 * Represents an amount range and the rate applied to that range.
 * validation is handled in the service layer.
 */

@Data
public class TierBracketRequest {
    @NotNull
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    @NotNull
    private BigDecimal rate;

    /**
     * the (toAmount) does not need to be NotNull,
     * becuase in last bracket it will be null since its the highest tier and has no upper bound. .
     */
}