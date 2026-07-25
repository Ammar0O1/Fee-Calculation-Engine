package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder
public class FeeCalculationResponse {
    private String transactionId;
    private BigDecimal transactionAmount;
    private Currency sourceCurrency;
    private Currency destinationCurrency;
    private TransactionType transactionType;
    private LocalDateTime calculatedAt;
    private FeeSideResultResponse senderResult;
    private FeeSideResultResponse receiverResult;
}
