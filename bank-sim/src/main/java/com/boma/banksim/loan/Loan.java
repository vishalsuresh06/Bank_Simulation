package com.boma.banksim.loan;

import java.time.LocalDate;

/**
 * Represents an outstanding loan — a bank asset.
 *
 * Accounting principle: loans are assets. When a loan is issued the bank
 * converts reserves (cash) into a loan receivable. When repaid, the loan
 * receivable decreases and reserves increase.
 */
public class Loan {

    private final String loanId;
    private final String customerId;
    private final LoanType type;
    private final double principal;
    private double outstandingBalance;
    private final double annualInterestRate;
    private final int termMonths;
    private final double monthlyPayment;
    private LoanStatus status;
    private int daysLate;
    private final LocalDate originationDate;

    public Loan(
            String loanId,
            String customerId,
            LoanType type,
            double principal,
            double annualInterestRate,
            int termMonths,
            double monthlyPayment,
            LocalDate originationDate
    ) {
        if (loanId == null || loanId.isBlank()) throw new IllegalArgumentException("Loan ID cannot be blank.");
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("Customer ID cannot be blank.");
        if (type == null) throw new IllegalArgumentException("Loan type cannot be null.");
        if (principal <= 0) throw new IllegalArgumentException("Principal must be positive.");
        if (annualInterestRate < 0) throw new IllegalArgumentException("Interest rate cannot be negative.");
        if (termMonths <= 0) throw new IllegalArgumentException("Term must be positive.");
        if (monthlyPayment <= 0) throw new IllegalArgumentException("Monthly payment must be positive.");
        if (originationDate == null) throw new IllegalArgumentException("Origination date cannot be null.");

        this.loanId = loanId;
        this.customerId = customerId;
        this.type = type;
        this.principal = principal;
        this.outstandingBalance = principal;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.monthlyPayment = monthlyPayment;
        this.status = LoanStatus.CURRENT;
        this.daysLate = 0;
        this.originationDate = originationDate;
    }

    /**
     * Applies a payment to the loan. Returns the interest portion of the payment.
     * The principal portion reduces outstandingBalance.
     *
     * @param payment amount paid
     * @return interest collected (credited to bank income)
     */
    public double applyPayment(double payment) {
        if (payment <= 0) throw new IllegalArgumentException("Payment must be positive.");
        if (status == LoanStatus.CHARGED_OFF || status == LoanStatus.CLOSED) {
            throw new IllegalStateException("Cannot apply payment to a " + status + " loan.");
        }

        double monthlyRate = annualInterestRate / 12.0;
        double interestDue = Math.round(outstandingBalance * monthlyRate * 100.0) / 100.0;
        double actualPayment = Math.min(payment, outstandingBalance + interestDue);

        double interestPaid = Math.min(actualPayment, interestDue);
        double principalPaid = actualPayment - interestPaid;

        outstandingBalance = Math.max(0, outstandingBalance - principalPaid);

        if (outstandingBalance == 0) {
            status = LoanStatus.CLOSED;
        } else if (daysLate > 0) {
            daysLate = 0;
            status = LoanStatus.CURRENT;
        }

        return interestPaid;
    }

    /**
     * Advances daysLate by 30 (one missed monthly payment) and updates status.
     */
    public void incrementDaysLate() {
        if (status == LoanStatus.CHARGED_OFF || status == LoanStatus.CLOSED) return;
        daysLate += 30;
        if (daysLate >= 180) {
            status = LoanStatus.DEFAULTED;
        } else if (daysLate >= 90) {
            status = LoanStatus.DELINQUENT;
        } else if (daysLate >= 30) {
            status = LoanStatus.LATE;
        }
    }

    /**
     * Marks the loan as CHARGED_OFF and returns the outstanding balance that will
     * be written off as a loss on the bank's balance sheet.
     */
    public double chargeOff() {
        if (status == LoanStatus.CHARGED_OFF || status == LoanStatus.CLOSED) {
            throw new IllegalStateException("Loan is already " + status + ".");
        }
        double loss = outstandingBalance;
        outstandingBalance = 0;
        status = LoanStatus.CHARGED_OFF;
        return loss;
    }

    public boolean isActive() {
        return status == LoanStatus.CURRENT || status == LoanStatus.LATE || status == LoanStatus.DELINQUENT;
    }

    public boolean isDelinquent() {
        return status == LoanStatus.DELINQUENT || status == LoanStatus.DEFAULTED;
    }

    // ---- Getters ----

    public String getLoanId() { return loanId; }
    public String getCustomerId() { return customerId; }
    public LoanType getType() { return type; }
    public double getPrincipal() { return principal; }
    public double getOutstandingBalance() { return outstandingBalance; }
    public double getAnnualInterestRate() { return annualInterestRate; }
    public int getTermMonths() { return termMonths; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public LoanStatus getStatus() { return status; }
    public int getDaysLate() { return daysLate; }
    public LocalDate getOriginationDate() { return originationDate; }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId='" + loanId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", type=" + type +
                ", principal=" + principal +
                ", outstanding=" + outstandingBalance +
                ", rate=" + annualInterestRate +
                ", status=" + status +
                ", daysLate=" + daysLate +
                '}';
    }
}
