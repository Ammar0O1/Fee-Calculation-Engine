package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;

import java.math.BigDecimal;
import java.util.Map;

public record FeeSummaryResponse(
        // total fees grouped by transaction type
        Map<TransactionType, BigDecimal> totalsByTransactionType,

        // total fees grouped by user type
        Map<UserType, BigDecimal> totalsByUserType,

        // total fees grouped by currency
        Map<Currency, BigDecimal> totalsByCurrency,

        // average fee per transaction, grouped by transaction type
        Map<TransactionType, BigDecimal> averagesByTransactionType
) {
}