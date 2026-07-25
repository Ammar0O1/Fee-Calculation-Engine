package com.gateway.fee.infrastructure.storage;

import com.gateway.fee.domain.model.FeeRule;
import com.gateway.fee.domain.registry.FeeRuleRegistry;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.mapper.FeeRuleMapper;
import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component

public class DatabaseFeeRuleRegistry implements FeeRuleRegistry {
    private final FeeRuleRepository repository;
    private final FeeRuleMapper mapper;

    @Override
    public List<FeeRule> findAllActive() {
        // Implementation to fetch active fee rules from the database
        return List.ofAll(repository.findByActiveTrue())
                .map(mapper::toDomain);
    }

    @Override
    public List<FeeRule> findByUserId(String userId) {
        // Implementation to fetch fee rules by user ID from the database
        return List.ofAll(repository.findByUserIdAndActiveTrue(userId))
                .map(mapper::toDomain);
    }

    @Override
    public void add(FeeRule feeRule) {
        FeeRuleEntity entity = mapper.toEntity(feeRule);
        repository.save(entity);
        }

}