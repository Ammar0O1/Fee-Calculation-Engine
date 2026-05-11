package com.gateway.fee.infrastructure.persistence;
import com.gateway.fee.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FeeTransactionLogRepository extends JpaRepository<FeeTransactionLogEntity, UUID> {

    List<FeeTransactionLogEntity> findByTransactionId(String transactionId);
    List<FeeTransactionLogEntity> findBySenderUserIdAndCalculatedAtBetween(String senderUserId, LocalDateTime from, LocalDateTime to);
    List<FeeTransactionLogEntity> findByTransactionTypeAndCalculatedAtBetween(TransactionType transactionType, LocalDateTime from, LocalDateTime to);
}
