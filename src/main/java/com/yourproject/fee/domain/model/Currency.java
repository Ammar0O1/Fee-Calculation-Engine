package com.yourproject.fee.domain.model;
import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Currency {
    USD(2,RoundingMode.HALF_UP),
    EUR(2,RoundingMode.HALF_UP),
    IQD(0,RoundingMode.UP);
    private final int decimalPlaces;
    private final RoundingMode roundingMode;
    private Currency(int decimalPlace, RoundingMode roundingMode) {
        this.decimalPlaces=decimalPlace;
        this.roundingMode=roundingMode;
    }
    public BigDecimal round(BigDecimal amount){
        return amount.setScale(decimalPlaces, roundingMode);//setScale is a rounding ready class using RoundingMode
    }
    public int getDecimalPlaces() {
        return decimalPlaces;
    }
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
}
