package com.gateway.fee.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Getter
@AllArgsConstructor
public class TierBracketResponse {
    private  BigDecimal fromAmount;
    private  BigDecimal toAmount;
    private  BigDecimal rate;
}
