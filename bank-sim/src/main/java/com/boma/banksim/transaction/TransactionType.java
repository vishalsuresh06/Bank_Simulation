package com.boma.banksim.transaction;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    INTERNAL_TRANSFER,
    EXTERNAL_TRANSFER_IN,
    EXTERNAL_TRANSFER_OUT,
    LOAN_ORIGINATION,
    LOAN_REPAYMENT,
    INTEREST_ACCRUAL,
    FEE,
    CHARGE_OFF
}
