package com.gateway.fee.infrastructure.storage;

import com.gateway.fee.domain.exception.DuplicateRuleException;
import com.gateway.fee.domain.model.FeeRule;
import com.gateway.fee.domain.registry.FeeRuleRegistry;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import java.util.UUID;

public class InMemoryFeeRuleRegistry implements FeeRuleRegistry {
    private Map<UUID,FeeRule> rules = HashMap.empty();


    @Override
    public List<FeeRule> findAllActive() {
        return List.ofAll(rules.values()
                .filter(FeeRule::isActive)
        );
    }

    @Override
    public List<FeeRule> findByUserId(String userId) {
        return List.ofAll(rules.values()
                .filter(rule -> rule.getUserId().equals(userId))
        );
    }
    @Override
    public void add(FeeRule feeRule) {
        if (rules.containsKey(feeRule.getRuleId())) {
            throw new DuplicateRuleException("duplicate rule id");
        }
         rules = rules.put(feeRule.getRuleId(), feeRule);
    }
}
