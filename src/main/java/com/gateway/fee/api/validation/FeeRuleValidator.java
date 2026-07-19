package com.gateway.fee.api.validation;

import com.gateway.fee.api.dto.request.FeeSideDefinitionRequest;
import com.gateway.fee.api.dto.request.TierBracketRequest;
import com.gateway.fee.domain.exception.InvalidRuleException;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.gateway.fee.domain.exception.DuplicateRuleException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeeRuleValidator {
    private final FeeRuleRepository feeRuleRepository;

    public void validateSide(FeeSideDefinitionRequest senderFee, FeeSideDefinitionRequest receiverFee) {
        validateAtLeastOneSide(senderFee, receiverFee);
        validateSide(senderFee);
        validateSide(receiverFee);
    }

    // we are checking if the fee rule already exists for the given parameters
    public void validateNotDuplicate(String userId, UserType userType, TransactionType transactionType, Currency src, Currency dst) {
        List<FeeRuleEntity> exists = feeRuleRepository.findByDimensions(
                userId, userType, transactionType, src, dst
        );
        if (!exists.isEmpty()) {
            throw new DuplicateRuleException("Fee rule already exists for the given parameters");        }
    }
    // same duplicate validation but this is for updating(dont want to check userId cuz its always matches)
    public void validateNotDuplicateForUpdate(UUID excludeRuleId, String userId, UserType userType,
                                              TransactionType transactionType, Currency src, Currency dst) {
        List<FeeRuleEntity> exists = feeRuleRepository.findByDimensionsExcludingId(
                excludeRuleId, userId, userType, transactionType, src, dst);
        if (!exists.isEmpty()) {
            throw new DuplicateRuleException("Another fee rule already exists with these dimensions");        }
    }


    // validating if at least one side is not waived
    private void validateAtLeastOneSide(FeeSideDefinitionRequest senderRequest, FeeSideDefinitionRequest receiverRequest) {
        if (senderRequest == null && receiverRequest == null) {
            throw new InvalidRuleException( "At least one of senderFee or receiverFee must be non-null");
        }
    }

    private void validateSide(FeeSideDefinitionRequest side) {
        if (side == null) {
            return; // checks if waived
        }
        CalculationMode mode = CalculationMode.valueOf(side.getCalculationMode());

        switch (mode) {
            case PERCENTAGE -> {
                if (side.getPercentage() == null) {
                    throw new InvalidRuleException( "Percentage must be provided for PERCENTAGE calculation mode");
                }
            }
            case FLAT -> {
                if (side.getFlatAmount() == null) {
                    throw new InvalidRuleException( "Flat amount must be provided for FLAT calculation mode");
                }
            }
            case TIERED_FLAT, TIERED_MARGINAL -> {
                if (side.getTiers() == null || side.getTiers().isEmpty()) {
                    throw new InvalidRuleException( "Tiers must be provided for TIERED_FLAT calculation mode");
                }
                validateTiers(side.getTiers());

            }

            case HYBRID -> {
                if (side.getFlatAmount() == null || side.getPercentage() == null) {
                    throw new InvalidRuleException( "Both flatAmount and percentage must be provided for HYBRID calculation mode");
                }
            }
        }
        validateCaps(side);
    }

    private void validateCaps(FeeSideDefinitionRequest side) {
        if (side.getMinCap() != null && side.getMaxCap() != null) {
            if (side.getMinCap().compareTo(side.getMaxCap()) > 0) {
                throw new InvalidRuleException( "minCap must be less than maxCap");
            }
        }
    }

    private void validateTiers(List<TierBracketRequest> tiers) {

        // Per-bracket checks
        tiers.forEach(tier -> {
            if (tier.getFromAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidRuleException( "fromAmount must be >= 0");
            }
            if (tier.getRate().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidRuleException( "rate must be >= 0");
            }
            if (tier.getToAmount() != null && tier.getToAmount().compareTo(tier.getFromAmount()) <= 0) {
                throw new InvalidRuleException( "toAmount must be greater than fromAmount");
            }
        });

        // First bracket must start at 0
        if (tiers.get(0).getFromAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidRuleException( "First tier must start at 0");
        }

        // Cross-bracket checks: ordering, no gaps, only last has null toAmount
        for (int i = 0; i < tiers.size(); i++) {
            TierBracketRequest current = tiers.get(i);
            boolean isLast = (i == tiers.size() - 1);

            if (isLast) {
                // only the last bracket may have a null toAmount (open-ended)
                continue;
            }

            // non-last brackets must have a toAmount
            if (current.getToAmount() == null) {
                throw new InvalidRuleException( "Only the last tier may have a null toAmount");
            }

            // next bracket's fromAmount must equal this bracket's toAmount (no gaps, sorted)
            TierBracketRequest next = tiers.get(i + 1);
            if (next.getFromAmount().compareTo(current.getToAmount()) != 0) {
                throw new InvalidRuleException( "Tiers must be contiguous with no gaps or overlaps");
            }
        }
    }


}


