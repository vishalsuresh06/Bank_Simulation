package com.boma.banksim.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckingAccountTest {

    private CheckingAccount account() {
        return new CheckingAccount("ACC1", "CUST1", 1_000.0);
    }

    @Test
    void constructor_setsCorrectType() {
        assertEquals(AccountType.CHECKING, account().getType());
    }

    @Test
    void constructor_setsInitialBalance() {
        assertEquals(1_000.0, account().getBalance(), 0.001);
    }

    @Test
    void constructor_nullAccountId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CheckingAccount(null, "CUST1", 100));
    }

    @Test
    void constructor_nullCustomerId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CheckingAccount("ACC1", null, 100));
    }

    @Test
    void constructor_negativeBalance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CheckingAccount("ACC1", "CUST1", -1));
    }

    @Test
    void constructor_zeroBalance_allowed() {
        assertDoesNotThrow(() -> new CheckingAccount("ACC1", "CUST1", 0));
    }

    @Test
    void deposit_increasesBalance() {
        CheckingAccount a = account();
        a.deposit(500);
        assertEquals(1_500.0, a.getBalance(), 0.001);
    }

    @Test
    void deposit_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> account().deposit(0));
    }

    @Test
    void deposit_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> account().deposit(-100));
    }

    @Test
    void withdraw_decreasesBalance() {
        CheckingAccount a = account();
        a.withdraw(400);
        assertEquals(600.0, a.getBalance(), 0.001);
    }

    @Test
    void withdraw_exactBalance_reducesToZero() {
        CheckingAccount a = account();
        a.withdraw(1_000);
        assertEquals(0.0, a.getBalance(), 0.001);
    }

    @Test
    void withdraw_insufficientFunds_throws() {
        assertThrows(IllegalStateException.class, () -> account().withdraw(1_001));
    }

    @Test
    void withdraw_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> account().withdraw(0));
    }

    @Test
    void applyMonthlyInterest_returnsZero() {
        assertEquals(0.0, account().applyMonthlyInterest(), 0.001);
    }

    @Test
    void applyMonthlyInterest_doesNotChangeBalance() {
        CheckingAccount a = account();
        a.applyMonthlyInterest();
        assertEquals(1_000.0, a.getBalance(), 0.001);
    }

    @Test
    void getInterestRate_returnsZero() {
        assertEquals(0.0, account().getInterestRate(), 0.001);
    }

    @Test
    void multipleDepositsAndWithdrawals_correctBalance() {
        CheckingAccount a = account(); // 1000
        a.deposit(200);               // 1200
        a.withdraw(500);              // 700
        a.deposit(100);               // 800
        assertEquals(800.0, a.getBalance(), 0.001);
    }
}
