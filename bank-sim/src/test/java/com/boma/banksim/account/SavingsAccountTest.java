package com.boma.banksim.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SavingsAccountTest {

    private SavingsAccount account(double balance, double rate) {
        return new SavingsAccount("SAV1", "CUST1", balance, rate);
    }

    @Test
    void constructor_setsCorrectType() {
        assertEquals(AccountType.SAVINGS, account(1_000, 0.05).getType());
    }

    @Test
    void constructor_negativeRate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new SavingsAccount("SAV1", "CUST1", 1_000, -0.01));
    }

    @Test
    void constructor_zeroRate_allowed() {
        assertDoesNotThrow(() -> new SavingsAccount("SAV1", "CUST1", 1_000, 0.0));
    }

    @Test
    void applyMonthlyInterest_correctAmount() {
        // balance=1200, rate=12% annual → monthly=1% → interest=12
        SavingsAccount a = account(1_200, 0.12);
        double interest = a.applyMonthlyInterest();
        assertEquals(12.0, interest, 0.001);
    }

    @Test
    void applyMonthlyInterest_creditsBalance() {
        SavingsAccount a = account(1_200, 0.12);
        a.applyMonthlyInterest();
        assertEquals(1_212.0, a.getBalance(), 0.001);
    }

    @Test
    void applyMonthlyInterest_returnsInterestAmount() {
        SavingsAccount a = account(1_200, 0.12);
        double returned = a.applyMonthlyInterest();
        assertEquals(12.0, returned, 0.001);
    }

    @Test
    void applyMonthlyInterest_zeroRate_noChange() {
        SavingsAccount a = account(1_000, 0.0);
        double interest = a.applyMonthlyInterest();
        assertEquals(0.0, interest, 0.001);
        assertEquals(1_000.0, a.getBalance(), 0.001);
    }

    @Test
    void applyMonthlyInterest_compoundsOnNextCall() {
        SavingsAccount a = account(1_000, 0.12);
        double first = a.applyMonthlyInterest();
        double second = a.applyMonthlyInterest();
        assertTrue(second > first, "Second interest should be larger due to compounding");
    }

    @Test
    void applyMonthlyInterest_roundedToCents() {
        // balance=1, rate=0.5% annual → monthly=~0.004167 → should round to 0.00
        SavingsAccount a = account(1, 0.005);
        double interest = a.applyMonthlyInterest();
        // Check it is rounded to 2 decimal places
        assertEquals(interest, Math.round(interest * 100.0) / 100.0, 0.0001);
    }

    @Test
    void setAnnualInterestRate_negative_throws() {
        SavingsAccount a = account(1_000, 0.05);
        assertThrows(IllegalArgumentException.class, () -> a.setAnnualInterestRate(-0.01));
    }

    @Test
    void setAnnualInterestRate_updatesGetterValue() {
        SavingsAccount a = account(1_000, 0.05);
        a.setAnnualInterestRate(0.04);
        assertEquals(0.04, a.getInterestRate(), 0.0001);
    }

    @Test
    void getInterestRate_returnsConstructedRate() {
        assertEquals(0.06, account(500, 0.06).getInterestRate(), 0.0001);
    }
}
