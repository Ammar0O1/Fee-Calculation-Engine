package com.gateway.fee.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.service.FeeRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(DefaultFeeRuleController.class)
public class DefaultFeeRuleControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean

    private FeeRuleService feeRuleService;

    private FeeRuleResponse sampleResponse(String description) {
        return new FeeRuleResponse(
                UUID.randomUUID(), null, null, null, null, null,
                null, null, LocalDateTime.now(), true, description,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void shouldCreateDefaultRuleAndReturn201() throws Exception {
        //build a fake response the mock service will return
        FeeRuleResponse fakeResponse = sampleResponse("Test default rule");

        // STUB: when the controller calls createDefaultRule with any request, return the fake
        when(feeRuleService.createDefaultRule(any())).thenReturn(fakeResponse);

        //send the request, check the controller's HTTP behavior
        String requestBody = """
                {
                    "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 },
                    "description": "Test default rule"
                }
                """;

        mockMvc.perform(post("/api/fees/rules/defaults")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test default rule"));
    }

    @Test
    void shouldGetDefaultRuleAndReturn200() throws Exception {
        // build a fake response the mock service will return
        FeeRuleResponse fakeResponse = sampleResponse("Test default rule");
        //: when the controller calls getDefaultRule with any request, return the fake
        when(feeRuleService.getDefaultRules()).thenReturn(List.of(fakeResponse));

        mockMvc.perform(get("/api/fees/rules/defaults"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Test default rule"));
    }

    @Test
    void shouldUpdateDefaultRuleAndReturn200() throws Exception {
        when(feeRuleService.updateDefaultRule(any(), any())).thenReturn(sampleResponse("Updated"));

        String body = """
                { "senderFee": { "calculationMode": "FLAT", "flatAmount": 5.00 }, "description": "Updated" }
                """;

        mockMvc.perform(put("/api/fees/rules/defaults/" + UUID.randomUUID())
                        .contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void shouldDeleteDefaultRuleAndReturn204() throws Exception {
        UUID ruleId = UUID.randomUUID();

        // no stubbing needed, void methods do nothing by default on a mock

        mockMvc.perform(delete("/api/fees/rules/defaults/" + ruleId))
                .andExpect(status().isNoContent());

        // verify the controller actually called the service
        verify(feeRuleService).deleteDefaultRule(ruleId);
    }


}