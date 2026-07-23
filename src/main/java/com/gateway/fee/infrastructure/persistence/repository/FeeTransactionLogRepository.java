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
            + " AND (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom)"
            + " AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo)")

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

    @Query("SELECT log.transactionType, SUM(COALESCE(log.senderFinalFee, 0) + COALESCE(log.receiverFinalFee, 0)) "
            + "FROM FeeTransactionLogEntity log "
            + "WHERE (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom) "
            + "AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo) "
            + "GROUP BY log.transactionType")
    List<Object[]> sumFeesByTransactionType(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT log.senderUserType, SUM(COALESCE(log.senderFinalFee, 0) ) "
            + "FROM FeeTransactionLogEntity log "
            + "WHERE (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom) "
            + "AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo) "
            + "GROUP BY log.senderUserType")
    List<Object[]> sumSenderFeesByUserType(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT log.receiverUserType, SUM(COALESCE(log.receiverFinalFee, 0) ) "
            + "FROM FeeTransactionLogEntity log "
            + "WHERE (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom) "
            + "AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo) "
            + "GROUP BY log.receiverUserType")
    List<Object[]> sumReceiverFeesByUserType(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT log.senderFeeCurrency, SUM(COALESCE(log.senderFinalFee, 0) ) "
            + "FROM FeeTransactionLogEntity log "
            + "WHERE (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom) "
            + "AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo) "
            + "GROUP BY log.senderFeeCurrency")
    List<Object[]> sumSenderFeesByCurrency(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT log.receiverFeeCurrency, SUM(COALESCE(log.receiverFinalFee, 0) ) "
            + "FROM FeeTransactionLogEntity log "
            + "WHERE (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom) "
            + "AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo) "
            + "GROUP BY log.receiverFeeCurrency")
    List<Object[]> sumReceiverFeesByCurrency(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT log.transactionType, AVG(COALESCE(log.senderFinalFee, 0) + COALESCE(log.receiverFinalFee, 0)) "
            + "FROM FeeTransactionLogEntity log "
            + "WHERE (CAST(:dateFrom AS timestamp) IS NULL OR log.calculatedAt >= :dateFrom) "
            + "AND (CAST(:dateTo AS timestamp ) IS NULL OR log.calculatedAt <= :dateTo) "
            + "GROUP BY log.transactionType")
    List<Object[]> avgFeesByTransactionType (
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );
}

