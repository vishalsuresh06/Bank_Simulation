package com.boma.banksim.service;

import com.boma.banksim.bank.Bank;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.RateModel;
import com.boma.banksim.ledger.LedgerEntry;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanApplication;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.transaction.Transaction;
import com.boma.banksim.transaction.TransactionType;
import com.boma.banksim.util.IdGenerator;
import com.boma.banksim.util.MathUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles loan origination, payment processing, and delinquency evaluation.
 */
public class LoanService {

    /**
     * Issues a new loan to the customer.
     *
     * Accounting: DR LOANS  /  CR RESERVES
     * (Bank converts cash reserves into a loan receivable asset)
     */
    public Loan issueLoan(Bank bank, Customer customer, LoanApplication application,
                          EconomicEnvironment economy, LocalDate date) {
        if (!application.isApproved()) {
            throw new IllegalStateException("Cannot issue loan — application not approved.");
        }
        if (bank.getBalanceSheet().getReserves() < application.getRequestedAmount()) {
            throw new IllegalStateException("Insufficient reserves to fund loan.");
        }

        double rate = RateModel.loanRate(economy, application.getType());
        rate = RateModel.adjustForCreditScore(rate, application.getCreditScore());
        double monthlyPayment = MathUtils.monthlyPayment(
                application.getRequestedAmount(), rate, application.getRequestedTermMonths());

        String loanId = IdGenerator.generate("LN");
        Loan loan = new Loan(
                loanId,
                customer.getCustomerId(),
                application.getType(),
                application.getRequestedAmount(),
                rate,
                application.getRequestedTermMonths(),
                monthlyPayment,
                date
        );

        bank.addLoan(loan);
        customer.addLoanId(loanId);

        // Balance sheet: loans ↑, reserves ↓
        bank.getBalanceSheet().increaseLoans(application.getRequestedAmount());
        bank.getBalanceSheet().decreaseReserves(application.getRequestedAmount());

        // Ledger entry
        String txId = IdGenerator.generate("LO");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "LOANS", "RESERVES", application.getRequestedAmount(),
                "Loan origination " + loanId));

        bank.logTransaction(new Transaction(txId, TransactionType.LOAN_ORIGINATION,
                application.getRequestedAmount(),
                LocalDateTime.of(date, java.time.LocalTime.NOON),
                bank.getBankId(), customer.getCustomerId(),
                "Loan origination"));

        return loan;
    }

    /**
     * Processes a monthly loan repayment.
     *
     * Accounting (principal portion): DR RESERVES  /  CR LOANS
     * Accounting (interest portion):  DR RESERVES  /  CR INTEREST_INCOME
     */
    public void processPayment(Bank bank, Loan loan, double payment, LocalDate date) {
        double interestCollected = loan.applyPayment(payment);
        double principalRepaid = payment - interestCollected;

        // Balance sheet
        bank.getBalanceSheet().increaseReserves(payment);
        if (principalRepaid > 0) {
            bank.getBalanceSheet().decreaseLoans(principalRepaid);
        }
        bank.getBalanceSheet().recordInterestIncome(interestCollected);

        // Ledger entries
        String txId = IdGenerator.generate("LP");
        if (principalRepaid > 0) {
            bank.getLedger().addEntry(new LedgerEntry(
                    IdGenerator.generate("LE"), txId, date,
                    "RESERVES", "LOANS", principalRepaid,
                    "Loan principal repayment " + loan.getLoanId()));
        }
        if (interestCollected > 0) {
            bank.getLedger().addEntry(new LedgerEntry(
                    IdGenerator.generate("LE"), txId, date,
                    "RESERVES", "INTEREST_INCOME", interestCollected,
                    "Loan interest payment " + loan.getLoanId()));
        }

        bank.logTransaction(new Transaction(txId, TransactionType.LOAN_REPAYMENT,
                payment, LocalDateTime.of(date, java.time.LocalTime.NOON),
                loan.getCustomerId(), bank.getBankId(), "Loan repayment"));
    }

    /**
     * Checks all active loans to see if a payment was missed.
     * If a customer has no checking account with enough funds to pay, the loan is marked late.
     */
    public List<Loan> evaluateDelinquency(Bank bank) {
        List<Loan> wentLate = new ArrayList<>();
        for (Loan loan : bank.getAllLoans()) {
            if (!loan.isActive()) continue;

            Customer customer = bank.getCustomer(loan.getCustomerId());
            if (customer == null) continue;

            boolean canPay = false;
            for (String accountId : customer.getAccountIds()) {
                var account = bank.getAccount(accountId);
                if (account != null && account.getBalance() >= loan.getMonthlyPayment()) {
                    canPay = true;
                    break;
                }
            }

            if (!canPay) {
                LoanStatus before = loan.getStatus();
                loan.incrementDaysLate();
                if (loan.getStatus() != before) {
                    wentLate.add(loan);
                }
            }
        }
        return wentLate;
    }

    /** Returns all active loans belonging to a specific customer. */
    public List<Loan> getActiveLoans(Bank bank, String customerId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : bank.getLoansForCustomer(customerId)) {
            if (loan.isActive()) result.add(loan);
        }
        return result;
    }

    /** Sums monthly payments owed by a customer across all active loans. */
    public double totalMonthlyObligations(Bank bank, String customerId) {
        return getActiveLoans(bank, customerId).stream()
                .mapToDouble(Loan::getMonthlyPayment)
                .sum();
    }
}
