package com.boma.banksim.customer;

public class CustomerProfile {
    private double income;
    private double spendingRate;
    private double creditScore;
    private double withdrawalSensitivity;
    private double rateSensitivity;

    public CustomerProfile(
            double income,
            double spendingRate,
            double creditScore,
            double withdrawalSensitivity,
            double rateSensitivity
    ) {
        validateNonNegative(income, "Income");
        validateNonNegative(spendingRate, "Spending rate");
        validateSensitivity(withdrawalSensitivity, "Withdrawal sensitivity");
        validateSensitivity(rateSensitivity, "Rate sensitivity");

        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("Credit score must be between 300 and 850.");
        }

        this.income = income;
        this.spendingRate = spendingRate;
        this.creditScore = creditScore;
        this.withdrawalSensitivity = withdrawalSensitivity;
        this.rateSensitivity = rateSensitivity;
    }

    //Getters
    public double getIncome() {
        return income;
    }
    public double getSpendingRate() {
        return spendingRate;
    }
    public double getCreditScore() {
        return creditScore;
    }
    public double getWithdrawalSensitivity() {
        return withdrawalSensitivity;
    }
    public double getRateSensitivity() {
        return rateSensitivity;
    }

    //Setters
    public void setIncome(double income) {
        validateNonNegative(income, "Income");
        this.income = income;
    }
    public void setSpendingRate(double spendingRate) {
        validateNonNegative(spendingRate, "Spending rate");
        this.spendingRate = spendingRate;
    }
    public void setCreditScore(double creditScore) {
        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("Credit score must be between 300 and 850.");
        }
        this.creditScore = creditScore;
    }
    public void setWithdrawalSensitivity(double withdrawalSensitivity) {
        validateSensitivity(withdrawalSensitivity, "Withdrawal sensitivity");
        this.withdrawalSensitivity = withdrawalSensitivity;
    }
    public void setRateSensitivity(double rateSensitivity) {
        validateSensitivity(rateSensitivity, "Rate sensitivity");
        this.rateSensitivity = rateSensitivity;
    }

    private void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
    }

    private void validateSensitivity(double value, String fieldName) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 1.");
        }
    }

    @Override
    public String toString() {
        return "CustomerProfile{" +
                "income=" + income +
                ", spendingRate=" + spendingRate +
                ", creditScore=" + creditScore +
                ", withdrawalSensitivity=" + withdrawalSensitivity +
                ", rateSensitivity=" + rateSensitivity +
                '}';
    }
}
