package com.gateway.fee.infrastructure.storage;

import com.gateway.fee.domain.exception.DuplicateRuleException;
import com.gateway.fee.domain.model.*;
import io.vavr.collection.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeeRuleRegistryTest {

    private InMemoryFeeRuleRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryFeeRuleRegistry();
    }

    private FeeRule createRule(UUID id, boolean active) {
        return createRule(id, active, "user-1");
    }

    private FeeRule createRule(UUID id, boolean active, String userId) {
        FeeSideDefinition fee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        return new FeeRule(id, userId, UserType.PERSONAL, TransactionType.PAYMENT, Currency.USD, Currency.EUR, fee, null, LocalDateTime.now(), active, "desc");
    }

    @Test
    void shouldFindAllActiveRules() {
        FeeRule activeRule = createRule(UUID.randomUUID(), true);
        FeeRule inactiveRule = createRule(UUID.randomUUID(), false);

        registry.add(activeRule);
        registry.add(inactiveRule);

        List<FeeRule> activeRules = registry.findAllActive();
        assertEquals(1, activeRules.size());
        assertTrue(activeRules.contains(activeRule));
    }

    @Test
    void shouldNotFindInactiveRulesInActiveList() {
        FeeRule inactiveRule = createRule(UUID.randomUUID(), false);
        registry.add(inactiveRule);
        
        List<FeeRule> activeRules = registry.findAllActive();
        assertTrue(activeRules.isEmpty());
    }

    @Test
    void shouldThrowOnDuplicateRuleId() {
        UUID ruleId = UUID.randomUUID();
        FeeRule rule1 = createRule(ruleId, true);
        FeeRule rule2 = createRule(ruleId, true);
        
        registry.add(rule1);
        assertThrows(DuplicateRuleException.class, () -> registry.add(rule2));
    }

    @Test
    void shouldFindByUserId() {
        FeeRule user1Rule = createRule(UUID.randomUUID(), true, "user-1");
        FeeRule user2Rule = createRule(UUID.randomUUID(), true, "user-2");
        
        registry.add(user1Rule);
        registry.add(user2Rule);
        
        List<FeeRule> user1Rules = registry.findByUserId("user-1");
        assertEquals(1, user1Rules.size());
        assertEquals("user-1", user1Rules.get(0).getUserId());
    }

    @Test
    void shouldReturnEmptyListWhenNoUserIdMatch() {
        FeeRule user1Rule = createRule(UUID.randomUUID(), true, "user-1");
        registry.add(user1Rule);
        
        List<FeeRule> results = registry.findByUserId("user-non-existent");
        assertTrue(results.isEmpty());
    }
}
