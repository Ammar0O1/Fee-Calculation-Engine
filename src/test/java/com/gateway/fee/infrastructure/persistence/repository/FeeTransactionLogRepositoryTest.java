package com.gateway.fee.infrastructure.persistence.repository;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.AbstractPostgresIntegrationTest;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FeeTransactionLogRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeeTransactionLogRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void saveAndRetrieveByTransactionId() {
        // Given
        FeeTransactionLogEntity log = createLog("tx-1", LocalDateTime.now());
        entityManager.persist(log);
        entityManager.flush();

        // When
        List<FeeTransactionLogEntity> result = repository.findByTransactionId("tx-1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionId()).isEqualTo("tx-1");
    }

    @Test
    void queryByDateRange_ReturnsOnlyLogsWithinRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FeeTransactionLogEntity log1 = createLog("tx-1", now.minusDays(2));
        FeeTransactionLogEntity log2 = createLog("tx-2", now);
        FeeTransactionLogEntity log3 = createLog("tx-3", now.plusDays(2));

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.flush();

        // When
        List<FeeTransactionLogEntity> result = repository.findBySenderUserIdAndCalculatedAtBetween(
                "sender-1", now.minusDays(1), now.plusDays(1));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionId()).isEqualTo("tx-2");
    }

    private FeeTransactionLogEntity createLog(String transactionId, LocalDateTime calculatedAt) {
        return FeeTransactionLogEntity.builder()
                .transactionId(transactionId)
                .transactionAmount(new BigDecimal("100.00"))
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.USD)
                .transactionType(TransactionType.PAYMENT)
                .senderUserId("sender-1")
                .senderUserType(UserType.PERSONAL)
                .receiverUserId("receiver-1")
                .receiverUserType(UserType.BUSINESS)
                .calculatedAt(calculatedAt)
                .senderWaived(false)
                .receiverWaived(false)
                .build();
    }
}