package com.gateway.fee.domain.model;

import com.gateway.fee.domain.exception.FeeConfigurationException;
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
            throw new FeeConfigurationException("calculationMode cannot be null");
        }//switch for enum thats why no numbers for every case
        switch (calculationMode) {
            case FLAT-> {
                if (flatAmount == null) {
                    throw new FeeConfigurationException("FlatAmount must not be null");
                }
            }
            case PERCENTAGE-> {
                if (percentage == null) {
                        throw new FeeConfigurationException("Percentage must not be null");
                }
            }
            case HYBRID ->  {
                if (flatAmount == null || percentage == null) {
                    throw new FeeConfigurationException("FlatAmount or percentage must not be null");
                }
            }
            case TIERED_FLAT,TIERED_MARGINAL ->{//here TIERED FLAT AND TIERED MARGINAL has the same logic so just wrote it once
                if(tiers == null || tiers.isEmpty()){
                    throw new FeeConfigurationException("tiers must not be null");
                }
                // making sure brackets start at 0
                if (tiers.get(0).getFromAmount().compareTo(BigDecimal.ZERO) != 0) {
                    throw new FeeConfigurationException("First tier bracket must start at 0");
                }
                // only last bracket can have null toAmount
                if(tiers.init().filter(bracket -> bracket.getToAmount() == null).nonEmpty()){
                    throw new FeeConfigurationException("Only the last bracket may have null toAmount");
                }
                for (int i = 1; i < tiers.size(); i++) {
                    TierBracket previous = tiers.get(i - 1);
                    TierBracket current = tiers.get(i);

                    if(current.getFromAmount().compareTo(previous.getToAmount()) != 0){
                        throw new FeeConfigurationException("the bracket are not sorted in order");
                    }
                }
            }
        }//checks first if minCap,maxCap are present or not then compares it
        if (minCap != null &&  maxCap != null) {
            if (minCap.compareTo(maxCap) > 0){
                throw new FeeConfigurationException("minCap must be less than maxCap");
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
