package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServiceTest {

    private Ledger ledger;
    private LedgerService ledgerService;
    private AuthorizationService authorizationService;
    private Account account;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledgerService = new LedgerService(ledger);
        authorizationService = new AuthorizationService(ledgerService);

        account = new Account(
                "ACC-001",
                Currency.AED,
                Money.of("0.00", Currency.AED)
        );
    }

    @Test
    void shouldApproveAuthorizationWhenAvailableBalanceIsSufficient() {

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

        AuthorizationService authorizationService =
                new AuthorizationService(ledgerService);

        Authorization authorization =
                authorizationService.authorize(
                        account,
                        "Auth-A",
                        Money.of("200.00", Currency.AED),
                        2
                );

        assertEquals(
                AuthorizationStatus.APPROVED,
                authorization.status()
        );

        assertEquals(
                Money.of("200.00", Currency.AED),
                authorization.remainingHold()
        );
    }

    @Test
    void shouldRejectAuthorizationWhenHoldExceedsAvailableBalance() {

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

        AuthorizationService authorizationService =
                new AuthorizationService(ledgerService);

        Authorization authorization =
                authorizationService.authorize(
                        account,
                        "Auth-B",
                        Money.of("300.00", Currency.AED),
                        2
                );

        assertEquals(
                AuthorizationStatus.REJECTED,
                authorization.status()
        );

        assertEquals(
                Money.of("0.00", Currency.AED),
                authorization.remainingHold()
        );
    }

    @Test
    void shouldAcceptSettlementForExistingAuthorization() {

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
                "E4",
                "ACC-001",
                EntryType.CREDIT,
                Money.of("400.00", Currency.AED),
                3,
                null
        ));
        AuthorizationService authorizationService =
                new AuthorizationService(ledgerService);

        authorizationService.authorize(
                account,
                "Auth-A",
                Money.of("200.00", Currency.AED),
                2
        );

        SettlementResult result =
                authorizationService.settle(
                        account,
                        "Auth-A",
                        Money.of("185.00", Currency.AED),
                        "E5",
                        4
                );

        assertEquals(true, result.accepted());

        assertEquals(
                Money.of("465.00", Currency.AED),
                ledgerService.closingBalance(account, 4)
        );
    }

    @Test
    void shouldRejectSettlementWhenAuthorizationDoesNotExist() {

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

        AuthorizationService authorizationService =
                new AuthorizationService(ledgerService);

        SettlementResult result =
                authorizationService.settle(
                        account,
                        "Auth-Z",
                        Money.of("180.00", Currency.AED),
                        "E6",
                        4
                );

        assertEquals(false, result.accepted());

        assertEquals(
                "Authorization not found",
                result.error()
        );

        assertEquals(
                Money.of("1200.00", Currency.AED),
                ledgerService.closingBalance(account, 4)
        );
    }

    @Test
    void shouldRejectAuthBWhenAvailableBalanceIsNegative() {

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

        // E7 arrives later but has value date Day 2.
        ledgerService.post(new LedgerEntry(
                "E7",
                "ACC-001",
                EntryType.DEBIT,
                Money.of("620.00", Currency.AED),
                2,
                null
        ));

        // Day 5 ledger balance is negative.
        Money ledgerBalance =
                ledgerService.closingBalance(account, 5);

        assertEquals(
                new BigDecimal("-370.00"),
                ledgerBalance.amount()
        );

        Authorization authorization =
                authorizationService.authorize(
                        account,
                        "Auth-B",
                        Money.of("90.00", Currency.AED),
                        5
                );

        assertEquals(
                AuthorizationStatus.REJECTED,
                authorization.status()
        );

        assertEquals(
                Money.of("0.00", Currency.AED),
                authorization.remainingHold()
        );

        // Authorization must not change the ledger balance.
        assertEquals(
                new BigDecimal("-370.00"),
                ledgerService
                        .closingBalance(account, 5)
                        .amount()
        );
    }

    @Test
    void shouldPartiallySettleAuthAAndKeepRemainingHold() {

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

        Authorization authorization =
                authorizationService.authorize(
                        account,
                        "Auth-A",
                        Money.of("200.00", Currency.AED),
                        2
                );

        assertEquals(
                AuthorizationStatus.APPROVED,
                authorization.status()
        );

        assertEquals(
                Money.of("200.00", Currency.AED),
                authorization.remainingHold()
        );

        // Hold does not reduce ledger balance.
        assertEquals(
                new BigDecimal("250.00"),
                ledgerService
                        .closingBalance(account, 2)
                        .amount()
        );

        SettlementResult result =
                authorizationService.settle(
                        account,
                        "Auth-A",
                        Money.of("185.00", Currency.AED),
                        "E5",
                        4
                );

        assertTrue(result.accepted());

        // 200 - 185 = 15 remaining hold.
        Authorization updatedAuthorization =
                authorizationService.getAuthorization("Auth-A");

        assertEquals(
                AuthorizationStatus.PARTIALLY_SETTLED,
                updatedAuthorization.status()
        );

        assertEquals(
                Money.of("15.00", Currency.AED),
                updatedAuthorization.remainingHold()
        );

        // Settlement is a debit:
        // 250 + 400 - 185 = 465
        ledgerService.post(new LedgerEntry(
                "E4",
                "ACC-001",
                EntryType.CREDIT,
                Money.of("400.00", Currency.AED),
                3,
                null
        ));

        assertEquals(
                new BigDecimal("465.00"),
                ledgerService
                        .closingBalance(account, 4)
                        .amount()
        );
    }

    @Test
    void shouldRejectSettlementForUnknownAuthorization() {

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

        SettlementResult result =
                authorizationService.settle(
                        account,
                        "Auth-Z",
                        Money.of("180.00", Currency.AED),
                        "E6",
                        4
                );

        assertFalse(result.accepted());

        assertEquals(
                "Authorization not found",
                result.error()
        );
        // No debit must be added to the ledger.
        assertEquals(
                new BigDecimal("250.00"),
                ledgerService
                        .closingBalance(account, 4)
                        .amount()
        );
    }
}