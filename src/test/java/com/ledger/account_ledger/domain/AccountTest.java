package com.ledger.account_ledger.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountTest {

    @Test
    void shouldCreateAedAccount() {

        Account account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );

        assertEquals("ACC-001", account.accountId());
        assertEquals(Currency.AED, account.currency());
        assertEquals(
                Money.of("0.00", Currency.AED),
                account.openingBalance()
        );
    }

    @Test
    void shouldCreateBhdAccount() {

        Account account = new Account(
                "ACC-002",
                Currency.BHD,
                Money.of("0.000", Currency.BHD)
        );

        assertEquals("ACC-002", account.accountId());
        assertEquals(Currency.BHD, account.currency());
    }
}