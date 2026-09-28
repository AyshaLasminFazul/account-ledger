# In-Memory Account Ledger Core

A deterministic, in-memory account ledger implementation for the six-day financial event stream defined in the assessment.

The implementation focuses on:

* Append-only ledger entries
* Value-date based balance calculation
* Overdraft fee assessment
* Authorization holds and settlements
* Reversals
* Currency-specific monetary precision
* Daily interest calculation
* Interest capitalization
* Multi-installment credits
* Deterministic replay
* Runnable automated tests

## Constraints

The implementation intentionally contains:

* No web/API layer
* No database
* No persistence
* No UI
* No external services

All account, authorization, hold, and ledger state exists in memory.

## Technology

* Java 17
* Spring Boot
* Gradle
* JUnit 5
* `BigDecimal` for monetary calculations

Spring Boot is used only as the project/runtime framework. The ledger core itself has no web layer.

## Running the Tests

From the project root:

```text
.\gradlew.bat clean test
```

The test suite validates the core domain, ledger calculations, authorization behavior, fees, interest, installments, and the assessment scenario.

## Running the Assessment Replay

The assessment replay is implemented by:

```text
com.ledger.account_ledger.AssessmentRunner
```

After compiling the project, run:

```text
.\gradlew.bat classes
java -cp build/classes/java/main com.ledger.account_ledger.AssessmentRunner
```

The runner replays the complete six-day event stream and prints:

* Event day and value date
* Closing balances
* Authorization states
* Settlement results
* Errors
* Overdraft fee assessment
* Daily interest
* Interest capitalization
* Final balances

## Domain Structure

### Account

Represents an account with:

* Account ID
* Currency
* Opening balance

### Money

An immutable monetary value object containing:

* `BigDecimal` amount
* Currency

Amounts are normalized to the precision of their currency.

* AED: 2 decimal places
* BHD: 3 decimal places

### Ledger

The ledger is append-only.

Existing ledger entries are never modified or deleted.

A balance for a particular day is derived from:

```text
Opening Balance
+ all ledger entries where value_date <= requested day
```

### Authorization

Authorization holds are maintained separately from ledger entries.

An authorization is approved only when:

```text
ledger balance - active holds >= authorization amount
```

A hold reduces available balance but does not reduce the ledger balance.

Settlements consume the authorization's remaining hold and create a corresponding ledger debit.

### Reversal

A reversal is represented as a new ledger entry.

The original entry is never deleted or modified.

## Event Processing

The event stream contains ten events across six event days.

The implementation distinguishes:

* `eventDay` — when the event arrives
* `valueDate` — the accounting date applied to the ledger

This distinction is important for E7 and E9 because their events arrive on later days while affecting the Day 2 ledger.

## Overdraft Fee

The overdraft fee is:

```text
AED 25.00
```

The assessment scenario causes E7 to make the Day 2 closing ledger balance negative:

```text
1200.00 - 950.00 - 620.00 = -370.00
```

The Day 2 overdraft fee is then booked as an additional ledger entry:

```text
-370.00 - 25.00 = -395.00
```

The fee is not removed when E7 is subsequently reversed because the ledger is append-only. This interpretation and the resulting conflict with acceptance criterion 6 are documented in `AMBIGUITIES.md` and `REJECTED.md`.

## Interest

The daily interest rate is:

```text
0.04% per day
```

or:

```text
0.0004
```

Interest is calculated only on positive closing balances.

Each daily accrual is rounded to the account currency's precision.

The rounded daily accruals are summed and capitalized as a single Day 6 ledger credit.

The implementation prevents duplicate capitalization.

## BHD Installments

E10 credits BHD 10.000 in three installments.

Exact equal division is impossible at three-decimal currency precision:

```text
10.000 / 3 = 3.333333...
```

The implementation preserves the exact total by allocating the rounding remainder to the first installment:

```text
3.334
3.333
3.333
-------
10.000
```

The ambiguity and rejected alternatives are documented in `AMBIGUITIES.md` and `REJEC
