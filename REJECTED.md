# Rejected Requirements and Approaches

This document records requirements or implementation approaches that were considered but deliberately not implemented.

## 1. Removing the E7 Debit During Reversal

### Rejected approach

When E9 arrives, delete or modify E7 so that the original debit disappears from the ledger.

### Reason

The assessment explicitly requires an append-only ledger.

Existing ledger entries must not be mutated or deleted.

### Implemented approach

E9 creates a new reversal entry for the same amount and value date as E7.

Therefore:

```text
E7       DEBIT      620.00
E9       REVERSAL   620.00