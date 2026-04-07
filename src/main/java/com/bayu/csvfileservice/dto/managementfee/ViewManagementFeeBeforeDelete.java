package com.bayu.csvfileservice.dto.managementfee;

import lombok.Builder;
import lombok.Data;

//karena untuk menyimpan data before sebelum dihapus
@Data
@Builder
public class ViewManagementFeeBeforeDelete {

    private String mutualFundName;
    private String investmentManager;
    private String fundCode;
    private String debitAccount;
    private String amount;
    private String creditAccount;
    private String beneficiaryName;
    private String bankName;
    private String paymentInstructions;
    private String paymentType;
    private String period;
    private String description;
    private String bankCode;

}
