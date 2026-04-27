package com.gateway.fee.domain.resolution;

import com.gateway.fee.domain.exception.NoMatchingRuleException;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.registry.FeeRuleRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class FeeRuleResolverTest {
    @Mock
    private FeeRuleRegistry feeRuleRegistry;

    @InjectMocks
    private FeeRuleResolver feeRuleResolver;

    private FeeRule feeRule;
    FeeSideDefinition flatFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);

    @BeforeEach
// a full wildcard rule
    public void setup() {
        feeRule = new FeeRule("id1", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Global Default Rule");
    }

    //•	Only a global default rule exists → always matches
    @Test
    public void shouldReturnGlobalDefaultWhenOnlyDefaultExists() {
        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(feeRule));
        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        // assert
        assertThat(resolvedRule.getRuleId()).isEqualTo("id1");
    }

    //•	Exact match on all dimensions → returns that rule
    @Test
    public void shouldReturnExactMatchOverDefault() {
        FeeRule specificRule = new FeeRule("id2", null, UserType.BUSINESS, TransactionType.PAYMENT, Currency.EUR, Currency.USD, flatFee, null, LocalDateTime.now(), true, "Specific Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(feeRule, specificRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.PAYMENT, Currency.EUR, Currency.USD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("id2");
    }

    //•	Exact transaction type beats wildcard transaction type
    @Test
    public void shouldPreferExactTransactionTypeOverWildcard() {
        FeeRule exactTransactionRule = new FeeRule("id3", null, null, TransactionType.WIRE_TRANSFER, null, null, flatFee, null, LocalDateTime.now(), true, "Exact Transaction Rule");
        FeeRule wildcardTransactionRule = new FeeRule("id4", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Transaction Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(exactTransactionRule, wildcardTransactionRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("id3");
    }

    //•	Exact user type beats wildcard user type
    @Test
    public void shouldPreferExactUserTypeOverWildcard() {
        FeeRule exactUserTypeRule = new FeeRule("id5", null, UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Exact User Type Rule");
        FeeRule wildcardUserTypeRule = new FeeRule("id6", null, null, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Wildcard User Type Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(exactUserTypeRule, wildcardUserTypeRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("id5");
    }

    //•	Exact currency pair beats wildcard currency pair
    @Test
    public void shouldPreferExactCurrencyPairOverWildcard() {
        FeeRule exactCurrencyRule = new FeeRule("id7", null, UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Exact Currency Rule");
        FeeRule wildcardCurrencyRule = new FeeRule("id8", null, UserType.BUSINESS, TransactionType.WIRE_TRANSFER, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Currency Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(exactCurrencyRule, wildcardCurrencyRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("id7");
    }

    //    •	Rule A: userId="user-123", Rule B: userId=null, same dimensions otherwise
    @Test
    public void shouldPreferExactFeeRuleOverWildcard() {
        FeeRule ruleA = new FeeRule("id9", "user-123", UserType.PERSONAL, null, null, null, flatFee, null, LocalDateTime.now(), true, "User Specific Rule");
        FeeRule ruleB = new FeeRule("id10", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "User Specific Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB));

        FeeRule resolvedRule = feeRuleResolver.resolve("user-123", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("id9");
    }

    @Test
    public void shouldFallbackToWildcardWhenUserIdDoesNotMatch() {
        FeeRule ruleA = new FeeRule("id11", "user-123", UserType.PERSONAL, null, null, null, flatFee, null, LocalDateTime.now(), true, "User Specific Rule");
        FeeRule ruleB = new FeeRule("id12", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB));

        FeeRule resolvedRule = feeRuleResolver.resolve("user-456", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("id12");
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_USD_IQD() {
        FeeRule ruleA = new FeeRule("A", null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule("B", null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule("C", null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule("D", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("A");
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_USD_EUR() {
        FeeRule ruleA = new FeeRule("A", null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule("B", null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule("C", null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule("D", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.EUR);
        assertThat(resolvedRule.getRuleId()).isEqualTo("B");
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_EUR_IQD() {
        FeeRule ruleA = new FeeRule("A", null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule("B", null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule("C", null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule("D", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.EUR, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("C");
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_EUR_EUR() {
        FeeRule ruleA = new FeeRule("A", null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule("B", null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule("C", null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule("D", null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.EUR, Currency.EUR);
        assertThat(resolvedRule.getRuleId()).isEqualTo("D");
    }

    @Test
    public void shouldPreferUserSpecificWildcardOverNonUserSpecificExact() {
        FeeRule ruleA = new FeeRule("A", "user-789", UserType.PERSONAL, null, null, null, flatFee, null, LocalDateTime.now(), true, "User-specific wildcard");
        FeeRule ruleB = new FeeRule("B", null, UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Non-user-specific exact");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB));

        FeeRule resolvedRule = feeRuleResolver.resolve("user-789", UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo("A");
    }

    @Test
    public void shouldThrowNoMatchingRuleExceptionWhenNoRulesMatch() {
        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.empty());

        assertThatThrownBy(() -> feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD))
                .isInstanceOf(NoMatchingRuleException.class);
    }
}
