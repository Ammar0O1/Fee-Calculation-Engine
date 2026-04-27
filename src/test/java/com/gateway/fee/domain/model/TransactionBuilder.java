package com.gateway.fee.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionBuilder {
    private String transactionId = UUID.randomUUID().toString();
    private String userId;
    private UserType userType;
    private TransactionType transactionType;
    private BigDecimal amount;
    private Currency currency = Currency.USD;
    private Currency destinationCurrency = Currency.USD;
    private LocalDateTime timestamp = LocalDateTime.now();

    public static TransactionBuilder aTransaction() {
        return new TransactionBuilder();
    }

    public TransactionBuilder withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public TransactionBuilder forUserType(UserType userType) {
        this.userType = userType;
        return this;
    }

    public TransactionBuilder forTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public TransactionBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder withAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
        return this;
    }

    public TransactionBuilder withCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public TransactionBuilder withDestinationCurrency(Currency destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
        return this;
    }

    public TransactionBuilder withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Transaction build() {
        if (transactionType == null) {
            throw new IllegalStateException("transactionType is required");
        }
        if (amount == null) {
            throw new IllegalStateException("amount is required");
        }
        return Transaction.builder()
                .transactionId(transactionId)
                .senderId(userId)
                .senderUserType(userType)
                .receiverId("receiver-1") // Default or allow configuration
                .receiverUserType(UserType.PERSONAL)
                .transactionType(transactionType)
                .amount(amount)
                .sourceCurrency(currency)
                .destinationCurrency(destinationCurrency)
                .timestamp(timestamp)
                .build();
    }
}
