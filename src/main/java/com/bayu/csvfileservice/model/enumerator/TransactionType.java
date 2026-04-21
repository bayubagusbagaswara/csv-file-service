package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransactionType {

    SKN("1"),
    RTGS("3")
    ;

    private final String code;

    TransactionType(String code) {
        this.code = code;
    }

    public static TransactionType fromValue(String name) {
        for (TransactionType transactionType : values()) {
            if (transactionType.name().equalsIgnoreCase(name)) {
                return transactionType;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + name);
    }
}
