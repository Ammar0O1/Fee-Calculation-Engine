package com.gateway.fee.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
//DEF we are using FeeRule DTO Class but wrapping it with two new fields
@Getter
@AllArgsConstructor
public class EffectiveRuleResponse {
    private  FeeRuleResponse rule;
    //label will determine if values are CUSTOM, INHERITED OR DEFAULT
    private String label;
}
