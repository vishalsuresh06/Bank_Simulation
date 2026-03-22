package com.boma.banksim.integration;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.service.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying PaymentProcessor maintains the balance sheet
 * equation (Assets = Liabilities + Equity) across deposit/withdrawal operations.
 */
class DepositWithdrawalIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 15);

    private Bank bank;
    private CheckingAccount account;
    private PaymentProcessor processor;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 1_000_000, 200_000);
        account = new CheckingAccount("ACC1", "C1", 0.0);
        bank.addAccount(account);
        processor = new PaymentProcessor();
    }

    @Test
    void deposit_increasesReservesAndDepositsEqually_equityUnchanged() {
        double equityBefore = bank.getBalanceSheet().getEquity();

        processor.deposit(bank, account, 5_000, DATE);

        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
        assertEquals(1_005_000, bank.getBalanceSheet().getReserves(), 0.01);
        assertEquals(5_000, bank.getBalanceSheet().getTotalDeposits(), 0.01);
    }

    @Test
    void withdrawal_decreasesReservesAndDepositsEqually_equityUnchanged() {
        processor.deposit(bank, account, 10_000, DATE);
        double equityBefore = bank.getBalanceSheet().getEquity();

        processor.withdraw(bank, account, 3_000, DATE);

        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
        assertEquals(7_000, bank.getBalanceSheet().getTotalDeposits(), 0.01);
    }

    @Test
    void multipleDeposits_balanceSheetRemainsBalanced() {
        for (int i = 1; i <= 10; i++) {
            processor.deposit(bank, account, 1_000 * i, DATE.plusDays(i));
        }
        assertTrue(bank.getBalanceSheet().isBalanced());
    }

    @Test
    void depositThenWithdraw_sameAmount_returnsToOriginalBalanceSheet() {
        double reservesBefore = bank.getBalanceSheet().getReserves();
        double depositsBefore = bank.getBalanceSheet().getTotalDeposits();

        processor.deposit(bank, account, 5_000, DATE);
        processor.withdraw(bank, account, 5_000, DATE);

        assertEquals(reservesBefore, bank.getBalanceSheet().getReserves(), 0.01);
        assertEquals(depositsBefore, bank.getBalanceSheet().getTotalDeposits(), 0.01);
    }

    @Test
    void internalTransfer_doesNotChangeBalanceSheet() {
        CheckingAccount accountB = new CheckingAccount("ACC2", "C2", 0.0);
        bank.addAccount(accountB);

        processor.deposit(bank, account, 10_000, DATE);
        double reservesBefore = bank.getBalanceSheet().getReserves();
        double depositsBefore = bank.getBalanceSheet().getTotalDeposits();
        double equityBefore = bank.getBalanceSheet().getEquity();

        processor.internalTransfer(bank, account, accountB, 4_000, DATE);

        assertEquals(reservesBefore, bank.getBalanceSheet().getReserves(), 0.01);
        assertEquals(depositsBefore, bank.getBalanceSheet().getTotalDeposits(), 0.01);
        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
        assertEquals(6_000, account.getBalance(), 0.01);
        assertEquals(4_000, accountB.getBalance(), 0.01);
    }

    @Test
    void externalTransferIn_sameAsDeposit_equityUnchanged() {
        double equityBefore = bank.getBalanceSheet().getEquity();
        processor.externalTransferIn(bank, account, 8_000, DATE);
        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
        assertEquals(8_000, account.getBalance(), 0.01);
    }

    @Test
    void externalTransferOut_sameAsWithdraw_equityUnchanged() {
        processor.deposit(bank, account, 10_000, DATE);
        double equityBefore = bank.getBalanceSheet().getEquity();
        processor.externalTransferOut(bank, account, 3_000, DATE);
        assertEquals(equityBefore, bank.getBalanceSheet().getEquity(), 0.01);
        assertEquals(7_000, account.getBalance(), 0.01);
    }

    @Test
    void deposit_ledgerEntryCreated() {
        processor.deposit(bank, account, 5_000, DATE);
        assertFalse(bank.getLedger().getEntriesBetween(DATE, DATE).isEmpty());
    }

    @Test
    void deposit_transactionLogRecorded() {
        processor.deposit(bank, account, 5_000, DATE);
        assertEquals(1, bank.getTransactionLog().size());
    }

    @Test
    void withdraw_insufficientFunds_throws() {
        processor.deposit(bank, account, 1_000, DATE);
        assertThrows(Exception.class, () -> processor.withdraw(bank, account, 5_000, DATE));
    }

    @Test
    void balanceSheet_alwaysBalanced_afterMixedOperations() {
        CheckingAccount accountB = new CheckingAccount("ACC2", "C2", 0.0);
        bank.addAccount(accountB);

        processor.deposit(bank, account, 20_000, DATE);
        processor.deposit(bank, accountB, 15_000, DATE);
        processor.withdraw(bank, account, 5_000, DATE);
        processor.internalTransfer(bank, account, accountB, 3_000, DATE);
        processor.externalTransferOut(bank, accountB, 2_000, DATE);

        assertTrue(bank.getBalanceSheet().isBalanced());
        assertTrue(bank.getBalanceSheet().isSolvent());
    }
}
