package com.gateway.fee.infrastructure.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TierBracketDto {
    private  BigDecimal fromAmount;
    private  BigDecimal toAmount;
    private  BigDecimal rate;
}
