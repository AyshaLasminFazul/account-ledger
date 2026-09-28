package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationService {

    private final LedgerService ledgerService;
    private final Map<String, Authorization> authorizations = new HashMap<>();

    public AuthorizationService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public Authorization authorize(
            Account account,
            String authorizationId,
            Money hold,
            int day) {

        Money ledgerBalance =
                ledgerService.closingBalance(account, day);

        Money availableBalance =
                ledgerBalance.subtract(activeHolds(account));

        Money availableAfterHold =
                availableBalance.subtract(hold);

        if (availableAfterHold.isNegative()) {

            Authorization rejected = new Authorization(
                    authorizationId,
                    account.accountId(),
                    hold,
                    Money.of("0", account.currency()),
                    AuthorizationStatus.REJECTED
            );

            authorizations.put(authorizationId, rejected);

            return rejected;
        }

        Authorization approved = new Authorization(
                authorizationId,
                account.accountId(),
                hold,
                hold,
                AuthorizationStatus.APPROVED
        );

        authorizations.put(authorizationId, approved);

        return approved;
    }

    private Money activeHolds(Account account) {

        Money total = Money.of("0", account.currency());

        for (Authorization authorization : authorizations.values()) {

            if (!authorization.accountId().equals(account.accountId())) {
                continue;
            }

            if (authorization.status() != AuthorizationStatus.APPROVED
                    && authorization.status() != AuthorizationStatus.PARTIALLY_SETTLED) {
                continue;
            }

            total = total.add(authorization.remainingHold());
        }

        return total;
    }
    public SettlementResult settle(
            Account account,
            String authorizationId,
            Money settlementAmount,
            String entryId,
            int valueDate) {

        Authorization authorization =
                authorizations.get(authorizationId);

        if (authorization == null) {
            return new SettlementResult(
                    false,
                    authorizationId,
                    "Authorization not found"
            );
        }

        if (authorization.status() == AuthorizationStatus.REJECTED) {
            return new SettlementResult(
                    false,
                    authorizationId,
                    "Authorization was rejected"
            );
        }

        if (settlementAmount.amount()
                .compareTo(authorization.remainingHold().amount()) > 0) {

            return new SettlementResult(
                    false,
                    authorizationId,
                    "Settlement exceeds remaining hold"
            );
        }

        // Create the actual accounting entry.
        ledgerService.post(new LedgerEntry(
                entryId,
                account.accountId(),
                EntryType.DEBIT,
                settlementAmount,
                valueDate,
                authorizationId
        ));

        Money remainingHold =
                authorization.remainingHold()
                        .subtract(settlementAmount);

        AuthorizationStatus newStatus =
                remainingHold.isZero()
                        ? AuthorizationStatus.SETTLED
                        : AuthorizationStatus.PARTIALLY_SETTLED;

        authorizations.put(
                authorizationId,
                new Authorization(
                        authorization.authorizationId(),
                        authorization.accountId(),
                        authorization.originalHold(),
                        remainingHold,
                        newStatus
                )
        );

        return new SettlementResult(
                true,
                authorizationId,
                null
        );
    }
    public Authorization getAuthorization(String authorizationId) {
        return authorizations.get(authorizationId);
    }
}