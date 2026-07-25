package com.gateway.fee.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

// def of class : An immutable record of how a fee was calculated for one side of a transaction, tracking the raw amount,
// rounding, caps applied, and which rule was used.
@Value
public class FeeSideResult {
     String userId;
     UserType userType;
     UUID matchedRuleId;
     String matchLevel;
     Currency currency;
     BigDecimal rawFee;
     BigDecimal roundedFee;
     String capApplied;
     BigDecimal capAdjustment;
     BigDecimal finalFee;
     FeeSideDefinition feeDefinitionUsed;
     boolean waived;
}