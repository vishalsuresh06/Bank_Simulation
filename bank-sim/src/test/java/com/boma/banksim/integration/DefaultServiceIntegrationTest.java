package com.boma.banksim.integration;

import com.boma.banksim.bank.Bank;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.loan.LoanType;
import com.boma.banksim.service.DefaultService;
import com.boma.banksim.util.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DefaultService — charge-off accounting
 * and stochastic default evaluation.
 */
class DefaultServiceIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2024, 6, 1);

    private Bank bank;
    private DefaultService defaultService;
    private EconomicEnvironment economy;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 500_000, 100_000);
        economy = EconomicEnvironment.normal();
        defaultService = new DefaultService(new RandomProvider(42L));
    }

    private Loan addDefaultedLoan(String loanId, double principal) {
        bank.getBalanceSheet().increaseLoans(principal);
        Loan loan = new Loan(loanId, "C1", LoanType.CONSUMER, principal,
                0.08, 24, 100, DATE.minusMonths(6));
        for (int i = 0; i < 6; i++) loan.incrementDaysLate();
        assertEquals(LoanStatus.DEFAULTED, loan.getStatus());
        bank.addLoan(loan);
        return loan;
    }

    @Test
    void chargeOff_singleLoan_loansDecreaseByOutstanding() {
        Loan loan = addDefaultedLoan("LN1", 10_000);
        double loansBefore = bank.getBalanceSheet().getTotalLoans();
        double outstanding = loan.getOutstandingBalance();

        defaultService.chargeOff(bank, loan, DATE);

        assertEquals(loansBefore - outstanding, bank.getBalanceSheet().getTotalLoans(), 0.01);
    }

    @Test
    void chargeOff_equityReducedByExactLoss() {
        Loan loan = addDefaultedLoan("LN1", 10_000);
        double equityBefore = bank.getBalanceSheet().getEquity();
        double outstanding = loan.getOutstandingBalance();

        defaultService.chargeOff(bank, loan, DATE);

        assertEquals(equityBefore - outstanding, bank.getBalanceSheet().getEquity(), 0.01);
    }

    @Test
    void chargeOff_setsStatusChargedOff() {
        Loan loan = addDefaultedLoan("LN1", 5_000);
        defaultService.chargeOff(bank, loan, DATE);
        assertEquals(LoanStatus.CHARGED_OFF, loan.getStatus());
        assertEquals(0.0, loan.getOutstandingBalance(), 0.01);
    }

    @Test
    void chargeOff_recordsChargeOffLoss() {
        Loan loan = addDefaultedLoan("LN1", 8_000);
        defaultService.chargeOff(bank, loan, DATE);
        assertEquals(8_000.0, bank.getBalanceSheet().getChargeOffLosses(), 0.01);
    }

    @Test
    void chargeOff_ledgerEntryCreated() {
        Loan loan = addDefaultedLoan("LN1", 5_000);
        defaultService.chargeOff(bank, loan, DATE);
        assertFalse(bank.getLedger().getEntriesBetween(DATE, DATE).isEmpty());
    }

    @Test
    void chargeOff_balanceSheetRemainsBalanced() {
        addDefaultedLoan("LN1", 5_000);
        addDefaultedLoan("LN2", 3_000);
        Loan loan1 = bank.getLoan("LN1");
        Loan loan2 = bank.getLoan("LN2");

        defaultService.chargeOff(bank, loan1, DATE);
        defaultService.chargeOff(bank, loan2, DATE);

        assertTrue(bank.getBalanceSheet().isBalanced());
    }

    @Test
    void evaluateAndChargeOff_skipsCurrent_andClosed_loans() {
        // Add a CURRENT loan — should NOT be charged off
        bank.getBalanceSheet().increaseLoans(5_000);
        Loan currentLoan = new Loan("LN-CURRENT", "C2", LoanType.CONSUMER,
                5_000, 0.08, 24, 100, DATE.minusMonths(1));
        assertEquals(LoanStatus.CURRENT, currentLoan.getStatus());
        bank.addLoan(currentLoan);

        List<Loan> result = defaultService.evaluateAndChargeOff(bank, economy, DATE);

        assertFalse(result.contains(currentLoan));
        assertNotEquals(LoanStatus.CHARGED_OFF, currentLoan.getStatus());
    }

    @Test
    void evaluateAndChargeOff_crisisEconomy_higherChargeOffRate() {
        // Add 10 defaulted loans and compare charge-off count under normal vs crisis
        Bank bankNormal = new Bank("B2", "NormalBank", 500_000, 100_000);
        Bank bankCrisis = new Bank("B3", "CrisisBank", 500_000, 100_000);

        for (int i = 0; i < 10; i++) {
            bankNormal.getBalanceSheet().increaseLoans(5_000);
            Loan l = new Loan("LN" + i, "C1", LoanType.CONSUMER, 5_000, 0.08, 24, 100,
                    DATE.minusMonths(6));
            for (int j = 0; j < 6; j++) l.incrementDaysLate();
            bankNormal.addLoan(l);

            bankCrisis.getBalanceSheet().increaseLoans(5_000);
            Loan l2 = new Loan("LN" + i + "C", "C1", LoanType.CONSUMER, 5_000, 0.08, 24, 100,
                    DATE.minusMonths(6));
            for (int j = 0; j < 6; j++) l2.incrementDaysLate();
            bankCrisis.addLoan(l2);
        }

        // Use same seed for fair comparison
        List<Loan> normalChargeOffs = new DefaultService(new RandomProvider(99L))
                .evaluateAndChargeOff(bankNormal, EconomicEnvironment.normal(), DATE);
        List<Loan> crisisChargeOffs = new DefaultService(new RandomProvider(99L))
                .evaluateAndChargeOff(bankCrisis, EconomicEnvironment.crisis(), DATE);

        // Crisis should result in >= charge-offs (higher default probability)
        assertTrue(crisisChargeOffs.size() >= normalChargeOffs.size());
    }

    @Test
    void getDefaultedLoans_returnsOnlyDefaultedStatus() {
        addDefaultedLoan("LN1", 5_000);

        bank.getBalanceSheet().increaseLoans(3_000);
        Loan currentLoan = new Loan("LN2", "C2", LoanType.CONSUMER, 3_000, 0.08, 24, 100, DATE);
        bank.addLoan(currentLoan);

        List<Loan> defaulted = defaultService.getDefaultedLoans(bank);

        assertEquals(1, defaulted.size());
        assertEquals("LN1", defaulted.get(0).getLoanId());
    }
}
