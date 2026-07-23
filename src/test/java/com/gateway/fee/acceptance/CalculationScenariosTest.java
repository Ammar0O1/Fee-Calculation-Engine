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
    public void scenario2_hybridWithCaps() throws Exception {
        // Rule: INTERNAL_TRANSFER and PERSONAL, sender pays $1.00 plus 0.5%, with min cap $3 and max cap $20
        String ruleBody = """
                {
                    "userType": "PERSONAL",
                    "transactionType": "INTERNAL_TRANSFER",
                    "senderFee": {
                        "calculationMode": "HYBRID",
                        "flatAmount": 1.00,
                        "percentage": 0.005,
                        "minCap": 3.00,
                        "maxCap": 20.00
                    },
                    "description": "Personal internal transfer hybrid with caps"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        // Transaction $100: raw fee is 1.00 plus 0.50 which equals 1.50, below the min cap, so the fee becomes 3.00
        String smallTransaction = """
                {
                    "transactionId": "tx-2a",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "INTERNAL_TRANSFER",
                    "senderId": "user-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(smallTransaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(3.00));

        // we have 2 tx... small and large, so we can test both min and max caps in the same scenario

        // Transaction $5,000: raw fee is 1.00 plus 25.00 which equals 26.00, above the max cap, so the fee becomes 20.00
        String largeTransaction = """
                {
                    "transactionId": "tx-2b",
                    "amount": 5000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "INTERNAL_TRANSFER",
                    "senderId": "user-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(largeTransaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(20.00));
    }

    @Test
    public void scenario3_marginalTiering() throws Exception {
        // Rule: PAYMENT and BUSINESS_TERMINAL, sender pays marginal tiers
        // First bracket 0 to 1000 at 2 percent, second 1000 to 5000 at 1.5 percent, third 5000 and above at 1 percent
        String ruleBody = """
                {
                    "userType": "BUSINESS_TERMINAL",
                    "transactionType": "PAYMENT",
                    "senderFee": {
                        "calculationMode": "TIERED_MARGINAL",
                        "tiers": [
                            { "fromAmount": 0,    "toAmount": 1000, "rate": 0.02 },
                            { "fromAmount": 1000, "toAmount": 5000, "rate": 0.015 },
                            { "fromAmount": 5000, "toAmount": null, "rate": 0.01 }
                        ]
                    },
                    "description": "Business terminal payment with marginal tiers"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        // $8,000 transaction: (1000 times 2 percent) plus (4000 times 1.5 percent) plus (3000 times 1 percent)
        // which equals 20 plus 60 plus 30, giving a total of 110.00
        String transaction = """
                {
                    "transactionId": "tx-3",
                    "amount": 8000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "PAYMENT",
                    "senderId": "user-1",
                    "senderUserType": "BUSINESS_TERMINAL",
                    "receiverId": "user-2",
                    "receiverUserType": "BUSINESS_TERMINAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(110.00));
    }

    @Test
    public void scenario4_differentModesPerSide() throws Exception {
        // Rule: WIRE_TRANSFER and CORPORATE, from USD to IQD
        // Sender pays 0.5 percent with min cap $5 and max cap $50, receiver pays a flat 5,000 IQD
        String ruleBody = """
                {
                    "userType": "CORPORATE",
                    "transactionType": "WIRE_TRANSFER",
                    "sourceCurrency": "USD",
                    "destinationCurrency": "IQD",
                    "senderFee": {
                        "calculationMode": "PERCENTAGE",
                        "percentage": 0.005,
                        "minCap": 5.00,
                        "maxCap": 50.00
                    },
                    "receiverFee": {
                        "calculationMode": "FLAT",
                        "flatAmount": 5000
                    },
                    "description": "Corporate USD to IQD wire transfer"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        // $10,000 transaction from USD to IQD: sender pays 0.5 percent which equals 50.00 USD, at the max cap
        // Receiver pays the flat 5,000 IQD
        String transaction = """
                {
                    "transactionId": "tx-4",
                    "amount": 10000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "IQD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-1",
                    "senderUserType": "CORPORATE",
                    "receiverId": "user-2",
                    "receiverUserType": "CORPORATE"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(50.00))
                .andExpect(jsonPath("$.senderResult.currency").value("USD"))
                .andExpect(jsonPath("$.receiverResult.finalFee").value(5000))
                .andExpect(jsonPath("$.receiverResult.currency").value("IQD"));
    }

    @Test
    public void scenario12_currencySpecificRounding() throws Exception {
        // Rule: PAYMENT with wildcard user type and wildcard currencies, sender pays 1.5 percent
        String ruleBody = """
                {
                    "transactionType": "PAYMENT",
                    "senderFee": {
                        "calculationMode": "PERCENTAGE",
                        "percentage": 0.015
                    },
                    "description": "Global payment rule 1.5% for rounding test"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        // USD: 1.5 percent of 333.33 equals 4.99995, rounded HALF_UP to 2 decimals gives 5.00
        String usdTransaction = """
                {
                    "transactionId": "tx-12a",
                    "amount": 333.33,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "PAYMENT",
                    "senderId": "user-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(usdTransaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(5.00));

        // IQD: 1.5 percent of 333,333 equals 4999.995, rounded UP to 0 decimals gives 5000
        String iqdTransaction = """
                {
                    "transactionId": "tx-12b",
                    "amount": 333333,
                    "sourceCurrency": "IQD",
                    "destinationCurrency": "IQD",
                    "transactionType": "PAYMENT",
                    "senderId": "user-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(iqdTransaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(5000));
    }

}
