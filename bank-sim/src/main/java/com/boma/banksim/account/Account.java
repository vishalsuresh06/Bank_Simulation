package com.boma.banksim.account;

public abstract class Account {

    private final String accountId;
    private final String customerId;
    private final AccountType type;
    private double balance;

    protected Account(String accountId, String customerId, AccountType type, double initialBalance) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Account type cannot be null.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountId = accountId;
        this.customerId = customerId;
        this.type = type;
        this.balance = initialBalance;
    }

    /**
     * Credits the account (adds funds). From the bank's perspective this increases
     * the deposit liability.
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.balance += amount;
    }

    /**
     * Debits the account (removes funds). From the bank's perspective this reduces
     * the deposit liability.
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds: balance=" + balance + ", requested=" + amount);
        }
        this.balance -= amount;
    }

    /**
     * Applies one month of interest to this account.
     *
     * @return the interest amount credited (0.0 for non-interest-bearing accounts)
     */
    public abstract double applyMonthlyInterest();

    /** Annual interest rate as a decimal (e.g. 0.04 = 4%). */
    public abstract double getInterestRate();

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public AccountType getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", type=" + type +
                ", balance=" + balance +
                '}';
    }
}
