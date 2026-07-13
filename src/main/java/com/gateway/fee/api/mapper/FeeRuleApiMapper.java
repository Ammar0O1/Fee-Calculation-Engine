package com.gateway.fee.api.mapper;

import com.gateway.fee.api.dto.request.FeeSideDefinitionRequest;
import com.gateway.fee.api.dto.request.TierBracketRequest;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.dto.response.FeeSideDefinitionResponse;
import com.gateway.fee.api.dto.response.TierBracketResponse;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import com.gateway.fee.infrastructure.persistence.entity.TierBracketEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FeeRuleApiMapper {
    public TransactionType toTransactionType(String transactionType) {
        return transactionType != null ? TransactionType.valueOf(transactionType) : null;
    }

    public Currency toCurrency(String currency) {
        return currency != null ? Currency.valueOf(currency) : null;
    }

    public CalculationMode toCalculationMode(String calculationMode) {
        return calculationMode != null ? CalculationMode.valueOf(calculationMode) : null;
    }
    public UserType toUserType(String userType) {
        return userType != null ? UserType.valueOf(userType) : null;
    }

    public TierBracketEntity toTierEntity(TierBracketRequest req, FeeSideDefinitionEntity feeSideDefinitionEntity, int order) {
        return TierBracketEntity.builder()
                .sideDefinition(feeSideDefinitionEntity)
                .fromAmount(req.getFromAmount())
                .toAmount(req.getToAmount())
                .rate(req.getRate())
                .bracketOrder(order)
                .build();
    }

    public TierBracketResponse toTierResponse(TierBracketEntity entity) {
        return new TierBracketResponse(
                entity.getFromAmount(),
                entity.getToAmount(),
                entity.getRate()
        );
    }

    public FeeSideDefinitionEntity toSideEntity(FeeSideDefinitionRequest req, FeeRuleEntity feeRuleEntity, String side) {
        FeeSideDefinitionEntity sideDef = FeeSideDefinitionEntity.builder()
                .feeRule(feeRuleEntity)
                .side(side)
                .calculationMode(toCalculationMode(req.getCalculationMode()))
                .flatAmount(req.getFlatAmount())
                .percentage(req.getPercentage())
                .minCap(req.getMinCap())
                .maxCap(req.getMaxCap())
                .tierBrackets(new ArrayList<>())
                .build();

        if (req.getTiers() != null) {
            int order = 0;
            for (TierBracketRequest tierReq : req.getTiers()) {
                sideDef.getTierBrackets().add(toTierEntity(tierReq, sideDef, order++));
            }
        }
        return sideDef;

    }

    public FeeSideDefinitionResponse toSideResponse(FeeSideDefinitionEntity entity) {
        return new FeeSideDefinitionResponse(
                entity.getCalculationMode(),
                entity.getFlatAmount(),
                entity.getPercentage(),
                entity.getTierBrackets().stream().map(this::toTierResponse).toList(),
                entity.getMinCap(),
                entity.getMaxCap()
        );
    }

    public FeeRuleResponse toRuleResponse(FeeRuleEntity entity) {

        FeeSideDefinitionResponse senderResponse = entity.getSideDefinitions().stream()
                .filter(s -> "SENDER".equals(s.getSide()))
                .findFirst()
                .map(this::toSideResponse)
                .orElse(null);

        FeeSideDefinitionResponse receiverResponse = entity.getSideDefinitions().stream()
                .filter(s -> "RECEIVER".equals(s.getSide()))
                .findFirst()
                .map(this::toSideResponse)
                .orElse(null);

        return new FeeRuleResponse(
                entity.getRuleId(),
                entity.getUserId(),
                entity.getUserType(),
                entity.getTransactionType(),
                entity.getSourceCurrency(),
                entity.getDestinationCurrency(),
                senderResponse,
                receiverResponse,
                entity.getEffectiveDate(),
                entity.isActive(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
