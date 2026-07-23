package com.gateway.fee.api.mapper;
import com.gateway.fee.api.dto.response.FeeCalculationResponse;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.api.dto.response.FeeSideResultResponse;
import com.gateway.fee.domain.model.FeeSideResult;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;
import java.util.UUID;


@Component
public class FeeCalculationResultMapper {
    public FeeCalculationResponse toResponse(FeeCalculationResult domain) {
        return FeeCalculationResponse.builder()
                .transactionId(domain.getTransactionId())
                .transactionAmount(domain.getTransactionAmount())
                .sourceCurrency(domain.getSourceCurrency())
                .destinationCurrency(domain.getDestinationCurrency())
                .transactionType(domain.getTransactionType())
                .calculatedAt(domain.getCalculatedAt())
                .senderResult(mapSideResult(domain.getSenderResult()))
                .receiverResult(mapSideResult(domain.getReceiverResult()))
                .build();
    }

    private FeeSideResultResponse mapSideResult(FeeSideResult domain){
        return FeeSideResultResponse.builder()
                .userId(domain.getUserId())
                .userType(domain.getUserType())
                .matchedRuleId(Option.of(domain.getMatchedRuleId()).map(UUID::toString).getOrNull())
                .matchLevel(domain.getMatchLevel())
                .currency(domain.getCurrency())
                .rawFee(domain.getRawFee())
                .roundedFee(domain.getRoundedFee())
                .capApplied(domain.getCapApplied())
                .capAdjustment(domain.getCapAdjustment())
                .finalFee(domain.getFinalFee())
                .waived(domain.isWaived())
                .build();
    }
}
