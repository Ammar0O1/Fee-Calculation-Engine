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
        BigDecimal total = BigDecimal.ZERO;
//TO DO: Change for loop to a better approach.
        for (int i = 0; i < fee.getTiers().size(); i++) {
            TierBracket bracket = fee.getTiers().get(i);
            // added those 3 only for better readability
            BigDecimal from = bracket.getFromAmount();
            BigDecimal to = bracket.getToAmount();
            BigDecimal rate = bracket.getRate();

            if (amount.compareTo(from) <= 0) {
                break;
            }

            BigDecimal portion;
            if (to == null || amount.compareTo(to) < 0) {
                // last bracket to be checked
                portion = amount.subtract(from);
                total = total.add(portion.multiply(rate));
                break;
            } else {
                //
                portion = to.subtract(from);
                total = total.add(portion.multiply(rate));
            }
        }
        return total;
    }
}