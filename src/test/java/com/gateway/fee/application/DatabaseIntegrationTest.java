package com.gateway.fee.application;

import com.gateway.fee.domain.calculation.CalculationStrategyFactory;
import com.gateway.fee.domain.calculation.FeeApplier;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.resolution.FeeRuleResolver;
import com.gateway.fee.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import com.gateway.fee.infrastructure.persistence.entity.TierBracketEntity;
import com.gateway.fee.infrastructure.persistence.mapper.FeeRuleMapper;
import com.gateway.fee.infrastructure.persistence.mapper.FeeTransactionLogMapper;
import com.gateway.fee.infrastructure.storage.DatabaseFeeRuleRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeeRuleRepository feeRuleRepository;

    @Autowired
    private FeeTransactionLogRepository logRepository;

    private FeeCalculator calculator;

    @BeforeEach
    public void setUp() {
        // Clear any leftover data from previous tests
        feeRuleRepository.deleteAll();
        logRepository.deleteAll();

        // Wire real objects — Spring gives us real repos, we create the rest manually
        FeeRuleMapper mapper = new FeeRuleMapper();
        DatabaseFeeRuleRegistry registry = new DatabaseFeeRuleRegistry(feeRuleRepository, mapper);
        FeeRuleResolver resolver = new FeeRuleResolver(registry);
        CalculationStrategyFactory factory = new CalculationStrategyFactory();
        FeeApplier applier = new FeeApplier();
        FeeTransactionLogMapper logMapper = new FeeTransactionLogMapper();
        FeeTransactionLogService logService = new FeeTransactionLogService(logMapper, logRepository);
        calculator = new FeeCalculator(resolver, factory, applier, logService);

        // Insert test rules into the real database
        insertRules();
    }

    private void insertRules() {
        // Rule 1: Global default — FLAT $1 sender, receiver waived
        saveRule(null, null, null, null, null,
                "SENDER", CalculationMode.FLAT, new BigDecimal("1"), null, null, null, null,
                "Global default rule");

        // Rule 2: CORPORATE + WIRE_TRANSFER — PERCENTAGE 0.5% sender
        saveRule(null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, null, null,
                "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.005"), null, null, null,
                "Corporate wire transfer rule");

        // Rule 3: PERSONAL + INTERNAL_TRANSFER — FLAT $1 sender
        saveRule(null, UserType.PERSONAL, TransactionType.INTERNAL_TRANSFER, null, null,
                "SENDER", CalculationMode.FLAT, new BigDecimal("1"), null, null, null, null,
                "Personal internal transfer rule");

        // Rule 4: PERSONAL + PAYMENT + USD→IQD — HYBRID sender with caps, FLAT 500 receiver
        saveRuleWithBothSides(
                null, UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.IQD,
                CalculationMode.HYBRID, new BigDecimal("2"), new BigDecimal("0.005"), new BigDecimal("3"), new BigDecimal("20"),
                CalculationMode.FLAT, new BigDecimal("500"), null, null, null,
                "Personal USD to IQD payment rule");

        // Rule 5: user-123 + CORPORATE + WIRE_TRANSFER — PERCENTAGE 0.2% sender (user-specific)
        saveRule("user-123", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, null, null,
                "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.002"), null, null, null,
                "User specific corporate wire transfer rule");

        // Rule 6: BUSINESS_TERMINAL + PAYMENT — TIERED_MARGINAL sender
        saveRuleWithTiers(null, UserType.BUSINESS_TERMINAL, TransactionType.PAYMENT, null, null,
                CalculationMode.TIERED_MARGINAL,
                new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0.02"),
                new BigDecimal("1000"), new BigDecimal("5000"), new BigDecimal("0.015"),
                new BigDecimal("5000"), null, new BigDecimal("0.01"),
                "Business terminal payment with marginal tiers");

        // Rule 7: CORPORATE + WIRE_TRANSFER + USD→* — PERCENTAGE 0.3% (partial source)
        saveRule(null, UserType.CORPORATE, TransactionType.WIRE_TRANSFER, Currency.USD, null,
                "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.003"), null, null, null,
                "Partial source currency match - USD to any destination");

        // Rule 8: user-456 + CORPORATE + WIRE_TRANSFER — FLAT $7.50 (second user-specific)
        saveRule("user-456", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, null, null,
                "SENDER", CalculationMode.FLAT, new BigDecimal("7.50"), null, null, null, null,
                "Second user-specific corporate wire transfer rule");
    }

    // Helper: saves a rule with one side definition (sender or receiver)
    private void saveRule(String userId, UserType userType, TransactionType txType,
                          Currency src, Currency dst,
                          String side, CalculationMode mode, BigDecimal flat, BigDecimal pct,
                          BigDecimal minCap, BigDecimal maxCap, java.util.List<TierBracketEntity> tiers,
                          String description) {

        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId(userId)
                .userType(userType)
                .transactionType(txType)
                .sourceCurrency(src)
                .destinationCurrency(dst)
                .effectiveDate(LocalDateTime.now())
                .active(true)
                .description(description)
                .sideDefinitions(new ArrayList<>())
                .build();

        FeeSideDefinitionEntity sideDef = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side(side)
                .calculationMode(mode)
                .flatAmount(flat)
                .percentage(pct)
                .minCap(minCap)
                .maxCap(maxCap)
                .tierBrackets(tiers != null ? tiers : new ArrayList<>())
                .build();

        rule.getSideDefinitions().add(sideDef);
        feeRuleRepository.save(rule);
    }

    // Helper: saves a rule with both sender and receiver sides
    private void saveRuleWithBothSides(String userId, UserType userType, TransactionType txType,
                                       Currency src, Currency dst,
                                       CalculationMode senderMode, BigDecimal senderFlat, BigDecimal senderPct,
                                       BigDecimal senderMinCap, BigDecimal senderMaxCap,
                                       CalculationMode receiverMode, BigDecimal receiverFlat, BigDecimal receiverPct,
                                       BigDecimal receiverMinCap, BigDecimal receiverMaxCap,
                                       String description) {

        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId(userId)
                .userType(userType)
                .transactionType(txType)
                .sourceCurrency(src)
                .destinationCurrency(dst)
                .effectiveDate(LocalDateTime.now())
                .active(true)
                .description(description)
                .sideDefinitions(new ArrayList<>())
                .build();

        FeeSideDefinitionEntity senderDef = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("SENDER")
                .calculationMode(senderMode)
                .flatAmount(senderFlat)
                .percentage(senderPct)
                .minCap(senderMinCap)
                .maxCap(senderMaxCap)
                .tierBrackets(new ArrayList<>())
                .build();

        FeeSideDefinitionEntity receiverDef = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("RECEIVER")
                .calculationMode(receiverMode)
                .flatAmount(receiverFlat)
                .percentage(receiverPct)
                .minCap(receiverMinCap)
                .maxCap(receiverMaxCap)
                .tierBrackets(new ArrayList<>())
                .build();

        rule.getSideDefinitions().add(senderDef);
        rule.getSideDefinitions().add(receiverDef);
        feeRuleRepository.save(rule);
    }

    // Helper: saves a rule with tiered pricing (3 brackets)
    private void saveRuleWithTiers(String userId, UserType userType, TransactionType txType,
                                   Currency src, Currency dst,
                                   CalculationMode mode,
                                   BigDecimal from1, BigDecimal to1, BigDecimal rate1,
                                   BigDecimal from2, BigDecimal to2, BigDecimal rate2,
                                   BigDecimal from3, BigDecimal to3, BigDecimal rate3,
                                   String description) {

        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId(userId)
                .userType(userType)
                .transactionType(txType)
                .sourceCurrency(src)
                .destinationCurrency(dst)
                .effectiveDate(LocalDateTime.now())
                .active(true)
                .description(description)
                .sideDefinitions(new ArrayList<>())
                .build();

        FeeSideDefinitionEntity sideDef = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("SENDER")
                .calculationMode(mode)
                .tierBrackets(new ArrayList<>())
                .build();

        TierBracketEntity bracket1 = TierBracketEntity.builder()
                .sideDefinition(sideDef).fromAmount(from1).toAmount(to1).rate(rate1).bracketOrder(0).build();
        TierBracketEntity bracket2 = TierBracketEntity.builder()
                .sideDefinition(sideDef).fromAmount(from2).toAmount(to2).rate(rate2).bracketOrder(1).build();
        TierBracketEntity bracket3 = TierBracketEntity.builder()
                .sideDefinition(sideDef).fromAmount(from3).toAmount(to3).rate(rate3).bracketOrder(2).build();

        sideDef.getTierBrackets().add(bracket1);
        sideDef.getTierBrackets().add(bracket2);
        sideDef.getTierBrackets().add(bracket3);

        rule.getSideDefinitions().add(sideDef);
        feeRuleRepository.save(rule);
    }

    // Helper: creates a transaction and runs it through the calculator
    private FeeCalculationResult runTransaction(String txId, BigDecimal amount,
                                                Currency src, Currency dst, TransactionType txType,
                                                String senderId, UserType senderType,
                                                String receiverId, UserType receiverType) {
        Transaction tx = new Transaction(txId, amount, src, dst, txType,
                senderId, senderType, receiverId, receiverType, LocalDateTime.now());
        return calculator.calculate(tx);
    }

    // ==================== TESTS ====================

    // Same scenarios as Assignment 1 integration tests — proves DB swap is seamless

    @Test
    public void shouldCalculateCorporateWireTransferFee() {
        FeeCalculationResult result = runTransaction("tx-1", new BigDecimal("10000"),
                Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "corp-sender", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        // CORPORATE + WIRE_TRANSFER → 0.5% of 10000 = 50.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isTrue();
    }

    @Test
    public void shouldMatchUserSpecificRuleForUser123() {
        FeeCalculationResult result = runTransaction("tx-2", new BigDecimal("10000"),
                Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "user-123", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        // user-123 specific rule → 0.2% of 10000 = 20.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
    }

    @Test
    public void shouldFallbackToGlobalDefault() {
        FeeCalculationResult result = runTransaction("tx-3", new BigDecimal("500"),
                Currency.EUR, Currency.EUR, TransactionType.INTERNAL_TRANSFER,
                "some-sender", UserType.CORPORATE_TERMINAL, "some-receiver", UserType.CORPORATE_TERMINAL);

        // No specific rule → global default FLAT $1.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
        assertThat(result.getReceiverResult().isWaived()).isTrue();
    }

    @Test
    public void shouldApplyMinCapForHybridFee() {
        FeeCalculationResult result = runTransaction("tx-4a", new BigDecimal("100"),
                Currency.USD, Currency.IQD, TransactionType.PAYMENT,
                "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        // HYBRID: 2 + (0.005 × 100) = 2.50 → below minCap 3.00 → fee = 3.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result.getSenderResult().getCapApplied()).isEqualTo("MIN_CAP_APPLIED");
    }

    @Test
    public void shouldApplyMaxCapForHybridFee() {
        FeeCalculationResult result = runTransaction("tx-4b", new BigDecimal("5000"),
                Currency.USD, Currency.IQD, TransactionType.PAYMENT,
                "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        // HYBRID: 2 + (0.005 × 5000) = 27.00 → above maxCap 20.00 → fee = 20.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getSenderResult().getCapApplied()).isEqualTo("MAX_CAP_APPLIED");
    }

    @Test
    public void shouldCalculateMarginalTieredFee() {
        FeeCalculationResult result = runTransaction("tx-5", new BigDecimal("8000"),
                Currency.USD, Currency.USD, TransactionType.PAYMENT,
                "terminal-sender", UserType.BUSINESS_TERMINAL, "receiver", UserType.PERSONAL);

        // Marginal: (1000×0.02) + (4000×0.015) + (3000×0.01) = 20 + 60 + 30 = 110.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("110.00"));
        assertThat(result.getSenderResult().isWaived()).isFalse();
    }

    @Test
    public void shouldCalculateBothSidesForCrossCurrencyTransaction() {
        FeeCalculationResult result = runTransaction("tx-6", new BigDecimal("1000"),
                Currency.USD, Currency.IQD, TransactionType.PAYMENT,
                "personal-sender", UserType.PERSONAL, "receiver", UserType.PERSONAL);

        // Sender: HYBRID 2 + (0.005 × 1000) = 7.00 USD (between caps)
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(result.getSenderResult().getCurrency()).isEqualTo(Currency.USD);

        // Receiver: FLAT 500 IQD
        assertThat(result.getReceiverResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(result.getReceiverResult().getCurrency()).isEqualTo(Currency.IQD);
    }

    @Test
    public void shouldPreferPartialSourceCurrencyMatch() {
        FeeCalculationResult result = runTransaction("tx-7", new BigDecimal("10000"),
                Currency.USD, Currency.EUR, TransactionType.WIRE_TRANSFER,
                "corp-sender", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        // Partial source match (USD→*) → 0.3% of 10000 = 30.00
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    public void shouldMatchSecondUserSpecificRule() {
        FeeCalculationResult result = runTransaction("tx-8", new BigDecimal("2000"),
                Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "user-456", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        // user-456 specific rule → FLAT $7.50
        assertThat(result.getSenderResult().getFinalFee()).isEqualByComparingTo(new BigDecimal("7.50"));
    }

    @Test
    public void shouldHandleSelfTransfer() {
        FeeCalculationResult result = runTransaction("tx-9", new BigDecimal("1000"),
                Currency.EUR, Currency.EUR, TransactionType.WIRE_TRANSFER,
                "corp-user", UserType.CORPORATE, "corp-user", UserType.CORPORATE);

        assertThat(result.getSenderResult()).isNotNull();
        assertThat(result.getReceiverResult()).isNotNull();
        assertThat(result.getSenderResult().getFinalFee()).isNotNull();
    }

    @Test
    public void shouldLogCalculationToDatabase() {
        runTransaction("tx-log-1", new BigDecimal("10000"),
                Currency.EUR, Currency.IQD, TransactionType.WIRE_TRANSFER,
                "corp-sender", UserType.CORPORATE, "receiver", UserType.PERSONAL);

        // Verify the calculation was logged
        var logs = logRepository.findByTransactionId("tx-log-1");
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getTransactionId()).isEqualTo("tx-log-1");
        assertThat(logs.get(0).getSenderFinalFee()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(logs.get(0).isSenderWaived()).isFalse();
        assertThat(logs.get(0).isReceiverWaived()).isTrue();
    }
}