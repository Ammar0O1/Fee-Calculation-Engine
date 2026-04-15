package com.yourproject.fee.domain.model;

import lombok.Getter;
import java.math.BigDecimal;

//TierBracket: is a value obj that has a tiered fee and is in one bracket
//(fromAmount,toAmount,rate) which is reused in the other classes.
@Getter
public class TierBracket {
    private final BigDecimal fromAmount;
    private final BigDecimal toAmount;
    private final BigDecimal rate;
    public TierBracket(BigDecimal fromAmount, BigDecimal toAmount, BigDecimal rate) {
        if (fromAmount==null || fromAmount.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("fromAmount must be >= 0 but received:" + fromAmount);
        }
        if ( toAmount!=null && toAmount.compareTo(fromAmount)<=0){
            throw new IllegalArgumentException("toAmount must be >fromAmount but received:" + toAmount);
        }
        if (rate==null || rate.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("rate must be >= 0 but received:" + rate);
        }
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.rate = rate;
    }
}
