package com.boma.banksim.service;

import com.boma.banksim.account.Account;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.ledger.LedgerEntry;
import com.boma.banksim.transaction.Transaction;
import com.boma.banksim.transaction.TransactionType;
import com.boma.banksim.util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Executes deposit, withdrawal, and transfer operations.
 *
 * Every operation:
 *   1. Updates the account balance
 *   2. Updates the bank's balance sheet (reserves and/or deposits)
 *   3. Records a double-entry LedgerEntry
 *   4. Logs the Transaction in the bank's audit trail
 */
public class PaymentProcessor {

    /**
     * Customer deposits cash into their account.
     *
     * Accounting: DR RESERVES  /  CR DEPOSITS
     */
    public void deposit(Bank bank, Account account, double amount, LocalDate date) {
        validateAmount(amount);

        account.deposit(amount);
        bank.getBalanceSheet().increaseReserves(amount);
        bank.getBalanceSheet().increaseDeposits(amount);

        String txId = IdGenerator.generate("DEP");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "RESERVES", "DEPOSITS", amount,
                "Customer deposit to " + account.getAccountId()));

        bank.logTransaction(new Transaction(txId, TransactionType.DEPOSIT, amount,
                LocalDateTime.of(date, java.time.LocalTime.NOON),
                null, account.getAccountId(), "Deposit"));
    }

    /**
     * Customer withdraws cash from their account.
     *
     * Accounting: DR DEPOSITS  /  CR RESERVES
     */
    public void withdraw(Bank bank, Account account, double amount, LocalDate date) {
        validateAmount(amount);

        account.withdraw(amount);
        bank.getBalanceSheet().decreaseDeposits(amount);
        bank.getBalanceSheet().decreaseReserves(amount);

        String txId = IdGenerator.generate("WD");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "DEPOSITS", "RESERVES", amount,
                "Customer withdrawal from " + account.getAccountId()));

        bank.logTransaction(new Transaction(txId, TransactionType.WITHDRAWAL, amount,
                LocalDateTime.of(date, java.time.LocalTime.NOON),
                account.getAccountId(), null, "Withdrawal"));
    }

    /**
     * Transfers funds between two accounts at the same bank.
     * Net balance sheet effect is zero (deposit liability moves between accounts).
     *
     * Accounting: DR DEPOSITS(from)  /  CR DEPOSITS(to)  — internal reclassification
     * For simplicity we record a single ledger entry with category INTERNAL_TRANSFER.
     */
    public void internalTransfer(Bank bank, Account from, Account to, double amount, LocalDate date) {
        validateAmount(amount);

        from.withdraw(amount);
        to.deposit(amount);
        // Net balance sheet impact: zero (deposits stay same, reserves stay same)

        String txId = IdGenerator.generate("INT");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "DEPOSITS", "DEPOSITS", amount,
                "Internal transfer " + from.getAccountId() + " → " + to.getAccountId()));

        bank.logTransaction(new Transaction(txId, TransactionType.INTERNAL_TRANSFER, amount,
                LocalDateTime.of(date, java.time.LocalTime.NOON),
                from.getAccountId(), to.getAccountId(), "Internal transfer"));
    }

    /**
     * External wire received — new money comes in from outside the bank.
     *
     * Accounting: DR RESERVES  /  CR DEPOSITS
     */
    public void externalTransferIn(Bank bank, Account account, double amount, LocalDate date) {
        validateAmount(amount);

        account.deposit(amount);
        bank.getBalanceSheet().increaseReserves(amount);
        bank.getBalanceSheet().increaseDeposits(amount);

        String txId = IdGenerator.generate("EXT-IN");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "RESERVES", "DEPOSITS", amount,
                "External transfer in to " + account.getAccountId()));

        bank.logTransaction(new Transaction(txId, TransactionType.EXTERNAL_TRANSFER_IN, amount,
                LocalDateTime.of(date, java.time.LocalTime.NOON),
                null, account.getAccountId(), "External transfer in"));
    }

    /**
     * External wire sent — money leaves the bank.
     *
     * Accounting: DR DEPOSITS  /  CR RESERVES
     */
    public void externalTransferOut(Bank bank, Account account, double amount, LocalDate date) {
        validateAmount(amount);

        account.withdraw(amount);
        bank.getBalanceSheet().decreaseDeposits(amount);
        bank.getBalanceSheet().decreaseReserves(amount);

        String txId = IdGenerator.generate("EXT-OUT");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "DEPOSITS", "RESERVES", amount,
                "External transfer out from " + account.getAccountId()));

        bank.logTransaction(new Transaction(txId, TransactionType.EXTERNAL_TRANSFER_OUT, amount,
                LocalDateTime.of(date, java.time.LocalTime.NOON),
                account.getAccountId(), null, "External transfer out"));
    }

    private void validateAmount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
    }
}
