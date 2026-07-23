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
import java.util.UUID;

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
    private UUID defaultRuleId = UUID.randomUUID();
    FeeSideDefinition flatFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);

    @BeforeEach
    public void setup() {
        feeRule = new FeeRule(defaultRuleId, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Global Default Rule");
    }

    @Test
    public void shouldReturnGlobalDefaultWhenOnlyDefaultExists() {
        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(feeRule));
        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(defaultRuleId);
    }

    @Test
    public void shouldReturnExactMatchOverDefault() {
        UUID specificId = UUID.randomUUID();
        FeeRule specificRule = new FeeRule(specificId, null, UserType.BUSINESS, TransactionType.PAYMENT, Currency.EUR, Currency.USD, flatFee, null, LocalDateTime.now(), true, "Specific Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(feeRule, specificRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.PAYMENT, Currency.EUR, Currency.USD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(specificId);
    }

    @Test
    public void shouldPreferExactTransactionTypeOverWildcard() {
        UUID exactId = UUID.randomUUID();
        UUID wildcardId = UUID.randomUUID();
        FeeRule exactTransactionRule = new FeeRule(exactId, null, null, TransactionType.WIRE_TRANSFER, null, null, flatFee, null, LocalDateTime.now(), true, "Exact Transaction Rule");
        FeeRule wildcardTransactionRule = new FeeRule(wildcardId, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Transaction Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(exactTransactionRule, wildcardTransactionRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(exactId);
    }

    @Test
    public void shouldPreferExactUserTypeOverWildcard() {
        UUID exactId = UUID.randomUUID();
        UUID wildcardId = UUID.randomUUID();
        FeeRule exactUserTypeRule = new FeeRule(exactId, null, UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Exact User Type Rule");
        FeeRule wildcardUserTypeRule = new FeeRule(wildcardId, null, null, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Wildcard User Type Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(exactUserTypeRule, wildcardUserTypeRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(exactId);
    }

    @Test
    public void shouldPreferExactCurrencyPairOverWildcard() {
        UUID exactId = UUID.randomUUID();
        UUID wildcardId = UUID.randomUUID();
        FeeRule exactCurrencyRule = new FeeRule(exactId, null, UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Exact Currency Rule");
        FeeRule wildcardCurrencyRule = new FeeRule(wildcardId, null, UserType.BUSINESS, TransactionType.WIRE_TRANSFER, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Currency Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(exactCurrencyRule, wildcardCurrencyRule));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.BUSINESS, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(exactId);
    }

    @Test
    public void shouldPreferExactFeeRuleOverWildcard() {
        UUID ruleAId = UUID.randomUUID();
        UUID ruleBId = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(ruleAId, "user-123", UserType.PERSONAL, null, null, null, flatFee, null, LocalDateTime.now(), true, "User Specific Rule");
        FeeRule ruleB = new FeeRule(ruleBId, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB));

        FeeRule resolvedRule = feeRuleResolver.resolve("user-123", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(ruleAId);
    }

    @Test
    public void shouldFallbackToWildcardWhenUserIdDoesNotMatch() {
        UUID ruleAId = UUID.randomUUID();
        UUID ruleBId = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(ruleAId, "user-123", UserType.PERSONAL, null, null, null, flatFee, null, LocalDateTime.now(), true, "User Specific Rule");
        FeeRule ruleB = new FeeRule(ruleBId, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Wildcard Rule");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB));

        FeeRule resolvedRule = feeRuleResolver.resolve("user-456", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(ruleBId);
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_USD_IQD() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        UUID idC = UUID.randomUUID();
        UUID idD = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(idA, null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule(idB, null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule(idC, null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule(idD, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(idA);
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_USD_EUR() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        UUID idC = UUID.randomUUID();
        UUID idD = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(idA, null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule(idB, null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule(idC, null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule(idD, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.EUR);
        assertThat(resolvedRule.getRuleId()).isEqualTo(idB);
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_EUR_IQD() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        UUID idC = UUID.randomUUID();
        UUID idD = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(idA, null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule(idB, null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule(idC, null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule(idD, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.EUR, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(idC);
    }

    @Test
    public void shouldResolveCurrencyPairSpecificity_EUR_EUR() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        UUID idC = UUID.randomUUID();
        UUID idD = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(idA, null, null, null, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule A");
        FeeRule ruleB = new FeeRule(idB, null, null, null, Currency.USD, null, flatFee, null, LocalDateTime.now(), true, "Rule B");
        FeeRule ruleC = new FeeRule(idC, null, null, null, null, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Rule C");
        FeeRule ruleD = new FeeRule(idD, null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Rule D");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB, ruleC, ruleD));

        FeeRule resolvedRule = feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.EUR, Currency.EUR);
        assertThat(resolvedRule.getRuleId()).isEqualTo(idD);
    }

    @Test
    public void shouldPreferUserSpecificWildcardOverNonUserSpecificExact() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        FeeRule ruleA = new FeeRule(idA, "user-789", UserType.PERSONAL, null, null, null, flatFee, null, LocalDateTime.now(), true, "User-specific wildcard");
        FeeRule ruleB = new FeeRule(idB, null, UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.IQD, flatFee, null, LocalDateTime.now(), true, "Non-user-specific exact");

        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.of(ruleA, ruleB));

        FeeRule resolvedRule = feeRuleResolver.resolve("user-789", UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.IQD);
        assertThat(resolvedRule.getRuleId()).isEqualTo(idA);
    }

    @Test
    public void shouldThrowNoMatchingRuleExceptionWhenNoRulesMatch() {
        when(feeRuleRegistry.findAllActive()).thenReturn(io.vavr.collection.List.empty());

        assertThatThrownBy(() -> feeRuleResolver.resolve("anyUser", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD))
                .isInstanceOf(NoMatchingRuleException.class);
    }
}