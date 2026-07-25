package com.gateway.fee.infrastructure.persistence.mapper;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.entity.*;
import io.vavr.collection.List;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.stream.Collectors;

//NOTE: when a method is declared as a domain variable, it means this method
//is doing (ENTITY to DOMAIN) the opposite is correct as well whenever its declared
//as an Entity Variable the method is doing (DOMAIN to ENTITY)
@Component
public class FeeRuleMapper {

    //DOMAIN to ENTITY (used when saving)

    public FeeRuleEntity toEntity(FeeRule domain) {
        FeeRuleEntity entity = FeeRuleEntity.builder()
                .ruleId(domain.getRuleId())
                .userId(domain.getUserId())
                .userType(domain.getUserType())
                .transactionType(domain.getTransactionType())
                .sourceCurrency(domain.getSourceCurrency())
                .destinationCurrency(domain.getDestinationCurrency())
                .effectiveDate(domain.getEffectiveDate())
                .active(domain.isActive())
                .description(domain.getDescription())
                .sideDefinitions(new ArrayList<>())
                .build();

        if (domain.getSenderFee() != null) {
            FeeSideDefinitionEntity senderEntity =
                    toSideDefinitionEntity(domain.getSenderFee(), entity, "SENDER");
            entity.getSideDefinitions().add(senderEntity);
        }

        if (domain.getReceiverFee() != null) {
            FeeSideDefinitionEntity receiverEntity =
                    toSideDefinitionEntity(domain.getReceiverFee(), entity, "RECEIVER");
            entity.getSideDefinitions().add(receiverEntity);
        }

        return entity;
    }

    //ENTITY to DOMAIN (used when loading)

    public FeeRule toDomain(FeeRuleEntity entity) {
        //we are using find here because we dont want to throw an exception and we are wrapping the list to vavr using List.ofAll
        //if the side is not found, we just return null
        //this is because we dont want to fail the whole transaction if one side is missing
        FeeSideDefinition senderFee = List.ofAll(entity.getSideDefinitions())
                .find(side -> "SENDER".equals(side.getSide()))
                .map(this::toSideDefinitionDomain).getOrNull();
        FeeSideDefinition receiverFee = List.ofAll(entity.getSideDefinitions())
                .find(side -> "RECEIVER".equals(side.getSide()))
                .map(this::toSideDefinitionDomain).getOrNull();

        return new FeeRule(
                entity.getRuleId(),
                entity.getUserId(),
                entity.getUserType(),
                entity.getTransactionType(),
                entity.getSourceCurrency(),
                entity.getDestinationCurrency(),
                senderFee,
                receiverFee,
                entity.getEffectiveDate(),
                entity.isActive(),
                entity.getDescription()
        );
    }

    //Private Helpers : created so that we dont repeat codes over and over
    //again and also its better for readability/debugging
    //why private? because we only use it in mapper it dosent make any sense anywhere

    private FeeSideDefinitionEntity toSideDefinitionEntity(
            FeeSideDefinition domain, FeeRuleEntity parent, String side) {

        FeeSideDefinitionEntity entity = FeeSideDefinitionEntity.builder()
                .feeRule(parent)
                .side(side)
                .calculationMode(domain.getCalculationMode())
                .flatAmount(domain.getFlatAmount())
                .percentage(domain.getPercentage())
                .minCap(domain.getMinCap())
                .maxCap(domain.getMaxCap())
                .tierBrackets(new ArrayList<>())
                .build();

        List<TierBracket> tiers = domain.getTiers();
        if (tiers != null && !tiers.isEmpty()) {
            for (int i = 0; i < tiers.size(); i++) {
                entity.getTierBrackets().add(toTierBracketEntity(tiers.get(i), entity, i));
            }
        }

        return entity;
    }

    private FeeSideDefinition toSideDefinitionDomain(FeeSideDefinitionEntity entity) {
        java.util.List<TierBracket> javaTiers = entity.getTierBrackets().stream()
                .map(this::toTierBracketDomain)
                .collect(Collectors.toList());

        List<TierBracket> tiers = javaTiers.isEmpty() ? null : List.ofAll(javaTiers);

        return new FeeSideDefinition(
                entity.getCalculationMode(),
                entity.getFlatAmount(),
                entity.getPercentage(),
                tiers,
                entity.getMinCap(),
                entity.getMaxCap()
        );
    }

    private TierBracketEntity toTierBracketEntity(
            TierBracket domain, FeeSideDefinitionEntity parent, int order) {
        return TierBracketEntity.builder()
                .sideDefinition(parent)
                .fromAmount(domain.getFromAmount())
                .toAmount(domain.getToAmount())
                .rate(domain.getRate())
                .bracketOrder(order)
                .build();
    }

    private TierBracket toTierBracketDomain(TierBracketEntity entity) {
        return new TierBracket(
                entity.getFromAmount(),
                entity.getToAmount(),
                entity.getRate()
        );
    }
}