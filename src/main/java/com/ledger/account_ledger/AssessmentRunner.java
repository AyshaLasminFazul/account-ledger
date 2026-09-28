package com.ledger.account_ledger;

import com.ledger.account_ledger.domain.*;
import com.ledger.account_ledger.scenario.AssessmentScenario;
import com.ledger.account_ledger.service.*;

import java.util.List;
import java.util.Map;

public class AssessmentRunner {

    public static void main(String[] args) {

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

        FeeService feeService =
                new FeeService(ledgerService);

        InterestService interestService =
                new InterestService(ledgerService);

        List<LedgerEvent> events =
                AssessmentScenario.events();

        System.out.println("=== ACCOUNT LEDGER ASSESSMENT ===");

        for (LedgerEvent event : events) {

            System.out.println();
            System.out.println(
                    "Processing " + event.eventId()
                            + " | Event Day=" + event.eventDay()
                            + " | Value Day=" + event.valueDate()
            );

            SettlementResult settlementResult =
                    replayEngine.process(event);

            if (settlementResult != null) {
                System.out.println(
                        "Settlement " + settlementResult.authorizationId()
                                + " | accepted=" + settlementResult.accepted()
                                + " | error=" + settlementResult.error()
                );
            }

            if (event.type() == EventType.AUTHORIZATION) {

                Authorization authorization =
                        authorizationService.getAuthorization(
                                event.referenceId()
                        );

                System.out.println(
                        "Authorization " + authorization.authorizationId()
                                + " | status=" + authorization.status()
                                + " | remainingHold="
                                + authorization.remainingHold().amount()
                );
            }
            /*
             * E7 arrives on Day 5 but has value date Day 2.
             * It makes the Day 2 closing balance negative.
             * Assess the Day 2 overdraft fee when E7 arrives.
             */
            if ("E7".equals(event.eventId())) {

                boolean feeAssessed =
                        feeService.assessOverdraftFee(
                                aedAccount,
                                2
                        );

                System.out.println(
                        "Day 2 overdraft fee assessed = "
                                + feeAssessed
                );

                System.out.println(
                        "Day 2 balance after fee = "
                                + ledgerService
                                .closingBalance(aedAccount, 2)
                                .amount()
                );
            }

            /*
             * E9 reverses E7.
             * The reversal is appended; E7 itself is not removed.
             */
            if ("E9".equals(event.eventId())) {

                System.out.println(
                        "Day 2 balance after E9 reversal = "
                                + ledgerService
                                .closingBalance(aedAccount, 2)
                                .amount()
                );
            }

            printCurrentBalances(
                    ledgerService,
                    aedAccount,
                    bhdAccount,
                    event.eventDay()
            );
        }

        System.out.println();
        System.out.println("=== DAILY INTEREST ===");

        for (int day = 1; day <= 6; day++) {

            Money aedInterest =
                    interestService.calculateDailyInterest(
                            aedAccount,
                            day
                    );

            Money bhdInterest =
                    interestService.calculateDailyInterest(
                            bhdAccount,
                            day
                    );

            System.out.println(
                    "Day " + day
                            + " | AED interest="
                            + aedInterest.amount()
                            + " | BHD interest="
                            + bhdInterest.amount()
            );
        }

        System.out.println();
        System.out.println("=== INTEREST CAPITALIZATION ===");

        interestService.capitalizeInterest(aedAccount);
        interestService.capitalizeInterest(bhdAccount);

        System.out.println(
                "AED capitalized interest = "
                        + ledger.entries()
                        .stream()
                        .filter(entry ->
                                entry.accountId().equals("ACC-001")
                                        && entry.type() == EntryType.INTEREST)
                        .findFirst()
                        .orElseThrow()
                        .amount()
                        .amount()
        );

        System.out.println(
                "BHD capitalized interest = "
                        + ledger.entries()
                        .stream()
                        .filter(entry ->
                                entry.accountId().equals("ACC-002")
                                        && entry.type() == EntryType.INTEREST)
                        .findFirst()
                        .orElseThrow()
                        .amount()
                        .amount()
        );

        System.out.println();
        System.out.println("=== FINAL BALANCES ===");

        System.out.println(
                "ACC-001 AED = "
                        + ledgerService
                        .closingBalance(aedAccount, 6)
                        .amount()
        );

        System.out.println(
                "ACC-002 BHD = "
                        + ledgerService
                        .closingBalance(bhdAccount, 6)
                        .amount()
        );
    }

    private static void printCurrentBalances(
            LedgerService ledgerService,
            Account aedAccount,
            Account bhdAccount,
            int day
    ) {

        System.out.println(
                "Balance through Day " + day
                        + " | AED="
                        + ledgerService
                        .closingBalance(aedAccount, day)
                        .amount()
                        + " | BHD="
                        + ledgerService
                        .closingBalance(bhdAccount, day)
                        .amount()
        );
    }
}