package com.ledger.account_ledger.domain;

import java.util.ArrayList;
import java.util.List;

public class Ledger {

    private final List<LedgerEntry> entries = new ArrayList<>();

    public void append(LedgerEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Ledger entry cannot be null");
        }

        entries.add(entry);
    }

    public List<LedgerEntry> entries() {
        return List.copyOf(entries);
    }
}