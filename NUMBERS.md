# Numbers and Constants

This document records the numeric constants used by the implementation and the reason for each value.

## Currency Precision

| Constant | Value | Reason |
|---|---:|---|
| AED scale | 2 | The assessment specifies AED amounts to 2 decimal places. |
| BHD scale | 3 | The assessment specifies BHD amounts to 3 decimal places. |

These are currency rules from the assessment, not implementation preferences.

## Account Opening Balances

| Account | Currency | Opening Balance | Reason |
|---|---|---:|---|
| ACC-001 | AED | 0.00 | Specified by the assessment. |
| ACC-002 | BHD | 0.000 | Specified by the assessment. |

## Overdraft Fee

| Constant | Value | Reason |
|---|---:|---|
| Overdraft fee | AED 25.00 | Specified by the assessment. |

The fee is not reduced to AED 12.50 or another value because the assessment explicitly defines the fee as AED 25.00.

## Daily Interest

| Constant | Value | Reason |
|---|---:|---|
| Daily interest rate | 0.04% | Specified by the assessment. |
| Decimal representation | 0.0004 | 0.04% expressed as a decimal for multiplication. |
| Number of days | 6 | The assessment scenario covers Day 1 through Day 6. |

The rate is not changed to 0.02% because that would not implement the specified 0.04% daily rate.

## Event Amounts

The following amounts come directly from the supplied event stream.

| Event | Amount | Reason |
|---|---:|---|
| E1 | AED 1,200.00 | Credit specified by the assessment. |
| E2 | AED 950.00 | Debit specified by the assessment. |
| E3 | AED 200.00 | Authorization hold specified by the assessment. |
| E4 | AED 400.00 | Credit specified by the assessment. |
| E5 | AED 185.00 | Auth-A settlement amount specified by the assessment. |
| E6 | AED 180.00 | Unknown Auth-Z settlement amount specified by the assessment. |
| E7 | AED 620.00 | Debit specified by the assessment. |
| E8 | AED 90.00 | Auth-B hold specified by the assessment. |
| E9 | AED 620.00 | Reversal of E7 for the same amount. |
| E10 | BHD 10.000 | Credit amount specified by the assessment. |

These values are scenario inputs and are not configurable business assumptions.

## E10 Installments

| Constant | Value | Reason |
|---|---:|---|
| Number of installments | 3 | The assessment explicitly requires three installments. |
| Total amount | BHD 10.000 | Specified by E10. |

An exact equal split is not representable at BHD's three-decimal precision:

```text
10.000 / 3 = 3.333333...