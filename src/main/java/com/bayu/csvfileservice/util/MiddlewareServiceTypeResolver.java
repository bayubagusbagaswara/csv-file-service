package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.FeatureType;
import com.bayu.csvfileservice.model.enumerator.MiddlewareServiceType;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import org.springframework.stereotype.Component;

@Component
public class MiddlewareServiceTypeResolver {

    public MiddlewareServiceType resolve(
            FeatureType featureType,
            TransferMethod transferMethod
    ) {

        if (TransferMethod.BI_FAST.equals(transferMethod)) {
            return MiddlewareServiceType.CREDIT_TRANSFER;
        }

        if (TransferMethod.SKN.equals(transferMethod)
                || TransferMethod.RTGS.equals(transferMethod)) {
            return MiddlewareServiceType.TRANSFER_SKN_RTGS;
        }

        if (TransferMethod.OVERBOOKING.equals(transferMethod)) {

            if (FeatureType.DEPOSIT_TRANSFER.equals(featureType)) {
                return MiddlewareServiceType.OVERBOOKING_CASA_TO_CASA;
            }

            if (FeatureType.MANAGEMENT_FEE.equals(featureType)) {
                return MiddlewareServiceType.OVERBOOKING_CASA_TO_CASA;
            }

            if (FeatureType.TAX_BROKER_FEE.equals(featureType)) {
                return MiddlewareServiceType.OVERBOOKING_CASA_TO_GL;
            }

            throw new IllegalArgumentException(
                    "Unsupported feature type for overbooking: " + featureType
            );
        }

        throw new IllegalArgumentException(
                "Unsupported transfer method: " + transferMethod
        );
    }
}
