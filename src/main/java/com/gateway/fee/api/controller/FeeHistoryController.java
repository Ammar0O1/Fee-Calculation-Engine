package com.gateway.fee.api.controller;

import com.gateway.fee.api.dto.response.FeeHistoryResponse;
import com.gateway.fee.api.dto.response.FeeSummaryResponse;
import com.gateway.fee.api.service.FeeHistoryService;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeHistoryController {

    private final FeeHistoryService feeHistoryService;

    @GetMapping("/history")
    public Page<FeeHistoryResponse> getHistory(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) Currency sourceCurrency,
            @RequestParam(required = false) Currency destinationCurrency,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            Pageable pageable) {

        return feeHistoryService.getHistory(transactionId, userId, transactionType, sourceCurrency, destinationCurrency, dateFrom, dateTo, pageable );
    }
    @GetMapping("/history/summary")
    public FeeSummaryResponse getHistorySummary(
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo

    ){
        return feeHistoryService.getSummary(dateFrom,dateTo);
    }
}
