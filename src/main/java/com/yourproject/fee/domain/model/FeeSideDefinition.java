package com.yourproject.fee.domain.model;

import io.vavr.collection.List;
import lombok.Getter;
import java.math.BigDecimal;

//FeeSideDefinition: its a class that describes how to calculate a fee.
@Getter
public class FeeSideDefinition {
    private final CalculationMode calculationMode;
    private final BigDecimal flatAmount;
    private final BigDecimal percentage;
    private final List<TierBracket> tiers;
    private final BigDecimal minCap;
    private final BigDecimal maxCap;

    public FeeSideDefinition(CalculationMode calculationMode, BigDecimal flatAmount, BigDecimal percentage, List<TierBracket> tiers, BigDecimal minCap, BigDecimal maxCap){

        if (calculationMode == null) {
            throw new IllegalArgumentException("calculationMode cannot be null");
        }//switch for enum thats why no numbers for every case
        switch (calculationMode) {
            case FLAT-> {
                if (flatAmount == null) {
                    throw new IllegalArgumentException("FlatAmount must not be null");
                }
            }
            case PERCENTAGE-> {
                if (percentage == null) {
                        throw new IllegalArgumentException("Percentage must not be null");
                }
            }
            case HYBRID ->  {
                if (flatAmount == null || percentage == null) {
                    throw new IllegalArgumentException("FlatAmount or percentage must not be null");
                }
            }
            case TIERED_FLAT,TIERED_MARGINAL ->{//here TIERED FLAT AND TIERED MARGINAL has the same logic so just wrote it once
                if(tiers == null || tiers.isEmpty()){
                    throw new IllegalArgumentException("tiers must not be null");
                }
            }
        }//checks first if minCap,maxCap are present or not then compares it
        if (minCap != null &&  maxCap != null) {
            if (minCap.compareTo(maxCap) > 0){
                throw new IllegalArgumentException("minCap must be less than maxCap");
            }
        }
        this.calculationMode = calculationMode;
        this.flatAmount = flatAmount;
        this.percentage = percentage;
        this.tiers = tiers;
        this.minCap = minCap;
        this.maxCap = maxCap;
    }
}
