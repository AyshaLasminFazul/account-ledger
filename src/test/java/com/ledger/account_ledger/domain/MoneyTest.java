package com.ledger.account_ledger.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

    @Test
    void shouldRoundAedToTwoDecimalPlaces() {
        Money money = Money.of("1200", Currency.AED);

        assertEquals(
                new BigDecimal("1200.00"),
                money.amount()
        );
    }

    @Test
    void shouldRoundBhdToThreeDecimalPlaces() {
        Money money = Money.of("10", Currency.BHD);

        assertEquals(
                new BigDecimal("10.000"),
                money.amount()
        );
    }

    @Test
    void shouldAddMoney() {
        Money first = Money.of("100.00", Currency.AED);
        Money second = Money.of("50.00", Currency.AED);

        Money result = first.add(second);

        assertEquals(
                new BigDecimal("150.00"),
                result.amount()
        );
    }

    @Test
    void shouldSubtractMoney() {
        Money first = Money.of("100.00", Currency.AED);
        Money second = Money.of("150.00", Currency.AED);

        Money result = first.subtract(second);

        assertEquals(
                new BigDecimal("-50.00"),
                result.amount()
        );
    }

    @Test
    void shouldRejectDifferentCurrencies() {
        Money aed = Money.of("100.00", Currency.AED);
        Money bhd = Money.of("50.000", Currency.BHD);

        assertThrows(
                IllegalArgumentException.class,
                () -> aed.add(bhd)
        );
    }
}