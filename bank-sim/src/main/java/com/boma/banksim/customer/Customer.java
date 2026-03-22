package com.boma.banksim.customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {

    private final String customerId;
    private final String name;
    private final CustomerType type;
    private final CustomerProfile profile;
    private final List<String> accountIds;
    private final List<String> loanIds;

    public Customer(String customerId, String name, CustomerType type, CustomerProfile profile) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Customer type cannot be null.");
        }
        if (profile == null) {
            throw new IllegalArgumentException("Customer profile cannot be null.");
        }
        this.customerId = customerId;
        this.name = name;
        this.type = type;
        this.profile = profile;
        this.accountIds = new ArrayList<>();
        this.loanIds = new ArrayList<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public CustomerType getType() {
        return type;
    }

    public CustomerProfile getProfile() {
        return profile;
    }

    public List<String> getAccountIds() {
        return Collections.unmodifiableList(accountIds);
    }

    public List<String> getLoanIds() {
        return Collections.unmodifiableList(loanIds);
    }

    public void addAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank.");
        }
        if (!accountIds.contains(accountId)) {
            accountIds.add(accountId);
        }
    }

    public void removeAccountId(String accountId) {
        accountIds.remove(accountId);
    }

    public void addLoanId(String loanId) {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("Loan ID cannot be null or blank.");
        }
        if (!loanIds.contains(loanId)) {
            loanIds.add(loanId);
        }
    }

    public void removeLoanId(String loanId) {
        loanIds.remove(loanId);
    }

    public boolean hasLoans() {
        return !loanIds.isEmpty();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", accounts=" + accountIds.size() +
                ", loans=" + loanIds.size() +
                '}';
    }
}
