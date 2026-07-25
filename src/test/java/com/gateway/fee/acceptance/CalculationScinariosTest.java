package com.gateway.fee.acceptance;

import com.gateway.fee.api.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CalculationScinariosTest extends AbstractApiIntegrationTest {

    private void seedDefaultRule() {
        com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity rule = com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity.builder()
                .ruleId(java.util.UUID.randomUUID())
                .effectiveDate(java.time.LocalDateTime.now())
                .active(true)
                .description("default rule")
                .sideDefinitions(new java.util.ArrayList<>())
                .build();

        com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity senderSide = com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("SENDER")
                .calculationMode(com.gateway.fee.domain.model.CalculationMode.FLAT)
                .flatAmount(new java.math.BigDecimal("2.50"))
                .build();
        
        rule.getSideDefinitions().add(senderSide);
        feeRuleRepository.save(rule);
    }

    @Test
    public void scinario1_basicResolution() throws Exception {
        seedDefaultRule();
        String requestBody = """
                {
                    "transactionId": "tx-1",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("tx-1"))
                .andExpect(jsonPath("$.senderResult.finalFee").value(2.50));
    }

}
