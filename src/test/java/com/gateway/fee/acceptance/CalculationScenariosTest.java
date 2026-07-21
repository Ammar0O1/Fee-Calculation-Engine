package com.gateway.fee.acceptance;

import com.gateway.fee.api.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CalculationScenariosTest extends AbstractApiIntegrationTest {
    @Test
    public void scenario1_basicResolution() throws Exception {

        // create rule
        String ruleBody = """
                {
                    "userType": "CORPORATE",
                    "transactionType": "WIRE_TRANSFER",
                    "senderFee": {
                        "calculationMode": "PERCENTAGE",
                        "percentage": 0.005
                    },
                    "description": "Corporate wire transfer sender 0.5%"
                }
                """;


        // create rule
        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        String requestBody = """
                {
                    "transactionId": "tx-1",
                    "amount": 10000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "senderUserType": "CORPORATE",
                    "receiverId": "user-2",
                    "receiverUserType": "CORPORATE"
                }
                """;
        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(50.00));
    }

//    Rule: INTERNAL_TRANSFER + PERSONAL → sender $1.00 + 0.5%, min $3, max $20.
//•	Transaction $100: raw = $1.50, below min → fee = $3.00
//            •	Transaction $5,000: raw = $26.00, above max → fee = $20.00

    @Test
    public void scenario2_hybridWithCaps() throws Exception{
        // creating rule
        String ruleBody = """
                {
                    "userType": "PERSONAL",
                    "transactionType": "INTERNAL_TRANSFER",
                    "senderFee": {
                        "calculationMode": "HYBRID",
                        "fixedAmount": 1.00,
                        "percentage": 0.005,
                        "minAmount": 3.00,
                        "maxAmount": 20.00
                    },
                    "description": "Personal internal transfer sender $1 + 0.5%, min $3, max $20"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

    }

    String requestBody = """
                {
                    "transactionId": "tx-1",
                    "amount": 10000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

}
