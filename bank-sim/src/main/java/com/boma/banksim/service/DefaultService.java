package com.boma.banksim.service;

import com.boma.banksim.bank.Bank;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.ledger.LedgerEntry;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.risk.DefaultModel;
import com.boma.banksim.transaction.Transaction;
import com.boma.banksim.transaction.TransactionType;
import com.boma.banksim.util.IdGenerator;
import com.boma.banksim.util.RandomProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates whether defaulted/delinquent loans should be charged off,
 * and performs the charge-off accounting.
 *
 * Charge-off accounting:
 *   DR CHARGE_OFF  /  CR LOANS
 *   Effect: loans ↓, equity ↓ (loss flows through charge-off expense → equity)
 */
public class DefaultService {

    private final DefaultModel defaultModel;
    private final RandomProvider random;

    public DefaultService(RandomProvider random) {
        this.defaultModel = new DefaultModel();
        this.random = random;
    }

    /**
     * Evaluates all delinquent loans. Loans that stochastically default are charged off.
     *
     * @return list of loans that were charged off this step
     */
    public List<Loan> evaluateAndChargeOff(Bank bank, EconomicEnvironment economy, LocalDate date) {
        List<Loan> chargedOff = new ArrayList<>();

        for (Loan loan : bank.getAllLoans()) {
            if (loan.getStatus() == LoanStatus.CHARGED_OFF || loan.getStatus() == LoanStatus.CLOSED) {
                continue;
            }
            if (!loan.isActive() && loan.getStatus() != LoanStatus.DEFAULTED) {
                continue;
            }

            double randomValue = random.nextDouble();
            if (defaultModel.shouldDefault(loan, economy, randomValue)) {
                chargeOff(bank, loan, date);
                chargedOff.add(loan);
            }
        }

        return chargedOff;
    }

    /**
     * Charges off a single loan — writes it off as a loss.
     *
     * Accounting: DR CHARGE_OFF  /  CR LOANS
     */
    public void chargeOff(Bank bank, Loan loan, LocalDate date) {
        double loss = loan.chargeOff();
        if (loss <= 0) return;

        bank.getBalanceSheet().decreaseLoans(loss);
        bank.getBalanceSheet().recordChargeOff(loss);

        String txId = IdGenerator.generate("CO");
        bank.getLedger().addEntry(new LedgerEntry(
                IdGenerator.generate("LE"), txId, date,
                "CHARGE_OFF", "LOANS", loss,
                "Charge-off of loan " + loan.getLoanId()));

        bank.logTransaction(new Transaction(txId, TransactionType.CHARGE_OFF,
                loss, LocalDateTime.of(date, java.time.LocalTime.NOON),
                loan.getCustomerId(), bank.getBankId(), "Loan charge-off"));
    }

    /** Returns all loans currently in DEFAULTED status. */
    public List<Loan> getDefaultedLoans(Bank bank) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : bank.getAllLoans()) {
            if (l.getStatus() == LoanStatus.DEFAULTED) result.add(l);
        }
        return result;
    }
}
