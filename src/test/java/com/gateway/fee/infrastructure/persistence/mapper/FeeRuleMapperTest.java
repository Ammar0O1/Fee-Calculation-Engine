package com.gateway.fee.infrastructure.persistence.mapper;

import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for FeeRuleMapper.
 *
 * Verifies the mapper correctly converts between the domain FeeRule and the
 * FeeRuleEntity in both directions, including nested side definitions and tier
 * brackets. The cases below match the spec (Section 3.2).
 *
 * Note: ruleId is UUID, userId is String (team decision).
 * FeeRuleMapper has no dependencies, so we just instantiate it directly.
 */
class FeeRuleMapperTest extends AbstractPostgresIntegrationTest {

    private final FeeRuleMapper mapper = new FeeRuleMapper();

    // ---- helper to build the standard 3-bracket tier list ----
    private List<TierBracket> threeBrackets() {
        return List.of(
                new TierBracket(new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0.02")),
                new TierBracket(new BigDecimal("1000"), new BigDecimal("5000"), new BigDecimal("0.015")),
                new TierBracket(new BigDecimal("5000"), null, new BigDecimal("0.01"))
        );
    }

    // ===== TEST 1: round-trip with TIERED_MARGINAL + caps on sender, FLAT receiver =====
    @Test
    void shouldRoundTripComplexRuleWithTieredMarginalAndCaps() {
        FeeSideDefinition senderFee = new FeeSideDefinition(
                CalculationMode.TIERED_MARGINAL,
                null,
                null,
                threeBrackets(),
                new BigDecimal("5.0000"),
                new BigDecimal("500.0000")
        );

        FeeSideDefinition receiverFee = new FeeSideDefinition(
                CalculationMode.FLAT,
                new BigDecimal("3.0000"),
                null,
                null,
                null,
                null
        );

        FeeRule original = new FeeRule(
                UUID.randomUUID(),       // ruleId (UUID)
                "user-123",              // userId (String)
                UserType.CORPORATE,
                TransactionType.WIRE_TRANSFER,
                Currency.USD,
                Currency.IQD,
                senderFee,
                receiverFee,
                LocalDateTime.now(),
                true,
                "complex rule"
        );

        // domain -> entity -> domain
        FeeRuleEntity entity = mapper.toEntity(original);
        FeeRule result = mapper.toDomain(entity);

        // top-level fields preserved
        assertThat(result.getRuleId()).isEqualTo(original.getRuleId());
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getUserType()).isEqualTo(UserType.CORPORATE);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.WIRE_TRANSFER);
        assertThat(result.getSourceCurrency()).isEqualTo(Currency.USD);
        assertThat(result.getDestinationCurrency()).isEqualTo(Currency.IQD);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getDescription()).isEqualTo("complex rule");

        // sender side preserved (mode, caps, tiers)
        FeeSideDefinition resultSender = result.getSenderFee();
        assertThat(resultSender.getCalculationMode()).isEqualTo(CalculationMode.TIERED_MARGINAL);
        assertThat(resultSender.getMinCap()).isEqualByComparingTo("5.0000");
        assertThat(resultSender.getMaxCap()).isEqualByComparingTo("500.0000");
        assertThat(resultSender.getTiers()).hasSize(3);
        // tier order and values preserved
        assertThat(resultSender.getTiers().get(0).getFromAmount()).isEqualByComparingTo("0");
        assertThat(resultSender.getTiers().get(0).getRate()).isEqualByComparingTo("0.02");
        assertThat(resultSender.getTiers().get(2).getToAmount()).isNull();
        assertThat(resultSender.getTiers().get(2).getRate()).isEqualByComparingTo("0.01");

        // receiver side preserved
        FeeSideDefinition resultReceiver = result.getReceiverFee();
        assertThat(resultReceiver.getCalculationMode()).isEqualTo(CalculationMode.FLAT);
        assertThat(resultReceiver.getFlatAmount()).isEqualByComparingTo("3.0000");
        assertThat(resultReceiver.getTiers()).isNull();
    }

    // ===== TEST 2: rule with only senderFee (receiverFee null) =====
    @Test
    void shouldRoundTripRuleWithOnlySenderFee() {
        FeeSideDefinition senderFee = new FeeSideDefinition(
                CalculationMode.PERCENTAGE,
                null,
                new BigDecimal("0.015000"),
                null,
                null,
                null
        );

        FeeRule original = new FeeRule(
                UUID.randomUUID(),
                "user-456",
                UserType.PERSONAL,
                TransactionType.PAYMENT,
                Currency.USD,
                Currency.USD,
                senderFee,
                null,                 // receiver waived
                LocalDateTime.now(),
                true,
                "sender only"
        );

        FeeRuleEntity entity = mapper.toEntity(original);

        // only ONE side definition row should exist
        assertThat(entity.getSideDefinitions()).hasSize(1);
        assertThat(entity.getSideDefinitions().get(0).getSide()).isEqualTo("SENDER");

        FeeRule result = mapper.toDomain(entity);
        assertThat(result.getSenderFee()).isNotNull();
        assertThat(result.getSenderFee().getCalculationMode()).isEqualTo(CalculationMode.PERCENTAGE);
        assertThat(result.getReceiverFee()).isNull();
    }

    // ===== TEST 3: wildcard fields (null) stored and read back as null =====
    @Test
    void shouldRoundTripRuleWithWildcardFields() {
        FeeSideDefinition senderFee = new FeeSideDefinition(
                CalculationMode.FLAT,
                new BigDecimal("1.0000"),
                null,
                null,
                null,
                null
        );

        FeeRule original = new FeeRule(
                UUID.randomUUID(),
                null,                 // userId null (not user-specific)
                null,                 // userType null (wildcard)
                null,                 // transactionType null (wildcard)
                null,                 // sourceCurrency null (wildcard)
                null,                 // destinationCurrency null (wildcard)
                senderFee,
                null,
                LocalDateTime.now(),
                true,
                "global default"
        );

        FeeRuleEntity entity = mapper.toEntity(original);

        // entity should store the wildcard fields as null
        assertThat(entity.getUserId()).isNull();
        assertThat(entity.getUserType()).isNull();
        assertThat(entity.getTransactionType()).isNull();
        assertThat(entity.getSourceCurrency()).isNull();
        assertThat(entity.getDestinationCurrency()).isNull();

        FeeRule result = mapper.toDomain(entity);
        assertThat(result.getUserId()).isNull();
        assertThat(result.getUserType()).isNull();
        assertThat(result.getTransactionType()).isNull();
        assertThat(result.getSourceCurrency()).isNull();
        assertThat(result.getDestinationCurrency()).isNull();
        assertThat(result.getSenderFee()).isNotNull();
    }

    // ===== TEST 4: back-references and bracket order set on children =====
    @Test
    void shouldSetBackReferencesOnChildren() {
        FeeSideDefinition senderFee = new FeeSideDefinition(
                CalculationMode.TIERED_FLAT,
                null,
                null,
                threeBrackets(),
                null,
                null
        );

        FeeRule original = new FeeRule(
                UUID.randomUUID(),
                "user-789",
                UserType.BUSINESS,
                TransactionType.CASH_WITHDRAWAL,
                Currency.EUR,
                Currency.EUR,
                senderFee,
                null,
                LocalDateTime.now(),
                true,
                "back-ref check"
        );

        FeeRuleEntity entity = mapper.toEntity(original);

        // side definition should point back to its parent rule
        var sideDef = entity.getSideDefinitions().get(0);
        assertThat(sideDef.getFeeRule()).isSameAs(entity);

        // each tier bracket should point back to its parent side definition,
        // and bracket_order should be preserved (0, 1, 2)
        assertThat(sideDef.getTierBrackets()).hasSize(3);
        for (int i = 0; i < sideDef.getTierBrackets().size(); i++) {
            var bracket = sideDef.getTierBrackets().get(i);
            assertThat(bracket.getSideDefinition()).isSameAs(sideDef);
            assertThat(bracket.getBracketOrder()).isEqualTo(i);
        }
    }
}