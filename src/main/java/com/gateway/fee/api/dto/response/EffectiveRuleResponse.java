package com.gateway.fee.api.dto.response;

import lombok.Builder;
import lombok.Getter;
//DEF we are using FeeRule DTO Class but wrapping it with two new fields
@Getter
@Builder
public class EffectiveRuleResponse {
    private  FeeRuleResponse rule;
    //label will determine if values are CUSTOM, INHERITED OR DEFAULT
    private RuleOrigin  label;
}
