package com.gateway.fee.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.gateway.fee.domain.model.CalculationMode.HYBRID;
import static com.gateway.fee.domain.model.CalculationMode.FLAT;
import static com.gateway.fee.domain.model.TransactionType.WIRE_TRANSFER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BuildersTest {

    @Test
    void feeRuleBuilder_missingBothFeeSides_throws() {
        assertThatThrownBy(() ->
                FeeRuleBuilder.aRule()
                        .forUserType(UserType.CORPORATE)
                        .forTransactionType(WIRE_TRANSFER)
                        .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Fee side definitions must not be null");
    }

    @Test
    void feeRuleBuilder_producesConfiguredObject() {
        FeeSideDefinition sender = FeeSideDefinitionBuilder.percentage(0.015);
        FeeRule rule = FeeRuleBuilder.aRule()
                .forUserType(UserType.CORPORATE)
                .forTransactionType(WIRE_TRANSFER)
                .withSenderFee(sender)
                .withSourceCurrency(Currency.USD)
                .withDestinationCurrency(Currency.EUR)
                .withDescription("Corporate wire with sender percentage")
                .build();

        assertThat(rule.getUserType()).isEqualTo(UserType.CORPORATE);
        assertThat(rule.getTransactionType()).isEqualTo(WIRE_TRANSFER);
        assertThat(rule.getSenderFee()).isEqualTo(sender);
        assertThat(rule.getSourceCurrency()).isEqualTo(Currency.USD);
        assertThat(rule.getDestinationCurrency()).isEqualTo(Currency.EUR);
        assertThat(rule.getDescription()).contains("sender percentage");
    }

    @Test
    void feeSideDefinitionBuilder_missingRequiredField_throws() {
        assertThatThrownBy(() -> new FeeSideDefinitionBuilder()
                .withCalculationMode(FLAT)
                .build()
        ).isExactlyInstanceOf(com.gateway.fee.domain.exception.FeeConfigurationException.class)
         .hasMessageContaining("FlatAmount");
    }

    @Test
    void feeSideDefinitionBuilder_producesConfiguredObject() {
        FeeSideDefinition def = FeeSideDefinitionBuilder.hybrid(2, 0.005);
        assertThat(def.getCalculationMode()).isEqualTo(HYBRID);
        assertThat(def.getFlatAmount()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(def.getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(0.005));
    }

    @Test
    void transactionBuilder_missingRequiredField_throws() {
        assertThatThrownBy(() -> TransactionBuilder.aTransaction()
                .forUserType(UserType.BUSINESS)
                .withAmount(100)
                .build() // missing transaction type
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("transactionType");

        assertThatThrownBy(() -> TransactionBuilder.aTransaction()
                .forUserType(UserType.BUSINESS)
                .forTransactionType(WIRE_TRANSFER)
                .build() // missing amount
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("amount");
    }

    @Test
    void transactionBuilder_producesConfiguredObject() {
        var tx = TransactionBuilder.aTransaction()
                .withUserId("u-1")
                .forUserType(UserType.CORPORATE)
                .forTransactionType(WIRE_TRANSFER)
                .withAmount(123.45)
                .withCurrency(Currency.USD)
                .withDestinationCurrency(Currency.EUR)
                .build();

        assertThat(tx.getSenderId()).isEqualTo("u-1");
        assertThat(tx.getSenderUserType()).isEqualTo(UserType.CORPORATE);
        assertThat(tx.getTransactionType()).isEqualTo(WIRE_TRANSFER);
        assertThat(tx.getAmount()).isEqualByComparingTo("123.45");
        assertThat(tx.getSourceCurrency()).isEqualTo(Currency.USD);
        assertThat(tx.getDestinationCurrency()).isEqualTo(Currency.EUR);
    }
}
