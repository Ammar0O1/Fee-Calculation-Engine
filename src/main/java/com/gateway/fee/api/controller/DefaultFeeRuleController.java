package com.gateway.fee.api.controller;

import com.gateway.fee.api.dto.request.CreateDefaultRuleRequest;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees/rules/defaults")
@RequiredArgsConstructor
public class DefaultFeeRuleController {

    private final FeeRuleService feeRuleService;

    //CREATE DEFAULT RULE
    @PostMapping
    public ResponseEntity<FeeRuleResponse> createDefaultRule(@Valid @RequestBody CreateDefaultRuleRequest request) {
        FeeRuleResponse response = feeRuleService.createDefaultRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // Long form of status code 201
    }

    //READ DEFAULT RULES
    @GetMapping
    public ResponseEntity<List<FeeRuleResponse>> getDefaultRules() {
        List<FeeRuleResponse> responses = feeRuleService.getDefaultRules();
        return ResponseEntity.ok(responses); // shortcut for status code 200 (OK)
    }

    //UPDATE DEFAULT RULE
    @PutMapping("/{ruleId}")
    public ResponseEntity<FeeRuleResponse> updateDefaultRule(@PathVariable UUID ruleId,
                                                             @Valid @RequestBody CreateDefaultRuleRequest request) {
        FeeRuleResponse response = feeRuleService.updateDefaultRule(ruleId, request);
        return ResponseEntity.ok(response); // shortcut for status code 200 (Updated)
    }

    //DELETE DEFAULT RULE
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteDefaultRule(@PathVariable UUID ruleId) {
        feeRuleService.deleteDefaultRule(ruleId);
        return ResponseEntity.noContent().build(); // shortcut for status code 204 (No Content)
    }
}