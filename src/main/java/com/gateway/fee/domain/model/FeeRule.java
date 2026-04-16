package com.gateway.fee.domain.model;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class FeeRule {
    private final String ruleId;
    private final String userId;
    private final UserType userType;
    private final TransactionType transactionType;
    private final Currency sourceCurrency;
    private final Currency destinationCurrency;
    private final FeeSideDefinition senderFee;
    private final FeeSideDefinition receiverFee;
    private final LocalDateTime effectiveDate;
    private final boolean active;
    private final String description;
    public FeeRule(String ruleId, String userId, UserType userType, TransactionType transactionType, Currency sourceCurrency, Currency destinationCurrency, FeeSideDefinition senderFee, FeeSideDefinition receiverFee, LocalDateTime effectiveDate, boolean active, String description){
//•	At least one of senderFee or receiverFee must be non-null
    if (senderFee == null && receiverFee == null){
        throw new IllegalArgumentException("Fee side definitions must not be null");
    }
//    •	If userId is set, userType must also be set
    if (userId != null && userType == null){
        throw new IllegalArgumentException("User type must not be null");
    }
    if(ruleId == null){
        throw new IllegalArgumentException("Rule id must not be null");
    }
    if (effectiveDate == null) {
        throw new IllegalArgumentException("Effective date must not be null");
    }
        this.ruleId = ruleId;
        this.userId = userId;
        this.userType = userType;
        this.transactionType = transactionType;
        this.sourceCurrency = sourceCurrency;
        this.destinationCurrency = destinationCurrency;
        this.senderFee = senderFee;
        this.receiverFee = receiverFee;
        this.effectiveDate = effectiveDate;
        this.active = active;
        this.description = description;
    }
}
