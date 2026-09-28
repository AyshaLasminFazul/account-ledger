package com.ledger.account_ledger.domain;

public record Account(
        String accountId,
        Currency currency,
        Money openingBalance
) {

    public Account {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be empty");
        }

        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        if (openingBalance == null) {
            throw new IllegalArgumentException("Opening balance cannot be null");
        }

        if (openingBalance.currency() != currency) {
            throw new IllegalArgumentException(
                    "Opening balance currency must match account currency"
            );
        }
    }
}