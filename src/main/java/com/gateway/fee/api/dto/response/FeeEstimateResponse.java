package com.gateway.fee.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
//feat: add FeeEstimateResponse wrapper and EstimateMode enum for dual-mode estimate endpoint
public class FeeEstimateResponse {
    private EstimateMode mode;
    private FeeCalculationResponse calculation;
    private FeeRuleResponse ruleDetails;
}
