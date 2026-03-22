package com.boma.banksim.app;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.account.SavingsAccount;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.customer.CustomerType;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.RateModel;
import com.boma.banksim.loan.LoanApplication;
import com.boma.banksim.loan.LoanType;
import com.boma.banksim.service.LoanService;
import com.boma.banksim.service.PaymentProcessor;
import com.boma.banksim.simulation.Scenario;
import com.boma.banksim.util.IdGenerator;
import com.boma.banksim.util.RandomProvider;

import java.time.LocalDate;

/**
 * Builds and populates a bank from a {@link Scenario} definition.
 * Generates customers with realistic profiles and optional initial loans.
 */
public class ScenarioLoader {

    private final PaymentProcessor paymentProcessor = new PaymentProcessor();
    private final LoanService loanService = new LoanService();

    /**
     * Creates a fully-populated {@link Bank} for the given scenario.
     */
    public Bank buildBank(Scenario scenario) {
        RandomProvider random = new RandomProvider(scenario.getRandomSeed());
        Bank bank = new Bank(IdGenerator.generate("BNK"), "Boma National Bank",
                scenario.getInitialReserves(), scenario.getInitialEquity());

        EconomicEnvironment economy = scenario.getEconomy();
        LocalDate startDate = scenario.getStartDate();
        double savingsRate = RateModel.savingsRate(economy);

        for (int i = 0; i < scenario.getNumCustomers(); i++) {
            Customer customer = generateCustomer(random, i);
            bank.addCustomer(customer);

            // Create checking account with small initial deposit
            double initialDeposit = random.nextDouble(500, 5000);
            CheckingAccount checking = new CheckingAccount(
                    IdGenerator.generate("CHK"), customer.getCustomerId(), 0.0);
            bank.addAccount(checking);
            customer.addAccountId(checking.getAccountId());
            paymentProcessor.deposit(bank, checking, initialDeposit, startDate);

            // 60% of customers also have a savings account
            if (random.nextChance(0.60)) {
                double savingsDeposit = random.nextDouble(1000, 20000);
                SavingsAccount savings = new SavingsAccount(
                        IdGenerator.generate("SAV"), customer.getCustomerId(), 0.0, savingsRate);
                bank.addAccount(savings);
                customer.addAccountId(savings.getAccountId());
                paymentProcessor.deposit(bank, savings, savingsDeposit, startDate);
            }

            // 30% of customers have an existing loan
            if (random.nextChance(0.30) && bank.getBalanceSheet().getReserves() > 10_000) {
                tryIssueLoan(bank, customer, economy, startDate, random);
            }
        }

        System.out.println("Bank loaded: " + bank.getCustomerCount() + " customers, " +
                bank.getAccountCount() + " accounts, " + bank.getLoanCount() + " loans.");
        System.out.printf("Initial balance sheet: reserves=%,.0f  deposits=%,.0f  equity=%,.0f%n",
                bank.getBalanceSheet().getReserves(),
                bank.getBalanceSheet().getTotalDeposits(),
                bank.getBalanceSheet().getEquity());

        return bank;
    }

    private Customer generateCustomer(RandomProvider random, int index) {
        CustomerType type = random.nextChance(0.80) ? CustomerType.RETAIL : CustomerType.BUSINESS;

        double income = type == CustomerType.RETAIL
                ? random.nextDouble(30_000, 150_000)
                : random.nextDouble(80_000, 500_000);

        double spendingRate = random.nextDouble(0.50, 0.90);
        double creditScore = random.nextDouble(580, 820);
        double withdrawalSensitivity = random.nextDouble(0.0, 0.8);
        double rateSensitivity = random.nextDouble(0.0, 0.6);

        CustomerProfile profile = new CustomerProfile(
                income, spendingRate, creditScore, withdrawalSensitivity, rateSensitivity);

        return new Customer(IdGenerator.generate("CUST"),
                "Customer-" + (index + 1), type, profile);
    }

    private void tryIssueLoan(Bank bank, Customer customer, EconomicEnvironment economy,
                               LocalDate date, RandomProvider random) {
        double monthlyIncome = customer.getProfile().getIncome() / 12.0;
        double loanAmount = random.nextDouble(5_000, 50_000);
        LoanType type = random.nextChance(0.60) ? LoanType.CONSUMER : LoanType.MORTGAGE;
        int term = type == LoanType.MORTGAGE ? 360 : 60;

        LoanApplication application = new LoanApplication(
                IdGenerator.generate("APP"),
                customer.getCustomerId(),
                type,
                loanAmount,
                monthlyIncome,
                0.0,
                customer.getProfile().getCreditScore(),
                term
        );

        try {
            com.boma.banksim.risk.UnderwritingEngine underwriting =
                    new com.boma.banksim.risk.UnderwritingEngine();
            underwriting.evaluate(application, economy);
            if (application.isApproved()) {
                loanService.issueLoan(bank, customer, application, economy, date);
            }
        } catch (Exception ignored) {
            // Skip this customer's loan if any validation fails
        }
    }
}
