package com.ledger.account_ledger.domain;

public record LedgerEntry(
        String entryId,
        String accountId,
        EntryType type,
        Money amount,
        int valueDate,
        String reference
) {

    public LedgerEntry {
        if (entryId == null || entryId.isBlank()) {
            throw new IllegalArgumentException("Entry ID cannot be empty");
        }

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be empty");
        }

        if (type == null) {
            throw new IllegalArgumentException("Entry type cannot be null");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (valueDate < 1 || valueDate > 6) {
            throw new IllegalArgumentException(
                    "Value date must be between Day 1 and Day 6"
            );
        }
    }
}