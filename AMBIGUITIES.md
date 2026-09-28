# Ambiguities and Resolutions

This document records requirements that are ambiguous, incomplete, or potentially contradictory, together with the interpretation used by the implementation.

## 1. Event Day vs Value Date

### Ambiguity

The event stream contains both an event day and a value date.

Most events use the same day, but E7 is different:

- Event day: Day 5
- Value date: Day 2

### Resolution

The implementation treats these as separate concepts.

- `eventDay` determines when the event arrives during replay.
- `valueDate` determines which historical closing balances are affected.

Therefore E7 is processed on Day 5 but changes the Day 2 ledger balance.

This is necessary to satisfy the explicit acceptance criterion that the Day 2 balance after E7, before the fee, is AED -370.00.

---

## 2. Overdraft Fee and Retroactive Value Dates

### Ambiguity

E7 arrives on Day 5 but has a Day 2 value date.

After E7 is applied retrospectively, the account can have negative balances for multiple later days.

The requirement says:

> assess the overdraft fee once per day per account when that day's closing ledger balance is negative.

However, the scenario explicitly identifies E7 as causing the Day 2 overdraft fee and does not clearly define whether historical days should be re-assessed whenever a late event changes their balance.

### Resolution

The implementation assesses the Day 2 fee when E7 arrives because E7 is the event that introduces the negative Day 2 balance.

The scenario therefore produces exactly one fee for the specified E7 condition.

The implementation does not automatically generate additional historical fees for every later negative day caused by the same late event.

This avoids generating additional fees that are not explicitly demonstrated by the supplied scenario.

---

## 3. Reversal vs Previously Assessed Fee

### Ambiguity

E7 creates a Day 2 overdraft and causes a Day 2 fee.

E9 later reverses E7.

The acceptance criterion says that after E9:

> all balances and fees return to pre-E7 values.

At the same time, the ledger is required to be append-only, meaning entries cannot be deleted or mutated.

If the Day 2 fee is already a ledger entry, reversing only E7 cannot remove that fee.

### Resolution

The implementation preserves the overdraft fee.

E9 adds a new reversal entry for E7 rather than modifying or deleting E7.

Therefore the underlying E7 debit is economically reversed, but the previously assessed fee remains in the append-only ledger.

This preserves the append-only requirement.

The apparent conflict between the fee reversal criterion and append-only ledger semantics is documented in `REJECTED.md`.

---

## 4. Equal BHD Installments

### Ambiguity

E10 requires BHD 10.000 to be posted as three equal installments.

BHD supports only three decimal places.

An exact division gives:

```text
10.000 / 3 = 3.333333...