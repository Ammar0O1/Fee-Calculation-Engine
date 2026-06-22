package com.gateway.fee.api.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TierBracketResponse {
    private  BigDecimal fromAmount;
    private  BigDecimal toAmount;
    private  BigDecimal rate;
}
