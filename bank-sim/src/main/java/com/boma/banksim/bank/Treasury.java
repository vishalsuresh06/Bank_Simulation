package com.boma.banksim.bank;

/**
 * Manages the bank's investment portfolio (securities) and wholesale funding (borrowings).
 *
 * Securities are assets the bank holds to earn yield while maintaining liquidity.
 * Borrowings are short-term liabilities the bank uses to fund operations when
 * deposit funding is insufficient.
 */
public class Treasury {

    private double securities;
    private double borrowings;

    public Treasury(double initialSecurities, double initialBorrowings) {
        if (initialSecurities < 0) throw new IllegalArgumentException("Securities cannot be negative.");
        if (initialBorrowings < 0) throw new IllegalArgumentException("Borrowings cannot be negative.");
        this.securities = initialSecurities;
        this.borrowings = initialBorrowings;
    }

    public Treasury() {
        this(0.0, 0.0);
    }

    // ---- Securities ----

    public void purchaseSecurities(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        securities += amount;
    }

    public void sellSecurities(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        if (amount > securities) throw new IllegalArgumentException("Cannot sell more securities than held.");
        securities -= amount;
    }

    // ---- Borrowings ----

    public void borrow(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        borrowings += amount;
    }

    public void repayBorrowing(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        if (amount > borrowings) throw new IllegalArgumentException("Cannot repay more than outstanding borrowings.");
        borrowings -= amount;
    }

    public double getSecurities() { return securities; }
    public double getBorrowings() { return borrowings; }

    public double netPosition() {
        return securities - borrowings;
    }

    @Override
    public String toString() {
        return "Treasury{securities=" + securities + ", borrowings=" + borrowings + '}';
    }
}
