package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransferScope {

    INTERNAL,
    EXTERNAL;

    public boolean isValidMethod(TransferMethod method) {
        if (this == INTERNAL) {
            return method == TransferMethod.OVERBOOKING;
        }
        if (this == EXTERNAL) {
            return method != TransferMethod.OVERBOOKING;
        }
        return false;
    }

    // CONTOH PEMAKAIAN
//    if (!transferScope.isValidMethod(transferMethod)) {
//        throw new IllegalArgumentException("Kombinasi transfer tidak valid");
//    }
}
