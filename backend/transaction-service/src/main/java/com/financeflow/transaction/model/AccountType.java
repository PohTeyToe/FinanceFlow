package com.financeflow.transaction.model;

public enum AccountType {
    CHECKING("CHK"),
    SAVINGS("SAV"),
    CREDIT("CRD");

    private final String prefix;

    AccountType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
