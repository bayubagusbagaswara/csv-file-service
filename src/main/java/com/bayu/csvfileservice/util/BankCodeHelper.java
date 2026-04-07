package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.TransferScope;
import org.springframework.stereotype.Component;

@Component
public class BankCodeHelper {

    public String extractBankCode(String raw) {
        return raw.substring(0, 3);
    }

    public String extractBranchCode(String raw) {
        return raw.substring(3);
    }

    public String formatBankCode(String bankCode) {
        return "0" + bankCode;
    }

    public TransferScope resolveScope(String branchCode) {
        return "0011".equals(branchCode)
                ? TransferScope.INTERNAL
                : TransferScope.EXTERNAL;
    }

}
