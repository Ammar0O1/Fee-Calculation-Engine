package com.gateway.fee.acceptance;

import com.gateway.fee.api.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CalculationScinariosTest extends AbstractApiIntegrationTest {
    @Test
    public void scinario1_basicResolution() throws Exception {
        String requestBody = """
                {
                    "transactionId": "tx-1",
                    "senderUserId": "user-1",
                    "receiverUserId": "user-2",
                    "amount": 100.00,
                    "currency": "USD",
                    "transactionType": "TRANSFER"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("tx-1"))
                .andExpect(jsonPath("$.calculatedFee").value(2.50))
                .andExpect(jsonPath("$.totalAmount").value(102.50));

    }

}
