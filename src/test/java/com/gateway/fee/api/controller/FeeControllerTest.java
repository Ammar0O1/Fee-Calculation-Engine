package com.gateway.fee.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.fee.api.dto.response.EffectiveRuleResponse;
import com.gateway.fee.api.dto.response.EstimateMode;
import com.gateway.fee.api.dto.response.FeeCalculationResponse;
import com.gateway.fee.api.dto.response.FeeEstimateResponse;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.dto.response.FeeScheduleEntryResponse;
import com.gateway.fee.api.dto.response.FeeSideResultResponse;
import com.gateway.fee.api.dto.response.RuleOrigin;
import com.gateway.fee.api.service.FeeCalculationService;
import com.gateway.fee.api.service.FeeRuleQueryService;
import com.gateway.fee.domain.exception.NoMatchingRuleException;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeeController.class)
class FeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeeCalculationService feeCalculationService;

    @MockitoBean
    private FeeRuleQueryService feeRuleQueryService;

    // ---------- helpers ----------

    private FeeSideResultResponse sampleSideResult(String userId, BigDecimal fee, boolean waived) {
        return FeeSideResultResponse.builder()
                .userId(userId)
                .userType(UserType.PERSONAL)
                .matchedRuleId(UUID.randomUUID().toString())
                .matchLevel("EXACT")
                .currency(Currency.USD)
                .rawFee(fee)
                .roundedFee(fee)
                .capApplied("NONE")
                .capAdjustment(BigDecimal.ZERO)
                .finalFee(fee)
                .waived(waived)
                .build();
    }

    private FeeCalculationResponse sampleCalculationResponse() {
        return FeeCalculationResponse.builder()
                .transactionId("txn-1")
                .transactionAmount(new BigDecimal("100.00"))
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.USD)
                .transactionType(TransactionType.WIRE_TRANSFER)
                .calculatedAt(LocalDateTime.now())
                .senderResult(sampleSideResult("sender-1", new BigDecimal("5.00"), false))
                .receiverResult(sampleSideResult("receiver-1", BigDecimal.ZERO, true))
                .build();
    }

    private FeeRuleResponse sampleRuleResponse() {
        return new FeeRuleResponse(
                UUID.randomUUID(),
                null,
                UserType.PERSONAL,
                TransactionType.WIRE_TRANSFER,
                Currency.USD,
                Currency.USD,
                null,
                null,
                LocalDateTime.now(),
                true,
                "sample rule",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private String validCalculateRequestJson() {
        return """
                {
                    "transactionId": "txn-1",
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
    }

    //  POST /calculate

    @Test
    void calculate_validRequest_returns200WithBreakdown() throws Exception {
        when(feeCalculationService.calculate(any())).thenReturn(sampleCalculationResponse());

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(validCalculateRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn-1"))
                .andExpect(jsonPath("$.senderResult.finalFee").value(5.00))
                .andExpect(jsonPath("$.receiverResult.waived").value(true));
    }

    @Test
    void calculate_missingRequiredField_returns400() throws Exception {
        String requestBody = """
                {
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_negativeAmount_returns400() throws Exception {
        String requestBody = """
                {
                    "transactionId": "txn-1",
                    "amount": -100.00,
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_invalidCurrency_returns400() throws Exception {
        String requestBody = """
                {
                    "transactionId": "txn-1",
                    "amount": 100.00,
                    "sourceCurrency": "NOT_A_CURRENCY",
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_invalidUserType_returns400() throws Exception {
        String requestBody = """
                {
                    "transactionId": "txn-1",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "WIRE_TRANSFER",
                    "senderId": "sender-1",
                    "senderUserType": "NOT_A_USER_TYPE",
                    "receiverId": "receiver-1",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_invalidTransactionType_returns400() throws Exception {
        String requestBody = """
                {
                    "transactionId": "txn-1",
                    "amount": 100.00,
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "transactionType": "NOT_A_TRANSACTION_TYPE",
                    "senderId": "sender-1",
                    "senderUserType": "PERSONAL",
                    "receiverId": "receiver-1",
                    "receiverUserType": "PERSONAL"
                }
                """;

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_noMatchingRule_returns422() throws Exception {
        when(feeCalculationService.calculate(any()))
                .thenThrow(new NoMatchingRuleException("No rule found for the given criteria"));

        mockMvc.perform(post("/api/fees/calculate")
                        .contentType("application/json")
                        .content(validCalculateRequestJson()))
                .andExpect(status().isUnprocessableEntity());
    }

    //  POST /estimate

    @Test
    void estimate_withAmount_returnsCalculationMode() throws Exception {
        FeeEstimateResponse response = FeeEstimateResponse.builder()
                .mode(EstimateMode.CALCULATION)
                .calculation(sampleCalculationResponse())
                .build();
        when(feeCalculationService.estimate(any())).thenReturn(response);

        String requestBody = """
                {
                    "userType": "PERSONAL",
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
                .andExpect(jsonPath("$.calculation.transactionId").value("txn-1"))
                .andExpect(jsonPath("$.ruleDetails").doesNotExist());
    }

    @Test
    void estimate_withoutAmount_returnsRuleDetailMode() throws Exception {
        FeeEstimateResponse response = FeeEstimateResponse.builder()
                .mode(EstimateMode.RULE_DETAIL)
                .ruleDetails(sampleRuleResponse())
                .build();
        when(feeCalculationService.estimate(any())).thenReturn(response);

        String requestBody = """
                {
                    "userType": "PERSONAL",
                    "transactionType": "WIRE_TRANSFER",
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD"
                }
                """;

        mockMvc.perform(post("/api/fees/estimate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("RULE_DETAIL"))
                .andExpect(jsonPath("$.ruleDetails.description").value("sample rule"))
                .andExpect(jsonPath("$.calculation").doesNotExist());
    }

    @Test
    void estimate_withUserId_resolvesUserSpecificPath() throws Exception {
        FeeEstimateResponse response = FeeEstimateResponse.builder()
                .mode(EstimateMode.CALCULATION)
                .calculation(sampleCalculationResponse())
                .build();
        when(feeCalculationService.estimate(any())).thenReturn(response);

        String requestBody = """
                {
                    "userType": "PERSONAL",
                    "userId": "user-42",
                    "transactionType": "WIRE_TRANSFER",
                    "sourceCurrency": "USD",
                    "destinationCurrency": "USD",
                    "amount": 100.00
                }
                """;

        mockMvc.perform(post("/api/fees/estimate")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(feeCalculationService).estimate(argThat(req -> "user-42".equals(req.getUserId())));
    }

    //  GET /schedule

    @Test
    void schedule_unfiltered_returnsAllEntries() throws Exception {
        FeeScheduleEntryResponse entryOne = FeeScheduleEntryResponse.builder()
                .userType(UserType.PERSONAL)
                .transactionType(TransactionType.WIRE_TRANSFER)
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.USD)
                .build();
        FeeScheduleEntryResponse entryTwo = FeeScheduleEntryResponse.builder()
                .userType(UserType.BUSINESS)
                .transactionType(TransactionType.CARD_PAYMENT)
                .sourceCurrency(Currency.EUR)
                .destinationCurrency(Currency.EUR)
                .build();
        when(feeRuleQueryService.schedule(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(entryOne, entryTwo));

        mockMvc.perform(get("/api/fees/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void schedule_filteredByUserType_returnsNarrowedSubset() throws Exception {
        FeeScheduleEntryResponse entry = FeeScheduleEntryResponse.builder()
                .userType(UserType.PERSONAL)
                .transactionType(TransactionType.WIRE_TRANSFER)
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.USD)
                .build();
        when(feeRuleQueryService.schedule(eq(UserType.PERSONAL), isNull(), isNull(), isNull()))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/fees/schedule").param("userType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userType").value("PERSONAL"));
    }

    @Test
    void schedule_filteredByMultipleDimensions_returnsCorrectSubset() throws Exception {
        FeeScheduleEntryResponse entry = FeeScheduleEntryResponse.builder()
                .userType(UserType.PERSONAL)
                .transactionType(TransactionType.WIRE_TRANSFER)
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.EUR)
                .build();
        when(feeRuleQueryService.schedule(eq(UserType.PERSONAL), eq(TransactionType.WIRE_TRANSFER), eq(Currency.USD), eq(Currency.EUR)))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/fees/schedule")
                        .param("userType", "PERSONAL")
                        .param("transactionType", "WIRE_TRANSFER")
                        .param("sourceCurrency", "USD")
                        .param("destinationCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sourceCurrency").value("USD"))
                .andExpect(jsonPath("$[0].destinationCurrency").value("EUR"));
    }

    //  GET /rules/users/{userId}/effective

    @Test
    void effective_showsCustomInheritedAndDefaultWithCorrectLabels() throws Exception {
        EffectiveRuleResponse custom = EffectiveRuleResponse.builder()
                .rule(sampleRuleResponse())
                .label(RuleOrigin.CUSTOM)
                .build();
        EffectiveRuleResponse inherited = EffectiveRuleResponse.builder()
                .rule(sampleRuleResponse())
                .label(RuleOrigin.INHERITED)
                .build();
        EffectiveRuleResponse defaultRule = EffectiveRuleResponse.builder()
                .rule(sampleRuleResponse())
                .label(RuleOrigin.DEFAULT)
                .build();
        when(feeRuleQueryService.effective("user-1", UserType.PERSONAL))
                .thenReturn(List.of(custom, inherited, defaultRule));

        mockMvc.perform(get("/api/fees/rules/users/user-1/effective").param("userType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].label").value("CUSTOM"))
                .andExpect(jsonPath("$[1].label").value("INHERITED"))
                .andExpect(jsonPath("$[2].label").value("DEFAULT"));
    }

    @Test
    void effective_userWithNoCustomRules_returnsOnlyInheritedAndDefault() throws Exception {
        EffectiveRuleResponse inherited = EffectiveRuleResponse.builder()
                .rule(sampleRuleResponse())
                .label(RuleOrigin.INHERITED)
                .build();
        EffectiveRuleResponse defaultRule = EffectiveRuleResponse.builder()
                .rule(sampleRuleResponse())
                .label(RuleOrigin.DEFAULT)
                .build();
        when(feeRuleQueryService.effective("user-2", UserType.PERSONAL))
                .thenReturn(List.of(inherited, defaultRule));

        mockMvc.perform(get("/api/fees/rules/users/user-2/effective").param("userType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].label").value("INHERITED"))
                .andExpect(jsonPath("$[1].label").value("DEFAULT"));
    }
}
