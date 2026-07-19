package com.gateway.fee.infrastructure.config;
import com.gateway.fee.application.FeeCalculator;
import com.gateway.fee.application.FeeTransactionLogService;
import com.gateway.fee.domain.calculation.CalculationStrategyFactory;
import com.gateway.fee.domain.calculation.FeeApplier;
import com.gateway.fee.domain.registry.FeeRuleRegistry;
import com.gateway.fee.domain.resolution.FeeRuleResolver;
import com.gateway.fee.infrastructure.persistence.mapper.FeeTransactionLogMapper;
import com.gateway.fee.infrastructure.persistence.repository.FeeTransactionLogRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//DEF: Wires the pure-Java fee engine (Assignment 1) into Spring.
// These classes have no Spring annotations by design — the domain
// and application layers stay framework-free. This config constructs
// them manually and registers them as beans so they can be injected
// (into FeeCalculationService). Bean order follows dependency

@Configuration
public class FeeEngineConfiguration {

    @Bean
    public FeeApplier feeApplier() {
        return new FeeApplier();
    }

    @Bean
    public CalculationStrategyFactory calculationStrategyFactory() {
        return new CalculationStrategyFactory();
    }

    @Bean
    public FeeRuleResolver feeRuleResolver(FeeRuleRegistry registry) {
        return new FeeRuleResolver(registry);
    }

    @Bean
    public FeeTransactionLogService feeTransactionLogService(

            FeeTransactionLogMapper mapper,
            FeeTransactionLogRepository repo){
        return new FeeTransactionLogService(mapper,repo);
    }

    @Bean
    public FeeCalculator feeCalculator(
            FeeRuleResolver resolver,
            CalculationStrategyFactory factory,
            FeeApplier applier,
            FeeTransactionLogService logService) {
        return new FeeCalculator(resolver,factory,applier,logService);
    }
}