package com.gateway.fee.domain.model;


import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class FeeCalculationResult {
    String transactionId;
    BigDecimal transactionAmount;
    Currency sourceCurrency;
    Currency destinationCurrency;
    TransactionType transactionType	;
    FeeSideResult senderResult;
    FeeSideResult receiverResult;
    LocalDateTime calculatedAt;

}
