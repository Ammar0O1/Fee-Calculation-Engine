package com.gateway.fee.infrastructure.dto;
import com.gateway.fee.domain.model.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeeRuleDto {
        private String ruleId;
        private String userId;
        private UserType userType;
        private TransactionType transactionType;
        private Currency sourceCurrency;
        private Currency destinationCurrency;
        private FeeSideDefinitionDto senderFee;
        private FeeSideDefinitionDto receiverFee;
        private LocalDateTime effectiveDate;
        private boolean active;
        private String description;
}
