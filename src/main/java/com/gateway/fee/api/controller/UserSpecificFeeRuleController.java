package com.gateway.fee.api.controller;

import com.gateway.fee.api.dto.request.CreateFeeRuleRequest;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.service.FeeRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees/rules/users")
@RequiredArgsConstructor
public class UserSpecificFeeRuleController {

    private final FeeRuleService feeRuleService;

    //CREATE USER-SPECIFIC RULE
    @PostMapping("/{userId}")
    public ResponseEntity<FeeRuleResponse> createUserSpecificRule(@PathVariable String userId,
                                                                  @Valid @RequestBody CreateFeeRuleRequest request) {
        FeeRuleResponse response = feeRuleService.createUserSpecificRule(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ USER-SPECIFIC RULES
    @GetMapping("/{userId}")
    public ResponseEntity<List<FeeRuleResponse>> getUserSpecificRules(@PathVariable String userId) {
        List<FeeRuleResponse> responses = feeRuleService.getUserSpecificRules(userId);
        return ResponseEntity.ok(responses);
    }

    // READ all the rules that applies to this user
    @GetMapping("/{userId}/effective")
    public ResponseEntity<List<FeeRuleResponse>> getUserSpecificEffectiveRules(
            @PathVariable String userId,
            @RequestParam String userType) {
        List<FeeRuleResponse> responses = feeRuleService.getEffectiveFeeSchedule(userId, userType);
        return ResponseEntity.ok(responses);
    }

    // UPDATE USER-SPECIFIC RULE
    @PutMapping("/{userId}/{ruleId}")
    public ResponseEntity<FeeRuleResponse> updateUserSpecificRule(@PathVariable String userId,
                                                                  @PathVariable UUID ruleId,
                                                                  @Valid @RequestBody CreateFeeRuleRequest request) {
        FeeRuleResponse response = feeRuleService.updateUserSpecificRule(userId, ruleId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE USER-SPECIFIC RULE
    @DeleteMapping("/{userId}/{ruleId}")
    public ResponseEntity<Void> deleteUserSpecificRule(@PathVariable String userId,
                                                       @PathVariable UUID ruleId) {
        feeRuleService.deleteUserSpecificRule(userId, ruleId);
        return ResponseEntity.noContent().build();
    }
}