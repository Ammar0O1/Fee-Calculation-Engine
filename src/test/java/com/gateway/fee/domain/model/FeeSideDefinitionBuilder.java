package com.gateway.fee.domain.model;

import io.vavr.collection.List;
import java.math.BigDecimal;

import static com.gateway.fee.domain.model.CalculationMode.*;

public class FeeSideDefinitionBuilder {
    private CalculationMode calculationMode;
    private BigDecimal flatAmount;
    private BigDecimal percentage;
    private List<TierBracket> tiers;
    private BigDecimal minCap;
    private BigDecimal maxCap;

    public static FeeSideDefinitionBuilder aFeeSide() {
        return new FeeSideDefinitionBuilder();
    }

    public static FeeSideDefinition flat(double amount) {
        return aFeeSide()
                .withCalculationMode(FLAT)
                .withFlatAmount(BigDecimal.valueOf(amount))
                .build();
    }

    public static FeeSideDefinition percentage(double rate) {
        return aFeeSide()
                .withCalculationMode(PERCENTAGE)
                .withPercentage(BigDecimal.valueOf(rate))
                .build();
    }

    public static FeeSideDefinition hybrid(double flatAmount, double percentage) {
        return aFeeSide()
                .withCalculationMode(HYBRID)
                .withFlatAmount(BigDecimal.valueOf(flatAmount))
                .withPercentage(BigDecimal.valueOf(percentage))
                .build();
    }

    public FeeSideDefinitionBuilder withCalculationMode(CalculationMode calculationMode) {
        this.calculationMode = calculationMode;
        return this;
    }

    public FeeSideDefinitionBuilder withFlatAmount(BigDecimal flatAmount) {
        this.flatAmount = flatAmount;
        return this;
    }

    public FeeSideDefinitionBuilder withPercentage(BigDecimal percentage) {
        this.percentage = percentage;
        return this;
    }

    public FeeSideDefinitionBuilder withTiers(List<TierBracket> tiers) {
        this.tiers = tiers;
        return this;
    }

    public FeeSideDefinitionBuilder withMinCap(BigDecimal minCap) {
        this.minCap = minCap;
        return this;
    }

    public FeeSideDefinitionBuilder withMaxCap(BigDecimal maxCap) {
        this.maxCap = maxCap;
        return this;
    }

    public FeeSideDefinition build() {
        return new FeeSideDefinition(calculationMode, flatAmount, percentage, tiers, minCap, maxCap);
    }
}
