package com.gateway.fee.domain.calculation;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.FeeSideDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FlatFeeStrategyTest {

    private final FlatFeeStrategy strategy = new FlatFeeStrategy();

    @Test
    void shouldReturnFlatAmountForRegularTransaction() {
        // arrange
        FeeSideDefinition fee = new FeeSideDefinition(
                CalculationMode.FLAT,
                new BigDecimal("5"),
                null, null, null, null
        );

        // act
        BigDecimal result = strategy.calculate(new BigDecimal("1000"), fee);

        // assert
        assertThat(result).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void shouldReturnFlatAmountForVerySmallTransaction() {
        // arrange
        FeeSideDefinition fee = new FeeSideDefinition(
                CalculationMode.FLAT,
                new BigDecimal("5"),
                null, null, null, null
        );

        // act
        BigDecimal result = strategy.calculate(new BigDecimal("0.01"), fee);

        // assert
        assertThat(result).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void shouldReturnFlatAmountForVeryLargeTransaction() {
        // arrange
        FeeSideDefinition fee = new FeeSideDefinition(
                CalculationMode.FLAT,
                new BigDecimal("0.50"),
                null, null, null, null
        );

        // act
        BigDecimal result = strategy.calculate(new BigDecimal("1000000"), fee);

        // assert
        assertThat(result).isEqualByComparingTo(new BigDecimal("0.50"));
    }
}