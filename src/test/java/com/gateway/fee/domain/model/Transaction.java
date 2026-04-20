package com.gateway.fee.domain.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class Transaction {
    String transactionId;
    String userId;
    UserType userType;
    TransactionType transactionType;
    BigDecimal amount;
    Currency currency;
    Currency destinationCurrency;
    LocalDateTime timestamp;
}
