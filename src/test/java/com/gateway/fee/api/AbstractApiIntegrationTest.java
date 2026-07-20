package com.gateway.fee.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

public abstract class AbstractApiIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected FeeRuleRepository feeRuleRepository;

    // this class has almost same job as AbstractPostgresIntegrationTest, but that one only serves for DB layer, this one is for api layer.


    @BeforeEach
    void clearDatabase() {
        feeRuleRepository.deleteAll();
    }
    @AfterEach
    void cleanupAfter() {
        feeRuleRepository.deleteAll();
    }

}
