package com.yourproject.fee.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
//checks currency and compares them we have full and partial
public class CurrencyPair {
    private final Currency sourceCurrency;
    private final Currency destinationCurrency;

    public boolean isExactMatch(Currency sourceCurrency,Currency destinationCurrency){
        return Objects.equals(this.sourceCurrency,sourceCurrency) && Objects.equals(this.destinationCurrency,destinationCurrency);
    }
    public boolean isPartialDestinationMatch(Currency destinationCurrency){
        return  Objects.equals(this.destinationCurrency, destinationCurrency) && this.sourceCurrency==null ;
    }
    public boolean isPartialSourceMatch(Currency sourceCurrency){
        return Objects.equals(this.sourceCurrency, sourceCurrency) && this.destinationCurrency==null;
    }
    public boolean isWildcard(){
        return Objects.isNull(this.sourceCurrency) && Objects.isNull(this.destinationCurrency);
    }
}