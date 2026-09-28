package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterestServiceTest {

    private Ledger ledger;
    private LedgerService ledgerService;
    private InterestService interestService;
    private Account account;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledgerService = new LedgerService(ledger);
        interestService = new InterestService(ledgerService);

        account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );
    }

    @Test
    void shouldCalculateDailyInterestForPositiveBalance() {

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

        Money interest =
                interestService.calculateDailyInterest(
                        account,
                        1
                );

        assertEquals(
                Money.of("0.10", Currency.AED),
                interest
        );
    }

    @Test
    void shouldReturnZeroInterestForNegativeBalance() {

        ledgerService.post(new LedgerEntry(
                "E1",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("100.00", Currency.AED),
                1,
                null
        ));

        Money interest =
                interestService.calculateDailyInterest(
                        account,
                        1
                );

        assertEquals(
                Money.of("0.00", Currency.AED),
                interest
        );
    }

    @Test
    void shouldSumRoundedDailyInterestBeforeCapitalization() {

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

        Money day1 =
                interestService.calculateDailyInterest(
                        account,
                        1
                );

        Money day2 =
                interestService.calculateDailyInterest(
                        account,
                        2
                );

        assertEquals(
                Money.of("0.10", Currency.AED),
                day1
        );

        assertEquals(
                Money.of("0.10", Currency.AED),
                day2
        );

        Money total =
                interestService.calculateTotalInterest(account);

        assertEquals(
                Money.of("0.60", Currency.AED),
                total
        );
    }

    @Test
    void shouldCapitalizeExactlyCalculatedInterest() {

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

        Money expectedInterest =
                interestService.calculateTotalInterest(account);

        interestService.capitalizeInterest(account);

        LedgerEntry interestEntry =
                ledger.entries()
                        .stream()
                        .filter(entry ->
                                entry.type() == EntryType.INTEREST)
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                expectedInterest,
                interestEntry.amount()
        );

        assertEquals(
                Money.of("250.60", Currency.AED),
                ledgerService.closingBalance(account, 6)
        );
    }
    @Test
    void shouldNotCapitalizeInterestMoreThanOnce() {

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

        interestService.capitalizeInterest(account);
        interestService.capitalizeInterest(account);

        long interestEntryCount =
                ledger.entries()
                        .stream()
                        .filter(entry ->
                                entry.type() == EntryType.INTEREST)
                        .count();

        assertEquals(1, interestEntryCount);
    }
}
