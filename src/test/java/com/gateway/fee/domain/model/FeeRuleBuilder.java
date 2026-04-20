package com.gateway.fee.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FeeRuleBuilder {
    private String ruleId = UUID.randomUUID().toString();
    private String userId;
    private UserType userType;
    private TransactionType transactionType;
    private Currency sourceCurrency = Currency.USD;
    private Currency destinationCurrency = Currency.USD;
    private FeeSideDefinition senderFee;
    private FeeSideDefinition receiverFee;
    private LocalDateTime effectiveDate = LocalDateTime.now();
    private boolean active = true;
    private String description = "Test rule";

    public static FeeRuleBuilder aRule() {
        return new FeeRuleBuilder();
    }

    public FeeRuleBuilder forUserType(UserType userType) {
        this.userType = userType;
        return this;
    }

    public FeeRuleBuilder forTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
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

    public FeeRuleBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public FeeRuleBuilder withRuleId(String ruleId) {
        this.ruleId = ruleId;
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
        return new FeeRule(ruleId, userId, userType, transactionType, sourceCurrency, destinationCurrency,
                senderFee, receiverFee, effectiveDate, active, description);
    }
}
