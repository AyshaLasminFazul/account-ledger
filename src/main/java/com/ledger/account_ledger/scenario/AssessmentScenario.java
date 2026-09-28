package com.ledger.account_ledger.scenario;

import com.ledger.account_ledger.domain.*;

import java.util.List;

public class AssessmentScenario {

    public static List<LedgerEvent> events() {

        return List.of(

                // E1 - Day 1
                new LedgerEvent(
                        "E1",
                        1,
                        "ACC-001",
                        EventType.CREDIT,
                        Money.of("1200.00", Currency.AED),
                        1,
                        null
                ),

                // E2 - Day 1
                new LedgerEvent(
                        "E2",
                        1,
                        "ACC-001",
                        EventType.DEBIT,
                        Money.of("950.00", Currency.AED),
                        1,
                        null
                ),

                // E3 - Day 2
                new LedgerEvent(
                        "E3",
                        2,
                        "ACC-001",
                        EventType.AUTHORIZATION,
                        Money.of("200.00", Currency.AED),
                        2,
                        "Auth-A"
                ),

                // E4 - Day 3
                new LedgerEvent(
                        "E4",
                        3,
                        "ACC-001",
                        EventType.CREDIT,
                        Money.of("400.00", Currency.AED),
                        3,
                        null
                ),

                // E5 - Day 4
                new LedgerEvent(
                        "E5",
                        4,
                        "ACC-001",
                        EventType.SETTLEMENT,
                        Money.of("185.00", Currency.AED),
                        4,
                        "Auth-A"
                ),

                // E6 - Day 4
                new LedgerEvent(
                        "E6",
                        4,
                        "ACC-001",
                        EventType.SETTLEMENT,
                        Money.of("180.00", Currency.AED),
                        4,
                        "Auth-Z"
                ),

                // E7 - Day 5, but value dated Day 2
                new LedgerEvent(
                        "E7",
                        5,
                        "ACC-001",
                        EventType.DEBIT,
                        Money.of("620.00", Currency.AED),
                        2,
                        null
                ),

                // E8 - Day 5
                new LedgerEvent(
                        "E8",
                        5,
                        "ACC-001",
                        EventType.AUTHORIZATION,
                        Money.of("90.00", Currency.AED),
                        5,
                        "Auth-B"
                ),

                // E9 - Day 6, reverses E7
                new LedgerEvent(
                        "E9",
                        6,
                        "ACC-001",
                        EventType.REVERSAL,
                        Money.of("620.00", Currency.AED),
                        2,
                        "E7"
                ),

                // E10 - Day 5
                new LedgerEvent(
                        "E10",
                        5,
                        "ACC-002",
                        EventType.CREDIT,
                        Money.of("10.000", Currency.BHD),
                        5,
                        null,
                        3
                )
        );
    }
}