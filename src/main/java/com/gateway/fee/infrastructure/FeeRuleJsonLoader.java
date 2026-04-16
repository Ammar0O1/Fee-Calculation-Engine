package com.gateway.fee.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gateway.fee.domain.model.FeeRule;
import com.gateway.fee.domain.model.FeeSideDefinition;
import com.gateway.fee.domain.model.TierBracket;
import com.gateway.fee.infrastructure.dto.FeeRuleDto;
import com.gateway.fee.infrastructure.dto.FeeSideDefinitionDto;
import com.gateway.fee.infrastructure.dto.TierBracketDto;
import io.vavr.collection.List;

import java.io.File;
import java.io.IOException;

public class FeeRuleJsonLoader {

    private final ObjectMapper objectMapper;

    public FeeRuleJsonLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<FeeRule> load(String filePath) throws IOException {
        File file = new File(filePath);
        java.util.List<FeeRuleDto> dtos = objectMapper
                .readValue(file, new TypeReference<java.util.List<FeeRuleDto>>(){
                });
        return List.ofAll(dtos).map(this::toFeeRule);    }
    private FeeRule toFeeRule(FeeRuleDto dto) {
        return new FeeRule(
                dto.getRuleId(),
                dto.getUserId(),
                dto.getUserType(),
                dto.getTransactionType(),
                dto.getSourceCurrency(),
                dto.getDestinationCurrency(),
                toFeeSideDefinition(dto.getSenderFee()),
                toFeeSideDefinition(dto.getReceiverFee()),
                dto.getEffectiveDate(),
                dto.isActive(),
                dto.getDescription()
    );
    }
    private FeeSideDefinition toFeeSideDefinition(FeeSideDefinitionDto dto) {
        if (dto == null) return null;

        var tiers = dto.getTiers() == null ? null : List.ofAll(dto.getTiers().stream()
                                                               .map(this::toTierBracket)
                                                               .collect(java.util.stream.Collectors.toList()));

        return new FeeSideDefinition(
                dto.getCalculationMode(),
                dto.getFlatAmount(),
                dto.getPercentage(),
                tiers,
                dto.getMinCap(),
                dto.getMaxCap()
        );
        }
    private TierBracket toTierBracket(TierBracketDto dto) {
        return new TierBracket(
                dto.getFromAmount(),
                dto.getToAmount(),
                dto.getRate()
        );
    }
}
