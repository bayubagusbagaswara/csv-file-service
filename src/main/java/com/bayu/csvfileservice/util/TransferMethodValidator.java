package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import org.springframework.stereotype.Component;

@Component
public class TransferMethodValidator {

    public void validate(TransferScope scope, TransferMethod method) {
        if (scope == TransferScope.INTERNAL && method != TransferMethod.OVERBOOKING) {
            throw new IllegalArgumentException("INTERNAL must be OVERBOOKING");
        }

        if (scope == TransferScope.EXTERNAL && method == TransferMethod.OVERBOOKING) {
            throw new IllegalArgumentException("EXTERNAL cannot OVERBOOKING");
        }
    }

}
