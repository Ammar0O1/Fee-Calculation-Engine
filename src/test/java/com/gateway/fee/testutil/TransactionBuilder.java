package com.gateway.fee.testutil;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.Transaction;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Custom test builder instead of Lombok's @Builder because:
//  Lombok's @Builder doesn't support default values — our builder provides sensible defaults
//    so that TransactionBuilder.aTransaction().build() produces a valid transaction with zero configuration
public class TransactionBuilder {

    private String transactionId = "tx-default";
    private BigDecimal amount = new BigDecimal("1000");
    private Currency sourceCurrency = Currency.USD;
    private Currency destinationCurrency = Currency.EUR;
    private TransactionType transactionType = TransactionType.PAYMENT;
    private String senderId = "sender-id";
    private UserType senderUserType = UserType.BUSINESS;
    private String receiverId = "receiver-id";
    private UserType receiverUserType = UserType.CORPORATE;
    private LocalDateTime timestamp = LocalDateTime.now();

    public static TransactionBuilder aTransaction() {
        return new TransactionBuilder();
    }

    public TransactionBuilder withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder withSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
        return this;
    }

    public TransactionBuilder withDestinationCurrency(Currency destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
        return this;
    }

    public TransactionBuilder withTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public TransactionBuilder withSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public TransactionBuilder withSenderUserType(UserType senderUserType) {
        this.senderUserType = senderUserType;
        return this;
    }

    public TransactionBuilder withReceiverId(String receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public TransactionBuilder withReceiverUserType(UserType receiverUserType) {
        this.receiverUserType = receiverUserType;
        return this;
    }

    public TransactionBuilder withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Transaction build() {
        return new Transaction(transactionId, amount, sourceCurrency, destinationCurrency, transactionType, senderId, senderUserType, receiverId, receiverUserType, timestamp);
    }
}