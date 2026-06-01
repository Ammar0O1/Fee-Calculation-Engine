package com.gateway.fee.infrastructure.persistence;

import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FeeRuleRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeeRuleRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByActiveTrue_ReturnsOnlyActiveRules() {
        // Given
        createRule(UUID.randomUUID(), "user1", true);
        createRule(UUID.randomUUID(), "user2", false);

        // When
        List<FeeRuleEntity> activeRules = repository.findByActiveTrue();

        // Then
        assertThat(activeRules).hasSize(1);
        assertThat(activeRules.get(0).getUserId()).isEqualTo("user1");
        assertThat(activeRules.get(0).isActive()).isTrue();
    }

    @Test
    void findByUserIdAndActiveTrue_ReturnsOnlyThatUsersRules() {
        // Given
        createRule(UUID.randomUUID(), "user1", true);
        createRule(UUID.randomUUID(), "user2", true);

        // When
        List<FeeRuleEntity> userRules = repository.findByUserIdAndActiveTrue("user1");

        // Then
        assertThat(userRules).hasSize(1);
        assertThat(userRules.get(0).getUserId()).isEqualTo("user1");
    }

    @Test
    void findByUserTypeAndActiveTrue_ReturnsOnlyThatTypesRules() {
        // Given
        FeeRuleEntity rule1 = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user1")
                .active(true)
                .userType(UserType.PERSONAL)
                .effectiveDate(LocalDateTime.now())
                .build();
        entityManager.persist(rule1);

        FeeRuleEntity rule2 = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user2")
                .active(true)
                .userType(UserType.BUSINESS)
                .effectiveDate(LocalDateTime.now())
                .build();
        entityManager.persist(rule2);

        // When
        List<FeeRuleEntity> personalRules = repository.findByUserTypeAndActiveTrue(UserType.PERSONAL);

        // Then
        assertThat(personalRules).hasSize(1);
        assertThat(personalRules.get(0).getUserType()).isEqualTo(UserType.PERSONAL);
    }

    @Test
    void findByTransactionTypeAndActiveTrue_ReturnsOnlyThatTransactionTypeRules() {
        // Given
        FeeRuleEntity rule1 = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user1")
                .active(true)
                .transactionType(TransactionType.PAYMENT)
                .effectiveDate(LocalDateTime.now())
                .build();
        entityManager.persist(rule1);

        FeeRuleEntity rule2 = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user2")
                .active(true)
                .transactionType(TransactionType.INTERNAL_TRANSFER)
                .effectiveDate(LocalDateTime.now())
                .build();
        entityManager.persist(rule2);

        // When
        List<FeeRuleEntity> paymentRules = repository.findByTransactionTypeAndActiveTrue(TransactionType.PAYMENT);

        // Then
        assertThat(paymentRules).hasSize(1);
        assertThat(paymentRules.get(0).getTransactionType()).isEqualTo(TransactionType.PAYMENT);
    }

    @Test
    void findByDimensions_ExactMatch_ReturnsTheRule() {
        // Given
        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user1")
                .userType(UserType.PERSONAL)
                .transactionType(TransactionType.PAYMENT)
                .active(true)
                .effectiveDate(LocalDateTime.now())
                .build();
        entityManager.persist(rule);
        entityManager.flush();

        // When
        List<FeeRuleEntity> result = repository.findByDimensions(
                "user1", UserType.PERSONAL, TransactionType.PAYMENT, null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user1");
    }

    @Test
    void findByDimensions_WithNullUserId_MatchesRuleWhereUserIdIsNull() {
        // Given
        createRule(UUID.randomUUID(), null, true);

        // When
        List<FeeRuleEntity> result = repository.findByDimensions(
                null, null, null, null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isNull();
    }

    @Test
    void uniqueConstraintViolation_ThrowsOnDuplicateActiveRule() {
        // Given
        FeeRuleEntity rule1 = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user1")
                .userType(UserType.PERSONAL)
                .transactionType(TransactionType.PAYMENT)
                .sourceCurrency(com.gateway.fee.domain.model.Currency.USD)
                .destinationCurrency(com.gateway.fee.domain.model.Currency.EUR)
                .active(true)
                .effectiveDate(LocalDateTime.now())
                .build();
        entityManager.persist(rule1);
        entityManager.flush();

        // When & Then
        FeeRuleEntity duplicateRule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId("user1")
                .userType(UserType.PERSONAL)
                .transactionType(TransactionType.PAYMENT)
                .sourceCurrency(com.gateway.fee.domain.model.Currency.USD)
                .destinationCurrency(com.gateway.fee.domain.model.Currency.EUR)
                .active(true)
                .effectiveDate(LocalDateTime.now())
                .build();
        
        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(duplicateRule);
            entityManager.flush();
        });
    }

    private FeeRuleEntity createRule(UUID id, String userId, boolean active) {
        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(id)
                .userId(userId)
                .active(active)
                .effectiveDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(rule);
        return rule;
    }
}
