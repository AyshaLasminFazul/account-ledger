package com.ledger.account_ledger.domain;

public record LedgerEvent(
        String eventId,
        int eventDay,
        String accountId,
        EventType type,
        Money amount,
        int valueDate,
        String referenceId,
        int installmentCount
) {

    public LedgerEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be empty");
        }

        if (eventDay < 1 || eventDay > 6) {
            throw new IllegalArgumentException(
                    "Event day must be between 1 and 6"
            );
        }

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException(
                    "Account ID cannot be empty"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Event type cannot be null"
            );
        }

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Amount cannot be null"
            );
        }

        if (valueDate < 1 || valueDate > 6) {
            throw new IllegalArgumentException(
                    "Value date must be between Day 1 and Day 6"
            );
        }

        // ADD IT HERE
        if (installmentCount < 1) {
            throw new IllegalArgumentException(
                    "Installment count must be at least 1"
            );
        }
    }

    // Existing 7-argument constructor
    public LedgerEvent(
            String eventId,
            int eventDay,
            String accountId,
            EventType type,
            Money amount,
            int valueDate,
            String referenceId
    ) {
        this(
                eventId,
                eventDay,
                accountId,
                type,
                amount,
                valueDate,
                referenceId,
                1
        );
    }
}