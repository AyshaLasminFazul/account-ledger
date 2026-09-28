package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.Currency;
import com.ledger.account_ledger.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class InstallmentService {

    public List<Money> splitIntoThree(Money total) {

        if (total == null) {
            throw new IllegalArgumentException("Total cannot be null");
        }

        BigDecimal baseAmount =
                total.amount()
                        .divide(
                                BigDecimal.valueOf(3),
                                total.currency().getScale(),
                                RoundingMode.DOWN
                        );

        Money second = new Money(
                baseAmount,
                total.currency()
        );

        Money third = new Money(
                baseAmount,
                total.currency()
        );

        Money first = total
                .subtract(second)
                .subtract(third);

        return List.of(first, second, third);
    }
}