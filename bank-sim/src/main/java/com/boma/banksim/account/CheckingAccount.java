package com.boma.banksim.account;

/**
 * A demand deposit account with no interest. Highly liquid — customers can
 * withdraw at any time. Represents a core liability for the bank.
 */
public class CheckingAccount extends Account {

    public CheckingAccount(String accountId, String customerId, double initialBalance) {
        super(accountId, customerId, AccountType.CHECKING, initialBalance);
    }

    @Override
    public double applyMonthlyInterest() {
        return 0.0;
    }

    @Override
    public double getInterestRate() {
        return 0.0;
    }

    @Override
    public String toString() {
        return "CheckingAccount{accountId='" + getAccountId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", balance=" + getBalance() + '}';
    }
}
