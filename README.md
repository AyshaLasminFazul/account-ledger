# In-Memory Account Ledger Core

A small in-memory account ledger implementation for processing a six-day financial event stream.

The implementation focuses on:

- Append-only ledger entries
- Value-date based balance calculation
- Overdraft fee assessment
- Authorization holds and settlements
- Reversals
- Currency-specific precision
- Daily interest calculation
- Interest capitalization
- Multi-installment credits
- Deterministic replay and testability

## Constraints

The implementation intentionally does not contain:

- Web/API endpoints
- Database or persistence layer
- UI
- External services

All account and ledger state exists in memory.

## Technology

- Java 17
- Spring Boot
- Gradle
- JUnit 5
- BigDecimal for monetary calculations

## Domain Structure

### Account

Represents an account with:

- Account ID
- Currency
- Opening balance

### Money

An immutable value object containing:

- BigDecimal amount
- Currency

Amounts are normalized to the precision of their currency.

- AED: 2 decimal places
- BHD: 3 decimal places

### Ledger

The ledger is append-only.

Existing ledger entries are never modified or deleted.

A balance for a particular day is derived from:

```text
Opening Balance
+ all ledger entries where value_date <= requested day