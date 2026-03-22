# Boma Bank Simulation

A Monte Carlo banking simulation built in Java 21 that models a retail bank's monthly operations — loan origination, deposit management, interest accrual, delinquency, and charge-offs — across configurable economic scenarios. The simulation enforces double-entry bookkeeping so the balance sheet equation (`Assets = Liabilities + Equity`) holds at every step.

---

## Table of Contents

- [Requirements](#requirements)
- [Building and Running](#building-and-running)
- [Scenarios](#scenarios)
- [Custom Scenarios](#custom-scenarios)
- [How the Simulation Works](#how-the-simulation-works)
- [Output](#output)
- [Project Structure](#project-structure)
- [Key Concepts](#key-concepts)
- [Running Tests](#running-tests)

---

## Requirements

- Java 21+
- Maven 3.6+

---

## Building and Running

### Quickest way — Maven exec

```bash
# Baseline scenario (12 months, stable economy)
mvn compile exec:java -Dexec.mainClass="com.boma.banksim.app.Main"

# Recession scenario (24 months, shock at month 6)
mvn compile exec:java -Dexec.mainClass="com.boma.banksim.app.Main" -Dexec.args="recession"
```

### Build a JAR first

```bash
mvn package -DskipTests
java -cp target/bank-sim-1.0-SNAPSHOT.jar com.boma.banksim.app.Main
java -cp target/bank-sim-1.0-SNAPSHOT.jar com.boma.banksim.app.Main recession
```

---

## Scenarios

Two preset scenarios ship out of the box:

### `baseline` (default)

A 12-month run in a stable economy.

| Parameter | Value |
|-----------|-------|
| Duration | 12 months |
| Customers | 200 |
| Initial Reserves | $5,000,000 |
| Economy | Normal (5% policy rate, 4% unemployment, 95% depositor confidence) |
| Market Shocks | None |
| Random Seed | 42 |

### `recession`

A 24-month run that starts normally, hits a recession at month 6, and begins recovering at month 18.

| Parameter | Value |
|-----------|-------|
| Duration | 24 months |
| Customers | 200 |
| Initial Reserves | $5,000,000 |
| Economy | Normal → Recession → Expansion |
| Market Shocks | Month 6: recession onset; Month 18: recovery |
| Random Seed | 99 |

**Recession shock** (month 6): policy rate +2%, unemployment +4%, depositor confidence −20%, state → RECESSION

**Recovery shock** (month 18): policy rate −1%, unemployment −2%, depositor confidence +15%, state → EXPANSION

---

## Custom Scenarios

Use the `Scenario.Builder` to define your own scenario programmatically:

```java
import com.boma.banksim.simulation.Scenario;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.MarketShock;
import java.time.LocalDate;

Scenario custom = new Scenario.Builder("CrisisRun")
        .durationMonths(36)
        .startDate(LocalDate.of(2023, 1, 1))
        .initialReserves(10_000_000)
        .initialEquity(10_000_000)
        .numCustomers(500)
        .economy(EconomicEnvironment.expansion())
        .addShock(MarketShock.recessionShock(LocalDate.of(2023, 7, 1)))
        .addShock(MarketShock.rateCutShock(LocalDate.of(2024, 1, 1), 0.02))
        .addShock(MarketShock.recoveryShock(LocalDate.of(2024, 6, 1)))
        .randomSeed(12345L)
        .build();

new SimulationRunner().run(custom);
```

### Builder options

| Method | Default | Description |
|--------|---------|-------------|
| `durationMonths(int)` | 12 | Number of monthly steps to simulate |
| `startDate(LocalDate)` | 2024-01-01 | Simulation start date |
| `initialReserves(double)` | 1,000,000 | Starting cash reserves |
| `initialEquity(double)` | 1,000,000 | Starting shareholder equity (informational) |
| `numCustomers(int)` | 100 | Number of customers generated at startup |
| `economy(EconomicEnvironment)` | normal() | Starting macroeconomic environment |
| `addShock(MarketShock)` | *(none)* | Schedule a market shock on a specific date |
| `randomSeed(long)` | 42 | Seed for reproducible results |

### Market shock factories

| Factory | Effect |
|---------|--------|
| `MarketShock.recessionShock(date)` | Rates up, unemployment up, confidence down, state → RECESSION |
| `MarketShock.recoveryShock(date)` | Rates down, unemployment down, confidence up, state → EXPANSION |
| `MarketShock.rateCutShock(date, cut)` | Reduces policy rate by `cut` (e.g. `0.01` = 1%) |

### Economic environment factories

| Factory | Policy Rate | Unemployment | Confidence | State |
|---------|-------------|--------------|------------|-------|
| `EconomicEnvironment.normal()` | 5% | 4% | 95% | NORMAL |
| `EconomicEnvironment.expansion()` | 3% | 3% | 98% | EXPANSION |
| `EconomicEnvironment.recession()` | 7% | 8% | 70% | RECESSION |
| `EconomicEnvironment.crisis()` | 10% | 15% | 40% | CRISIS |

---

## How the Simulation Works

Each month the engine runs 8 steps in order:

1. **Market shocks** — Any events scheduled for this date are applied to the economy.
2. **Income deposits** — Each customer deposits their monthly income (±10% variance).
3. **Deposit interest accrual** — The bank credits monthly interest to all savings accounts, increasing deposit liabilities (and reducing equity).
4. **Loan payment collection** — Customers with sufficient account balances make their monthly loan payment. The payment is split: interest portion → bank income, principal portion → reduces outstanding loan balance.
5. **Delinquency evaluation** — Customers who cannot pay are marked late. Loans progress through: CURRENT → LATE (30 days) → DELINQUENT (90 days) → DEFAULTED (180 days).
6. **Default and charge-off** — Delinquent loans are stochastically evaluated for charge-off. The default probability scales with loan status and economic severity. Charged-off loans reduce equity directly.
7. **Spending withdrawals** — Customers withdraw funds proportional to their spending rate (keeps a 5% balance buffer).
8. **Stress withdrawals** — During stressed economies (RECESSION/CRISIS), customers with high withdrawal sensitivity pull additional funds (panic behaviour).

At the end of each step a `SimulationStepResult` snapshot is recorded for reporting.

### Balance sheet accounting

The simulation enforces double-entry bookkeeping throughout:

| Operation | Debit | Credit | Equity Effect |
|-----------|-------|--------|---------------|
| Customer deposit | RESERVES ↑ | DEPOSITS ↑ | Neutral |
| Customer withdrawal | DEPOSITS ↓ | RESERVES ↓ | Neutral |
| Loan origination | LOANS ↑ | RESERVES ↓ | Neutral |
| Loan payment (principal) | RESERVES ↑ | LOANS ↓ | Neutral |
| Loan payment (interest) | RESERVES ↑ | INTEREST_INCOME | **Increases equity** |
| Deposit interest accrual | INTEREST_EXPENSE | DEPOSITS ↑ | **Decreases equity** |
| Loan charge-off | CHARGE_OFF | LOANS ↓ | **Decreases equity** |

### Risk models

**Underwriting** (`UnderwritingEngine`): Loan applications are evaluated against:
- Minimum credit score: 620 (normal economy) / 680 (stressed economy)
- Maximum DTI: 43% (normal) / 36% (stressed)
- Maximum loan amount: 5× annual income

**Default probability** (`DefaultModel`): Monthly default probability by loan status and economic state:

| Status | Normal | Recession | Crisis |
|--------|--------|-----------|--------|
| CURRENT | 0.1% | 0.15% | 0.25% |
| LATE | 2% | 3% | 5% |
| DELINQUENT | 8% | 12% | 20% |
| DEFAULTED | 25% | 37.5% | 62.5% |

**Loss Given Default** (`CreditRiskModel`): Expected recovery by loan type:

| Loan Type | LGD |
|-----------|-----|
| MORTGAGE | 25% |
| BUSINESS | 50% |
| CONSUMER | 70% |

### Customer generation

At startup, `ScenarioLoader` generates `numCustomers` customers:
- 80% retail, 20% business
- Each gets a checking account with a randomised initial deposit ($500–$5,000)
- 60% also receive a savings account ($1,000–$20,000)
- 30% start with an existing loan (subject to underwriting approval)

Customer profiles include income, spending rate, credit score, withdrawal sensitivity, and rate sensitivity — all drawn from realistic random distributions seeded by `randomSeed`.

---

## Output

### Console output

```
=== Boma Bank Simulation ===
Scenario : Baseline
Duration : 12 months
Customers: 200

--- Opening Balance Sheet ---
...

--- Monthly Steps ---
Step   1 [2024-02-01] | Assets= 4,923,450 | Equity= 4,851,200 | NII=   1,230 | Defaults=0 | ChargeOffs=0
Step   2 [2024-03-01] | Assets= 4,901,110 | Equity= 4,842,800 | NII=   2,100 | Defaults=1 | ChargeOffs=0
...

--- Closing Balance Sheet ---
...

=== Final Report ===
Total Net Income  : $28,450.00
Total Charge-Offs : $12,300.00
Peak Equity       : $4,890,100.00
Trough Equity     : $4,720,300.00
Avg NIM           : 0.0312%
```

### CSV export

A CSV file is written to the working directory after every run:

- `baseline_results.csv` for the baseline scenario
- `recession_results.csv` for the recession scenario

**Columns:**

| Column | Description |
|--------|-------------|
| `step` | Month number |
| `date` | Simulation date (YYYY-MM-DD) |
| `reserves` | Cash reserves |
| `totalLoans` | Outstanding loan book |
| `totalDeposits` | Customer deposit liabilities |
| `equity` | Shareholders' equity (Assets − Liabilities) |
| `totalAssets` | Reserves + Loans |
| `grossInterestIncome` | Interest earned on loans this month |
| `depositInterestExpense` | Interest paid on savings accounts this month |
| `chargeOffLosses` | Loans written off this month |
| `netIncome` | Monthly net profit |
| `totalActiveLoans` | Loans currently in repayment |
| `numDefaulted` | Loans in DEFAULTED status |
| `numChargedOff` | Loans charged off this month |
| `numNewLoans` | New loan payments collected |
| `policyRate` | Central bank policy rate |
| `unemploymentRate` | Unemployment rate |
| `depositorConfidence` | Depositor confidence index [0, 1] |

---

## Project Structure

```
src/
├── main/java/com/boma/banksim/
│   ├── app/
│   │   ├── Main.java               # Entry point
│   │   ├── SimulationRunner.java   # Wires all components
│   │   └── ScenarioLoader.java     # Generates populated bank from scenario
│   ├── bank/
│   │   ├── Bank.java               # Root entity (customers, accounts, loans)
│   │   ├── BankBalanceSheet.java   # Assets / liabilities / equity tracking
│   │   ├── Treasury.java           # Securities and borrowings
│   │   └── LiquidityManager.java   # Reserve adequacy checks
│   ├── account/
│   │   ├── Account.java            # Abstract base
│   │   ├── CheckingAccount.java    # Non-interest bearing
│   │   └── SavingsAccount.java     # Interest bearing
│   ├── customer/
│   │   ├── Customer.java           # Customer entity
│   │   └── CustomerProfile.java    # Behavioural parameters
│   ├── loan/
│   │   ├── Loan.java               # Loan asset with state machine
│   │   ├── LoanApplication.java    # Pending application
│   │   └── LoanPaymentSchedule.java # Amortisation schedule
│   ├── ledger/
│   │   ├── Ledger.java             # Double-entry journal
│   │   ├── LedgerEntry.java        # Single debit/credit record
│   │   └── Journal.java            # Queryable ledger view
│   ├── economy/
│   │   ├── EconomicEnvironment.java # Macro state
│   │   ├── MarketShock.java         # Scheduled economic event
│   │   └── RateModel.java           # Loan and savings rate calculator
│   ├── service/
│   │   ├── PaymentProcessor.java       # Deposits and withdrawals
│   │   ├── LoanService.java            # Origination and payments
│   │   ├── InterestAccrualService.java # Monthly deposit interest
│   │   ├── DefaultService.java         # Charge-off execution
│   │   └── CustomerBehaviorService.java # Income/spending/stress
│   ├── risk/
│   │   ├── UnderwritingEngine.java     # Loan application evaluation
│   │   ├── DefaultModel.java           # Default probability model
│   │   ├── CreditRiskModel.java        # Expected loss calculation
│   │   └── LiquidityStressModel.java   # Panic withdrawal model
│   ├── simulation/
│   │   ├── SimulationEngine.java    # Monthly simulation loop
│   │   ├── SimulationClock.java     # Simulated time
│   │   ├── Scenario.java            # Configuration + Builder
│   │   ├── SimulationStepResult.java # Monthly snapshot
│   │   └── EventQueue.java          # Scheduled event dispatcher
│   ├── report/
│   │   ├── SimulationMetrics.java   # Metric aggregation
│   │   ├── ReportGenerator.java     # Console reports
│   │   ├── BankReport.java          # Balance sheet printer
│   │   └── CsvExporter.java         # CSV file export
│   ├── transaction/
│   │   ├── Transaction.java         # Audit trail entry
│   │   └── TransactionType.java     # Enum of transaction types
│   └── util/
│       ├── IdGenerator.java         # UUID-based ID creation
│       ├── RandomProvider.java      # Seeded RNG wrapper
│       ├── MathUtils.java           # Amortisation, DTI, rounding
│       └── DateUtils.java           # Date calculations
└── test/java/com/boma/banksim/
    ├── account/          # 29 unit tests
    ├── bank/             # 72 unit tests
    ├── customer/         # 28 unit tests
    ├── economy/          # 41 unit tests
    ├── ledger/           # 28 unit tests
    ├── loan/             # 57 unit tests
    ├── report/           # 13 unit tests
    ├── risk/             # 43 unit tests
    ├── simulation/       # 26 unit tests
    ├── transaction/      # 10 unit tests
    ├── util/             # 32 unit tests
    └── integration/      # 78 integration tests
```

---

## Key Concepts

**Deposits are liabilities.** When a customer deposits money, the bank's reserves increase (asset) and deposits increase (liability). Equity is unchanged. This is standard banking accounting — the bank *owes* the deposit back to the customer.

**Loans are assets.** When a loan is issued, reserves decrease and loans increase. Equity is again unchanged. When a loan is charged off, the loan asset disappears — equity absorbs the loss.

**Equity is always derived.** `Equity = Assets − Liabilities = (Reserves + Loans) − Deposits`. It is never set directly; it is computed from balance sheet positions. This means the balance sheet equation holds mathematically at all times.

**Reproducibility via seed.** All stochastic behaviour (customer income variance, default evaluation, spending patterns) uses a seeded `RandomProvider`. The same seed always produces the same simulation run.

---

## Running Tests

```bash
mvn test
```

475 tests — 397 unit tests and 78 integration tests — all pass. Integration tests verify the balance sheet invariant (`Assets = Liabilities + Equity`) and correct accounting treatment after every type of operation.
