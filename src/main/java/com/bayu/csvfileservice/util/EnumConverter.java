package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class EnumConverter {

    public static Month fromMonth(String month) {
        try {
            return Month.valueOf(month.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
    }

    public static TransferMethod fromTransferMethod(String method) {
        try {
            return TransferMethod.fromName(method);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid transfer method: '%s'. Allowed values are: %s",
                            method,
                            Arrays.toString(TransferMethod.values())
                    )
            );
        }
    }

    public static ApprovalStatus fromApprovalStatus(String status) {
        try {
            return ApprovalStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid approval status: '%s'. Allowed values are: %s",
                            status,
                            Arrays.toString(ApprovalStatus.values())
                    )
            );
        }
    }

    public static MappingStatus fromMappingStatus(String status) {
        try {
            return MappingStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid mapping status: '%s'. Allowed values are: %s",
                            status,
                            Arrays.toString(MappingStatus.values())
                    )
            );
        }
    }



}
