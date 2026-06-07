package com.gateway.fee.application;

import com.gateway.fee.domain.model.*;
import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import com.gateway.fee.infrastructure.persistence.mapper.FeeTransactionLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeeTransactionLogServiceTest {
    @Mock
    private FeeTransactionLogRepository logRepository;
    @Mock
    private FeeTransactionLogMapper logMapper;

    @InjectMocks
    private FeeTransactionLogService feeTransactionLogService;

    @Test
    public void shouldConvertAndSaveTransactionLog() {
        FeeSideResult senderResult = new FeeSideResult(
                "sender-1", UserType.CORPORATE, UUID.randomUUID(),
                "SENDER", Currency.USD, new BigDecimal("5"), new BigDecimal("5"),
                "NONE", BigDecimal.ZERO, new BigDecimal("5"), null, false
        );

        FeeSideResult receiverResult = new FeeSideResult(
                "receiver-1", UserType.PERSONAL, UUID.randomUUID(),
                "RECEIVER", Currency.IQD, new BigDecimal("3"), new BigDecimal("3"),
                "NONE", BigDecimal.ZERO, new BigDecimal("3"), null, false
        );

        FeeCalculationResult result = new FeeCalculationResult(
                "tx-1", new BigDecimal("1000"), Currency.USD, Currency.IQD,
                TransactionType.WIRE_TRANSFER, senderResult, receiverResult,
                LocalDateTime.now()
        );


        FeeTransactionLogEntity entity = FeeTransactionLogEntity.builder()
                .logId(UUID.randomUUID())
                .transactionId("tx-1")
                .build();

        when(logMapper.toEntity(result)).thenReturn(entity);


        feeTransactionLogService.log(result);


        // verify the repository's save was called with the entity
        verify(logRepository).save(entity);
    }
}
