package com.ledger.account_ledger.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyTest {

    @Test
    void shouldHaveCorrectDecimalScale() {
        assertEquals(2, Currency.AED.getScale());
        assertEquals(3, Currency.BHD.getScale());
    }
}