package com.gateway.fee.infrastructure.persistence.mapper;

import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import com.gateway.fee.infrastructure.persistence.entity.TierBracketEntity;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ON DELETE CASCADE behavior on fee_rule.
 *
 * Unlike FeeRuleMapperTest (which is pure object conversion), this test
 * persists to the real database and verifies that deleting a fee_rule row
 * also deletes its fee_side_definition and tier_bracket children, as
 * configured by the FK constraints in the Flyway migrations.
 */
class FeeRuleCascadeDeleteIT extends AbstractPostgresIntegrationTest {

    private final FeeRuleMapper mapper = new FeeRuleMapper();

    @Autowired
    private TestEntityManager em;

    private List<TierBracket> threeBrackets() {
        return List.of(
                new TierBracket(new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0.02")),
                new TierBracket(new BigDecimal("1000"), new BigDecimal("5000"), new BigDecimal("0.015")),
                new TierBracket(new BigDecimal("5000"), null, new BigDecimal("0.01"))
        );
    }

    @Test
    void shouldCascadeDeleteSideDefinitionsAndTierBracketsWhenRuleDeleted() {
        // build a rule with sender (3 tier brackets) + receiver (flat)
        FeeSideDefinition senderFee = new FeeSideDefinition(
                CalculationMode.TIERED_FLAT, null, null,
                threeBrackets(), null, null);
        FeeSideDefinition receiverFee = new FeeSideDefinition(
                CalculationMode.FLAT, new BigDecimal("2.0000"),
                null, null, null, null);

        FeeRule domain = new FeeRule(
                UUID.randomUUID(), "user-cascade", UserType.CORPORATE,
                TransactionType.WIRE_TRANSFER, Currency.USD, Currency.IQD,
                senderFee, receiverFee, LocalDateTime.now(), true, "cascade test");

        // map + persist + flush -> now in DB
        FeeRuleEntity entity = mapper.toEntity(domain);
        em.persist(entity);
        em.flush();

        // capture child IDs so we can look them up after delete
        UUID ruleId = entity.getRuleId();
        java.util.List<UUID> sideDefIds = new ArrayList<>();
        java.util.List<UUID> bracketIds = new ArrayList<>();
        for (FeeSideDefinitionEntity sd : entity.getSideDefinitions()) {
            sideDefIds.add(sd.getDefinitionId());
            for (TierBracketEntity tb : sd.getTierBrackets()) {
                bracketIds.add(tb.getBracketId());
            }
        }

        // sanity check: children were actually saved
        assertThat(sideDefIds).hasSize(2);   // sender + receiver
        assertThat(bracketIds).hasSize(3);   // 3 sender brackets

        // clear persistence context so next reads come from DB, not cache
        em.clear();

        // delete the parent, flush so CASCADE actually executes in DB
        FeeRuleEntity toDelete = em.find(FeeRuleEntity.class, ruleId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // verify cascade: parent gone, all children gone
        assertThat(em.find(FeeRuleEntity.class, ruleId)).isNull();
        for (UUID id : sideDefIds) {
            assertThat(em.find(FeeSideDefinitionEntity.class, id)).isNull();
        }
        for (UUID id : bracketIds) {
            assertThat(em.find(TierBracketEntity.class, id)).isNull();
        }
    }
}