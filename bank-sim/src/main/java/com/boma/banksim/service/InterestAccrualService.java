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
 * Applies monthly interest to all accounts and loans.
 *
 * Deposit interest (bank pays):
 *   Accounting: DR INTEREST_EXPENSE  /  CR DEPOSITS
 *   Effect: deposits ↑, equity ↓ (via increased liabilities)
 *
 * Loan interest (bank earns, collected via LoanService.processPayment):
 *   This service only handles the deposit side. Loan interest accrual
 *   is captured when a payment is processed.
 */
public class InterestAccrualService {

    /**
     * Credits monthly interest to all savings accounts.
     * Updates the bank's balance sheet (deposits ↑) and records ledger entries.
     *
     * @return total deposit interest paid out this month
     */
    public double accrueDepositInterest(Bank bank, LocalDate date) {
        double totalInterestPaid = 0.0;

        for (Account account : bank.getAllAccounts()) {
            double interest = account.applyMonthlyInterest();
            if (interest <= 0) continue;

            totalInterestPaid += interest;

            // Balance sheet: deposits ↑ (we owe more to the customer)
            bank.getBalanceSheet().increaseDeposits(interest);
            bank.getBalanceSheet().recordDepositInterestExpense(interest);

            // Ledger entry: DR INTEREST_EXPENSE  /  CR DEPOSITS
            String txId = IdGenerator.generate("INT-DEP");
            bank.getLedger().addEntry(new LedgerEntry(
                    IdGenerator.generate("LE"), txId, date,
                    "INTEREST_EXPENSE", "DEPOSITS", interest,
                    "Deposit interest on account " + account.getAccountId()));

            bank.logTransaction(new Transaction(txId, TransactionType.INTEREST_ACCRUAL,
                    interest, LocalDateTime.of(date, java.time.LocalTime.NOON),
                    bank.getBankId(), account.getAccountId(), "Deposit interest accrual"));
        }

        return totalInterestPaid;
    }
}
