package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import org.springframework.stereotype.Component;

@Component
public class TransferMethodValidator {

    public void validate(TransferScope scope, TransferMethod method) {
        boolean isOverbooking =
                method == TransferMethod.OVERBOOKING_CASA_TO_CASA
                        || method == TransferMethod.OVERBOOKING_CASA_TO_GL;

        if (scope == TransferScope.INTERNAL && !isOverbooking) {
            throw new IllegalArgumentException("INTERNAL can only user OVERBOOKING_CASA_TO_CASA or OVERBOOKING_CASA_TO_GL");
        }

        if (scope == TransferScope.EXTERNAL && isOverbooking) {
            throw new IllegalArgumentException("EXTERNAL may not use OVERBOOKING");
        }
    }

}
