package com.boma.banksim.util;

public class MathUtils {

    private MathUtils() {}

    /**
     * Calculates the fixed monthly payment for a fully-amortizing loan.
     * Uses the standard annuity formula: M = P * [r(1+r)^n] / [(1+r)^n - 1]
     *
     * @param principal   loan principal
     * @param annualRate  annual interest rate (e.g. 0.06 for 6%)
     * @param termMonths  number of monthly payments
     * @return monthly payment amount
     */
    public static double monthlyPayment(double principal, double annualRate, int termMonths) {
        if (annualRate == 0.0) {
            return principal / termMonths;
        }
        double r = annualRate / 12.0;
        double factor = Math.pow(1 + r, termMonths);
        return principal * (r * factor) / (factor - 1);
    }

    /**
     * Rounds a value to 2 decimal places (cents).
     */
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Clamps a value between min and max (inclusive).
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns the debt-to-income (DTI) ratio given monthly debt payments and monthly income.
     */
    public static double debtToIncome(double monthlyDebtPayments, double monthlyIncome) {
        if (monthlyIncome <= 0) return Double.MAX_VALUE;
        return monthlyDebtPayments / monthlyIncome;
    }
}
