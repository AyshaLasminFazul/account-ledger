package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FeeServiceTest {

    private Ledger ledger;
    private LedgerService ledgerService;
    private FeeService feeService;
    private Account account;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledgerService = new LedgerService(ledger);
        feeService = new FeeService(ledgerService);

        account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );
    }

    @Test
    void shouldAssessOverdraftFeeWhenClosingBalanceIsNegative() {

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

        FeeService feeService = new FeeService(ledgerService);

        boolean assessed =
                feeService.assessOverdraftFee(account, 2);

        assertTrue(assessed);

        assertEquals(
                Money.of("-395.00", Currency.AED),
                ledgerService.closingBalance(account, 2)
        );
    }
    @Test
    void shouldAssessOverdraftFeeOnlyOncePerDay() {

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

        FeeService feeService = new FeeService(ledgerService);

        assertTrue(feeService.assessOverdraftFee(account, 2));

        assertFalse(feeService.assessOverdraftFee(account, 2));

        assertEquals(
                Money.of("-395.00", Currency.AED),
                ledgerService.closingBalance(account, 2)
        );
    }

    @Test
    void shouldAssessOverdraftFeeWhenLateValueDatedDebitMakesDay2Negative() {

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

        // E7 arrives on Day 5 but has value date Day 2.
        ledgerService.post(new LedgerEntry(
                "E7",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("620.00", Currency.AED),
                2,
                null
        ));

        // Acceptance criterion: Day 2 before fee = -370.
        assertEquals(
                new BigDecimal("-370.00"),
                ledgerService
                        .closingBalance(account, 2)
                        .amount()
        );

        assertTrue(
                feeService.assessOverdraftFee(account, 2)
        );

        // Fee is a ledger entry, so balance becomes -395.
        assertEquals(
                new BigDecimal("-395.00"),
                ledgerService
                        .closingBalance(account, 2)
                        .amount()
        );
    }
}