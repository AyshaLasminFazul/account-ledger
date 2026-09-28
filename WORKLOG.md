# Work Log

This log records the implementation work, decisions, tests, and validation performed during development.

## 2026-09-28

### 10:47:44 +0400 — Project implementation and domain model

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
- Implemented append-only ledger behavior.
- Implemented value-date based closing balance calculation.
- Kept event arrival day separate from accounting value date.
- Added reversal entries instead of modifying existing entries.
- Implemented authorization holds separately from the ledger.
- Implemented available-balance calculation.
- Implemented authorization approval/rejection.
- Implemented settlement against an existing authorization.
- Added rejection for unknown authorization IDs.
- Added support for partial settlement.
- Implemented the AED 25.00 overdraft fee.
- Implemented three-way BHD installment splitting.
- Implemented 0.04% daily interest with currency-specific rounding.
- Implemented single Day 6 interest capitalization.
- Implemented the runnable assessment replay.
- Added automated tests covering the core ledger, authorization, settlement, fee, installment, and interest behavior.

### Design decisions validated during implementation

- E7 is processed on Day 5 but uses value date Day 2.
- E7 produces a Day 2 pre-fee balance of AED -370.00.
- The Day 2 overdraft fee is assessed once for the E7 scenario.
- Auth-A is approved and partially settled by E5.
- Unknown Auth-Z settlement is rejected without creating a ledger debit.
- Auth-B is rejected because the ledger balance is negative when authorization is evaluated.
- E9 reverses E7 through an appended reversal entry rather than modifying E7.
- BHD 10.000 is split as 3.334 + 3.333 + 3.333 to preserve the exact total at three-decimal currency precision.
- Daily interest is calculated from rounded daily accruals and capitalized once on Day 6.

### Duplicate capitalization test

- Added a test to verify that calling interest capitalization twice does not create two interest entries.
- The test was initially run before the idempotency guard was implemented.
- The failure exposed duplicate capitalization in the initial design.
- Added the idempotency check for the Day 6 `DAILY_INTEREST` entry.
- Re-ran the test successfully after the fix.

### Assessment replay

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

### 11:13:24 +0400 — README and submission documentation

- Updated `README.md` with:
  - Project purpose
  - Architecture and domain structure
  - Test command
  - Assessment runner command
  - Key assessment results
  - Documentation references
  - Reproducibility instructions

### Final validation

- Ran:

```text
.\gradlew.bat clean test
