package com.ledger.account_ledger.domain;

public record Authorization(
        String authorizationId,
        String accountId,
        Money originalHold,
        Money remainingHold,
        AuthorizationStatus status
) {
}