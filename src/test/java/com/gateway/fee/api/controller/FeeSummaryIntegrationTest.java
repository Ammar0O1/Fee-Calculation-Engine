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

@SpringBootTest      //loads the entire application
@AutoConfigureMockMvc  //setup MockMvc

class FeeSummaryIntegrationTest {

    @Autowired  //spring gives u the fake MockMvc client to send request with
    private MockMvc mockMvc;

    @Autowired
    private FeeTransactionLogRepository repository;


    private void seedTransaction(
            TransactionType type,
            UserType senderType, BigDecimal senderFee, Currency senderCurr, boolean senderWaived,
            UserType receiverType, BigDecimal receiverFee, Currency receiverCurr, boolean receiverWaived) {

        FeeTransactionLogEntity entity = FeeTransactionLogEntity.builder()
                // dummy but required fields
                .transactionId("tx-test")
                .transactionAmount(BigDecimal.TEN)
                .sourceCurrency(Currency.USD)
                .destinationCurrency(Currency.IQD)
                .senderUserId("sender-1")
                .receiverUserId("receiver-1")
                .calculatedAt(LocalDateTime.now())
                //fields your test actually varies
                .transactionType(type)
                .senderUserType(senderType)
                .senderFinalFee(senderFee)
                .senderFeeCurrency(senderCurr)
                .senderWaived(senderWaived)
                .receiverUserType(receiverType)
                .receiverFinalFee(receiverFee)
                .receiverFeeCurrency(receiverCurr)
                .receiverWaived(receiverWaived)
                .build();

        repository.save(entity);
    }
    @Test
    void shouldReturnCorrectSummaryTotals() throws Exception {
        // 1. SEED — call the helper 3 times
        seedTransaction(TransactionType.WIRE_TRANSFER,
                UserType.CORPORATE, new BigDecimal("50"), Currency.USD, false,
                UserType.PERSONAL,  new BigDecimal("10"), Currency.IQD, false);

        seedTransaction(TransactionType.WIRE_TRANSFER,
                UserType.CORPORATE,new BigDecimal("30"),Currency.USD,false,
                UserType.BUSINESS,  new BigDecimal("5"), Currency.IQD, false);

        seedTransaction(TransactionType.PAYMENT,
                UserType.PERSONAL,new BigDecimal("20"),Currency.USD,false,
                UserType.CORPORATE, null, Currency.IQD, true);

        // one chained MockMvc statement
        mockMvc.perform(get("/api/fees/history/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalsByTransactionType.WIRE_TRANSFER").value(95))
                .andExpect(jsonPath("$.totalsByTransactionType.PAYMENT").value(20))
        .andExpect(jsonPath("$.totalsByUserType.CORPORATE").value(80))
        .andExpect(jsonPath("$.totalsByUserType.PERSONAL").value(30))
        .andExpect(jsonPath("$.totalsByUserType.BUSINESS").value(5))
        .andExpect(jsonPath("$.totalsByCurrency.USD").value(100))
        .andExpect(jsonPath("$.totalsByCurrency.IQD").value(15))
        .andExpect(jsonPath("$.averagesByTransactionType.PAYMENT").value(20));

    }
    @BeforeEach  // clean the tables before each test
    //ps: this approach is only reliably for this assignment otherwise you have to use different approach
    void cleanUp() {
        repository.deleteAll();
    }
}