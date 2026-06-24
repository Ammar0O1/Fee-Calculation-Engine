package com.gateway.fee.api.service;

import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeeHistoryService {
    private final FeeTransactionLogRepository repository;

}
