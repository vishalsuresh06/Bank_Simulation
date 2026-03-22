package com.boma.banksim.integration;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.customer.CustomerType;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanApplication;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.loan.LoanType;
import com.boma.banksim.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the delinquency evaluation pathway in LoanService.
 */
class LoanServiceDelinquencyIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    private Bank bank;
    private LoanService loanService;
    private EconomicEnvironment economy;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 500_000, 100_000);
        loanService = new LoanService();
        economy = EconomicEnvironment.normal();
    }

    private Customer addCustomerWithAccount(String customerId, String accountId, double balance) {
        Customer customer = new Customer(customerId, "Customer " + customerId,
                CustomerType.RETAIL, new CustomerProfile(60_000, 0.3, 700, 0.2, 0.7));
        CheckingAccount account = new CheckingAccount(accountId, customerId, balance);
        bank.addCustomer(customer);
        bank.addAccount(account);
        customer.addAccountId(accountId);
        return customer;
    }

    private Loan issueLoan(Customer customer, double amount, int termMonths) {
        LoanApplication app = new LoanApplication("APP-" + customer.getCustomerId(),
                customer.getCustomerId(), LoanType.CONSUMER, amount, 5_000, 0, 700, termMonths);
        app.approve();
        return loanService.issueLoan(bank, customer, app, economy, DATE);
    }

    @Test
    void evaluateDelinquency_customerHasFunds_loanRemainsCurrentStatus() {
        Customer c = addCustomerWithAccount("C1", "A1", 50_000);
        Loan loan = issueLoan(c, 5_000, 12);

        loanService.evaluateDelinquency(bank);

        assertEquals(LoanStatus.CURRENT, loan.getStatus());
        assertEquals(0, loan.getDaysLate());
    }

    @Test
    void evaluateDelinquency_customerHasNoFunds_loanGoesLate() {
        Customer c = addCustomerWithAccount("C1", "A1", 0.0); // no balance
        Loan loan = issueLoan(c, 5_000, 12);

        List<Loan> wentLate = loanService.evaluateDelinquency(bank);

        assertTrue(loan.getDaysLate() > 0);
        assertFalse(wentLate.isEmpty() || loan.getStatus() == LoanStatus.CURRENT);
    }

    @Test
    void evaluateDelinquency_insufficientFunds_loanGoesLate() {
        Customer c = addCustomerWithAccount("C1", "A1", 10.0); // far less than payment
        Loan loan = issueLoan(c, 5_000, 12);

        loanService.evaluateDelinquency(bank);

        assertEquals(LoanStatus.LATE, loan.getStatus());
        assertEquals(30, loan.getDaysLate());
    }

    @Test
    void evaluateDelinquency_multipleMissedPayments_progressThroughStates() {
        Customer c = addCustomerWithAccount("C1", "A1", 0.0);
        Loan loan = issueLoan(c, 5_000, 12);

        // 30 days late
        loanService.evaluateDelinquency(bank);
        assertEquals(LoanStatus.LATE, loan.getStatus());

        // 60 days — still LATE (not yet at 90)
        loanService.evaluateDelinquency(bank);
        assertEquals(60, loan.getDaysLate());

        // 90 days → DELINQUENT
        loanService.evaluateDelinquency(bank);
        assertEquals(LoanStatus.DELINQUENT, loan.getStatus());
    }

    @Test
    void evaluateDelinquency_sixMissedPayments_loanBecomesDefaulted() {
        Customer c = addCustomerWithAccount("C1", "A1", 0.0);
        Loan loan = issueLoan(c, 5_000, 12);

        for (int i = 0; i < 6; i++) {
            loanService.evaluateDelinquency(bank);
        }

        assertEquals(LoanStatus.DEFAULTED, loan.getStatus());
        assertEquals(180, loan.getDaysLate());
    }

    @Test
    void evaluateDelinquency_onlyActiveLoansAffected() {
        Customer c = addCustomerWithAccount("C1", "A1", 0.0);
        Loan loan = issueLoan(c, 5_000, 12);

        // Force charge-off
        for (int i = 0; i < 6; i++) loan.incrementDaysLate();
        loan.chargeOff();

        List<Loan> wentLate = loanService.evaluateDelinquency(bank);

        // Already charged off — should not be processed again
        assertTrue(wentLate.isEmpty());
        assertEquals(LoanStatus.CHARGED_OFF, loan.getStatus());
    }

    @Test
    void evaluateDelinquency_customerWithMultipleAccounts_usesAnyWithSufficientFunds() {
        Customer c = new Customer("C1", "Alice", CustomerType.RETAIL,
                new CustomerProfile(60_000, 0.3, 700, 0.2, 0.7));
        CheckingAccount acc1 = new CheckingAccount("A1", "C1", 0.0);    // empty
        CheckingAccount acc2 = new CheckingAccount("A2", "C1", 50_000); // sufficient
        bank.addCustomer(c);
        bank.addAccount(acc1);
        bank.addAccount(acc2);
        c.addAccountId("A1");
        c.addAccountId("A2");

        Loan loan = issueLoan(c, 5_000, 12);

        loanService.evaluateDelinquency(bank);

        // acc2 has enough — loan should stay current
        assertEquals(LoanStatus.CURRENT, loan.getStatus());
    }

    @Test
    void totalMonthlyObligations_sumsActiveLoans() {
        Customer c = addCustomerWithAccount("C1", "A1", 500_000);
        Loan l1 = issueLoan(c, 5_000, 12);
        Loan l2 = issueLoan(c, 10_000, 24);

        double total = loanService.totalMonthlyObligations(bank, "C1");

        assertEquals(l1.getMonthlyPayment() + l2.getMonthlyPayment(), total, 0.01);
    }
}
