package com.ledger.account_ledger.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerEntryTest {

    @Test
    void shouldCreateLedgerEntry() {

        LedgerEntry entry = new LedgerEntry(
                "E1",
                "ACC-001",
                EntryType.CREDIT,
                Money.of("1200.00", Currency.AED),
                1,
                null
        );

        assertEquals("E1", entry.entryId());
        assertEquals("ACC-001", entry.accountId());
        assertEquals(EntryType.CREDIT, entry.type());
        assertEquals(
                Money.of("1200.00", Currency.AED),
                entry.amount()
        );
        assertEquals(1, entry.valueDate());
    }
}