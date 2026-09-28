package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.*;

import java.util.HashSet;
import java.util.Set;

public class FeeService {

    private static final Money AED_OVERDRAFT_FEE =
            Money.of("25.00", Currency.AED);

    private final LedgerService ledgerService;

    private final Set<String> assessedFees = new HashSet<>();

    public FeeService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public boolean assessOverdraftFee(Account account, int day) {

        String feeKey = account.accountId() + "-" + day;

        if (assessedFees.contains(feeKey)) {
            return false;
        }

        Money closingBalance =
                ledgerService.closingBalance(account, day);

        if (!closingBalance.isNegative()) {
            return false;
        }

        ledgerService.post(new LedgerEntry(
                "FEE-" + account.accountId() + "-D" + day,
                account.accountId(),
                EntryType.FEE,
                AED_OVERDRAFT_FEE,
                day,
                "OVERDRAFT"
        ));

        assessedFees.add(feeKey);

        return true;
    }
}