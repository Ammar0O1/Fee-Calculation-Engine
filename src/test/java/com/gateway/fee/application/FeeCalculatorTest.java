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

    //Simple case: both sides charged flat → verify both results
    @Test
    public void shouldCalculateBothSidesWhenBothAreFlatFee() {
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);

        FeeRule rule = new FeeRule("id2", null, UserType.BUSINESS, TransactionType.PAYMENT, Currency.EUR, Currency.USD, senderFeeDef, receiverFeeDef, LocalDateTime.now(), true, "Specific Rule");

        // Mock resolver to return our rule
        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), senderFeeDef, false);

        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("5"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(applier.apply(new BigDecimal("5"), senderFeeDef, Currency.USD)).thenReturn(senderResult);
        // Mock strategy factory to return a flat fee strategy
        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        // Mock strategy to return the flat fee amount
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("5"));        // Call the method under test

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isFalse();
    }

    //•	Sender and receiver have different user types → may match different rules
    @Test
    public void shouldCalculateBothSidesWhenDifferentUserTypesMatchDifferentRules() {
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("10"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("3"), null, null, null, null);

        // Rule for sender (CORPORATE user type)
        FeeRule senderRule = new FeeRule("sender-rule", null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, null, LocalDateTime.now(), true, "Corporate Sender Rule");

        // Rule for receiver (PERSONAL user type)
        FeeRule receiverRule = new FeeRule("receiver-rule", null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, receiverFeeDef, LocalDateTime.now(), true, "Personal Receiver Rule");

        // Mock resolver to return different rules based on user type
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

    // Sender has user-specific rule, receiver doesn't → different resolution paths
    @Test
    public void shouldResolveDifferentPathsWhenSenderHasUserSpecificRule() {
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("15"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("2"), null, null, null, null);

        // Sender-specific rule (user-specific)
        FeeRule senderSpecificRule = new FeeRule("sender-specific", "sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, null, LocalDateTime.now(), true, "Sender-Specific Rule");

        // Generic receiver rule (no userId)
        FeeRule receiverGenericRule = new FeeRule("receiver-generic", null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, receiverFeeDef, LocalDateTime.now(), true, "Receiver Generic Rule");

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

    // Sender's matched rule has senderFee=null → sender waived, receiver calculated normally
    @Test
    public void shouldWaiveSenderFeeWhenRuleHasNullSenderFee() {
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeSideDefinition dummyFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("0"), null, null, null, null);

        // Rule with null sender fee (waived) but non-null receiver fee to satisfy validation
        FeeRule senderWaivedRule = new FeeRule("sender-waived", null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, dummyFeeDef, LocalDateTime.now(), true, "Sender Waived Rule");

        // Rule with receiver fee
        FeeRule receiverRule = new FeeRule("receiver-rule", null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, receiverFeeDef, LocalDateTime.now(), true, "Receiver Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(senderWaivedRule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(receiverRule);

        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, BigDecimal.ZERO, BigDecimal.ZERO, "NONE", BigDecimal.ZERO, BigDecimal.ZERO, null, true);
        FeeSideResult receiverResult = new FeeSideResult(null, null, null, null, Currency.IQD, new BigDecimal("5"), new BigDecimal("5"), "NONE", BigDecimal.ZERO, new BigDecimal("5"), receiverFeeDef, false);

        when(applier.apply(new BigDecimal("5"), receiverFeeDef, Currency.IQD)).thenReturn(receiverResult);

        when(strategyFactory.getStrategy(CalculationMode.FLAT)).thenReturn(strategy);
        when(strategy.calculate(any(), any())).thenReturn(new BigDecimal("5"));

        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().isWaived()).isTrue();
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(new BigDecimal("5"));
        assertThat(result.getReceiverResult().isWaived()).isFalse();
    }

    // Both sides waived
    @Test
    public void shouldWaiveBothSidesWhenBothRulesHaveNullFees() {
        FeeSideDefinition dummyFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("0"), null, null, null, null);

        // Rule for sender with null senderFee (waived) but non-null receiverFee to satisfy validation
        FeeRule senderWaivedRule = new FeeRule("sender-waived", null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, null, dummyFeeDef, LocalDateTime.now(), true, "Sender Waived Rule");

        // Rule for receiver with null receiverFee (waived) but non-null senderFee to satisfy validation
        FeeRule receiverWaivedRule = new FeeRule("receiver-waived", null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, dummyFeeDef, null, LocalDateTime.now(), true, "Receiver Waived Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(senderWaivedRule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(receiverWaivedRule);


        FeeCalculationResult result = feeCalculator.calculate(transaction);
        assertThat(result.getSenderResult().isWaived()).isTrue();
        assertThat(result.getReceiverResult().isWaived()).isTrue();
        assertThat(result.getSenderResult().getFinalFee()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getReceiverResult().getFinalFee()).isEqualTo(BigDecimal.ZERO);
    }

    // Cross-currency USD→IQD: sender fee in USD, receiver fee in IQD
    @Test
    public void shouldApplyFeesInCorrectCurrencies() {
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("10"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("15000"), null, null, null, null);

        FeeRule rule = new FeeRule("cross-currency-rule", null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, receiverFeeDef, LocalDateTime.now(), true, "Cross-Currency Rule");

        when(resolver.resolve("sender-1", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);
        when(resolver.resolve("receiver-1", UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD)).thenReturn(rule);

        // Sender fee in USD
        FeeSideResult senderResult = new FeeSideResult(null, null, null, null, Currency.USD, new BigDecimal("10"), new BigDecimal("10"), "NONE", BigDecimal.ZERO, new BigDecimal("10"), senderFeeDef, false);
        // Receiver fee in IQD
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

    // Transaction amount=0 throws, amount=-100 throws
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

    // Self-transfer (senderId equals receiverId)
    @Test
    public void shouldCalculateFeeForSelfTransfer() {
        FeeSideDefinition senderFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeSideDefinition receiverFeeDef = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("2"), null, null, null, null);

        FeeRule rule = new FeeRule("self-transfer-rule", null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD, senderFeeDef, receiverFeeDef, LocalDateTime.now(), true, "Self-Transfer Rule");

        // Self-transfer transaction
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


