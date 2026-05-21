package com.gateway.fee.application;

import com.gateway.fee.domain.model.FeeCalculationResult;
import com.gateway.fee.infrastructure.persistence.FeeTransactionLogRepository;
import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import com.gateway.fee.infrastructure.persistence.mapper.FeeTransactionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeeTransactionLogService {
    private final FeeTransactionLogMapper logMapper;
    private final FeeTransactionLogRepository logRepository;


    public void log(FeeCalculationResult result) {

        // convert result to entity using mapper
        FeeTransactionLogEntity logEntity = logMapper.toEntity(result);

        //saving entity to db
        logRepository.save(logEntity);
    }
}
