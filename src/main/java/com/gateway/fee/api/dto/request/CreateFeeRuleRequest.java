package com.gateway.fee.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating user-type and user-specific fee rules.
 * Used by both POST /api/fees/rules/user-types and POST /api/fees/rules/users/{userId}.
 * For user-specific rules, userId comes from the URL path parameter, not this body.
 */
@Getter
@NoArgsConstructor
public class CreateFeeRuleRequest {
    @NotNull
    private String userType;

    private String transactionType;
    private String sourceCurrency;
    private String destinationCurrency;
    private FeeSideDefinitionRequest senderFee;
    private FeeSideDefinitionRequest receiverFee;

    @NotNull
    private String description;
}
