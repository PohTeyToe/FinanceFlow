package com.financeflow.account.model;

public enum AccountType {
    CHECKING,
    SAVINGS,
    CREDIT;

    /**
     * Returns the prefix used for account number generation
     */
    public String getPrefix() {
        return switch (this) {
            case CHECKING -> "CHK";
            case SAVINGS -> "SAV";
            case CREDIT -> "CRD";
        };
    }
}
