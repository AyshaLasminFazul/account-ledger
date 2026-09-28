package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;

import java.util.List;
import java.util.Map;

public class ReplayEngine {

    private final LedgerService ledgerService;
    private final AuthorizationService authorizationService;
    private final Map<String, Account> accounts;
    private final InstallmentService installmentService;

    public ReplayEngine(
            LedgerService ledgerService,
            AuthorizationService authorizationService,
            Map<String, Account> accounts,
            InstallmentService installmentService) {

        this.ledgerService = ledgerService;
        this.authorizationService = authorizationService;
        this.accounts = accounts;
        this.installmentService = installmentService;
    }

    public SettlementResult process(LedgerEvent event) {

        Account account = accounts.get(event.accountId());

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account not found: " + event.accountId()
            );
        }

        switch (event.type()) {

            case CREDIT -> {

                if (event.installmentCount() == 1) {

                    ledgerService.post(
                            new LedgerEntry(
                                    event.eventId(),
                                    event.accountId(),
                                    EntryType.CREDIT,
                                    event.amount(),
                                    event.valueDate(),
                                    event.referenceId()
                            )
                    );

                } else {

                    List<Money> installments =
                            installmentService.splitIntoThree(event.amount());

                    for (int i = 0; i < installments.size(); i++) {

                        ledgerService.post(
                                new LedgerEntry(
                                        event.eventId() + "-INST-" + (i + 1),
                                        event.accountId(),
                                        EntryType.CREDIT,
                                        installments.get(i),
                                        event.valueDate(),
                                        event.referenceId()
                                )
                        );
                    }
                }
            }

            case DEBIT -> ledgerService.post(
                    new LedgerEntry(
                            event.eventId(),
                            event.accountId(),
                            EntryType.DEBIT,
                            event.amount(),
                            event.valueDate(),
                            event.referenceId()
                    )
            );

            case AUTHORIZATION -> authorizationService.authorize(
                    account,
                    event.referenceId(),
                    event.amount(),
                    event.valueDate()
            );

            case SETTLEMENT -> {
                return authorizationService.settle(
                        account,
                        event.referenceId(),
                        event.amount(),
                        event.eventId(),
                        event.valueDate()
                );
            }

            case REVERSAL -> ledgerService.post(
                    new LedgerEntry(
                            event.eventId(),
                            event.accountId(),
                            EntryType.REVERSAL,
                            event.amount(),
                            event.valueDate(),
                            event.referenceId()
                    )
            );
        }
        return null;
    }
}