package com.boma.banksim.bank;

import com.boma.banksim.account.Account;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.ledger.Ledger;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The root entity of the simulation. The Bank owns all accounts, loans,
 * customers, and the financial infrastructure (balance sheet, ledger, treasury).
 *
 * Banking fundamentals encoded here:
 *   - Deposits  → bank liabilities  (bank owes money to depositors)
 *   - Loans     → bank assets       (borrowers owe money to the bank)
 *   - Reserves  → bank assets       (cash on hand / central bank deposits)
 *   - Equity    = Assets − Liabilities
 */
public class Bank {

    private final String bankId;
    private final String name;

    private final Map<String, Customer> customers;
    private final Map<String, Account> accounts;
    private final Map<String, Loan> loans;
    private final List<Transaction> transactionLog;

    private final BankBalanceSheet balanceSheet;
    private final Ledger ledger;
    private final Treasury treasury;
    private final LiquidityManager liquidityManager;

    public Bank(String bankId, String name, double initialReserves, double initialEquity) {
        if (bankId == null || bankId.isBlank()) throw new IllegalArgumentException("Bank ID cannot be blank.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Bank name cannot be blank.");
        if (initialReserves < 0) throw new IllegalArgumentException("Initial reserves cannot be negative.");
        if (initialEquity < 0) throw new IllegalArgumentException("Initial equity cannot be negative.");

        this.bankId = bankId;
        this.name = name;
        this.customers = new LinkedHashMap<>();
        this.accounts = new LinkedHashMap<>();
        this.loans = new LinkedHashMap<>();
        this.transactionLog = new ArrayList<>();

        // Start with reserves funded by equity, no deposits or loans yet.
        this.balanceSheet = new BankBalanceSheet(initialReserves, 0.0, 0.0);
        this.ledger = new Ledger();
        this.treasury = new Treasury();
        this.liquidityManager = new LiquidityManager();
    }

    // ---- Customer management ----

    public void addCustomer(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null.");
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer getCustomer(String customerId) {
        return customers.get(customerId);
    }

    public Collection<Customer> getAllCustomers() {
        return Collections.unmodifiableCollection(customers.values());
    }

    public boolean hasCustomer(String customerId) {
        return customers.containsKey(customerId);
    }

    // ---- Account management ----

    public void addAccount(Account account) {
        if (account == null) throw new IllegalArgumentException("Account cannot be null.");
        accounts.put(account.getAccountId(), account);
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    // ---- Loan management ----

    public void addLoan(Loan loan) {
        if (loan == null) throw new IllegalArgumentException("Loan cannot be null.");
        loans.put(loan.getLoanId(), loan);
    }

    public Loan getLoan(String loanId) {
        return loans.get(loanId);
    }

    public Collection<Loan> getAllLoans() {
        return Collections.unmodifiableCollection(loans.values());
    }

    public List<Loan> getLoansForCustomer(String customerId) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans.values()) {
            if (l.getCustomerId().equals(customerId)) result.add(l);
        }
        return result;
    }

    // ---- Transaction log ----

    public void logTransaction(Transaction tx) {
        if (tx != null) transactionLog.add(tx);
    }

    public List<Transaction> getTransactionLog() {
        return Collections.unmodifiableList(transactionLog);
    }

    // ---- Accessors ----

    public String getBankId() { return bankId; }
    public String getName() { return name; }
    public BankBalanceSheet getBalanceSheet() { return balanceSheet; }
    public Ledger getLedger() { return ledger; }
    public Treasury getTreasury() { return treasury; }
    public LiquidityManager getLiquidityManager() { return liquidityManager; }

    public int getCustomerCount() { return customers.size(); }
    public int getAccountCount() { return accounts.size(); }
    public int getLoanCount() { return loans.size(); }

    /** Convenience: sums outstanding balance of all non-closed, non-charged-off loans. */
    public double getTotalOutstandingLoans() {
        return loans.values().stream()
                .filter(Loan::isActive)
                .mapToDouble(Loan::getOutstandingBalance)
                .sum();
    }

    /** Convenience: sums balances of all deposit accounts. */
    public double getTotalDepositBalance() {
        return accounts.values().stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id='" + bankId + '\'' +
                ", name='" + name + '\'' +
                ", customers=" + customers.size() +
                ", accounts=" + accounts.size() +
                ", loans=" + loans.size() +
                ", " + balanceSheet +
                '}';
    }
}
