package com.gateway.fee.infrastructure.persistence.mapper;
import com.gateway.fee.domain.model.FeeCalculationResult;
import com.gateway.fee.domain.model.FeeSideResult;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import org.springframework.stereotype.Component;

@Component
public class FeeTransactionLogMapper {

    public FeeTransactionLogEntity toEntity(FeeCalculationResult result) {
        FeeSideResult sender = result.getSenderResult();
        FeeSideResult receiver = result.getReceiverResult();

        return FeeTransactionLogEntity.builder()
                // Transaction context
                .transactionId(result.getTransactionId())
                .transactionAmount(result.getTransactionAmount())
                .sourceCurrency(result.getSourceCurrency())
                .destinationCurrency(result.getDestinationCurrency())
                .transactionType(result.getTransactionType())

                // Sender side
                .senderUserId(sender.getUserId())
                .senderUserType(sender.getUserType())
                .senderMatchedRuleId(sender.getMatchedRuleId())
                .senderRawFee(sender.getRawFee())
                .senderFinalFee(sender.getFinalFee())
                .senderFeeCurrency(sender.getCurrency())
                .senderCapApplied(sender.getCapApplied())
                .senderWaived(sender.isWaived())

                // Receiver side
                .receiverUserId(receiver.getUserId())
                .receiverUserType(receiver.getUserType())
                .receiverMatchedRuleId(receiver.getMatchedRuleId())
                .receiverRawFee(receiver.getRawFee())
                .receiverFinalFee(receiver.getFinalFee())
                .receiverFeeCurrency(receiver.getCurrency())
                .receiverCapApplied(receiver.getCapApplied())
                .receiverWaived(receiver.isWaived())

                // Timestamp
                .calculatedAt(result.getCalculatedAt())

                .build();
    }
}