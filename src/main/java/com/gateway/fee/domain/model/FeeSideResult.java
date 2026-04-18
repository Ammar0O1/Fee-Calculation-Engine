package com.gateway.fee.domain.model;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;
// def of class : An immutable record of how a fee was calculated for one side of a transaction, tracking the raw amount,
// rounding, caps applied, and which rule was used.
@Getter
public class FeeSideResult {
    private final UUID userId;
    private final UserType userType;
    private final UUID matchedRuleId;
    private final String matchLevel;
    private final Currency currency;
    private final BigDecimal rawFee;
    private final BigDecimal roundedFee;
    private final String capApplied;
    private final BigDecimal capAdjustment;
    private final BigDecimal finalFee;
    private final FeeSideDefinition feeDefinitionUsed;
    private final boolean waived;

    public FeeSideResult(UUID userId, UserType userType, UUID matchedRuleId,
                         String matchLevel, Currency currency, BigDecimal rawFee,
                         BigDecimal roundedFee, String capApplied, BigDecimal capAdjustment,
                         BigDecimal finalFee, FeeSideDefinition feeDefinitionUsed, boolean waived) {
        this.userId = userId;
        this.userType = userType;
        this.matchedRuleId = matchedRuleId;
        this.matchLevel = matchLevel;
        this.currency = currency;
        this.rawFee = rawFee;
        this.roundedFee = roundedFee;
        this.capApplied = capApplied;
        this.capAdjustment = capAdjustment;
        this.finalFee = finalFee;
        this.feeDefinitionUsed = feeDefinitionUsed;
        this.waived = waived;
    }
}