package com.gateway.fee.domain.model;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;

public final class FeeResultAssertions {

    private FeeResultAssertions() {}

    public static FeeCalculationResultAssert assertThat(FeeCalculationResult result) {
        return new FeeCalculationResultAssert(result);
    }

    public static FeeSideResultAssert assertThat(FeeSideResult result) {
        return new FeeSideResultAssert(result);
    }

    public static class FeeCalculationResultAssert extends AbstractAssert<FeeCalculationResultAssert, FeeCalculationResult> {
        public FeeCalculationResultAssert(FeeCalculationResult actual) {
            super(actual, FeeCalculationResultAssert.class);
        }

        public FeeCalculationResultAssert hasTransactionId(String id) {
            isNotNull();
            Assertions.assertThat(actual.getTransactionId()).isEqualTo(id);
            return this;
        }

        public FeeCalculationResultAssert hasSourceCurrency(Currency currency) {
            isNotNull();
            Assertions.assertThat(actual.getSourceCurrency()).isEqualTo(currency);
            return this;
        }

        public FeeCalculationResultAssert hasDestinationCurrency(Currency currency) {
            isNotNull();
            Assertions.assertThat(actual.getDestinationCurrency()).isEqualTo(currency);
            return this;
        }

        public FeeCalculationResultAssert senderFeeSatisfies(java.util.function.Consumer<FeeSideResultAssert> consumer) {
            isNotNull();
            FeeSideResultAssert sideAssert = new FeeSideResultAssert(actual.getSenderResult());
            consumer.accept(sideAssert);
            return this;
        }

        public FeeCalculationResultAssert receiverFeeSatisfies(java.util.function.Consumer<FeeSideResultAssert> consumer) {
            isNotNull();
            FeeSideResultAssert sideAssert = new FeeSideResultAssert(actual.getReceiverResult());
            consumer.accept(sideAssert);
            return this;
        }
    }

    public static class FeeSideResultAssert extends AbstractAssert<FeeSideResultAssert, FeeSideResult> {
        public FeeSideResultAssert(FeeSideResult actual) {
            super(actual, FeeSideResultAssert.class);
        }

        public FeeSideResultAssert hasCurrency(Currency currency) {
            isNotNull();
            Assertions.assertThat(actual.getCurrency()).isEqualTo(currency);
            return this;
        }

        public FeeSideResultAssert hasRawFee(BigDecimal expected) {
            isNotNull();
            Assertions.assertThat(actual.getRawFee()).isEqualByComparingTo(expected);
            return this;
        }

        public FeeSideResultAssert hasRoundedFee(BigDecimal expected) {
            isNotNull();
            Assertions.assertThat(actual.getRoundedFee()).isEqualByComparingTo(expected);
            return this;
        }

        public FeeSideResultAssert hasFinalFee(BigDecimal expected) {
            isNotNull();
            Assertions.assertThat(actual.getFinalFee()).isEqualByComparingTo(expected);
            return this;
        }

        public FeeSideResultAssert usedDefinition(FeeSideDefinition def) {
            isNotNull();
            Assertions.assertThat(actual.getFeeDefinitionUsed()).isSameAs(def);
            return this;
        }

        public FeeSideResultAssert isWaived() {
            isNotNull();
            Assertions.assertThat(actual.isWaived()).isTrue();
            return this;
        }

        public FeeSideResultAssert isNotWaived() {
            isNotNull();
            Assertions.assertThat(actual.isWaived()).isFalse();
            return this;
        }

        public FeeSideResultAssert hasCapApplied(String code) {
            isNotNull();
            Assertions.assertThat(actual.getCapApplied()).isEqualTo(code);
            return this;
        }
    }
}
