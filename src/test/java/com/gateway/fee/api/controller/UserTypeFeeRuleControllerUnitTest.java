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

@WebMvcTest(UserTypeFeeRuleController.class)
public class UserTypeFeeRuleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeeRuleService feeRuleService;

    private FeeRuleResponse sampleResponse(String description) {
        return new FeeRuleResponse(
                UUID.randomUUID(), null, null, null, null, null,
                null, null, LocalDateTime.now(), true, description,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void shouldCreateUserTypeRuleAndReturn201() throws Exception {
        when(feeRuleService.createUserTypeRule(any())).thenReturn(sampleResponse("Corporate rule"));

        String body = """
                {
                    "userType": "CORPORATE",
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Corporate rule"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/user-types")
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Corporate rule"));
    }

    @Test
    void shouldGetUserTypeRulesAndReturn200() throws Exception {
        when(feeRuleService.getUserTypeRules()).thenReturn(List.of(sampleResponse("Corporate rule")));

        mockMvc.perform(get("/api/fees/rules/user-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Corporate rule"));
    }

    @Test
    void shouldGetUserTypeRulesByTypeAndReturn200() throws Exception {
        when(feeRuleService.getUserTypeRulesByType(any())).thenReturn(List.of(sampleResponse("Corporate rule")));

        mockMvc.perform(get("/api/fees/rules/user-types/CORPORATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Corporate rule"));
    }

    @Test
    void shouldUpdateUserTypeRuleAndReturn200() throws Exception {
        when(feeRuleService.updateUserTypeRule(any(), any())).thenReturn(sampleResponse("Updated"));

        String body = """
                {
                    "userType": "CORPORATE",
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Updated"
                }
                """;

        mockMvc.perform(put("/api/fees/rules/user-types/" + UUID.randomUUID())
                        .contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void shouldDeleteUserTypeRuleAndReturn204() throws Exception {
        UUID ruleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/fees/rules/user-types/" + ruleId))
                .andExpect(status().isNoContent());

        verify(feeRuleService).deleteUserTypeRule(ruleId);
    }
}