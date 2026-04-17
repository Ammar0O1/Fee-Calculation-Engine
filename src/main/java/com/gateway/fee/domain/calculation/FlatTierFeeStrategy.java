package com.gateway.fee.domain.calculation;
import com.gateway.fee.domain.model.FeeSideDefinition;
import java.math.BigDecimal;
// def of class : Finds the bracket the transaction amount falls into and applies that bracket's
// rate to the entire amount. The whole amount is taxed at one rate (the bracket it lands in).

public class FlatTierFeeStrategy implements FeeCalculationStrategy {

    @Override
    public BigDecimal calculate(BigDecimal amount, FeeSideDefinition fee) {
        return fee.getTiers()
                .find(bracket -> amount.compareTo(bracket.getFromAmount()) >= 0
                                    && (bracket.getToAmount() == null || amount.compareTo(bracket.getToAmount()) < 0))
                .map(bracket -> amount.multiply(bracket.getRate()))
                .getOrElseThrow(() -> new IllegalStateException(
                        "No matching tier bracket for amount: " + amount));
    }
}