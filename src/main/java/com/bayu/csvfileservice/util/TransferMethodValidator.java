package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import org.springframework.stereotype.Component;

@Component
public class TransferMethodValidator {

    public void validate(TransferScope scope, TransferMethod method) {

        if (scope == null) {
            throw new IllegalArgumentException("Transfer scope is required");
        }

        if (method == null) {
            throw new IllegalArgumentException("Transfer method is required");
        }

        if (scope == TransferScope.INTERNAL) {
            if (method != TransferMethod.OVERBOOKING) {
                throw new IllegalArgumentException(
                        "INTERNAL transfer can only use OVERBOOKING"
                );
            }
            return;
        }

        if (scope == TransferScope.EXTERNAL) {
            if (method == TransferMethod.OVERBOOKING) {
                throw new IllegalArgumentException(
                        "EXTERNAL transfer may not use OVERBOOKING"
                );
            }

            if (method != TransferMethod.BI_FAST
                    && method != TransferMethod.SKN
                    && method != TransferMethod.RTGS) {
                throw new IllegalArgumentException(
                        "EXTERNAL transfer can only use BI_FAST, SKN, or RTGS"
                );
            }
        }
    }

}
