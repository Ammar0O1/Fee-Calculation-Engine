package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.FeeSideDefinition;
import com.gateway.fee.domain.model.FeeSideResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeeApplierTest {

    private final FeeApplier applier = new FeeApplier();

    private FeeSideDefinition flatFee(String flat, String minCap, String maxCap) {
        return new FeeSideDefinition(
                CalculationMode.FLAT,
                new BigDecimal(flat), null, null,
                minCap == null ? null : new BigDecimal(minCap),
                maxCap == null ? null : new BigDecimal(maxCap)
        );
    }

    @Test
    void shouldRoundUsdToTwoDecimalsHalfUp() {
        FeeSideResult result = applier.apply(new BigDecimal("4.99995"), flatFee("5", null, null), Currency.USD);

        assertThat(result.getRoundedFee()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getFinalFee()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getCapApplied()).isEqualTo("NONE");
    }

    @Test
    void shouldRoundIqdToZeroDecimalsUp() {
        FeeSideResult result = applier.apply(new BigDecimal("4999.995"), flatFee("5000", null, null), Currency.IQD);

        assertThat(result.getRoundedFee()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.getFinalFee()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.getCapApplied()).isEqualTo("NONE");
    }

    @Test
    void shouldRaiseFeeToMinCapWhenBelowMinimum() {
        FeeSideResult result = applier.apply(new BigDecimal("3"), flatFee("3", "5", "50"), Currency.USD);

        assertThat(result.getRoundedFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result.getFinalFee()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getCapApplied()).isEqualTo("MIN_CAP_APPLIED");
        assertThat(result.getCapAdjustment()).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void shouldLowerFeeToMaxCapWhenAboveMaximum() {
        FeeSideResult result = applier.apply(new BigDecimal("75"), flatFee("75", "5", "50"), Currency.USD);

        assertThat(result.getRoundedFee()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.getFinalFee()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getCapApplied()).isEqualTo("MAX_CAP_APPLIED");
        assertThat(result.getCapAdjustment()).isEqualByComparingTo(new BigDecimal("-25"));
    }

    @Test
    void shouldNotApplyCapWhenFeeWithinBounds() {
        FeeSideResult result = applier.apply(new BigDecimal("25"), flatFee("25", "5", "50"), Currency.USD);

        assertThat(result.getFinalFee()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.getCapApplied()).isEqualTo("NONE");
        assertThat(result.getCapAdjustment()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleZeroFeeWithCorrectCurrencyPrecision() {
        FeeSideResult result = applier.apply(new BigDecimal("0"), flatFee("0", null, null), Currency.USD);

        assertThat(result.getRoundedFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getFinalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCapApplied()).isEqualTo("NONE");
    }
}