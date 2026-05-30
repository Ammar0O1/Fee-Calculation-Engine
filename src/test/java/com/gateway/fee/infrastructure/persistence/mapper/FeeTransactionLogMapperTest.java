package com.gateway.fee.infrastructure.persistence.mapper;

import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for FeeTransactionLogMapper.
 *
 * Pure unit test — no Spring, no database. The mapper is just object
 * conversion (FeeCalculationResult -> FeeTransactionLogEntity), so we
 * don't need @DataJpaTest here.
 *
 * Logs are write-only: the mapper has no toDomain method, so no round-trip.
 * We verify the mapper sets every field on the entity correctly.
 *
 * FeeSideResult and FeeCalculationResult are @Value classes (immutable,
 * no builder) — we use their generated all-args constructors.
 */
class FeeTransactionLogMapperTest {

    private final FeeTransactionLogMapper mapper = new FeeTransactionLogMapper();

    // helper to build a FeeSideResult — 12 args is verbose, so we centralize it
    private FeeSideResult sideResult(
            String userId, UserType userType, UUID matchedRuleId,
            Currency currency, BigDecimal rawFee, BigDecimal finalFee,
            String capApplied, boolean waived) {
        return new FeeSideResult(
                userId,
                userType,
                matchedRuleId,
                "match-level",       // matchLevel (not asserted)
                currency,
                rawFee,
                rawFee,              // roundedFee (same as raw for these tests)
                capApplied,
                BigDecimal.ZERO,     // capAdjustment (not asserted)
                finalFee,
                null,                // feeDefinitionUsed (not asserted)
                waived
        );
    }

    @Test
    void shouldMapAllFieldsFromCalculationResultToLogEntity() {
        UUID senderRuleId = UUID.randomUUID();
        UUID receiverRuleId = UUID.randomUUID();
        LocalDateTime calcTime = LocalDateTime.now();

        FeeSideResult sender = sideResult(
                "user-sender", UserType.CORPORATE, senderRuleId,
                Currency.USD, new BigDecimal("50.00"), new BigDecimal("50.00"),
                "NONE", false);

        FeeSideResult receiver = sideResult(
                "user-receiver", UserType.PERSONAL, receiverRuleId,
                Currency.IQD, new BigDecimal("5000"), new BigDecimal("4500"),
                "MAX_CAP_APPLIED", false);

        FeeCalculationResult result = new FeeCalculationResult(
                "tx-123",
                new BigDecimal("10000.00"),
                Currency.USD,
                Currency.IQD,
                TransactionType.WIRE_TRANSFER,
                sender,
                receiver,
                calcTime
        );

        FeeTransactionLogEntity entity = mapper.toEntity(result);

        // transaction context
        assertThat(entity.getTransactionId()).isEqualTo("tx-123");
        assertThat(entity.getTransactionAmount()).isEqualByComparingTo("10000.00");
        assertThat(entity.getSourceCurrency()).isEqualTo(Currency.USD);
        assertThat(entity.getDestinationCurrency()).isEqualTo(Currency.IQD);
        assertThat(entity.getTransactionType()).isEqualTo(TransactionType.WIRE_TRANSFER);
        assertThat(entity.getCalculatedAt()).isEqualTo(calcTime);

        // sender side
        assertThat(entity.getSenderUserId()).isEqualTo("user-sender");
        assertThat(entity.getSenderUserType()).isEqualTo(UserType.CORPORATE);
        assertThat(entity.getSenderMatchedRuleId()).isEqualTo(senderRuleId);
        assertThat(entity.getSenderRawFee()).isEqualByComparingTo("50.00");
        assertThat(entity.getSenderFinalFee()).isEqualByComparingTo("50.00");
        assertThat(entity.getSenderFeeCurrency()).isEqualTo(Currency.USD);
        assertThat(entity.getSenderCapApplied()).isEqualTo("NONE");
        assertThat(entity.isSenderWaived()).isFalse();

        // receiver side
        assertThat(entity.getReceiverUserId()).isEqualTo("user-receiver");
        assertThat(entity.getReceiverUserType()).isEqualTo(UserType.PERSONAL);
        assertThat(entity.getReceiverMatchedRuleId()).isEqualTo(receiverRuleId);
        assertThat(entity.getReceiverRawFee()).isEqualByComparingTo("5000");
        assertThat(entity.getReceiverFinalFee()).isEqualByComparingTo("4500");
        assertThat(entity.getReceiverFeeCurrency()).isEqualTo(Currency.IQD);
        assertThat(entity.getReceiverCapApplied()).isEqualTo("MAX_CAP_APPLIED");
        assertThat(entity.isReceiverWaived()).isFalse();
    }

    @Test
    void shouldMapWaivedReceiverWithNullMatchedRuleId() {
        FeeSideResult sender = sideResult(
                "user-sender", UserType.CORPORATE, UUID.randomUUID(),
                Currency.USD, new BigDecimal("10.00"), new BigDecimal("10.00"),
                "NONE", false);

        // receiver waived: no matched rule, fees are zero
        FeeSideResult waivedReceiver = sideResult(
                "user-receiver", UserType.PERSONAL, null,
                Currency.IQD, BigDecimal.ZERO, BigDecimal.ZERO,
                "NONE", true);

        FeeCalculationResult result = new FeeCalculationResult(
                "tx-456",
                new BigDecimal("100.00"),
                Currency.USD,
                Currency.IQD,
                TransactionType.INTERNAL_TRANSFER,
                sender,
                waivedReceiver,
                LocalDateTime.now()
        );

        FeeTransactionLogEntity entity = mapper.toEntity(result);

        assertThat(entity.isReceiverWaived()).isTrue();
        assertThat(entity.getReceiverMatchedRuleId()).isNull();
        assertThat(entity.getReceiverFinalFee()).isEqualByComparingTo("0");
        assertThat(entity.isSenderWaived()).isFalse();
        assertThat(entity.getSenderFinalFee()).isEqualByComparingTo("10.00");
    }
}