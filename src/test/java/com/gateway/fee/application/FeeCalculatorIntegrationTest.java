package com.gateway.fee.application;

import com.gateway.fee.domain.calculation.CalculationStrategyFactory;
import com.gateway.fee.domain.calculation.FeeApplier;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.resolution.FeeRuleResolver;
import com.gateway.fee.infrastructure.FeeRuleJsonLoader;
import com.gateway.fee.infrastructure.storage.InMemoryFeeRuleRegistry;
import io.vavr.collection.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FeeCalculatorIntegrationTest {

    private FeeCalculator calculator;

    @BeforeEach
    public void setUp() throws IOException {
        FeeRuleJsonLoader loader = new FeeRuleJsonLoader();
        List<FeeRule> rules = loader.load("src/test/resources/test-rules.json");
        InMemoryFeeRuleRegistry registry = new InMemoryFeeRuleRegistry();
        rules.forEach(registry::add);
        FeeRuleResolver resolver = new FeeRuleResolver(registry);
        CalculationStrategyFactory factory = new CalculationStrategyFactory();
        FeeApplier applier = new FeeApplier();
        FeeTransactionLogService logService = mock(FeeTransactionLogService.class);
        calculator = new FeeCalculator(resolver, factory, applier, logService);
    }

    private FeeCalculationResult runTransaction(String txId, BigDecimal amount, Currency src, Currency dst, TransactionType txType, String senderId, UserType senderType, String receiverId, UserType receiverType) {
        Transaction tx = new Transaction(txId, amount, src, dst, txType, senderId, senderType, receiverId, receiverType, LocalDateTime.now());
        return calculator.calculate(tx);
    }

    @Test
    public void shouldCalculateCorporateWireTransferFee() {
        FeeCalculationResult result = runTransaction("tx-1", new BigDecimal("10000"), Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER, "corp-sender", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isTrue();
        assertThat(result.getReceiverResult().getFinalFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void shouldMatchUserSpecificRuleForUser123() {
        FeeCalculationResult result = runTransaction("tx-2", new BigDecimal("10000"), Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER, "user-123", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
    }

    @Test
    public void shouldFallbackToGlobalDefault() {
        FeeCalculationResult result = runTransaction("tx-3", new BigDecimal("500"), Currency.EUR, Currency.EUR, TransactionType.INTERNAL_TRANSFER, "some-sender", UserType.CORPORATE_TERMINAL, "some-receiver", UserType.CORPORATE_TERMINAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isTrue();
    }

    @Test
    public void shouldApplyMinCapForHybridFee() {
        FeeCalculationResult result = runTransaction("tx-4a", new BigDecimal("100"), Currency.USD, Currency.IQD, TransactionType.PAYMENT, "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result.getSenderResult().getCapApplied()).isEqualTo("MIN_CAP_APPLIED");
    }

    @Test
    public void shouldApplyMaxCapForHybridFee() {
        FeeCalculationResult result = runTransaction("tx-4b", new BigDecimal("5000"), Currency.USD, Currency.IQD, TransactionType.PAYMENT, "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getSenderResult().getCapApplied()).isEqualTo("MAX_CAP_APPLIED");
    }

    @Test
    public void shouldCalculateMarginalTieredFee() {
        FeeCalculationResult result = runTransaction("tx-5", new BigDecimal("8000"), Currency.USD, Currency.USD, TransactionType.PAYMENT, "terminal-sender", UserType.BUSINESS_TERMINAL, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("110.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
    }

    @Test
    public void shouldCalculateBothSidesForCrossCurrencyTransaction() {
        FeeCalculationResult result = runTransaction("tx-6", new BigDecimal("1000"), Currency.USD, Currency.IQD, TransactionType.PAYMENT, "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(result.getSenderResult().getCurrency()).isEqualTo(Currency.USD);

        assertThat(result.getReceiverResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(result.getReceiverResult().getCurrency()).isEqualTo(Currency.IQD);
        assertThat(result.getReceiverResult().isWaived()).isFalse();
    }

    @Test
    public void shouldPreferPartialSourceCurrencyMatch() {
        FeeCalculationResult result = runTransaction("tx-7", new BigDecimal("10000"), Currency.USD, Currency.EUR, TransactionType.WIRE_TRANSFER, "corp-sender", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    public void shouldIgnoreInactiveRule() {
        FeeCalculationResult result = runTransaction("tx-8", new BigDecimal("1000"), Currency.USD, Currency.EUR, TransactionType.PAYMENT, "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        BigDecimal fivePercentFee = new BigDecimal("50.00");
        assertThat(result.getSenderResult().getFinalFee()).isNotEqualByComparingTo(fivePercentFee);
        assertThat(result.getSenderResult().isWaived()).isFalse();
    }

    @Test
    public void shouldHandleSelfTransfer() {
        FeeCalculationResult result = runTransaction("tx-9", new BigDecimal("1000"), Currency.EUR, Currency.EUR, TransactionType.WIRE_TRANSFER, "corp-user", UserType.CORPORATE, "corp-user", UserType.CORPORATE);

        assertThat(result.getSenderResult()).isNotNull();
        assertThat(result.getReceiverResult()).isNotNull();
        assertThat(result.getSenderResult().getFinalFee()).isNotNull();
    }

    @Test
    public void shouldMatchSecondUserSpecificRule() {
        FeeCalculationResult result = runTransaction("tx-10", new BigDecimal("2000"), Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER, "user-456", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("7.50"));
    }

    @Test
    public void shouldCalculateBusinessWireTransferFee() {
        FeeCalculationResult result = runTransaction("tx-11", new BigDecimal("5000"), Currency.EUR, Currency.EUR, TransactionType.WIRE_TRANSFER, "biz-sender", UserType.BUSINESS, "receiver", UserType.PERSONAL);

        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getSenderResult().getFinalFee()).isNotNull();
    }
}