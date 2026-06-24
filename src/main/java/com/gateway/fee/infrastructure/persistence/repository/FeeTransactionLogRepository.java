package com.gateway.fee.infrastructure.persistence.repository;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gateway.fee.domain.model.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FeeTransactionLogRepository extends JpaRepository<FeeTransactionLogEntity, UUID> {

    List<FeeTransactionLogEntity> findByTransactionId(String transactionId);
    List<FeeTransactionLogEntity> findBySenderUserIdAndCalculatedAtBetween(String senderUserId, LocalDateTime from, LocalDateTime to);
    List<FeeTransactionLogEntity> findByTransactionTypeAndCalculatedAtBetween(TransactionType transactionType, LocalDateTime from, LocalDateTime to);

    @Query("SELECT log FROM FeeTransactionLogEntity log WHERE "
            + "(:transactionId IS NULL OR log.transactionId = :transactionId) "
            + " AND (:userId IS NULL OR log.senderUserId = :userId OR log.receiverUserId = :userId) "
            + " AND (:transactionType IS NULL OR log.transactionType = :transactionType)"
            + " AND (:sourceCurrency IS NULL OR log.sourceCurrency = :sourceCurrency)"
            + " AND (:destinationCurrency IS NULL OR log.destinationCurrency = :destinationCurrency)"
            + " AND (:dateFrom IS NULL OR log.calculatedAt >= :dateFrom)"
            + " AND (:dateTo IS NULL OR log.calculatedAt <= :dateTo)")

    Page<FeeTransactionLogEntity> findHistory(
            @Param("transactionId") String transactionId,
            @Param("userId") String userId,
            @Param("transactionType") TransactionType transactionType,
            @Param("sourceCurrency") Currency sourceCurrency,
            @Param("destinationCurrency") Currency destinationCurrency,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}


