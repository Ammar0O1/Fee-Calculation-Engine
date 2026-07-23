package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.FeeSideDefinition;
import com.gateway.fee.domain.model.TierBracket;
import java.math.BigDecimal;
// def of class : Splits the transaction amount across brackets and applies each bracket's rate
// only to the portion that falls within it. Works like progressive income tax
// each portion is taxed at the rate of the bracket it occupies.

public class MarginalTierFeeStrategy implements FeeCalculationStrategy {

    @Override
    public BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee) {
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return fee.getTiers().toJavaStream()
                .filter(bracket -> amount.compareTo(bracket.getFromAmount()) > 0)

                .map(bracket -> {
                    BigDecimal from = bracket.getFromAmount();
                    BigDecimal to = bracket.getToAmount();
                    BigDecimal rate = bracket.getRate();

                    BigDecimal upperLimit;
                    if (to == null || amount.compareTo(to) < 0) {
                        upperLimit = amount;
                    } else {
                        upperLimit = to;
                    }
                    BigDecimal portion = upperLimit.subtract(from);
                    return portion.multiply(rate);
                })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
}