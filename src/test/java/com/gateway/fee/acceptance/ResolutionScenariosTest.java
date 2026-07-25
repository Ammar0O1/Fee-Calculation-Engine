package com.gateway.fee.acceptance;

import com.gateway.fee.api.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResolutionScenariosTest extends AbstractApiIntegrationTest {

    //  Scenario 5 — one side waived
    @Test
    public void scenario5_oneSideWaived() throws Exception {
        String ruleBody = """
                {
                    "userType": "PERSONAL",
                    "transactionType": "INTERNAL_TRANSFER",
                    "receiverFee": {
                        "calculationMode": "PERCENTAGE",
                        "percentage": 0.001
                    },
                    "description": "Personal internal transfer, sender waived"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        String transaction = """
                {
                    "transactionId": "tx-5",
                    "amount": 1000.00,
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
                        .content(transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.waived").value(true))
                .andExpect(jsonPath("$.receiverResult.finalFee").value(1.00));
    }

    // Scenario 7 — user-specific wildcard beats user-type exact
    @Test
    public void scenario7_userSpecificWildcardBeatsUserTypeExact() throws Exception {
        // general rule (Rule B): PAYMENT + PERSONAL, sender 2%
        String userTypeRuleBody = """
                {
                    "userType": "PERSONAL",
                    "transactionType": "PAYMENT",
                    "senderFee": {
                        "calculationMode": "PERCENTAGE",
                        "percentage": 0.02
                    },
                    "description": "Personal payment 2%"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(userTypeRuleBody))
                .andExpect(status().isCreated());

        // user-specific wildcard rule (Rule A): user-789, no transactionType, sender flat $1
        String userSpecificRuleBody = """
                {
                    "userType": "PERSONAL",
                    "senderFee": {
                        "calculationMode": "FLAT",
                        "flatAmount": 1.00
                    },
                    "description": "user-789 flat $1 on any transaction"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/users/user-789")
                        .contentType("application/json")
                        .content(userSpecificRuleBody))
                .andExpect(status().isCreated());

        String transaction = """
                {
                    "transactionId": "tx-7",
                    "amount": 500.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "PAYMENT",
                    "senderId": "user-789",
                    "senderUserType": "PERSONAL",
                    "receiverId": "user-2",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(1.00));
    }

    // Scenario 8 — fallback to user-type rule
    @Test
    public void scenario8_fallbackToUserTypeRule() throws Exception {
        String ruleBody = """
                {
                    "userType": "BUSINESS",
                    "transactionType": "WIRE_TRANSFER",
                    "senderFee": {
                        "calculationMode": "PERCENTAGE",
                        "percentage": 0.01
                    },
                    "description": "Business wire transfer 1%"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(ruleBody))
                .andExpect(status().isCreated());

        String transaction = """
                {
                    "transactionId": "tx-8",
                    "amount": 1000.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "user-999",
                    "senderUserType": "BUSINESS",
                    "receiverId": "user-2",
                    "receiverUserType": "BUSINESS"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderResult.finalFee").value(10.00));
    }

    // Scenario 14 — effective fee schedule
    @Test
    public void scenario14_effectiveFeeSchedule() throws Exception {
        String defaultRule = """
                {
                    "senderFee": { "calculationMode": "PERCENTAGE", "percentage": 0.01 },
                    "description": "global default"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(defaultRule))
                .andExpect(status().isCreated());

        String userTypeRule = """
                {
                    "userType": "CORPORATE",
                    "transactionType": "CASH_WITHDRAWAL",
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 2.00 },
                    "description": "corporate cash withdrawal flat"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json")
                        .content(userTypeRule))
                .andExpect(status().isCreated());

        String customWire = """
                {
                    "userType": "CORPORATE",
                    "transactionType": "WIRE_TRANSFER",
                    "senderFee": { "calculationMode": "PERCENTAGE", "percentage": 0.003 },
                    "description": "user-456 custom wire"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/users/user-456")
                        .contentType("application/json")
                        .content(customWire))
                .andExpect(status().isCreated());

        String customPayment = """
                {
                    "userType": "CORPORATE",
                    "transactionType": "PAYMENT",
                    "senderFee": { "calculationMode": "PERCENTAGE", "percentage": 0.004 },
                    "description": "user-456 custom payment"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/users/user-456")
                        .contentType("application/json")
                        .content(customPayment))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/fees/rules/users/user-456/effective")
                        .param("userType", "CORPORATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4)) // 2 custom + 1 inherited + 1 default
                .andExpect(jsonPath("$[?(@.rule.transactionType=='WIRE_TRANSFER')].label").value("CUSTOM"))
                .andExpect(jsonPath("$[?(@.rule.transactionType=='PAYMENT')].label").value("CUSTOM"))
                .andExpect(jsonPath("$[?(@.rule.transactionType=='CASH_WITHDRAWAL')].label").value("INHERITED"));
    }
}