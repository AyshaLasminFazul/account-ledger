# Work Log

This log records the implementation work, decisions, tests, and validation performed during development.

## 2026-09-28

### [ENTER ACTUAL TIME] — Project setup and domain model

- Created the Gradle/Spring Boot project.
- Defined the core domain model:
    - Account
    - Currency
    - Money
    - LedgerEntry
    - Ledger
    - Authorization
    - LedgerEvent
- Chose `BigDecimal` for monetary calculations.
- Made monetary/domain values immutable where practical.

### [ENTER ACTUAL TIME] — Ledger implementation

- Implemented append-only ledger behavior.
- Implemented value-date based closing balance calculation.
- Kept event arrival day separate from accounting value date.
- Added reversal entries instead of modifying existing entries.

### [ENTER ACTUAL TIME] — Authorization and settlement

- Implemented authorization holds separately from the ledger.
- Implemented available-balance calculation.
- Implemented authorization approval/rejection.
- Implemented settlement against an existing authorization.
- Added rejection for unknown authorization IDs.
- Added support for partial settlement.

### [ENTER ACTUAL TIME] — Overdraft fee

- Implemented the AED 25.00 overdraft fee.
- Tested the late value-dated E7 transaction.
- Confirmed that E7 produces a Day 2 pre-fee balance of AED -370.00.
- Documented the retroactive-fee ambiguity.

### [ENTER ACTUAL TIME] — Installment rounding

- Implemented three-way BHD installment splitting.
- Identified that BHD 10.000 cannot be divided into three identical amounts at three-decimal precision.
- Chose a total-preserving split:
    - 3.334
    - 3.333
    - 3.333
- Documented the decision in `AMBIGUITIES.md` and `REJECTED.md`.

### [ENTER ACTUAL TIME] — Interest calculation

- Implemented 0.04% daily interest.
- Applied interest only to positive closing balances.
- Rounded each daily accrual to currency precision.
- Summed rounded daily accruals before capitalization.
- Implemented a single Day 6 capitalization entry.

### [ENTER ACTUAL TIME] — Duplicate capitalization test

- Added a test to verify that calling interest capitalization twice does not create two interest entries.
- The test was intentionally run before the idempotency guard was implemented.
- The test failed because two interest entries were created instead of one.
- This failure exposed a duplicate-capitalization defect in the initial design.
- Added an idempotency check for the Day 6 `DAILY_INTEREST` entry.
- Re-ran the test successfully after the fix.

### [ENTER ACTUAL TIME] — Assessment replay

- Implemented the runnable assessment event replay.
- Added output for:
    - Event day
    - Value date
    - Authorization state
    - Settlement result
    - Errors
    - Daily balances
    - Overdraft fee
    - Daily interest
    - Interest capitalization
    - Final balances

### [ENTER ACTUAL TIME] — Final validation

- Ran:

```text
./gradlew clean test