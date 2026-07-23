package com.gateway.fee.api.controller;


import com.gateway.fee.api.dto.request.FeeCalculationRequest;
import com.gateway.fee.api.dto.request.FeeEstimateRequest;
import com.gateway.fee.api.dto.response.EffectiveRuleResponse;
import com.gateway.fee.api.dto.response.FeeEstimateResponse;
import com.gateway.fee.api.dto.response.FeeScheduleEntryResponse;
import com.gateway.fee.api.service.FeeCalculationService;
import com.gateway.fee.api.service.FeeRuleQueryService;
import com.gateway.fee.domain.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gateway.fee.api.dto.response.FeeCalculationResponse;

import java.util.List;
//feat: add FeeController exposing calculation-facing REST endpoints
//
//Wires the four calculation-facing endpoints to their services:
//- POST /api/fees/calculate    — real transaction, calculates + logs, returns breakdown
//- POST /api/fees/estimate     — preview (never logged), dual-mode calc or rule details
//- GET  /api/fees/schedule     — full fee table with optional dimension filters
//- GET  /api/fees/rules/users/{userId}/effective — three-tier CUSTOM/INHERITED/DEFAULT rules
//
//Controller stays thin: validates input, delegates to FeeCalculationService and
//FeeRuleQueryService, wraps results in ResponseEntity. No business logic.
@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeCalculationService feeCalculationService;
    private final FeeRuleQueryService feeRuleQueryService;

    @PostMapping("/calculate")
    public ResponseEntity<FeeCalculationResponse> calculate(@Valid @RequestBody FeeCalculationRequest request) {
        return ResponseEntity.ok(feeCalculationService.calculate(request));
    }
    @PostMapping("/estimate")
    public ResponseEntity<FeeEstimateResponse> estimate(@Valid @RequestBody FeeEstimateRequest request) {
        return ResponseEntity.ok(feeCalculationService.estimate(request));
    }
    @GetMapping("/schedule")
    public ResponseEntity<List<FeeScheduleEntryResponse>> schedule(
            @RequestParam(required = false) UserType userType,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) Currency sourceCurrency,
            @RequestParam(required = false) Currency destinationCurrency) {

        return ResponseEntity.ok(
                feeRuleQueryService.schedule(userType, transactionType, sourceCurrency, destinationCurrency));
    }
    @GetMapping("/rules/users/{userId}/effective")
    public ResponseEntity<List<EffectiveRuleResponse>> effective(
            @PathVariable String userId,
            @RequestParam UserType userType) {

        return ResponseEntity.ok(feeRuleQueryService.effective(userId, userType));
    }

}