package com.gateway.fee.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

// this class takes the request body for creating a default rule, thats why we have the fields of TxType and the rest.
//but why not userId and userType? because in default rule they are wildcard we dont need to have them
@Data
public class CreateDefaultRuleRequest {
    private String transactionType;
    private String sourceCurrency;
    private String destinationCurrency;
    private FeeSideDefinitionRequest senderFee;
    private FeeSideDefinitionRequest receiverFee;

    @NotNull
    private String description;
}
