package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.Account;
import com.ledger.account_ledger.domain.EntryType;
import com.ledger.account_ledger.domain.Ledger;
import com.ledger.account_ledger.domain.LedgerEntry;
import com.ledger.account_ledger.domain.Money;

public class LedgerService {

    private final Ledger ledger;

    public LedgerService(Ledger ledger) {
        this.ledger = ledger;
    }

    public void post(LedgerEntry entry) {
        ledger.append(entry);
    }

    public Money closingBalance(Account account, int day) {

        Money balance = account.openingBalance();

        for (LedgerEntry entry : ledger.entries()) {

            if (!entry.accountId().equals(account.accountId())) {
                continue;
            }

            if (entry.valueDate() > day) {
                continue;
            }

            balance = applyEntry(balance, entry);
        }

        return balance;
    }

    private Money applyEntry(Money balance, LedgerEntry entry) {

        return switch (entry.type()) {

            case CREDIT, REVERSAL, INTEREST ->
                    balance.add(entry.amount());

            case DEBIT, FEE ->
                    balance.subtract(entry.amount());
        };
    }
    public java.util.List<LedgerEntry> getEntries() {
        return ledger.entries();
    }
}