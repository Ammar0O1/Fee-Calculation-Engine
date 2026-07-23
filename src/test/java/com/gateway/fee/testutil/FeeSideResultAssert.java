package com.gateway.fee.testutil;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.FeeSideResult;
import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;
import java.util.Objects;

public class FeeSideResultAssert extends AbstractAssert<FeeSideResultAssert, FeeSideResult> {

    public FeeSideResultAssert(FeeSideResult actual) {
        super(actual, FeeSideResultAssert.class);
    }

    public static FeeSideResultAssert assertThat(FeeSideResult actual) {
        return new FeeSideResultAssert(actual);
    }

    public FeeSideResultAssert hasFinalFee(BigDecimal expected) {
        isNotNull();
        if (actual.getFinalFee().compareTo(expected) != 0) {
            failWithMessage("Expected calculated fee amount to be <%s> but was <%s>", expected, actual.getFinalFee());
        }
        return this;
    }


    public FeeSideResultAssert hasRawFee(BigDecimal expected) {
        isNotNull();
        if (!Objects.equals(actual.getRawFee(), expected)) {
            failWithMessage("Expected raw fee amount to be <%s> but was <%s>", expected, actual.getRawFee());
        }
        return this;
    }


    public FeeSideResultAssert isWaived() {
        isNotNull();
        if (!Objects.equals(actual.isWaived(), true)) {
            failWithMessage("Expected fee to be waived but was not");
        }
        return this;
    }

    public FeeSideResultAssert isNotWaived() {
        isNotNull();
        if (!Objects.equals(actual.isWaived(), false)) {
            failWithMessage("Expected fee to not be waived but was waived");
        }
        return this;
    }

    public FeeSideResultAssert hasCurrency(Currency expected) {
        isNotNull();
        if (!Objects.equals(actual.getCurrency(), expected)) {
            failWithMessage("Expected fee currency to be <%s> but was <%s>", expected, actual.getCurrency());
        }
        return this;
    }

    public FeeSideResultAssert hasNoCapsApplied() {
        isNotNull();
        if (!Objects.equals(actual.getCapApplied(), "NONE")) {
            failWithMessage("Expected no caps to be applied but found cap <%s>", actual.getCapApplied());
        }
        return this;
    }

    public FeeSideResultAssert hasMinCapApplied(BigDecimal expectedMinCap) {
        isNotNull();
        if (!Objects.equals(actual.getCapApplied(), "MIN_CAP_APPLIED")) {
            failWithMessage("Expected min cap to be applied but found cap <%s>", actual.getCapApplied());
        }
        if (actual.getCapAdjustment().compareTo(expectedMinCap) != 0) {
            failWithMessage("Expected min cap adjustment to be <%s> but was <%s>", expectedMinCap, actual.getCapAdjustment());
        }
        return this;
    }

    public FeeSideResultAssert hasMaxCapApplied(BigDecimal expectedMaxCap) {
        isNotNull();
        if (!Objects.equals(actual.getCapApplied(), "MAX_CAP_APPLIED")) {
            failWithMessage("Expected max cap to be applied but found cap <%s>", actual.getCapApplied());
        }
        if (actual.getCapAdjustment().compareTo(expectedMaxCap) != 0) {
            failWithMessage("Expected max cap adjustment to be <%s> but was <%s>", expectedMaxCap, actual.getCapAdjustment());
        }
        return this;
    }

    public FeeSideResultAssert hasMatchedRuleId(String expectedRuleId) {
        isNotNull();
        if (!Objects.equals(actual.getMatchedRuleId(), expectedRuleId)) {
            failWithMessage("Expected matched rule ID to be <%s> but was <%s>", expectedRuleId, actual.getMatchedRuleId());
        }
        return this;
    }

}
