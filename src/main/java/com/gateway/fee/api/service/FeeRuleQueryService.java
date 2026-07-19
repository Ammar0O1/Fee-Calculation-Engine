package com.gateway.fee.api.service;

import com.gateway.fee.api.dto.response.EffectiveRuleResponse;
import com.gateway.fee.api.dto.response.FeeScheduleEntryResponse;
import com.gateway.fee.api.dto.response.RuleOrigin;
import com.gateway.fee.api.mapper.FeeRuleApiMapper;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class FeeRuleQueryService {
    private final FeeRuleRepository feeRuleRepository;
    private final FeeRuleApiMapper mapper;
    //feat: add dimension filtering to fee schedule endpoint(instead of returning all of the tables we filter and send back what user wants
    public List<FeeScheduleEntryResponse> schedule(UserType userType, TransactionType transactionType, Currency sourceCurrency, Currency destinationCurrency) {
        return feeRuleRepository.findByActiveTrue().stream()
                .filter(e -> userType == null || userType.equals(e.getUserType()))
                .filter(e -> transactionType == null || transactionType.equals(e.getTransactionType()))
                .filter(e -> sourceCurrency == null || sourceCurrency.equals(e.getSourceCurrency()))
                .filter(e -> destinationCurrency == null || destinationCurrency.equals(e.getDestinationCurrency()))
                .map(mapper::toScheduleEntry)
                .toList();

    }
    //feat: add effective rules endpoint with three-tier CUSTOM/INHERITED/DEFAULT labelling
    public List<EffectiveRuleResponse> effective(String userId, UserType userType){
        Stream<EffectiveRuleResponse> custom = feeRuleRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(e -> EffectiveRuleResponse.builder()
                        .rule(mapper.toRuleResponse(e))
                        .label(RuleOrigin.CUSTOM)
                        .build());
        Stream<EffectiveRuleResponse> inherited = feeRuleRepository.findByUserIdIsNullAndUserTypeAndActiveTrue(userType)
                .stream()
                .map(e -> EffectiveRuleResponse.builder()
                        .rule(mapper.toRuleResponse(e))
                        .label(RuleOrigin.INHERITED)
                        .build());
        Stream<EffectiveRuleResponse> defaults = feeRuleRepository.findByUserIdIsNullAndUserTypeIsNullAndActiveTrue()
                .stream()
                .map(e->EffectiveRuleResponse.builder()
                        .rule(mapper.toRuleResponse(e))
                        .label(RuleOrigin.DEFAULT)
                        .build());
        return Stream.concat(Stream.concat(custom, inherited), defaults).toList();
    }
}
