package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransferMethod {

    BI_FAST("BI-FAST"),
    SKN("SKN"),
    RTGS("RTGS"),
    OVERBOOKING("OVERBOOKING");

    private final String transferMethodName;

    TransferMethod(String transferMethodName) {
        this.transferMethodName = transferMethodName;
    }

}
