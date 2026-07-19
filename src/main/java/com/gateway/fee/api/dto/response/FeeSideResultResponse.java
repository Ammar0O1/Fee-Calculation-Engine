package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.UserType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FeeSideResultResponse {
    private String userId;
    private UserType userType;
    private String matchedRuleId;
    private String matchLevel;
    private Currency currency;
    private BigDecimal rawFee;
    private BigDecimal roundedFee;
    private String capApplied;
    private BigDecimal capAdjustment;
    private BigDecimal finalFee;
    private boolean waived;
}
