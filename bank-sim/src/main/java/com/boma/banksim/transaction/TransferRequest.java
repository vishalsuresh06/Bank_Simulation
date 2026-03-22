package com.boma.banksim.transaction;

/**
 * A value object capturing a request to move funds between two accounts.
 * Passed to {@link com.boma.banksim.service.PaymentProcessor} for execution.
 */
public class TransferRequest {

    private final String fromAccountId;
    private final String toAccountId;
    private final double amount;
    private final String description;
    private final boolean external;

    public TransferRequest(String fromAccountId, String toAccountId,
                           double amount, String description, boolean external) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = (description == null) ? "" : description;
        this.external = external;
    }

    public TransferRequest(String fromAccountId, String toAccountId, double amount, String description) {
        this(fromAccountId, toAccountId, amount, description, false);
    }

    public String getFromAccountId() { return fromAccountId; }
    public String getToAccountId() { return toAccountId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public boolean isExternal() { return external; }

    @Override
    public String toString() {
        return "TransferRequest{from='" + fromAccountId + "', to='" + toAccountId +
               "', amount=" + amount + ", external=" + external + '}';
    }
}
