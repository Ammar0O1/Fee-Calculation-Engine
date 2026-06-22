package com.gateway.fee.api.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TierBracketResponse {
    private final BigDecimal fromAmount;
    private final BigDecimal toAmount;
    private final BigDecimal rate;
}
