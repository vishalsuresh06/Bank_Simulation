package com.boma.banksim.integration;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.account.SavingsAccount;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.service.InterestAccrualService;
import com.boma.banksim.service.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying deposit interest accrual correctly
 * increases deposits (liability) and reduces equity.
 */
class InterestAccrualIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 31);

    private Bank bank;
    private SavingsAccount savings;
    private InterestAccrualService accrualService;
    private PaymentProcessor processor;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 500_000, 100_000);
        savings = new SavingsAccount("SAV1", "C1", 12_000, 0.12); // 12% annual = 1%/month
        bank.addAccount(savings);
        accrualService = new InterestAccrualService();
        processor = new PaymentProcessor();
    }

    @Test
    void accrueInterest_increasesSavingsBalance() {
        double balanceBefore = savings.getBalance();
        accrualService.accrueDepositInterest(bank, DATE);
        assertEquals(balanceBefore + 120.0, savings.getBalance(), 0.01);
    }

    @Test
    void accrueInterest_increasesDepositsOnBalanceSheet() {
        // We need to set deposits to match the actual savings balance first
        bank.getBalanceSheet().increaseDeposits(12_000);
        double depositsBefore = bank.getBalanceSheet().getTotalDeposits();

        accrualService.accrueDepositInterest(bank, DATE);

        assertEquals(depositsBefore + 120.0, bank.getBalanceSheet().getTotalDeposits(), 0.01);
    }

    @Test
    void accrueInterest_reducesEquity() {
        bank.getBalanceSheet().increaseDeposits(12_000);
        double equityBefore = bank.getBalanceSheet().getEquity();

        accrualService.accrueDepositInterest(bank, DATE);

        assertEquals(equityBefore - 120.0, bank.getBalanceSheet().getEquity(), 0.01);
    }

    @Test
    void accrueInterest_recordsDepositInterestExpense() {
        accrualService.accrueDepositInterest(bank, DATE);
        assertEquals(120.0, bank.getBalanceSheet().getDepositInterestExpense(), 0.01);
    }

    @Test
    void accrueInterest_checkingAccount_earnsNoInterest() {
        CheckingAccount checking = new CheckingAccount("CHK1", "C1", 50_000);
        bank.addAccount(checking);

        double balanceBefore = checking.getBalance();
        accrualService.accrueDepositInterest(bank, DATE);
        assertEquals(balanceBefore, checking.getBalance(), 0.01);
    }

    @Test
    void accrueInterest_multipleSavingsAccounts_totalInterestSummed() {
        SavingsAccount savings2 = new SavingsAccount("SAV2", "C2", 24_000, 0.12);
        bank.addAccount(savings2);

        double totalInterest = accrualService.accrueDepositInterest(bank, DATE);

        // 12000 * 1% + 24000 * 1% = 120 + 240 = 360
        assertEquals(360.0, totalInterest, 0.01);
    }

    @Test
    void accrueInterest_balanceSheetRemainsBalanced() {
        bank.getBalanceSheet().increaseDeposits(12_000);
        accrualService.accrueDepositInterest(bank, DATE);
        assertTrue(bank.getBalanceSheet().isBalanced());
    }

    @Test
    void accrueInterest_noAccounts_returnsZero() {
        Bank emptyBank = new Bank("B2", "EmptyBank", 100_000, 50_000);
        double interest = accrualService.accrueDepositInterest(emptyBank, DATE);
        assertEquals(0.0, interest, 0.001);
    }

    @Test
    void accrueInterest_ledgerEntryCreated() {
        accrualService.accrueDepositInterest(bank, DATE);
        assertFalse(bank.getLedger().getEntriesBetween(DATE, DATE).isEmpty());
    }

    @Test
    void accrueInterest_zeroRateSavings_noBalanceSheetChange() {
        SavingsAccount zeroRate = new SavingsAccount("SAV2", "C2", 50_000, 0.0);
        bank.addAccount(zeroRate);
        bank.getBalanceSheet().increaseDeposits(62_000);
        double depositsBefore = bank.getBalanceSheet().getTotalDeposits();

        // Only the original savings account should earn interest
        accrualService.accrueDepositInterest(bank, DATE);

        // Only 120 increase from savings with 12% rate
        assertEquals(depositsBefore + 120.0, bank.getBalanceSheet().getTotalDeposits(), 0.01);
    }
}
