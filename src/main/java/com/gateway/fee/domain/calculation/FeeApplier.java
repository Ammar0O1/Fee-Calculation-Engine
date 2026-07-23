package com.gateway.fee.domain.calculation;
// i'll fix formating later (Intern-B)
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.FeeSideDefinition;
import com.gateway.fee.domain.model.FeeSideResult;

import java.math.BigDecimal;
// def of class :  Takes a raw fee, rounds it using currency rules, applies min/max caps, and returns a detailed result object tracking all adjustments made.
public class FeeApplier {
    public FeeSideResult apply(BigDecimal rawFee, FeeSideDefinition fee, Currency currency) {
BigDecimal roundedFee = currency.round(rawFee);
BigDecimal finalFee = roundedFee;
String capApplied = "NONE";
BigDecimal capAdjustment = BigDecimal.ZERO;
BigDecimal minCap = fee.getMinCap();
BigDecimal maxCap = fee.getMaxCap();
if(minCap != null && roundedFee.compareTo(minCap) < 0) {
    finalFee = minCap;
    capApplied = "MIN_CAP_APPLIED";
    capAdjustment = minCap.subtract(roundedFee);
}else if(maxCap != null && roundedFee.compareTo(maxCap) > 0) {
    finalFee = maxCap;
    capApplied = "MAX_CAP_APPLIED";
    capAdjustment = maxCap.subtract(roundedFee);
}
        return new FeeSideResult(null, null, null, null, currency, rawFee, roundedFee,
                capApplied, capAdjustment, finalFee, fee, false); // the reason waved is false is because FeeApplier only run when not waived
    }
}