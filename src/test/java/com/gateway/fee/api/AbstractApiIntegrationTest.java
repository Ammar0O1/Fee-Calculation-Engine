package com.gateway.fee.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc

public abstract class AbstractApiIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected FeeRuleRepository feeRuleRepository;

    // this class has almost same job as AbstractPostgresIntegrationTest, but that one only serves for DB layer, this one is for api layer.

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/fee_engine");
        registry.add("spring.datasource.username", () -> "Hardy");
        registry.add("spring.datasource.password", () -> "1234");
        registry.add("spring.flyway.enabled", () -> "true");
    }
    @BeforeEach
    void clearDatabase() {
        feeRuleRepository.deleteAll();
    }

}
