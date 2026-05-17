package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransferMethod {

    BI_FAST("BI-FAST"),
    SKN("SKN"),
    RTGS("RTGS"),
    OVERBOOKING("OVERBOOKING");

    private final String name;

    TransferMethod(String name) {
        this.name = name;
    }

    public static TransferMethod fromName(String name) {
        for (TransferMethod value : values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid transfer method: " + name);
    }

}
