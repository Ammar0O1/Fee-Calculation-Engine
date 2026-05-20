package com.gateway.fee.application;

import com.gateway.fee.domain.calculation.CalculationStrategyFactory;
import com.gateway.fee.domain.calculation.FeeApplier;
import com.gateway.fee.domain.calculation.FeeCalculationStrategy;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.resolution.FeeRuleResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class FeeCalculator {

    private final FeeRuleResolver feeRuleResolver;
    private final CalculationStrategyFactory calculationStrategyFactory;
    private final FeeApplier feeApplier;

    // this method has no logic in it, its just using other private methods to do the calculation
    public FeeCalculationResult calculate(Transaction transaction) {
        FeeSideResult senderResult = processSenderSide(transaction);
        FeeSideResult receiverResult = processReceiverSide(transaction);

        return new FeeCalculationResult(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getSourceCurrency(),
                transaction.getDestinationCurrency(),
                transaction.getTransactionType(),
                senderResult,
                receiverResult,
                LocalDateTime.now()
        );
    }

    // this method is used to process and pass the values for the sender side of the fee calculation (calling resolver)
    private FeeSideResult processSenderSide(Transaction transaction) {
        FeeRule rule = feeRuleResolver.resolve(
                transaction.getSenderId(),
                transaction.getSenderUserType(),
                transaction.getTransactionType(),
                transaction.getSourceCurrency(),
                transaction.getDestinationCurrency()
        );

        FeeSideDefinition senderFee = rule.getSenderFee();

        if (senderFee == null) {
            return buildWaivedResult(
                    transaction.getSenderId(),
                    transaction.getSenderUserType(),
                    rule.getRuleId(),
                    transaction.getSourceCurrency()
            );
        }

        // Calculate the fee using the appropriate strategy
        FeeCalculationStrategy strategy = calculationStrategyFactory.getStrategy(senderFee.getCalculationMode());
        BigDecimal rawFee = strategy.calculate(transaction.getAmount(), senderFee);

        // Apply rounding and caps
        FeeSideResult feeApplierResult = feeApplier.apply(rawFee, senderFee, transaction.getSourceCurrency());

        return new FeeSideResult(
                transaction.getSenderId(),
                transaction.getSenderUserType(),
                rule.getRuleId(),
                "SENDER",
                transaction.getSourceCurrency(),
                feeApplierResult.getRawFee(),
                feeApplierResult.getRoundedFee(),
                feeApplierResult.getCapApplied(),
                feeApplierResult.getCapAdjustment(),
                feeApplierResult.getFinalFee(),
                senderFee,
                false
        );
    }

    // this method is used to process and pass the values for the receiver side of the fee calculation (calling resolve)
    private FeeSideResult processReceiverSide(Transaction transaction) {
        FeeRule rule = feeRuleResolver.resolve(
                transaction.getReceiverId(),
                transaction.getReceiverUserType(),
                transaction.getTransactionType(),
                transaction.getSourceCurrency(),
                transaction.getDestinationCurrency()
        );

        FeeSideDefinition receiverFee = rule.getReceiverFee();

        if (receiverFee == null) {
            return buildWaivedResult(
                    transaction.getReceiverId(),
                    transaction.getReceiverUserType(),
                    rule.getRuleId(),
                    transaction.getDestinationCurrency()
            );
        }

        // Calculate the fee using the correct strategy
        FeeCalculationStrategy strategy = calculationStrategyFactory.getStrategy(receiverFee.getCalculationMode());
        BigDecimal rawFee = strategy.calculate(transaction.getAmount(), receiverFee);

        // Apply rounding and caps
        FeeSideResult feeApplierResult = feeApplier.apply(rawFee, receiverFee, transaction.getDestinationCurrency());

        return new FeeSideResult(
                transaction.getReceiverId(),
                transaction.getReceiverUserType(),
                rule.getRuleId(),
                "RECEIVER",
                transaction.getDestinationCurrency(),
                feeApplierResult.getRawFee(),
                feeApplierResult.getRoundedFee(),
                feeApplierResult.getCapApplied(),
                feeApplierResult.getCapAdjustment(),
                feeApplierResult.getFinalFee(),
                receiverFee,
                false
        );
    }
    // Builds a result for a waived fee side, only 4 parameters because the rest are constant for any waived transaction
    private FeeSideResult buildWaivedResult(String userId, UserType userType, UUID matchedRuleId, Currency currency) {
        return new FeeSideResult(
                userId,
                userType,
                matchedRuleId,
                "WAIVED",
                currency,
                null,
                null,
                "NONE",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                true
        );
    }
}