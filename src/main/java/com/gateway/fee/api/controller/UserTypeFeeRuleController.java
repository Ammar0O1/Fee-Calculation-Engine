package com.gateway.fee.api.controller;

import com.gateway.fee.api.dto.request.CreateFeeRuleRequest;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees/rules/user-types")
@RequiredArgsConstructor
public class UserTypeFeeRuleController {

    private final FeeRuleService feeRuleService;

    //CREATE USER-TYPE RULES
    @PostMapping
    public ResponseEntity<FeeRuleResponse> createUserTypeRule(@Valid @RequestBody CreateFeeRuleRequest request) {
        FeeRuleResponse response = feeRuleService.createUserTypeRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ USER-TYPE RULES
    @GetMapping
    public ResponseEntity<List<FeeRuleResponse>> getUserTypeRules() {
        List<FeeRuleResponse> responses = feeRuleService.getUserTypeRules();
        return ResponseEntity.ok(responses);
    }

    //READ RULES FOR SPECIFIC USER TYPE for example /api/fees/rules/user-types/BUSINESS
    @GetMapping("/{userType}")
    public ResponseEntity<List<FeeRuleResponse>> getUserTypeRulesByType(@PathVariable String userType) {
        List<FeeRuleResponse> responses = feeRuleService.getUserTypeRulesByType(userType);
        return ResponseEntity.ok(responses);
    }

    //UPDATE USER-TYPE RULES
    @PutMapping("/{ruleId}")
    public ResponseEntity<FeeRuleResponse> updateUserTypeRules(@PathVariable UUID ruleId, @Valid @RequestBody CreateFeeRuleRequest request) {
        FeeRuleResponse response = feeRuleService.updateUserTypeRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    //DELETE USER-TYPE RULES
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteUserTypeRule(@PathVariable UUID ruleId) {
        feeRuleService.deleteUserTypeRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}
