package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.Account;
import com.ledger.account_ledger.domain.Currency;
import com.ledger.account_ledger.domain.EntryType;
import com.ledger.account_ledger.domain.Ledger;
import com.ledger.account_ledger.domain.LedgerEntry;
import com.ledger.account_ledger.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerServiceTest {
    private Ledger ledger;
    private LedgerService ledgerService;
    private Account account;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledgerService = new LedgerService(ledger);

        account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );
    }
    @Test
    void shouldCalculateClosingBalanceBasedOnValueDate() {

        Account account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );

        Ledger ledger = new Ledger();
        LedgerService ledgerService = new LedgerService(ledger);

        ledgerService.post(new LedgerEntry(
                "E1",
                "ACC-001",
                EntryType.CREDIT,
                Money.of("1200.00", Currency.AED),
                1,
                null
        ));

        ledgerService.post(new LedgerEntry(
                "E2",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("950.00", Currency.AED),
                1,
                null
        ));

        ledgerService.post(new LedgerEntry(
                "E7",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("620.00", Currency.AED),
                2,
                null
        ));

        Money day2Balance =
                ledgerService.closingBalance(account, 2);

        assertEquals(
                Money.of("-370.00", Currency.AED),
                day2Balance
        );
    }

    @Test
    void shouldReverseDebitAndRestoreBalance() {

        Account account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );

        Ledger ledger = new Ledger();
        LedgerService ledgerService = new LedgerService(ledger);

        // E1
        ledgerService.post(new LedgerEntry(
                "E1",
                "ACC-001",
                EntryType.CREDIT,
                Money.of("1200.00", Currency.AED),
                1,
                null
        ));

        // E2
        ledgerService.post(new LedgerEntry(
                "E2",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("950.00", Currency.AED),
                1,
                null
        ));

        // E7 - late debit, but value dated Day 2
        ledgerService.post(new LedgerEntry(
                "E7",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("620.00", Currency.AED),
                2,
                null
        ));

        assertEquals(
                Money.of("-370.00", Currency.AED),
                ledgerService.closingBalance(account, 2)
        );

        // E9 - reversal of E7
        ledgerService.post(new LedgerEntry(
                "E9",
                "ACC-001",
                EntryType.REVERSAL,
                Money.of("620.00", Currency.AED),
                2,
                "E7"
        ));

        assertEquals(
                Money.of("250.00", Currency.AED),
                ledgerService.closingBalance(account, 2)
        );
    }

    @Test
    void shouldRoundBhdAmountToThreeDecimalPlaces() {

        Money instalment = Money.of("3.333333", Currency.BHD);

        assertEquals(
                Money.of("3.333", Currency.BHD),
                instalment
        );
    }
    @Test
    void shouldReverseE7WithoutRemovingOriginalLedgerEntry() {

        ledgerService.post(new LedgerEntry(
                "E1",
                "ACC-001",
                EntryType.CREDIT,
                Money.of("1200.00", Currency.AED),
                1,
                null
        ));

        ledgerService.post(new LedgerEntry(
                "E2",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("950.00", Currency.AED),
                1,
                null
        ));

        ledgerService.post(new LedgerEntry(
                "E7",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("620.00", Currency.AED),
                2,
                null
        ));

        assertEquals(
                new BigDecimal("-370.00"),
                ledgerService
                        .closingBalance(account, 2)
                        .amount()
        );

        int entriesBeforeReversal =
                ledger.entries().size();

        ledgerService.post(new LedgerEntry(
                "E9",
                "ACC-001",
                EntryType.REVERSAL,
                Money.of("620.00", Currency.AED),
                2,
                "E7"
        ));

        assertEquals(
                new BigDecimal("250.00"),
                ledgerService
                        .closingBalance(account, 2)
                        .amount()
        );

        // Reversal must be appended, not replace/delete E7.
        assertEquals(
                entriesBeforeReversal + 1,
                ledger.entries().size()
        );

        assertTrue(
                ledger.entries().stream()
                        .anyMatch(entry ->
                                entry.entryId().equals("E7")
                                        && entry.type() == EntryType.DEBIT)
        );

        assertTrue(
                ledger.entries().stream()
                        .anyMatch(entry ->
                                entry.entryId().equals("E9")
                                        && entry.type() == EntryType.REVERSAL)
        );
    }
}