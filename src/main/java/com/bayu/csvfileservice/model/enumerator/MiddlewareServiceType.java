package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum MiddlewareServiceType {

    INQUIRY_ACCOUNT("INQUIRY_ACCOUNT"),
    CREDIT_TRANSFER("CREDIT_TRANSFER"),
    OVERBOOKING_CASA_TO_CASA("OVERBOOKING_CASA_TO_CASA"),
    TRANSFER_SKN_RTGS("TRANSFER_SKN_RTGS"),
    BI_FAST_PAYMENT_STATUS("BI_FAST_PAYMENT_STATUS"),
    OVERBOOKING_CASA_TO_GL("OVERBOOKING_CASA_TO_GL");

    private final String label;

    MiddlewareServiceType(String name) {
        this.label = name;
    }

    public static MiddlewareServiceType fromName(String name) {
        for (MiddlewareServiceType value : values()) {
            if (value.label.equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid middleware service type: " + name);
    }

}
