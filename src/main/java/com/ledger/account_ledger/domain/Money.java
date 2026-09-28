package com.ledger.account_ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        amount = amount.setScale(currency.getScale(), RoundingMode.HALF_UP);
    }

    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public Money add(Money other) {
        validateCurrency(other);

        return new Money(
                amount.add(other.amount),
                currency
        );
    }

    public Money subtract(Money other) {
        validateCurrency(other);

        return new Money(
                amount.subtract(other.amount),
                currency
        );
    }

    public Money negate() {
        return new Money(
                amount.negate(),
                currency
        );
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    private void validateCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Money cannot be null");
        }

        if (currency != other.currency) {
            throw new IllegalArgumentException(
                    "Cannot operate on different currencies"
            );
        }
    }
}