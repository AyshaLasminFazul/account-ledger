package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.Account;
import com.ledger.account_ledger.domain.EntryType;
import com.ledger.account_ledger.domain.LedgerEntry;
import com.ledger.account_ledger.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InterestService {

    private static final BigDecimal DAILY_RATE =
            new BigDecimal("0.0004");

    private final LedgerService ledgerService;

    public InterestService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public Money calculateDailyInterest(Account account, int day) {

        Money closingBalance =
                ledgerService.closingBalance(account, day);

        if (!closingBalance.isPositive()) {
            return Money.of(
                    "0",
                    account.currency()
            );
        }

        BigDecimal interest =
                closingBalance.amount()
                        .multiply(DAILY_RATE)
                        .setScale(
                                account.currency().getScale(),
                                RoundingMode.HALF_UP
                        );

        return new Money(
                interest,
                account.currency()
        );
    }
    public Money calculateTotalInterest(Account account) {

        Money total = Money.of("0", account.currency());

        for (int day = 1; day <= 6; day++) {

            Money dailyInterest =
                    calculateDailyInterest(account, day);

            total = total.add(dailyInterest);
        }

        return total;
    }

    public void capitalizeInterest(Account account) {
        if (alreadyCapitalized(account)) {
            return;
        }
        Money totalInterest =
                calculateTotalInterest(account);

        if (totalInterest.isZero()) {
            return;
        }

        ledgerService.post(new LedgerEntry(
                "INTEREST-" + account.accountId() + "-D6",
                account.accountId(),
                EntryType.INTEREST,
                totalInterest,
                6,
                "DAILY_INTEREST"
        ));
    }
    private boolean alreadyCapitalized(Account account) {
        return ledgerService.getEntries()
                .stream()
                .anyMatch(entry ->
                        entry.accountId().equals(account.accountId())
                                && entry.type() == EntryType.INTEREST
                                && entry.valueDate() == 6
                                && "DAILY_INTEREST".equals(entry.reference())
                );
    }

}