package com.gateway.fee.api.dto.request;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

//here we have everything notnull because all of the fields are required, and we also have positive annotation for amount to be bigger than 0.
@Getter
@NoArgsConstructor
public class FeeCalculationRequest {
    @NotNull
    private String transactionId;
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotNull
    private Currency sourceCurrency;
    @NotNull
    private Currency destinationCurrency;
    @NotNull
    private TransactionType transactionType;
    @NotNull
    private String senderId;
    @NotNull
    private UserType senderUserType;
    @NotNull
    private String receiverId;
    @NotNull
    private UserType receiverUserType;
}
