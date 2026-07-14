package com.gateway.fee.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefaultFeeRuleControllerIntegrationTest extends AbstractApiIntegrationTest {
    @Test
    void shouldCreateDefaultRuleAndReturn201() throws Exception {
        // build json request
        String requestBody = """
                {
                    "senderFee": {
                        "calculationMode": "FLAT",
                        "flatAmount": 5.00
                    },
                    "description": "Test default rule"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ruleId").exists())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.senderFee.calculationMode").value("FLAT"))
                .andExpect(jsonPath("$.senderFee.flatAmount").value(5.00))
                .andExpect(jsonPath("$.description").value("Test default rule"));
    }

    @Test
    void shouldReturnDefaultRules() throws Exception {
        // ARRANGE: create a rule first(because we need to have a rule to GET one)
        String requestBody = """
                {
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Test default rule"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        // get the list and verify
        mockMvc.perform(get("/api/fees/rules/defaults"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // checks the response is an array
                .andExpect(jsonPath("$.length()").value(1)) // this checks the number of items in array(we only created 1 rule so we will pass 1)
                .andExpect(jsonPath("$[0].description").value("Test default rule"));    // first item in the array should have the description we sent
    }

    // duplicate rule creation should return 409
    @Test
    void shouldReturnConflictOnDuplicateDefaultRuleCreation() throws Exception {
        //  create a rule first (first one)
        String requestBody = """
                {
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Test default rule"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        //  create a rule (second one, duplicate)
        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenBothSidesNull() throws Exception {
        String requestBody = """
                {
                    "description": "Rule with no sides"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadResponseWhenSenderFeeInvalid() throws Exception {
        String requestBody = """
                {
                    "senderFee": {
                        "calculationMode": "FLAT"
                    },
                    "description": "Rule with invalid sender fee"
                }
                """;
        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // update non-existent ruleId 404
    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentRule() throws Exception {
        String requestBody = """
                                {
                                    "senderFee": {
                                        "calculationMode": "FLAT",
                                        "flatAmount": 10.00
                                    },
                                    "description": "Updated description"
                
                }
                """;
        mockMvc.perform(put("/api/fees/rules/defaults/" + UUID.randomUUID())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    // create a rule then delete it, then verify it is deleted
    @Test
    void shouldDeleteDefaultRule() throws Exception {
        // create a rule first
        String requestBody = """
                {
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Test default rule"
                }
                """;
        String response = mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ruleId = objectMapper.readTree(response).get("ruleId").asText();
        mockMvc.perform(delete("/api/fees/rules/defaults/"+ruleId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/fees/rules/defaults"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

    }
    @Test
    void shouldUpdateDefaultRule() throws Exception {
        // create
        String createBody = """
            { "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 }, "description": "Original" }
            """;
        String response = mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json").content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String ruleId = objectMapper.readTree(response).get("ruleId").asText();

        // update with changed data
        String updateBody = """
            { "senderFee": { "calculationMode": "FLAT", "flatAmount": 99.00 }, "description": "Updated" }
            """;
        mockMvc.perform(put("/api/fees/rules/defaults/" + ruleId)
                        .contentType("application/json").content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"))
                .andExpect(jsonPath("$.senderFee.flatAmount").value(99.00));
    }
    @Test
    void shouldCreateUserSpecificRule() throws Exception {
        String requestBody = """
            {
                "userType": "CORPORATE",
                "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                "description": "Custom deal for user-123"
            }
            """;

        mockMvc.perform(post("/api/fees/rules/users/user-123")
                        .contentType("application/json").content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.userType").value("CORPORATE"));
    }
    @Test
    void shouldReturnEffectiveScheduleForUser() throws Exception {
        // create a custom rule for the user
        String customRule = """
            {
                "userType": "CORPORATE",
                "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                "description": "Custom deal"
            }
            """;
        mockMvc.perform(post("/api/fees/rules/users/user-123")
                        .contentType("application/json").content(customRule))
                .andExpect(status().isCreated());

        // fetch the effective schedule (note the query param)
        mockMvc.perform(get("/api/fees/rules/users/user-123/effective?userType=CORPORATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}