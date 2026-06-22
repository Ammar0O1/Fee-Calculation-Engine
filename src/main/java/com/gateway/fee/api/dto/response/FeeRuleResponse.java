package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FeeRuleResponse {
    private final UUID ruleId;
    private final String userId;
    private final UserType userType;
    private final TransactionType transactionType;
    private final Currency sourceCurrency;
    private final Currency destinationCurrency;
    private final FeeSideDefinitionResponse senderFee;
    private final FeeSideDefinitionResponse receiverFee;
    private final LocalDateTime effectiveDate;
    private final boolean active;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
