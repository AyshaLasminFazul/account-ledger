package com.ledger.account_ledger.service;

import com.ledger.account_ledger.domain.Currency;
import com.ledger.account_ledger.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstallmentServiceTest {
    private InstallmentService installmentService;

    @BeforeEach
    void setUp() {
        installmentService = new InstallmentService();
    }
    @Test
    void shouldSplitBhdTenIntoThreeInstallmentsPreservingTotal() {

        InstallmentService service = new InstallmentService();

        List<Money> installments =
                service.splitIntoThree(
                        Money.of("10.000", Currency.BHD)
                );

        assertEquals(3, installments.size());

        assertEquals(
                Money.of("3.334", Currency.BHD),
                installments.get(0)
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                installments.get(1)
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                installments.get(2)
        );

        Money total = installments.get(0)
                .add(installments.get(1))
                .add(installments.get(2));

        assertEquals(
                Money.of("10.000", Currency.BHD),
                total
        );
    }

    @Test
    void shouldSplitBhdTenIntoThreeCurrencyRoundedInstallments() {

        Money total = Money.of(
                "10.000",
                Currency.BHD
        );

        List<Money> installments =
                installmentService.splitIntoThree(total);

        assertEquals(3, installments.size());

        assertEquals(
                Money.of("3.334", Currency.BHD),
                installments.get(0)
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                installments.get(1)
        );

        assertEquals(
                Money.of("3.333", Currency.BHD),
                installments.get(2)
        );

        Money reconstructed =
                installments.get(0)
                        .add(installments.get(1))
                        .add(installments.get(2));

        assertEquals(
                Money.of("10.000", Currency.BHD),
                reconstructed
        );
    }
}