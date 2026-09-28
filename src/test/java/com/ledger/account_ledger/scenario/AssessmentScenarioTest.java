package com.ledger.account_ledger.scenario;

import com.ledger.account_ledger.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentScenarioTest {

    @Test
    void shouldContainAllAssessmentEventsInOrder() {

        List<LedgerEvent> events = AssessmentScenario.events();

        assertEquals(10, events.size());

        assertEquals("E1", events.get(0).eventId());
        assertEquals("E7", events.get(6).eventId());
        assertEquals("E9", events.get(8).eventId());
        assertEquals("E10", events.get(9).eventId());

        assertEquals(5, events.get(6).eventDay());
        assertEquals(2, events.get(6).valueDate());
    }
}