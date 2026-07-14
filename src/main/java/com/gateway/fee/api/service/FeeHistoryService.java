package com.gateway.fee.api.service;

import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.gateway.fee.api.dto.response.FeeHistoryResponse;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gateway.fee.api.dto.response.FeeSummaryResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeeHistoryService {
    private final FeeTransactionLogRepository repository;

    //Page is a chunk of query results plus info about the whole result set
    //which page, how many total how many pages.
    //We use it so we don't return thousands of records at once and
    //the database gives us 20 at a time, and the frontend gets navigation info with it
    public Page<FeeHistoryResponse> getHistory(
            String transactionId,
            String userId,
            TransactionType transactionType,
            Currency sourceCurrency,
            Currency destinationCurrency,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable) {

        Page<FeeTransactionLogEntity> history = repository.findHistory(transactionId, userId, transactionType, sourceCurrency, destinationCurrency, dateFrom, dateTo, pageable);

        return history.map(this::toResponse);
    }

    // private helper: converts ONE entity into ONE FeeHistoryResponse
    private FeeHistoryResponse toResponse(FeeTransactionLogEntity entity) {
        return new FeeHistoryResponse(
                entity.getTransactionId(),
                entity.getTransactionAmount(),
                entity.getSourceCurrency(),
                entity.getDestinationCurrency(),
                entity.getTransactionType(),
                entity.getCalculatedAt(),
                entity.getSenderUserId(),
                entity.getReceiverUserId(),
                entity.getSenderFeeCurrency(),
                entity.isSenderWaived(),
                entity.getSenderFinalFee(),
                entity.getReceiverFinalFee(),
                entity.getReceiverFeeCurrency(),
                entity.isReceiverWaived()
        );
    }

    public FeeSummaryResponse getSummary(LocalDateTime dateFrom, LocalDateTime dateTo){
        //calling all 6 queries
        List<Object[]> transTypeRows = repository.sumFeesByTransactionType(dateFrom, dateTo);
        List<Object[]> senderTypeRows = repository.sumSenderFeesByUserType(dateFrom, dateTo);
        List<Object[]> receiverTypeRows = repository.sumReceiverFeesByUserType(dateFrom, dateTo);
        List<Object[]> senderCurrRows = repository.sumSenderFeesByCurrency(dateFrom, dateTo);
        List<Object[]> receiverCurrRows = repository.sumReceiverFeesByCurrency(dateFrom, dateTo);
        List<Object[]> avgRows = repository.avgFeesByTransactionType(dateFrom, dateTo);

        //convert to map
        Map<TransactionType, BigDecimal> totalsByType = toMap(transTypeRows);
        Map<UserType, BigDecimal> senderByUserType = toMap(senderTypeRows);
        Map<UserType, BigDecimal> receiverByUserType = toMap(receiverTypeRows);
        Map<Currency, BigDecimal> senderByCurrency = toMap(senderCurrRows);
        Map<Currency, BigDecimal> receiverByCurrency  = toMap(receiverCurrRows);
        Map<TransactionType, BigDecimal> avgByType = toMap(avgRows);

        //merge the ones that has sides (UserType,Currency)
        Map<UserType,BigDecimal> totalsByUserType =mergeMaps(senderByUserType, receiverByUserType);
        Map<Currency,BigDecimal> totalsByCurrency = mergeMaps(senderByCurrency, receiverByCurrency);

        return new FeeSummaryResponse(totalsByType,totalsByUserType,totalsByCurrency,avgByType);

    }

    //private helper using generics <K>
    //works for TransactionType, UserType, or Currency keys
    private <K> Map<K, BigDecimal> toMap(List<Object[]> rows) {
        Map<K, BigDecimal> map = new HashMap<>();

        for (Object[] row : rows) {
            K key = (K) row[0];
            BigDecimal value = (BigDecimal) row[1];
            map.put(key, value);
        }

        return map;
    }

    // another private helper for merging 2 maps and
    // adding values that have the same key
    private <K> Map<K,BigDecimal> mergeMaps(Map<K, BigDecimal> map1, Map<K, BigDecimal> map2)
    {
        Map<K, BigDecimal> result = new HashMap<>(map1);

        for (Map.Entry<K, BigDecimal> entry : map2.entrySet()) { //entrySet() :gives you the set of entries in this map.
            K key = entry.getKey();
            BigDecimal value = entry.getValue();
            if (result.containsKey(key)) {
                result.put(key, result.get(key).add(value));
            }else{
                result.put(key, value);
            }
    }
        return result;
    }

}
