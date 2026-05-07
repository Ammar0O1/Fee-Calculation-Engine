package com.gateway.fee.infrastructure;

import com.gateway.fee.domain.exception.FeeConfigurationException;
import com.gateway.fee.domain.model.CalculationMode;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.FeeRule;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeRuleJsonLoaderTest {

    private final FeeRuleJsonLoader loader = new FeeRuleJsonLoader();

    @Test
    void shouldLoadValidJsonFile() throws IOException {
        // given
        String path = "src/test/resources/test-data/valid-rules.json";

        // when
        List<FeeRule> rules = loader.load(path);

        // then
        assertThat(rules).hasSize(2);

        // Verify Rule 1 mapping
        FeeRule rule1 = rules.get(0);
        assertThat(rule1.getRuleId()).isEqualTo("RULE-1");
        assertThat(rule1.getUserId()).isEqualTo("USER-1");
        assertThat(rule1.getUserType()).isEqualTo(UserType.PERSONAL);
        assertThat(rule1.getTransactionType()).isEqualTo(TransactionType.WIRE_TRANSFER);
        assertThat(rule1.getSourceCurrency()).isEqualTo(Currency.USD);
        assertThat(rule1.getDestinationCurrency()).isEqualTo(Currency.EUR);
        assertThat(rule1.isActive()).isTrue();
        assertThat(rule1.getDescription()).isEqualTo("Test Rule 1");
        assertThat(rule1.getEffectiveDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0));

        // Sender Fee (Hybrid)
        assertThat(rule1.getSenderFee().getCalculationMode()).isEqualTo(CalculationMode.HYBRID);
        assertThat(rule1.getSenderFee().getFlatAmount()).isEqualByComparingTo("1.00");
        assertThat(rule1.getSenderFee().getPercentage()).isEqualByComparingTo("0.5");
        assertThat(rule1.getSenderFee().getMinCap()).isEqualByComparingTo("0.50");
        assertThat(rule1.getSenderFee().getMaxCap()).isEqualByComparingTo("10.00");

        // Receiver Fee (Tiers)
        assertThat(rule1.getReceiverFee().getCalculationMode()).isEqualTo(CalculationMode.TIERED_FLAT);
        assertThat(rule1.getReceiverFee().getTiers()).hasSize(2);
        assertThat(rule1.getReceiverFee().getTiers().get(0).getFromAmount()).isEqualByComparingTo("0");
        assertThat(rule1.getReceiverFee().getTiers().get(0).getToAmount()).isEqualByComparingTo("100");
        assertThat(rule1.getReceiverFee().getTiers().get(0).getRate()).isEqualByComparingTo("1.00");

        // Verify Rule 2 mapping (nulls and different values)
        FeeRule rule2 = rules.get(1);
        assertThat(rule2.getRuleId()).isEqualTo("RULE-2");
        assertThat(rule2.getUserId()).isNull();
        assertThat(rule2.getSourceCurrency()).isEqualTo(Currency.EUR);
        assertThat(rule2.getDestinationCurrency()).isEqualTo(Currency.EUR);
        assertThat(rule2.isActive()).isFalse();
        assertThat(rule2.getReceiverFee()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() {
        // given
        String path = "src/test/resources/test-data/non-existent.json";

        // when / then
        assertThatThrownBy(() -> loader.load(path))
                .isInstanceOf(IOException.class);
    }

    @Test
    void shouldThrowExceptionWhenJsonIsMalformed() {
        // given
        String path = "src/test/resources/test-data/malformed.json";

        // when / then
        assertThatThrownBy(() -> loader.load(path))
                .isInstanceOf(com.fasterxml.jackson.core.JacksonException.class);
    }

    @Test
    void shouldThrowExceptionWhenTiersAreNullForTieredMode() throws IOException {
        // given
        Path tempFile = Files.createTempFile("invalid-rules", ".json");
        String json = """
            [
              {
                "ruleId": "RULE-TIERS-NULL",
                "senderFee": {
                  "calculationMode": "TIERED_FLAT",
                  "tiers": null
                }
              }
            ]
            """;
        Files.writeString(tempFile, json);

        // when / then
        try {
            assertThatThrownBy(() -> loader.load(tempFile.toString()))
                    .isInstanceOf(FeeConfigurationException.class)
                    .hasMessageContaining("tiers must not be null");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
