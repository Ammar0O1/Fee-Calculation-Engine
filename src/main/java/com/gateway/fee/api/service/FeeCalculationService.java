package com.gateway.fee.api.service;

import com.gateway.fee.api.dto.request.FeeCalculationRequest;
import com.gateway.fee.api.dto.request.FeeEstimateRequest;
import com.gateway.fee.api.dto.response.EstimateMode;
import com.gateway.fee.api.dto.response.FeeCalculationResponse;
import com.gateway.fee.api.dto.response.FeeEstimateResponse;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.mapper.FeeCalculationResultMapper;
import com.gateway.fee.api.mapper.FeeRuleApiMapper;
import com.gateway.fee.application.FeeCalculator;
import com.gateway.fee.domain.calculation.CalculationStrategyFactory;
import com.gateway.fee.domain.calculation.FeeApplier;
import com.gateway.fee.domain.calculation.FeeCalculationStrategy;
import com.gateway.fee.domain.exception.NoMatchingRuleException;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.resolution.FeeRuleResolver;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//feat: implement estimate endpoint with dual-mode calculation preview and rule details
@Service
@RequiredArgsConstructor
public class FeeCalculationService {

    // Dependencies
    private final FeeCalculator feeCalculator;
    private final FeeCalculationResultMapper mapper;
    private final FeeRuleResolver feeRuleResolver;
    private final CalculationStrategyFactory calculationStrategyFactory;
    private final FeeApplier feeApplier;
    private final FeeRuleRepository feeRuleRepository;
    private final FeeRuleApiMapper  feeRuleApiMapper;
    //Public endpoints

    public FeeCalculationResponse calculate(FeeCalculationRequest request) {
        Transaction transaction = Transaction.builder()
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .sourceCurrency(request.getSourceCurrency())
                .destinationCurrency(request.getDestinationCurrency())
                .transactionType(request.getTransactionType())
                .senderId(request.getSenderId())
                .senderUserType(request.getSenderUserType())
                .receiverId(request.getReceiverId())
                .receiverUserType(request.getReceiverUserType())
                .build();
        FeeCalculationResult result = feeCalculator.calculate(transaction);
        return mapper.toResponse(result);
    }

    public FeeEstimateResponse estimate(FeeEstimateRequest request) {

        if (request.getAmount() != null) {
            // MODE 1  amount present
            // we will calculate the fee (without logging normal calculation but no logging)
            Transaction transaction = Transaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .amount(request.getAmount())
                    .sourceCurrency(request.getSourceCurrency())
                    .destinationCurrency(request.getDestinationCurrency())
                    .senderId(request.getUserId())
                    .senderUserType(request.getUserType())
                    .receiverId(request.getUserId())
                    .receiverUserType(request.getUserType())
                    .transactionType(request.getTransactionType())
                    .build();
            FeeCalculationResult result = calculateWithoutLogging(transaction);
            FeeCalculationResponse mapped = mapper.toResponse(result);

            return FeeEstimateResponse.builder()
                    .mode(EstimateMode.CALCULATION)
                    .calculation(mapped)
                    .build();
        } else {
            // MODE 2  amount not present
            // we will return the rule details no calculation
            FeeRule rule = feeRuleResolver.resolve(
                    request.getUserId(),
                    request.getUserType(),
                    request.getTransactionType(),
                    request.getSourceCurrency(),
                    request.getDestinationCurrency()
            );

            FeeRuleEntity entity = feeRuleRepository.findById(rule.getRuleId())
                    .orElseThrow(() -> new NoMatchingRuleException("No rule found for the given criteria"));

            FeeRuleResponse ruleResponse = feeRuleApiMapper.toRuleResponse(entity);

            return FeeEstimateResponse.builder()
                    .mode(EstimateMode.RULE_DETAIL)
                    .ruleDetails(ruleResponse)
                    .build();
        }
    }

    //  Private helpers no-logging calculation path for estimate

    private FeeCalculationResult calculateWithoutLogging(Transaction transaction) {
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

        FeeCalculationStrategy strategy = calculationStrategyFactory.getStrategy(senderFee.getCalculationMode());
        BigDecimal rawFee = strategy.calculate(transaction.getAmount(), senderFee);

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

        FeeCalculationStrategy strategy = calculationStrategyFactory.getStrategy(receiverFee.getCalculationMode());
        BigDecimal rawFee = strategy.calculate(transaction.getAmount(), receiverFee);

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