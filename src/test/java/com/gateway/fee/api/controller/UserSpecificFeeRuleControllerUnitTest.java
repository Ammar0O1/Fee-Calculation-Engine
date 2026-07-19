package com.gateway.fee.api.controller;

import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.service.FeeRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSpecificFeeRuleController.class)
public class UserSpecificFeeRuleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeeRuleService feeRuleService;

    private FeeRuleResponse sampleResponse(String description) {
        return new FeeRuleResponse(
                UUID.randomUUID(), "user-123", null, null, null, null,
                null, null, LocalDateTime.now(), true, description,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void shouldCreateUserSpecificRuleAndReturn201() throws Exception {
        when(feeRuleService.createUserSpecificRule(any(), any())).thenReturn(sampleResponse("Custom deal"));

        String body = """
                {
                    "userType": "CORPORATE",
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Custom deal"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/users/user-123")
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Custom deal"));
    }

    @Test
    void shouldGetUserSpecificRulesAndReturn200() throws Exception {
        when(feeRuleService.getUserSpecificRules(any())).thenReturn(List.of(sampleResponse("Custom deal")));

        mockMvc.perform(get("/api/fees/rules/users/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Custom deal"));
    }



    @Test
    void shouldUpdateUserSpecificRuleAndReturn200() throws Exception {
        when(feeRuleService.updateUserSpecificRule(any(), any(), any())).thenReturn(sampleResponse("Updated"));

        String body = """
                {
                    "userType": "CORPORATE",
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Updated"
                }
                """;

        mockMvc.perform(put("/api/fees/rules/users/user-123/" + UUID.randomUUID())
                        .contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void shouldDeleteUserSpecificRuleAndReturn204() throws Exception {
        UUID ruleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/fees/rules/users/user-123/" + ruleId))
                .andExpect(status().isNoContent());

        verify(feeRuleService).deleteUserSpecificRule("user-123", ruleId);
    }
}