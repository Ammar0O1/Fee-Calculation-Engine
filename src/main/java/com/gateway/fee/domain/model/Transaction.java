package com.gateway.fee.domain.model;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class Transaction {
    private final String transactionId;
    private final BigDecimal amount;
    private final Currency sourceCurrency;
    private final Currency destinationCurrency;
    private final TransactionType transactionType;
    private final String senderId;
    private final UserType senderUserType;
    private final String receiverId;
    private final UserType receiverUserType;

    public Transaction(String transactionId, BigDecimal amount, Currency sourceCurrency, Currency destinationCurrency, TransactionType transactionType, String senderId, UserType senderUserType, String receiverId, UserType receiverUserType) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId is required");
        if (this.transactionId.isBlank()) {// using isBlank instad of isEmpty to also catch strings that are just spaces "   "
            throw new IllegalArgumentException("transactionId must not be empty");
        }
        this.amount = Objects.requireNonNull(amount, "amount is required");
        if (this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }

        // java built-in for null prevention
        this.sourceCurrency = Objects.requireNonNull(sourceCurrency, "sourceCurrency is required");
        this.destinationCurrency = Objects.requireNonNull(destinationCurrency, "destinationCurrency is required");
        this.transactionType = Objects.requireNonNull(transactionType, "transactionType is required");
        this.senderId = Objects.requireNonNull(senderId, "senderId is required");
        this.senderUserType = Objects.requireNonNull(senderUserType, "senderUserType is required");
        this.receiverId = Objects.requireNonNull(receiverId, "receiverId is required");
        this.receiverUserType = Objects.requireNonNull(receiverUserType, "receiverUserType is required");
    }
}
