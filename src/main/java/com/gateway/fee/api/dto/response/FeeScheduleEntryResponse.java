package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeeScheduleEntryResponse {
    private UserType userType;
    private TransactionType transactionType;
    private Currency sourceCurrency;
    private Currency destinationCurrency;
    private FeeSideDefinitionResponse senderFee;
    private FeeSideDefinitionResponse receiverFee;
}
