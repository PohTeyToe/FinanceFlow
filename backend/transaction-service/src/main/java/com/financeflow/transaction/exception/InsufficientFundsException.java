package com.financeflow.transaction.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal requested, BigDecimal available) {
        super(String.format(
                "Insufficient funds in account %s. Requested: %.2f, Available: %.2f",
                accountId, requested, available
        ));
    }
}
