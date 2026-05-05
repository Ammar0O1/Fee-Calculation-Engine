package com.gateway.fee.testutil;

import com.gateway.fee.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Custom test builder instead of Lombok's @Builder because:
//Lombok's @Builder doesn't support default values — our builder provides sensible defaults
//    so that FeeRuleBuilder.aRule().build() produces a valid rule with zero configuration
public class FeeRuleBuilder {

    private String ruleId = "default-rule";
    private String userId = null;
    private UserType userType = null;
    private TransactionType transactionType = null;
    private Currency sourceCurrency = null;
    private Currency destinationCurrency = null;
    private FeeSideDefinition senderFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
    private FeeSideDefinition receiverFee = null;
    private LocalDateTime effectiveDate = LocalDateTime.now();
    private boolean active = true;
    private String description = "Test rule";

    public static FeeRuleBuilder aRule() {
        return new FeeRuleBuilder();
    }

    public FeeRuleBuilder withRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public FeeRuleBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public FeeRuleBuilder withUserType(UserType userType) {
        this.userType = userType;
        return this;
    }

    public FeeRuleBuilder withTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public FeeRuleBuilder withSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
        return this;
    }

    public FeeRuleBuilder withDestinationCurrency(Currency destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
        return this;
    }

    public FeeRuleBuilder withSenderFee(FeeSideDefinition senderFee) {
        this.senderFee = senderFee;
        return this;
    }

    public FeeRuleBuilder withReceiverFee(FeeSideDefinition receiverFee) {
        this.receiverFee = receiverFee;
        return this;
    }

    public FeeRuleBuilder withEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
        return this;
    }

    public FeeRuleBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public FeeRuleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public FeeRule build() {
        return new FeeRule(ruleId, userId, userType, transactionType, sourceCurrency, destinationCurrency, senderFee, receiverFee, effectiveDate, active, description);
    }

}