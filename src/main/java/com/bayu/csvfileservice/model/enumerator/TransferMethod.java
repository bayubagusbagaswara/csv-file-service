package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransferMethod {

    BI_FAST("BI-FAST"),
    SKN("SKN"),
    RTGS("RTGS"),
    OVERBOOKING_CASA_TO_CASA("OVERBOOKING_CASA_TO_CASA"),
    OVERBOOKING_CASA_TO_GL("OVERBOOKING_CASA_TO_GL")
    ;

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
