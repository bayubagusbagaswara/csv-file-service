package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum ProcessType {

    SINGLE("Single"),
    BULK("Bulk")
    ;

    private final String name;

    ProcessType(String name) {
        this.name = name;
    }

    public static ProcessType fromName(String name) {
        for (ProcessType value : values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid process type: " + name);
    }

}
