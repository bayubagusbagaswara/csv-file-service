package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransferScope {

    INTERNAL("Internal"),
    EXTERNAL("External")
    ;

    private final String name;

    TransferScope(String name) {
        this.name = name;
    }

    public static TransferScope fromName(String name) {
        for (TransferScope value : values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid transfer scope: " + name);
    }

}
