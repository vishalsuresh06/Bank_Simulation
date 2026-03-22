package com.boma.banksim.bank;

public class BankBalanceSheet {
    private double reserves;
    private double totalLoans;
    private double totalDeposits;

    // Period income tracking (for reporting; does not affect balance sheet totals directly)
    private double grossInterestIncome;
    private double depositInterestExpense;
    private double chargeOffLosses;

    public BankBalanceSheet(double initialReserves, double initialLoans, double initialDeposits) {
        validateNonNegative(initialReserves, "Initial reserves");
        validateNonNegative(initialLoans, "Initial loans");
        validateNonNegative(initialDeposits, "Initial deposits");

        this.reserves = initialReserves;
        this.totalLoans = initialLoans;
        this.totalDeposits = initialDeposits;
    }

    public double getReserves() {
        return reserves;
    }

    public double getTotalLoans() {
        return totalLoans;
    }

    public double getTotalDeposits() {
        return totalDeposits;
    }

    public double getTotalAssets() {
        return reserves + totalLoans;
    }

    public double getTotalLiabilities() {
        return totalDeposits;
    }

    public double getEquity() {
        return getTotalAssets() - getTotalLiabilities();
    }

    public void increaseReserves(double amount) {
        validatePositive(amount, "Reserve increase amount");
        reserves += amount;
    }

    public void decreaseReserves(double amount) {
        validatePositive(amount, "Reserve decrease amount");
        if (amount > reserves) {
            throw new IllegalArgumentException("Cannot decrease reserves below zero.");
        }
        reserves -= amount;
    }

    public void increaseLoans(double amount) {
        validatePositive(amount, "Loan increase amount");
        totalLoans += amount;
    }

    public void decreaseLoans(double amount) {
        validatePositive(amount, "Loan decrease amount");
        if (amount > totalLoans) {
            throw new IllegalArgumentException("Cannot decrease loans below zero.");
        }
        totalLoans -= amount;
    }

    public void increaseDeposits(double amount) {
        validatePositive(amount, "Deposit increase amount");
        totalDeposits += amount;
    }

    public void decreaseDeposits(double amount) {
        validatePositive(amount, "Deposit decrease amount");
        if (amount > totalDeposits) {
            throw new IllegalArgumentException("Cannot decrease deposits below zero.");
        }
        totalDeposits -= amount;
    }

    // ---- Income tracking ----

    public void recordInterestIncome(double amount) {
        if (amount > 0) grossInterestIncome += amount;
    }

    public void recordDepositInterestExpense(double amount) {
        if (amount > 0) depositInterestExpense += amount;
    }

    public void recordChargeOff(double amount) {
        if (amount > 0) chargeOffLosses += amount;
    }

    public double getGrossInterestIncome() { return grossInterestIncome; }
    public double getDepositInterestExpense() { return depositInterestExpense; }
    public double getChargeOffLosses() { return chargeOffLosses; }

    public double getNetInterestIncome() {
        return grossInterestIncome - depositInterestExpense;
    }

    public double getNetIncome() {
        return grossInterestIncome - depositInterestExpense - chargeOffLosses;
    }

    /** Resets period income/expense counters (call at start of each new period). */
    public void resetPeriodCounters() {
        grossInterestIncome = 0;
        depositInterestExpense = 0;
        chargeOffLosses = 0;
    }

    /**
     * Checks that Assets = Liabilities + Equity (within rounding tolerance).
     * Because equity is derived, this is always true — but the check guards
     * against negative equity (insolvency).
     */
    public boolean isSolvent() {
        return getEquity() >= 0;
    }

    public boolean isBalanced() {
        double assets = getTotalAssets();
        double liabPlusEquity = getTotalLiabilities() + getEquity();
        return Math.abs(assets - liabPlusEquity) < 0.01;
    }

    private void validatePositive(double amount, String fieldName) {
        if (amount <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    private void validateNonNegative(double amount, String fieldName) {
        if (amount < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
    }

    @Override
    public String toString() {
        return "BankBalanceSheet{" +
                "reserves=" + reserves +
                ", totalLoans=" + totalLoans +
                ", totalDeposits=" + totalDeposits +
                ", totalAssets=" + getTotalAssets() +
                ", totalLiabilities=" + getTotalLiabilities() +
                ", equity=" + getEquity() +
                '}';
    }
}