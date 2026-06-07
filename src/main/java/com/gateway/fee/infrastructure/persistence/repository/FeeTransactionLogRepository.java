<<<<<<<< HEAD:src/main/java/com/gateway/fee/infrastructure/persistence/FeeTransactionLogRepository.java
package com.gateway.fee.infrastructure.persistence;
========
package com.gateway.fee.infrastructure.persistence.repository;
>>>>>>>> aecf952 (add DatabaseIntegrationTest extending AbstractPostgresIntegrationTest, organize repository folder):src/main/java/com/gateway/fee/infrastructure/persistence/repository/FeeTransactionLogRepository.java
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FeeTransactionLogRepository extends JpaRepository<FeeTransactionLogEntity, UUID> {

    List<FeeTransactionLogEntity> findByTransactionId(String transactionId);
    List<FeeTransactionLogEntity> findBySenderUserIdAndCalculatedAtBetween(String senderUserId, LocalDateTime from, LocalDateTime to);
    List<FeeTransactionLogEntity> findByTransactionTypeAndCalculatedAtBetween(TransactionType transactionType, LocalDateTime from, LocalDateTime to);
}
