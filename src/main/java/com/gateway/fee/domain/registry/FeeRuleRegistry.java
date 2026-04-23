package com.gateway.fee.domain.registry;

import com.gateway.fee.domain.model.FeeRule;
import io.vavr.collection.List;
//InMemoryFeeRuleRegistry Uses this interface to store and retrieve fee rules.
public interface FeeRuleRegistry {
     List<FeeRule> findAllActive();
     List<FeeRule> findByUserId(String userId);
     void add(FeeRule feeRule);
}
