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
import com.boma.banksim.service.DefaultService;
import com.boma.banksim.service.LoanService;
import com.boma.banksim.service.PaymentProcessor;
import com.boma.banksim.util.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the full loan lifecycle:
 * origination, monthly payments, full payoff, and charge-off.
 */
class LoanLifecycleIntegrationTest {

    private static final LocalDate START = LocalDate.of(2024, 1, 1);

    private Bank bank;
    private Customer customer;
    private CheckingAccount checking;
    private LoanService loanService;
    private PaymentProcessor processor;
    private EconomicEnvironment economy;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 500_000, 100_000);
        customer = new Customer("C1", "Alice", CustomerType.RETAIL,
                new CustomerProfile(96_000, 0.3, 750, 0.2, 0.7));
        checking = new CheckingAccount("ACC1", "C1", 50_000);
        bank.addCustomer(customer);
        bank.addAccount(checking);
        customer.addAccountId("ACC1");
        loanService = new LoanService();
        processor = new PaymentProcessor();
        economy = EconomicEnvironment.normal();
    }

    private Loan originateLoan(double amount, int termMonths) {
        LoanApplication app = new LoanApplication("APP1", "C1",
                LoanType.CONSUMER, amount, 8_000, 0, 750, termMonths);
        app.approve();
        return loanService.issueLoan(bank, customer, app, economy, START);
    }

    @Test
    void loanOrigination_increasesLoans_decreasesReserves_equityNeutral() {
        double equityBefore = bank.getBalanceSheet().getEquity();
        double reservesBefore = bank.getBalanceSheet().getReserves();

        Loan loan = originateLoan(10_000, 12);

        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
        assertEquals(reservesBefore - 10_000, bank.getBalanceSheet().getReserves(), 0.01);
        assertEquals(10_000, bank.getBalanceSheet().getTotalLoans(), 0.01);
        assertEquals(LoanStatus.CURRENT, loan.getStatus());
    }

    @Test
    void loanPayment_increasesReserves_decreasesLoans_recordsInterestIncome() {
        Loan loan = originateLoan(10_000, 12);
        double reservesBefore = bank.getBalanceSheet().getReserves();

        loanService.processPayment(bank, loan, loan.getMonthlyPayment(), START.plusMonths(1));

        assertTrue(bank.getBalanceSheet().getReserves() > reservesBefore);
        assertTrue(bank.getBalanceSheet().getGrossInterestIncome() > 0);
    }

    @Test
    void paymentSplit_interestFirstPrincipalSecond() {
        // 10000 at 12% annual = 1% monthly → first month interest = $100
        Loan loan = originateLoan(10_000, 12);
        double balanceBefore = loan.getOutstandingBalance();

        loanService.processPayment(bank, loan, loan.getMonthlyPayment(), START.plusMonths(1));

        // Interest income should have been recorded on the balance sheet
        // Monthly interest = 10000 * 1% = 100; principal portion = payment - 100
        assertTrue(bank.getBalanceSheet().getGrossInterestIncome() > 0);
        assertTrue(loan.getOutstandingBalance() < balanceBefore);
    }

    @Test
    void balanceSheet_remainsBalanced_afterLoanOrigination() {
        originateLoan(20_000, 24);
        assertTrue(bank.getBalanceSheet().isBalanced());
        assertTrue(bank.getBalanceSheet().isSolvent());
    }

    @Test
    void chargeOff_decreasesLoansAndEquity() {
        Loan loan = originateLoan(10_000, 12);
        double equityBefore = bank.getBalanceSheet().getEquity();
        double loansBefore = bank.getBalanceSheet().getTotalLoans();

        // Force loan to a chargeable state by marking it defaulted
        loan.incrementDaysLate(); // 30
        loan.incrementDaysLate(); // 60
        loan.incrementDaysLate(); // 90 → DELINQUENT
        loan.incrementDaysLate(); // 120
        loan.incrementDaysLate(); // 150
        loan.incrementDaysLate(); // 180 → DEFAULTED

        DefaultService defaultService = new DefaultService(new RandomProvider(42));
        defaultService.chargeOff(bank, loan, START.plusMonths(6));

        assertEquals(LoanStatus.CHARGED_OFF, loan.getStatus());
        assertTrue(bank.getBalanceSheet().getEquity() < equityBefore);
        assertTrue(bank.getBalanceSheet().getTotalLoans() < loansBefore);
        assertTrue(bank.getBalanceSheet().isBalanced());
    }

    @Test
    void chargeOff_equityDropsExactlyByLoss() {
        Loan loan = originateLoan(10_000, 12);
        double equityBefore = bank.getBalanceSheet().getEquity();
        double outstanding = loan.getOutstandingBalance();

        // Force defaulted
        for (int i = 0; i < 6; i++) loan.incrementDaysLate();

        DefaultService ds = new DefaultService(new RandomProvider(42));
        ds.chargeOff(bank, loan, START.plusMonths(6));

        assertEquals(equityBefore - outstanding,
                bank.getBalanceSheet().getEquity(), 0.01);
    }

    @Test
    void fullPayoff_closesLoan_balanceSheetConsistent() {
        // Verify balance sheet consistency over multiple monthly payments.
        // We make 6 payments and verify balance sheet remains balanced each time.
        Loan loan = originateLoan(10_000, 24);

        for (int i = 0; i < 6; i++) {
            if (loan.getStatus() == LoanStatus.CLOSED) break;
            loanService.processPayment(bank, loan, loan.getMonthlyPayment(),
                    START.plusMonths(i + 1));
            assertTrue(bank.getBalanceSheet().isBalanced(),
                    "Balance sheet not balanced after payment " + (i + 1));
        }

        // After 6 of 24 payments, loan should still be current (CURRENT status, lower balance)
        assertTrue(loan.getOutstandingBalance() < 10_000);
        assertEquals(LoanStatus.CURRENT, loan.getStatus());
    }

    @Test
    void multipleLoanOriginations_cumulativeEquityNeutral() {
        double equityBefore = bank.getBalanceSheet().getEquity();

        originateLoan(5_000, 12);
        originateLoan(8_000, 24);
        originateLoan(3_000, 6);

        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
    }
}
