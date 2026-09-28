package com.ledger.account_ledger.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LedgerEventTest {

    @Test
    void shouldCreateEventWithDifferentEventDayAndValueDate() {

        LedgerEvent event = new LedgerEvent(
                "E7",
                5,
                "ACC-001",
                EventType.DEBIT,
                Money.of("620.00", Currency.AED),
                2,
                null
        );

        assertEquals("E7", event.eventId());
        assertEquals(5, event.eventDay());
        assertEquals(2, event.valueDate());
        assertEquals(Money.of("620.00", Currency.AED), event.amount());
    }
}