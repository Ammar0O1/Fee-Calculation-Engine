package com.gateway.fee.api.service;

import com.gateway.fee.api.dto.request.CreateDefaultRuleRequest;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeeRuleService {
    private final FeeRuleRepository feeRuleRepository;

    // serving CREATE default fee rule
    public FeeRuleResponse createDefaultFeeRule(CreateDefaultRuleRequest request) {
        TransactionType txType = request.getTransactionType() != null
                ? TransactionType.valueOf(request.getTransactionType())
                : null;
        Currency dstCurrency = request.getDestinationCurrency() != null
                ? Currency.valueOf(request.getDestinationCurrency())
                : null;
        Currency srcCurrency = request.getSourceCurrency() != null
                ? Currency.valueOf(request.getSourceCurrency())
                : null;
        FeeRuleEntity feeRuleEntity = FeeRuleEntity.builder().
                ruleId(UUID.randomUUID()).
                userId(null).
                userType(null).
                destinationCurrency(dstCurrency).
                sourceCurrency(srcCurrency).
                transactionType(txType).
                effectiveDate(LocalDateTime.now()).
                active(true).
                description(request.getDescription()).
                sideDefinitions(new ArrayList<>()).
                build();
        if(request.getSenderFee() != null) {
            FeeSideDefinitionEntity senderDef = FeeSideDefinitionEntity.builder()
                    .feeRule(feeRuleEntity)
                    .side("SENDER").build();
            feeRuleEntity.getSideDefinitions().add(senderDef);
        }

        if(request.getReceiverFee() != null) {
            FeeSideDefinitionEntity receiverDef = FeeSideDefinitionEntity.builder()
                    .feeRule(feeRuleEntity)
                    .side("RECEIVER").build();
            feeRuleEntity.getSideDefinitions().add(receiverDef);
        }



    }

}
