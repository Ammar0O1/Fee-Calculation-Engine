package com.gateway.fee.domain.registry;

import com.gateway.fee.domain.model.FeeRule;
import io.vavr.collection.List;

public interface FeeRuleRegistry {
     List<FeeRule> findAllActive();
     List<FeeRule> findByUserId(String userId);
     void add(FeeRule feeRule);
}
