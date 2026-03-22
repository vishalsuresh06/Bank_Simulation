package com.boma.banksim.loan;

/**
 * Represents a customer's request for a loan before it has been evaluated.
 * The underwriting engine inspects this object and either approves or rejects it.
 */
public class LoanApplication {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    private final String applicationId;
    private final String customerId;
    private final LoanType type;
    private final double requestedAmount;
    private final double monthlyIncome;
    private final double existingMonthlyDebtPayments;
    private final double creditScore;
    private final int requestedTermMonths;
    private Status status;
    private String rejectionReason;

    public LoanApplication(
            String applicationId,
            String customerId,
            LoanType type,
            double requestedAmount,
            double monthlyIncome,
            double existingMonthlyDebtPayments,
            double creditScore,
            int requestedTermMonths
    ) {
        if (applicationId == null || applicationId.isBlank()) throw new IllegalArgumentException("Application ID cannot be blank.");
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("Customer ID cannot be blank.");
        if (type == null) throw new IllegalArgumentException("Loan type cannot be null.");
        if (requestedAmount <= 0) throw new IllegalArgumentException("Requested amount must be positive.");
        if (monthlyIncome <= 0) throw new IllegalArgumentException("Monthly income must be positive.");
        if (creditScore < 300 || creditScore > 850) throw new IllegalArgumentException("Credit score must be 300–850.");
        if (requestedTermMonths <= 0) throw new IllegalArgumentException("Term must be positive.");

        this.applicationId = applicationId;
        this.customerId = customerId;
        this.type = type;
        this.requestedAmount = requestedAmount;
        this.monthlyIncome = monthlyIncome;
        this.existingMonthlyDebtPayments = Math.max(0, existingMonthlyDebtPayments);
        this.creditScore = creditScore;
        this.requestedTermMonths = requestedTermMonths;
        this.status = Status.PENDING;
    }

    public void approve() {
        if (status != Status.PENDING) throw new IllegalStateException("Application is not pending.");
        this.status = Status.APPROVED;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        if (status != Status.PENDING) throw new IllegalStateException("Application is not pending.");
        this.status = Status.REJECTED;
        this.rejectionReason = reason;
    }

    public boolean isPending() { return status == Status.PENDING; }
    public boolean isApproved() { return status == Status.APPROVED; }
    public boolean isRejected() { return status == Status.REJECTED; }

    // ---- Getters ----

    public String getApplicationId() { return applicationId; }
    public String getCustomerId() { return customerId; }
    public LoanType getType() { return type; }
    public double getRequestedAmount() { return requestedAmount; }
    public double getMonthlyIncome() { return monthlyIncome; }
    public double getExistingMonthlyDebtPayments() { return existingMonthlyDebtPayments; }
    public double getCreditScore() { return creditScore; }
    public int getRequestedTermMonths() { return requestedTermMonths; }
    public Status getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }

    @Override
    public String toString() {
        return "LoanApplication{" +
                "applicationId='" + applicationId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", type=" + type +
                ", amount=" + requestedAmount +
                ", creditScore=" + creditScore +
                ", status=" + status +
                (rejectionReason != null ? ", reason='" + rejectionReason + '\'' : "") +
                '}';
    }
}
