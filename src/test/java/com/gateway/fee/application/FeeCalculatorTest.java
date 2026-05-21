package com.gateway.fee.application;

import com.gateway.fee.domain.calculation.CalculationStrategyFactory;
import com.gateway.fee.domain.calculation.FeeApplier;
import com.gateway.fee.domain.calculation.FeeCalculationStrategy;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.resolution.FeeRuleResolver;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeeCalculatorTest {

    @Mock
    private CalculationStrategyFactory strategyFactory;
    @Mock
    private FeeRuleResolver resolver;
    @Mock
    private FeeCalculationStrategy strategy;
    @Mock
    private FeeApplier applier;
    @Mock
    private FeeTransactionLogService logService;

    private Transaction transaction;
    FeeSideDefinition flatFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);

    @InjectMocks
    private FeeCalculator feeCalculator;

    @BeforeEach
    public void setup() {
        transaction = new Transaction("tx-1", new BigDecimal("1000"),
                Currency.USD, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "sender-1", UserType.CORPORATE, "receiver-1", UserType.PERSONAL, LocalDateTime.now());
    }

    @Test
    public void shouldCalculateBothSidesWhenBothAreFlatFee() {
        UUID ruleId = UUID.randomUUID();
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);

        FeeRule rule = new FeeRule(ruleId, null, UserType.BUSINESS, TransactionType.PAYMENT, Currency.EUR, Currency.USD, senderFeeDef, receiverFeeDef, LocalDateTime.now(), true, "Specific Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), senderFeeDef, false);
        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("5"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);
        when(applier.apply(new BigDecimal("5"), senderFeeDef, Currency.USD)).thenReturn(senderResult);
        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("5"));

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isFalse();
    }

    @Test
    public void shouldCalculateBothSidesWhenDifferentUserTypesMatchDifferentRules() {
        UUID senderRuleId = UUID.randomUUID();
        UUID receiverRuleId = UUID.randomUUID();
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("10"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("3"), null, null, null, null);

        FeeRule senderRule = new FeeRule(senderRuleId, null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, null, LocalDateTime.now(), true, "Corporate Sender Rule");
        FeeRule receiverRule = new FeeRule(receiverRuleId, null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, receiverFeeDef, LocalDateTime.now(), true, "Personal Receiver Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(senderRule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(receiverRule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("10"), new BigDecimal("10"), "NONE", BigDecimal.ZERO, new BigDecimal("10"), senderFeeDef, false);
        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("3"), new BigDecimal("3"), "NONE", BigDecimal.ZERO, new BigDecimal("3"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("10"), senderFeeDef, Currency.USD)).thenReturn(senderResult);
        when(applier.apply(new BigDecimal("3"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("10")).thenReturn(new BigDecimal("3"));

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(new BigDecimal("10"));
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("3"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isFalse();
    }

    @Test
    public void shouldResolveDifferentPathsWhenSenderHasUserSpecificRule() {
        UUID senderRuleId = UUID.randomUUID();
        UUID receiverRuleId = UUID.randomUUID();
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("15"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("2"), null, null, null, null);

        FeeRule senderSpecificRule = new FeeRule(senderRuleId, "sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, null, LocalDateTime.now(), true, "Sender-Specific Rule");
        FeeRule receiverGenericRule = new FeeRule(receiverRuleId, null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, receiverFeeDef, LocalDateTime.now(), true, "Receiver Generic Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(senderSpecificRule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(receiverGenericRule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("15"), new BigDecimal("15"), "NONE", BigDecimal.ZERO, new BigDecimal("15"), senderFeeDef, false);
        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("2"), new BigDecimal("2"), "NONE", BigDecimal.ZERO, new BigDecimal("2"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("15"), senderFeeDef, Currency.USD)).thenReturn(senderResult);
        when(applier.apply(new BigDecimal("2"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("15")).thenReturn(new BigDecimal("2"));

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(new BigDecimal("15"));
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("2"));
    }

    @Test
    public void shouldWaiveSenderFeeWhenRuleHasNullSenderFee() {
        UUID senderRuleId = UUID.randomUUID();
        UUID receiverRuleId = UUID.randomUUID();
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeSideDefinition dummyFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("0"), null, null, null, null);

        FeeRule senderWaivedRule = new FeeRule(senderRuleId, null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, dummyFeeDef, LocalDateTime.now(), true, "Sender Waived Rule");
        FeeRule receiverRule = new FeeRule(receiverRuleId, null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, receiverFeeDef, LocalDateTime.now(), true, "Receiver Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(senderWaivedRule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(receiverRule);

        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("5"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("5"));

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().isWaived()).isTrue();
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getReceiverResult().isWaived()).isFalse();
    }

    @Test
    public void shouldWaiveBothSidesWhenBothRulesHaveNullFees() {
        UUID senderRuleId = UUID.randomUUID();
        UUID receiverRuleId = UUID.randomUUID();
        FeeSideDefinition dummyFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("0"), null, null, null, null);

        FeeRule senderWaivedRule = new FeeRule(senderRuleId, null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, dummyFeeDef, LocalDateTime.now(), true, "Sender Waived Rule");
        FeeRule receiverWaivedRule = new FeeRule(receiverRuleId, null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, dummyFeeDef, null, LocalDateTime.now(), true, "Receiver Waived Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(senderWaivedRule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(receiverWaivedRule);

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().isWaived()).isTrue();
        assertThat(result.getReceiverResult().isWaived()).isTrue();
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void shouldApplyFeesInCorrectCurrencies() {
        UUID ruleId = UUID.randomUUID();
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("10"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("15000"), null, null, null, null);

        FeeRule rule = new FeeRule(ruleId, null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, receiverFeeDef, LocalDateTime.now(), true, "Cross-Currency Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("10"), new BigDecimal("10"), "NONE", BigDecimal.ZERO, new BigDecimal("10"), senderFeeDef, false);
        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("15000"), new BigDecimal("15000"), "NONE", BigDecimal.ZERO, new BigDecimal("15000"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("10"), senderFeeDef, Currency.USD)).thenReturn(senderResult);
        when(applier.apply(new BigDecimal("15000"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("10")).thenReturn(new BigDecimal("15000"));

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().getCurrency()).isEqualTo(Currency.USD);
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(new BigDecimal("10"));
        assertThat(result.getReceiverResult().getCurrency()).isEqualTo(Currency.IQD);
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("15000"));
    }

    @Test
    public void shouldThrowWhenTransactionAmountIsZero() {
        assertThatThrownBy(() -> new Transaction("tx-2", BigDecimal.ZERO,
                Currency.USD, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "sender-1", UserType.CORPORATE, "receiver-1", UserType.PERSONAL, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be > 0");
    }

    @Test
    public void shouldThrowWhenTransactionAmountIsNegative() {
        assertThatThrownBy(() -> new Transaction("tx-3", new BigDecimal("-100"),
                Currency.USD, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "sender-1", UserType.CORPORATE, "receiver-1", UserType.PERSONAL, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be > 0");
    }

    @Test
    public void shouldCalculateFeeForSelfTransfer() {
        UUID ruleId = UUID.randomUUID();
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("2"), null, null, null, null);

        FeeRule rule = new FeeRule(ruleId, null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, receiverFeeDef, LocalDateTime.now(), true, "Self-Transfer Rule");

        Transaction selfTransferTx = new Transaction("tx-self", new BigDecimal("1000"),
                Currency.USD, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "user-same", UserType.CORPORATE, "user-same", UserType.CORPORATE, LocalDateTime.now());

        when(resolver.resolve("user-same", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), senderFeeDef, false);
        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("2"), new BigDecimal("2"), "NONE", BigDecimal.ZERO, new BigDecimal("2"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("5"), senderFeeDef, Currency.USD)).thenReturn(senderResult);
        when(applier.apply(new BigDecimal("2"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("5")).thenReturn(new BigDecimal("2"));

        FeeCalculationResult result = feeCalculator.calculate(selfTransferTx);
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("2"));
    }
}