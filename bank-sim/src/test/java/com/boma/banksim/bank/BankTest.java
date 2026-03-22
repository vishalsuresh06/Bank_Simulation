package com.boma.banksim.bank;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.customer.CustomerType;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanType;
import com.boma.banksim.transaction.Transaction;
import com.boma.banksim.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "Test Bank", 1_000_000, 500_000);
    }

    private Customer customer(String id) {
        return new Customer(id, "Test Customer",
                CustomerType.RETAIL, new CustomerProfile(60_000, 0.6, 700, 0.3, 0.4));
    }

    private Loan loan(String id, String custId) {
        return new Loan(id, custId, LoanType.CONSUMER, 10_000, 0.08, 60,
                202.76, LocalDate.of(2024, 1, 1));
    }

    // ---- Construction ----

    @Test
    void constructor_blankBankId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bank("", "Name", 1_000_000, 500_000));
    }

    @Test
    void constructor_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bank("B1", " ", 1_000_000, 500_000));
    }

    @Test
    void constructor_negativeReserves_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bank("B1", "Name", -1, 500_000));
    }

    @Test
    void constructor_negativeEquity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bank("B1", "Name", 1_000_000, -1));
    }

    @Test
    void constructor_initialReservesMatchBalanceSheet() {
        assertEquals(1_000_000, bank.getBalanceSheet().getReserves(), 0.01);
    }

    // ---- Customer ----

    @Test
    void addCustomer_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> bank.addCustomer(null));
    }

    @Test
    void addCustomer_retrievableById() {
        Customer c = customer("C1");
        bank.addCustomer(c);
        assertSame(c, bank.getCustomer("C1"));
    }

    @Test
    void getCustomer_unknownId_returnsNull() {
        assertNull(bank.getCustomer("UNKNOWN"));
    }

    @Test
    void hasCustomer_absent_false() {
        assertFalse(bank.hasCustomer("C99"));
    }

    @Test
    void hasCustomer_present_true() {
        bank.addCustomer(customer("C1"));
        assertTrue(bank.hasCustomer("C1"));
    }

    @Test
    void getCustomerCount_incrementsWithAdds() {
        bank.addCustomer(customer("C1"));
        bank.addCustomer(customer("C2"));
        assertEquals(2, bank.getCustomerCount());
    }

    // ---- Account ----

    @Test
    void addAccount_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> bank.addAccount(null));
    }

    @Test
    void addAccount_retrievableById() {
        CheckingAccount acc = new CheckingAccount("ACC1", "C1", 500);
        bank.addAccount(acc);
        assertSame(acc, bank.getAccount("ACC1"));
    }

    @Test
    void getAccount_unknownId_returnsNull() {
        assertNull(bank.getAccount("UNKNOWN"));
    }

    // ---- Loan ----

    @Test
    void addLoan_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> bank.addLoan(null));
    }

    @Test
    void addLoan_retrievableById() {
        Loan l = loan("LN1", "C1");
        bank.addLoan(l);
        assertSame(l, bank.getLoan("LN1"));
    }

    @Test
    void getLoansForCustomer_returnsOnlyMatchingCustomer() {
        Loan l1 = loan("LN1", "C1");
        Loan l2 = loan("LN2", "C2");
        bank.addLoan(l1);
        bank.addLoan(l2);
        var loans = bank.getLoansForCustomer("C1");
        assertEquals(1, loans.size());
        assertSame(l1, loans.get(0));
    }

    @Test
    void getTotalOutstandingLoans_onlyCountsActiveLoans() {
        Loan active = loan("LN1", "C1");
        Loan charged = loan("LN2", "C2");
        charged.chargeOff();
        bank.addLoan(active);
        bank.addLoan(charged);
        assertEquals(10_000, bank.getTotalOutstandingLoans(), 0.01);
    }

    @Test
    void getTotalDepositBalance_sumsAllAccounts() {
        bank.addAccount(new CheckingAccount("ACC1", "C1", 1_000));
        bank.addAccount(new CheckingAccount("ACC2", "C2", 2_000));
        assertEquals(3_000, bank.getTotalDepositBalance(), 0.01);
    }

    // ---- Transaction log ----

    @Test
    void logTransaction_null_ignoredGracefully() {
        int before = bank.getTransactionLog().size();
        bank.logTransaction(null);
        assertEquals(before, bank.getTransactionLog().size());
    }

    @Test
    void logTransaction_storesTransaction() {
        Transaction tx = new Transaction("TX1", TransactionType.DEPOSIT, 500,
                LocalDateTime.now(), null, "ACC1", "test");
        bank.logTransaction(tx);
        assertEquals(1, bank.getTransactionLog().size());
    }

    @Test
    void getTransactionLog_returnsUnmodifiableView() {
        var log = bank.getTransactionLog();
        assertThrows(UnsupportedOperationException.class,
                () -> log.add(new Transaction("TX1", TransactionType.DEPOSIT, 500,
                        LocalDateTime.now(), null, "ACC1", "test")));
    }

    // ---- Convenience methods ----

    @Test
    void accessors_returnCorrectValues() {
        assertEquals("B1", bank.getBankId());
        assertEquals("Test Bank", bank.getName());
        assertNotNull(bank.getBalanceSheet());
        assertNotNull(bank.getLedger());
        assertNotNull(bank.getTreasury());
        assertNotNull(bank.getLiquidityManager());
    }
}
