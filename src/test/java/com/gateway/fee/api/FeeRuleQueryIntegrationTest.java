package com.gateway.fee.api;

import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.entity.FeeSideDefinitionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// full-stack tests: real Spring context + real Postgres, seeding actual FeeRuleEntity rows and
// asserting the /schedule and /effective endpoints read them back correctly
public class FeeRuleQueryIntegrationTest extends AbstractApiIntegrationTest {

    private FeeRuleEntity seedRule(String userId, UserType userType, TransactionType transactionType,
                                    Currency sourceCurrency, Currency destinationCurrency) {
        FeeRuleEntity rule = FeeRuleEntity.builder()
                .ruleId(UUID.randomUUID())
                .userId(userId)
                .userType(userType)
                .transactionType(transactionType)
                .sourceCurrency(sourceCurrency)
                .destinationCurrency(destinationCurrency)
                .effectiveDate(LocalDateTime.now())
                .active(true)
                .description("seeded rule")
                .sideDefinitions(new ArrayList<>())
                .build();

        FeeSideDefinitionEntity senderSide = FeeSideDefinitionEntity.builder()
                .feeRule(rule)
                .side("SENDER")
                .calculationMode(CalculationMode.FLAT)
                .flatAmount(new BigDecimal("5.00"))
                .build();
        rule.getSideDefinitions().add(senderSide);

        return feeRuleRepository.save(rule);
    }

    @Test
    void schedule_returnsSeededRules() throws Exception {
        seedRule(null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.USD);
        seedRule(null, UserType.BUSINESS, TransactionType.CARD_PAYMENT, Currency.EUR, Currency.EUR);

        mockMvc.perform(get("/api/fees/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void schedule_filteredByUserType_returnsOnlyMatchingRule() throws Exception {
        seedRule(null, UserType.PERSONAL, TransactionType.WIRE_TRANSFER, Currency.USD, Currency.USD);
        seedRule(null, UserType.BUSINESS, TransactionType.CARD_PAYMENT, Currency.EUR, Currency.EUR);

        mockMvc.perform(get("/api/fees/schedule").param("userType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userType").value("PERSONAL"));
    }

    @Test
    void effective_mergesCustomInheritedAndDefaultRulesWithCorrectLabels() throws Exception {
        seedRule("user-1", UserType.PERSONAL, null, null, null);          // CUSTOM
        seedRule(null, UserType.PERSONAL, null, null, null);              // INHERITED
        seedRule(null, null, null, null, null);                          // DEFAULT

        mockMvc.perform(get("/api/fees/rules/users/user-1/effective").param("userType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].label").value("CUSTOM"))
                .andExpect(jsonPath("$[0].rule.userId").value("user-1"))
                .andExpect(jsonPath("$[1].label").value("INHERITED"))
                .andExpect(jsonPath("$[2].label").value("DEFAULT"));
    }

    @Test
    void effective_userWithNoCustomRules_returnsOnlyInheritedAndDefault() throws Exception {
        seedRule(null, UserType.PERSONAL, null, null, null);              // INHERITED
        seedRule(null, null, null, null, null);                          // DEFAULT

        mockMvc.perform(get("/api/fees/rules/users/user-with-no-custom-rules/effective").param("userType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].label").value("INHERITED"))
                .andExpect(jsonPath("$[1].label").value("DEFAULT"));
    }
}
