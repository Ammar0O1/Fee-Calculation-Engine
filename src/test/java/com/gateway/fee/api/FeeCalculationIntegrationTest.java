package com.gateway.fee.api;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// full-stack tests: real Spring context + real Postgres, verifying the /calculate vs /estimate
// logging distinction end to end (the thing MockMvc can't prove, since the service is mocked there)
public class FeeCalculationIntegrationTest extends AbstractApiIntegrationTest {

    @Autowired
    private FeeTransactionLogRepository feeTransactionLogRepository;

    @BeforeEach
    void clearLogs() {
        feeTransactionLogRepository.deleteAll();
    }

    // wildcard default rule (no userId/userType/transactionType/currency constraints) so any
    // transaction in these tests resolves to it
    private void seedDefaultRule(BigDecimal senderFlatAmount, BigDecimal receiverFlatAmount) {
        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .effectiveDate(LocalDateTime.now())
                .active(true)
                .description("integration test default rule")
                .sideDefinitions(new ArrayList<>())
                .build();

        FeeSideDefinitionEntity senderSide = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("SENDER")
                .calculationMode(CalculationMode.FLAT)
                .flatAmount(senderFlatAmount)
                .build();
        FeeSideDefinitionEntity receiverSide = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("RECEIVER")
                .calculationMode(CalculationMode.FLAT)
                .flatAmount(receiverFlatAmount)
                .build();

        rule.getSideDefinitions().add(senderSide);
        rule.getSideDefinitions().add(receiverSide);

        feeRuleRepository.save(rule);
    }

    @Test
    void calculate_endToEnd_returnsCorrectBreakdownAndWritesLogRow() throws Exception {
        seedDefaultRule(new BigDecimal("5.00"), new BigDecimal("2.00"));

        String requestBody = """
                {
                    "transactionId": "txn-int-1",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "sender-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "receiver-1",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn-int-1"))
                .andExpect(jsonPath("$.senderResult.finalFee").value(5.00))
                .andExpect(jsonPath("$.receiverResult.finalFee").value(2.00));

        List<?> logRows = feeTransactionLogRepository.findByTransactionId("txn-int-1");
        assertThat(logRows).hasSize(1);
    }

    @Test
    void estimate_endToEnd_writesNoLogRow() throws Exception {
        seedDefaultRule(new BigDecimal("5.00"), new BigDecimal("2.00"));

        String requestBody = """
                {
                    "userType": "PERSONAL",
                    "userId": "user-1",
                    "transactionType": "WIRE_TRANSFER",
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "amount": 100.00
                }
                """;

        mockMvc.perform(post("/api/fees/estimate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("CALCULATION"))
                .andExpect(jsonPath("$.calculation.senderResult.finalFee").value(5.00));

        assertThat(feeTransactionLogRepository.findAll()).isEmpty();
    }
}
