package com.gateway.fee.api.controller;

import com.gateway.fee.infrastructure.persistence.entity.FeeTransactionLogEntity;
import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FeeHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeeTransactionLogRepository repository;

    // wipe the table before each test so leftover rows don't skew results
    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    // seed one transaction, varying only the user IDs and type (what this test cares about)
    private void seedWithUsers(String senderUserId, String receiverUserId, TransactionType type) {
        FeeTransactionLogEntity entity = FeeTransactionLogEntity.builder()
                .transactionId("tx-" + senderUserId)
                .transactionAmount(BigDecimal.TEN)
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.IQD)
                .transactionType(type)
                .senderUserId(senderUserId)
                .senderUserType(UserType.CORPORATE)
                .senderFinalFee(BigDecimal.TEN)
                .senderFeeCurrency(Currency.USD)
                .senderWaived(false)
                .receiverUserId(receiverUserId)
                .receiverUserType(UserType.PERSONAL)
                .receiverFinalFee(BigDecimal.ONE)
                .receiverFeeCurrency(Currency.IQD)
                .receiverWaived(false)
                .calculatedAt(LocalDateTime.now())
                .build();
        repository.save(entity);
    }

    @Test
    void shouldReturnTransactionsWhereUserIsSenderOrReceiver() throws Exception {

        seedWithUsers("hardy","blnd",TransactionType.WIRE_TRANSFER);  //in this test the transactionType dosent matter
        seedWithUsers("ammar","hardy",TransactionType.WIRE_TRANSFER);  //but since its in the helper we have to add it
        seedWithUsers("aland","ismael",TransactionType.WIRE_TRANSFER);


        mockMvc.perform(get("/api/fees/history").param("userId", "hardy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));

    }
}