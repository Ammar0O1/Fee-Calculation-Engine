package com.gateway.fee.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyPairTest {

    @Test
    void shouldExactMatch() {
        CurrencyPair pair = new CurrencyPair(Currency.USD, Currency.IQD);
        assertTrue(pair.isExactMatch(Currency.USD, Currency.IQD));
    }

    @Test
    void shouldNotExactMatchIfReversed() {
        CurrencyPair pair = new CurrencyPair(Currency.USD, Currency.IQD);
        assertFalse(pair.isExactMatch(Currency.IQD, Currency.USD));
    }

    @Test
    void shouldPartialSourceMatch() {
        CurrencyPair pair = new CurrencyPair(Currency.USD, null);
        assertTrue(pair.isPartialSourceMatch(Currency.USD));
    }

    @Test
    void shouldNotPartialSourceMatchIfDifferent() {
        CurrencyPair pair = new CurrencyPair(Currency.USD, null);
        assertFalse(pair.isPartialSourceMatch(Currency.EUR));
    }

    @Test
    void shouldPartialDestinationMatch() {
        CurrencyPair pair = new CurrencyPair(null, Currency.IQD);
        assertTrue(pair.isPartialDestinationMatch(Currency.IQD));
    }

    @Test
    void shouldBeWildcard() {
        CurrencyPair pair = new CurrencyPair(null, null);
        assertTrue(pair.isWildcard());
    }

    @Test
    void shouldNotBeWildcardIfSet() {
        CurrencyPair pair = new CurrencyPair(Currency.USD, Currency.IQD);
        assertFalse(pair.isWildcard());
    }
}
