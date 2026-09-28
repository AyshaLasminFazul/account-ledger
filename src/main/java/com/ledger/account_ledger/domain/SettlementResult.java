package com.ledger.account_ledger.domain;

public record SettlementResult(
        boolean accepted,
        String authorizationId,
        String error
) {
}