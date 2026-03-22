package com.boma.banksim.account;

/**
 * An interest-bearing deposit account. The bank pays interest monthly,
 * which increases its deposit liabilities (and reduces equity via interest expense).
 */
public class SavingsAccount extends Account {

    private double annualInterestRate;

    public SavingsAccount(String accountId, String customerId, double initialBalance, double annualInterestRate) {
        super(accountId, customerId, AccountType.SAVINGS, initialBalance);
        if (annualInterestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }
        this.annualInterestRate = annualInterestRate;
    }

    /**
     * Applies one month of interest: credits the account and returns the interest amount
     * so the caller (InterestAccrualService) can update the bank's balance sheet.
     */
    @Override
    public double applyMonthlyInterest() {
        double monthlyRate = annualInterestRate / 12.0;
        double interest = Math.round(getBalance() * monthlyRate * 100.0) / 100.0;
        if (interest > 0) {
            deposit(interest);
        }
        return interest;
    }

    @Override
    public double getInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }
        this.annualInterestRate = rate;
    }

    @Override
    public String toString() {
        return "SavingsAccount{accountId='" + getAccountId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", balance=" + getBalance() +
                ", annualRate=" + annualInterestRate + '}';
    }
}
