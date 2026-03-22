package com.boma.banksim.loan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates the full amortization schedule for a fixed-rate loan.
 * Each installment shows how much of the payment goes to interest vs. principal.
 */
public class LoanPaymentSchedule {

    public record PaymentInstallment(
            int month,
            double payment,
            double principalPortion,
            double interestPortion,
            double remainingBalance
    ) {}

    private final List<PaymentInstallment> installments;

    private LoanPaymentSchedule(List<PaymentInstallment> installments) {
        this.installments = Collections.unmodifiableList(installments);
    }

    /**
     * Generates an amortization schedule.
     *
     * @param principal  original loan amount
     * @param annualRate annual interest rate (e.g. 0.06 for 6%)
     * @param termMonths number of monthly payments
     * @return schedule with one entry per month
     */
    public static LoanPaymentSchedule generate(double principal, double annualRate, int termMonths) {
        if (principal <= 0) throw new IllegalArgumentException("Principal must be positive.");
        if (annualRate < 0) throw new IllegalArgumentException("Annual rate cannot be negative.");
        if (termMonths <= 0) throw new IllegalArgumentException("Term must be positive.");

        List<PaymentInstallment> schedule = new ArrayList<>(termMonths);
        double monthlyRate = annualRate / 12.0;
        double monthlyPayment;

        if (annualRate == 0.0) {
            monthlyPayment = principal / termMonths;
        } else {
            double factor = Math.pow(1 + monthlyRate, termMonths);
            monthlyPayment = principal * (monthlyRate * factor) / (factor - 1);
        }
        monthlyPayment = Math.round(monthlyPayment * 100.0) / 100.0;

        double balance = principal;
        for (int month = 1; month <= termMonths; month++) {
            double interest = Math.round(balance * monthlyRate * 100.0) / 100.0;
            double payment = (month == termMonths) ? Math.round((balance + interest) * 100.0) / 100.0 : monthlyPayment;
            double principalPaid = payment - interest;
            balance = Math.max(0, Math.round((balance - principalPaid) * 100.0) / 100.0);
            schedule.add(new PaymentInstallment(month, payment, principalPaid, interest, balance));
        }

        return new LoanPaymentSchedule(schedule);
    }

    public List<PaymentInstallment> getInstallments() {
        return installments;
    }

    public int size() {
        return installments.size();
    }

    public PaymentInstallment getInstallment(int month) {
        if (month < 1 || month > installments.size()) {
            throw new IllegalArgumentException("Month out of range: " + month);
        }
        return installments.get(month - 1);
    }

    /** Total interest the borrower pays over the life of the loan. */
    public double totalInterestCost() {
        return installments.stream().mapToDouble(PaymentInstallment::interestPortion).sum();
    }
}
