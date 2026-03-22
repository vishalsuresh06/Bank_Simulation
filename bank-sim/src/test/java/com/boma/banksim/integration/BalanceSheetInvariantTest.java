package com.boma.banksim.integration;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.account.SavingsAccount;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.bank.BankBalanceSheet;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.customer.CustomerType;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanApplication;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.loan.LoanType;
import com.boma.banksim.service.DefaultService;
import com.boma.banksim.service.InterestAccrualService;
import com.boma.banksim.service.LoanService;
import com.boma.banksim.service.PaymentProcessor;
import com.boma.banksim.util.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Critical balance sheet invariant tests.
 * After every operation: Assets = Liabilities + Equity (always true by design)
 * and Equity >= 0 (solvency).
 */
class BalanceSheetInvariantTest {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    private Bank bank;
    private PaymentProcessor processor;
    private LoanService loanService;
    private InterestAccrualService accrualService;
    private DefaultService defaultService;
    private EconomicEnvironment economy;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 1_000_000, 200_000);
        processor = new PaymentProcessor();
        loanService = new LoanService();
        accrualService = new InterestAccrualService();
        defaultService = new DefaultService(new RandomProvider(42L));
        economy = EconomicEnvironment.normal();
    }

    private void assertInvariant() {
        BankBalanceSheet bs = bank.getBalanceSheet();
        assertTrue(bs.isBalanced(),
                "Balance sheet not balanced: assets=" + bs.getTotalAssets() +
                " liabilities+equity=" + (bs.getTotalLiabilities() + bs.getEquity()));
    }

    @Test
    void initialState_isBalanced() {
        assertInvariant();
    }

    @Test
    void afterDeposit_isBalanced() {
        CheckingAccount acc = new CheckingAccount("A1", "C1", 0.0);
        bank.addAccount(acc);
        processor.deposit(bank, acc, 50_000, DATE);
        assertInvariant();
    }

    @Test
    void afterWithdrawal_isBalanced() {
        CheckingAccount acc = new CheckingAccount("A1", "C1", 0.0);
        bank.addAccount(acc);
        processor.deposit(bank, acc, 50_000, DATE);
        processor.withdraw(bank, acc, 20_000, DATE);
        assertInvariant();
    }

    @Test
    void afterLoanOrigination_isBalanced() {
        Customer c = new Customer("C1", "Alice", CustomerType.RETAIL,
                new CustomerProfile(96_000, 0.3, 750, 0.2, 0.7));
        bank.addCustomer(c);
        LoanApplication app = new LoanApplication("APP1", "C1",
                LoanType.CONSUMER, 20_000, 8_000, 0, 750, 24);
        app.approve();
        loanService.issueLoan(bank, c, app, economy, DATE);
        assertInvariant();
    }

    @Test
    void afterLoanPayment_isBalanced() {
        Customer c = new Customer("C1", "Alice", CustomerType.RETAIL,
                new CustomerProfile(96_000, 0.3, 750, 0.2, 0.7));
        CheckingAccount acc = new CheckingAccount("A1", "C1", 10_000);
        bank.addCustomer(c);
        bank.addAccount(acc);
        bank.getBalanceSheet().increaseDeposits(10_000);

        LoanApplication app = new LoanApplication("APP1", "C1",
                LoanType.CONSUMER, 10_000, 8_000, 0, 750, 12);
        app.approve();
        Loan loan = loanService.issueLoan(bank, c, app, economy, DATE);

        // Simulate payment: debit account, then process
        acc.withdraw(loan.getMonthlyPayment());
        bank.getBalanceSheet().decreaseDeposits(loan.getMonthlyPayment());
        loanService.processPayment(bank, loan, loan.getMonthlyPayment(), DATE.plusMonths(1));
        assertInvariant();
    }

    @Test
    void afterDepositInterestAccrual_isBalanced() {
        SavingsAccount sav = new SavingsAccount("S1", "C1", 12_000, 0.12);
        bank.addAccount(sav);
        bank.getBalanceSheet().increaseDeposits(12_000);
        accrualService.accrueDepositInterest(bank, DATE.plusMonths(1));
        assertInvariant();
    }

    @Test
    void afterChargeOff_isBalanced() {
        bank.getBalanceSheet().increaseLoans(15_000);
        Loan loan = new Loan("LN1", "C1", LoanType.CONSUMER, 15_000, 0.08,
                24, 200, DATE.minusMonths(6));
        for (int i = 0; i < 6; i++) loan.incrementDaysLate();
        bank.addLoan(loan);

        defaultService.chargeOff(bank, loan, DATE);
        assertInvariant();
    }

    @Test
    void afterMixedOperations_isBalanced() {
        // Deposit from two customers
        CheckingAccount acc1 = new CheckingAccount("A1", "C1", 0.0);
        CheckingAccount acc2 = new CheckingAccount("A2", "C2", 0.0);
        SavingsAccount sav1 = new SavingsAccount("S1", "C1", 0.0, 0.06);
        bank.addAccount(acc1);
        bank.addAccount(acc2);
        bank.addAccount(sav1);

        processor.deposit(bank, acc1, 30_000, DATE);
        processor.deposit(bank, acc2, 20_000, DATE);
        processor.deposit(bank, sav1, 10_000, DATE);

        // Issue a loan
        Customer c1 = new Customer("C1", "Alice", CustomerType.RETAIL,
                new CustomerProfile(96_000, 0.3, 750, 0.2, 0.7));
        bank.addCustomer(c1);
        c1.addAccountId("A1");
        LoanApplication app = new LoanApplication("APP1", "C1",
                LoanType.CONSUMER, 15_000, 8_000, 0, 750, 12);
        app.approve();
        Loan loan = loanService.issueLoan(bank, c1, app, economy, DATE);

        // Make a payment
        acc1.withdraw(loan.getMonthlyPayment());
        bank.getBalanceSheet().decreaseDeposits(loan.getMonthlyPayment());
        loanService.processPayment(bank, loan, loan.getMonthlyPayment(), DATE.plusMonths(1));

        // Accrue deposit interest
        accrualService.accrueDepositInterest(bank, DATE.plusMonths(1));

        // Withdraw some funds
        processor.withdraw(bank, acc2, 5_000, DATE.plusMonths(1));

        assertInvariant();
    }

    @Test
    void equityIsAlwaysDerived_cannotBeManuallySet() {
        // Equity = Assets - Liabilities, always auto-computed
        double initialEquity = bank.getBalanceSheet().getEquity();

        // Make a deposit (equity-neutral)
        CheckingAccount acc = new CheckingAccount("A1", "C1", 0.0);
        bank.addAccount(acc);
        processor.deposit(bank, acc, 10_000, DATE);
        assertEquals(initialEquity, bank.getBalanceSheet().getEquity(), 0.01);
    }

    @Test
    void multipleChargeOffs_sumOfLossesEqualsEquityReduction() {
        // Properly originate loans (reserves↓, loans↑ — equity neutral),
        // then charge them off (loans↓ — equity decreases below initial).
        double totalLoss = 0;
        for (int i = 0; i < 5; i++) {
            double amount = 5_000 * (i + 1);
            // Simulate origination: reserves↓, loans↑ (equity stays at initial value)
            bank.getBalanceSheet().decreaseReserves(amount);
            bank.getBalanceSheet().increaseLoans(amount);
            Loan loan = new Loan("LN" + i, "C1", LoanType.CONSUMER, amount,
                    0.08, 24, 200, DATE.minusMonths(6));
            for (int j = 0; j < 6; j++) loan.incrementDaysLate();
            bank.addLoan(loan);
            totalLoss += loan.getOutstandingBalance();
        }

        double equityBefore = bank.getBalanceSheet().getEquity();

        // Charge off all loans — each reduces loans↓ → equity↓
        for (Loan loan : bank.getAllLoans()) {
            defaultService.chargeOff(bank, loan, DATE);
        }

        assertEquals(equityBefore - totalLoss, bank.getBalanceSheet().getEquity(), 0.01);
        assertInvariant();
    }
}
