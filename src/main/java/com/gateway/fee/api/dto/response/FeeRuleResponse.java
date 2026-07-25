package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class FeeRuleResponse {
    private  UUID ruleId;
    private  String userId;
    private  UserType userType;
    private  TransactionType transactionType;
    private  Currency sourceCurrency;
    private  Currency destinationCurrency;
    private  FeeSideDefinitionResponse senderFee;
    private  FeeSideDefinitionResponse receiverFee;
    private  LocalDateTime effectiveDate;
    private  boolean active;
    private  String description;
    private  LocalDateTime createdAt;
    private  LocalDateTime updatedAt;
}