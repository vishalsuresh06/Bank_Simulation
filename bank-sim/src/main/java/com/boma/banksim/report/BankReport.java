package com.boma.banksim.report;

import com.boma.banksim.bank.Bank;
import com.boma.banksim.bank.BankBalanceSheet;

import java.time.LocalDate;

/**
 * Prints a point-in-time snapshot of the bank's financial position.
 */
public class BankReport {

    public void print(Bank bank, LocalDate date) {
        BankBalanceSheet bs = bank.getBalanceSheet();

        System.out.println();
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.printf( "│ BANK SNAPSHOT  %-26s│%n", date.toString());
        System.out.printf( "│ %-40s│%n", bank.getName());
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ ASSETS                                  │");
        System.out.printf( "│   Reserves          : %,14.2f     │%n", bs.getReserves());
        System.out.printf( "│   Loan Portfolio    : %,14.2f     │%n", bs.getTotalLoans());
        System.out.printf( "│   Securities        : %,14.2f     │%n", bank.getTreasury().getSecurities());
        System.out.printf( "│   Total Assets      : %,14.2f     │%n", bs.getTotalAssets() + bank.getTreasury().getSecurities());
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ LIABILITIES                             │");
        System.out.printf( "│   Deposits          : %,14.2f     │%n", bs.getTotalDeposits());
        System.out.printf( "│   Borrowings        : %,14.2f     │%n", bank.getTreasury().getBorrowings());
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ EQUITY                                  │");
        System.out.printf( "│   Equity (A − L)    : %,14.2f     │%n", bs.getEquity());
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ PERIOD INCOME                           │");
        System.out.printf( "│   Gross Interest    : %,14.2f     │%n", bs.getGrossInterestIncome());
        System.out.printf( "│   Deposit Expense   : %,14.2f     │%n", bs.getDepositInterestExpense());
        System.out.printf( "│   Net Interest Inc  : %,14.2f     │%n", bs.getNetInterestIncome());
        System.out.printf( "│   Charge-Offs       : %,14.2f     │%n", bs.getChargeOffLosses());
        System.out.printf( "│   Net Income        : %,14.2f     │%n", bs.getNetIncome());
        System.out.println("├─────────────────────────────────────────┤");
        System.out.printf( "│ Customers: %-5d  Accounts: %-5d      │%n",
                bank.getCustomerCount(), bank.getAccountCount());
        System.out.printf( "│ Active Loans: %-5d                     │%n", bank.getLoanCount());
        System.out.println("└─────────────────────────────────────────┘");
    }
}
