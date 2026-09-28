package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;
import com.ledger.account_ledger.scenario.AssessmentScenario;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReplayEngineTest {

    @Test
    void shouldReplayAssessmentEventsInOrder() {

        Account aedAccount = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );

        Account bhdAccount = new Account(
                "ACC-002",
                Currency.BHD,
                Money.of("0.000", Currency.BHD)
        );

        Ledger ledger = new Ledger();
        LedgerService ledgerService = new LedgerService(ledger);

        AuthorizationService authorizationService =
                new AuthorizationService(ledgerService);

        InstallmentService installmentService =
                new InstallmentService();

        ReplayEngine replayEngine =
                new ReplayEngine(
                        ledgerService,
                        authorizationService,
                        Map.of(
                                "ACC-001", aedAccount,
                                "ACC-002", bhdAccount
                        ),
                        installmentService
                );

        List<LedgerEvent> events =
                AssessmentScenario.events();

        for (LedgerEvent event : events) {

            replayEngine.process(event);

            if (event.eventId().equals("E7")) {

                assertEquals(
                        Money.of("-370.00", Currency.AED),
                        ledgerService.closingBalance(
                                aedAccount,
                                2
                        )
                );
            }
        }

        // E9 reverses E7.
        assertEquals(
                Money.of("250.00", Currency.AED),
                ledgerService.closingBalance(
                        aedAccount,
                        2
                )
        );

        // E10 is split into three BHD instalments.
        assertEquals(
                Money.of("10.000", Currency.BHD),
                ledgerService.closingBalance(
                        bhdAccount,
                        5
                )
        );

        // Verify that E10 created three ledger entries.
        List<LedgerEntry> bhdEntries =
                ledger.entries()
                        .stream()
                        .filter(entry ->
                                entry.accountId().equals("ACC-002"))
                        .toList();

        assertEquals(3, bhdEntries.size());

        assertEquals(
                Money.of("3.334", Currency.BHD),
                bhdEntries.get(0).amount()
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                bhdEntries.get(1).amount()
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                bhdEntries.get(2).amount()
        );
    }

    @Test
    void shouldProcessBhdInstallments() {

        Account account = new Account(
                "ACC-002",
                Currency.BHD,
                Money.of("0.000", Currency.BHD)
        );

        Ledger ledger = new Ledger();
        LedgerService ledgerService =
                new LedgerService(ledger);

        AuthorizationService authorizationService =
                new AuthorizationService(ledgerService);

        InstallmentService installmentService =
                new InstallmentService();

        ReplayEngine replayEngine =
                new ReplayEngine(
                        ledgerService,
                        authorizationService,
                        Map.of("ACC-002", account),
                        installmentService
                );

        LedgerEvent event =
                new LedgerEvent(
                        "E10",
                        5,
                        "ACC-002",
                        EventType.CREDIT,
                        Money.of("10.000", Currency.BHD),
                        5,
                        null,
                        3
                );

        replayEngine.process(event);

        assertEquals(
                Money.of("10.000", Currency.BHD),
                ledgerService.closingBalance(
                        account,
                        5
                )
        );

        assertEquals(
                3,
                ledger.entries().size()
        );

        assertEquals(
                Money.of("3.334", Currency.BHD),
                ledger.entries().get(0).amount()
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                ledger.entries().get(1).amount()
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                ledger.entries().get(2).amount()
        );
    }
}