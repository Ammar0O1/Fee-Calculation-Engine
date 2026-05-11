package com.gateway.fee.infrastructure.persistence;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface FeeRuleRepository extends JpaRepository<FeeRuleEntity, UUID> {
    List<FeeRuleEntity> findByActiveTrue();
    List<FeeRuleEntity> findByUserIdAndActiveTrue(String userId);
    List<FeeRuleEntity> findByUserTypeAndActiveTrue(UserType userType);
    List<FeeRuleEntity> findByTransactionTypeAndActiveTrue(TransactionType transactionType);

    @Query("SELECT f FROM FeeRuleEntity f WHERE " +
            "((:userId IS NULL AND f.userId IS NULL)OR f.userId=:userId) " +
            "AND ((:userType IS NULL AND  f.userType IS NULL) OR f.userType= :userType) " +
            "AND ((:transactionType IS NULL AND f.transactionType IS NULL) OR f.transactionType= :transactionType) " +
            "AND ((:sourceCurrency IS NULL AND f.sourceCurrency IS NULL) OR f.sourceCurrency= :sourceCurrency) " +
            "AND ((:destinationCurrency IS NULL AND f.destinationCurrency IS NULL ) OR f.destinationCurrency= :destinationCurrency) " +
            "AND f.active=true")

    List<FeeRuleEntity> findByDimensions(
            @Param("userId") String userId,
            @Param("userType") UserType userType,
            @Param("transactionType") TransactionType transactionType,
            @Param("sourceCurrency") Currency sourceCurrency,
            @Param("destinationCurrency") Currency destinationCurrency
    );
}
