package com.gateway.fee.acceptance;

import com.gateway.fee.api.AbstractApiIntegrationTest;
import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResolutionScenarioTest extends AbstractApiIntegrationTest {

    // Helper to create rules
    private FeeRuleEntity createRule(String description, String userId, UserType userType, TransactionType transactionType, Currency sourceCurrency, Currency destinationCurrency) {
        return FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .effectiveDate(LocalDateTime.now())
                .active(true)
                .description(description)
                .userId(userId)
                .userType(userType)
                .transactionType(transactionType)
                .sourceCurrency(sourceCurrency)
                .destinationCurrency(destinationCurrency)
                .sideDefinitions(new ArrayList<>())
                .build();
    }

    private void addSide(FeeRuleEntity rule, String side, CalculationMode mode, BigDecimal amount) {
        addSide(rule, side, mode, amount, null);
    }

    private void addSide(FeeRuleEntity rule, String side, CalculationMode mode, BigDecimal amount, BigDecimal percentage) {
        FeeSideDefinitionEntity definition = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side(side)
                .calculationMode(mode)
                .flatAmount(amount)
                .percentage(percentage)
                .build();
        rule.getSideDefinitions().add(definition);
    }
    // proves exact currency pair outscores wildcard in resolver scoring
    @Test
    public void scenario9() throws Exception {
        FeeRuleEntity exactRule = createRule("Exact USD->IQD", null, null, null, Currency.USD, Currency.IQD);
        addSide(exactRule, "SENDER", CalculationMode.FLAT, new BigDecimal("50.00"));
        feeRuleRepository.save(exactRule);

        FeeRuleEntity wildcardRule = createRule("Wildcard", null, null, null, null, null);
        addSide(wildcardRule, "SENDER", CalculationMode.FLAT, new BigDecimal("10.00"));
        feeRuleRepository.save(wildcardRule);

        String requestBody = """
                {
                    "transactionId": "txn-9",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "IQD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "receiverId": "user-2",
                    "senderUserType": "PERSONAL",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(50.00))
                .andExpect(jsonPath("$.senderResult.matchedRuleId").value(exactRule.getRuleId().toString()));
    }

    @Test
    public void scenario10() throws Exception {
        FeeRuleEntity usdUsdRule = createRule("USD->USD", null, null, null, Currency.USD, Currency.USD);
        addSide(usdUsdRule, "SENDER", CalculationMode.FLAT, new BigDecimal("10.00"));
        feeRuleRepository.save(usdUsdRule);

        FeeRuleEntity wildcardRule = createRule("Wildcard", null, null, null, null, null);
        addSide(wildcardRule, "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.02"));
        feeRuleRepository.save(wildcardRule);

        // Transaction 1: USD->USD
        String request1 = """
                {
                    "transactionId": "txn-10-1",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "receiverId": "user-2",
                    "senderUserType": "PERSONAL",
                    "receiverUserType": "PERSONAL"
                }
                """;
        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(request1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.matchedRuleId").value(usdUsdRule.getRuleId().toString()));

        // Transaction 2: USD->EUR
        String request2 = """
                {
                    "transactionId": "txn-10-2",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "EUR",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "receiverId": "user-2",
                    "senderUserType": "PERSONAL",
                    "receiverUserType": "PERSONAL"
                }
                """;
        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(request2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.matchedRuleId").value(wildcardRule.getRuleId().toString()));
    }

    @Test
    public void scenario11() throws Exception {
        FeeRuleEntity sourceRule = createRule("USD->*", null, null, null, Currency.USD, null);
        addSide(sourceRule, "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.003"));
        feeRuleRepository.save(sourceRule);

        FeeRuleEntity destRule = createRule("*->IQD", null, null, null, null, Currency.IQD);
        addSide(destRule, "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.004"));
        feeRuleRepository.save(destRule);

        String request = """
                {
                    "transactionId": "txn-11",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "IQD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "receiverId": "user-2",
                    "senderUserType": "PERSONAL",
                    "receiverUserType": "PERSONAL"
                }
                """;
        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.matchedRuleId").value(sourceRule.getRuleId().toString()));
    }

    @Test
    public void scenario6() throws Exception {
        FeeRuleEntity senderRule = createRule("User-123 sender", "user-123", UserType.CORPORATE, TransactionType.WIRE_TRANSFER, null, null);
        addSide(senderRule, "SENDER", CalculationMode.PERCENTAGE, null, new BigDecimal("0.002"));
        feeRuleRepository.save(senderRule);

        FeeRuleEntity receiverRule = createRule("Business receiver", null, UserType.BUSINESS_TERMINAL, null, null, null);
        addSide(receiverRule, "RECEIVER", CalculationMode.FLAT, new BigDecimal("3.00"));
        feeRuleRepository.save(receiverRule);

        String request = """
                {
                    "transactionId": "txn-6",
                    "amount": 20000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "senderId": "user-123",
                    "senderUserType": "CORPORATE",
                    "receiverId": "user-456",
                    "receiverUserType": "BUSINESS_TERMINAL",
                    "transactionType": "WIRE_TRANSFER"
                }
                """;
        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(40.00))
                .andExpect(jsonPath("$.senderResult.matchedRuleId").value(senderRule.getRuleId().toString()))
                .andExpect(jsonPath("$.receiverResult.finalFee").value(3.00))
                .andExpect(jsonPath("$.receiverResult.matchedRuleId").value(receiverRule.getRuleId().toString()));
    }

    @Test
    public void scenario13() throws Exception {
        FeeRuleEntity rule = createRule("Internal transfer", null, UserType.PERSONAL, TransactionType.INTERNAL_TRANSFER, null, null);
        addSide(rule, "SENDER", CalculationMode.FLAT, new BigDecimal("1.00"));
        feeRuleRepository.save(rule);

        String request = """
                {
                    "transactionId": "txn-13",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "senderId": "user-1",
                    "receiverId": "user-1",
                    "transactionType": "INTERNAL_TRANSFER",
                    "senderUserType": "PERSONAL",
                    "receiverUserType": "PERSONAL"
                }
                """;
        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(1.00))
                .andExpect(jsonPath("$.receiverResult.waived").value(true))
                .andExpect(jsonPath("$.receiverResult.finalFee").value(0.00));
    }
}
