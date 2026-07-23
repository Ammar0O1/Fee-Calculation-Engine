package com.gateway.fee.infrastructure.storage;

import com.gateway.fee.domain.model.*;

import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.mapper.FeeRuleMapper;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseFeeRuleRegistryTest {

    @Mock
    private FeeRuleRepository repository;
    @Mock
    private FeeRuleMapper mapper;

    @InjectMocks
    private DatabaseFeeRuleRegistry registry;

    @Test
    public void shouldReturnAllActiveFeeRulesAsDomainObjects() {
        FeeRuleEntity entity = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .active(true)
                .effectiveDate(LocalDateTime.now())
                .build();

        FeeSideDefinition flatFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeRule domainRule = new FeeRule(UUID.randomUUID(), null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Test rule");

        when(repository.findByActiveTrue()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainRule);

        io.vavr.collection.List<FeeRule> result = registry.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.head()).isEqualTo(domainRule);
    }

    @Test
    public void shouldReturnFeeRulesByUserId() {
        String userId = "user-123";

        FeeRuleEntity entity = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId(userId)
                .active(true)
                .effectiveDate(LocalDateTime.now())
                .build();

        FeeSideDefinition flatFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeRule domainRule = new FeeRule(UUID.randomUUID(), userId, UserType.CORPORATE, null, null, null, flatFee, null, LocalDateTime.now(), true, "User specific rule");

        when(repository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainRule);

        io.vavr.collection.List<FeeRule> result = registry.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.head().getUserId()).isEqualTo(userId);
        verify(repository).findByUserIdAndActiveTrue(userId);
    }

    @Test
    public void shouldConvertAndSaveFeeRule() {
        FeeSideDefinition flatFee = new FeeSideDefinition(CalculationMode.FLAT, new BigDecimal("5"), null, null, null, null);
        FeeRule domainRule = new FeeRule(UUID.randomUUID(), null, null, null, null, null, flatFee, null, LocalDateTime.now(), true, "Test rule");

        FeeRuleEntity entity = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .active(true)
                .effectiveDate(LocalDateTime.now())
                .build();

        when(mapper.toEntity(domainRule)).thenReturn(entity);

        registry.add(domainRule);

        verify(repository).save(entity);
    }
}