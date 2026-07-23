package com.gateway.fee.api.dto.request;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class FeeEstimateRequest {
    @NotNull
    private UserType userType;
    private String userId;
    private TransactionType transactionType;
    private Currency sourceCurrency;
    private Currency destinationCurrency;
    private BigDecimal amount;

}
