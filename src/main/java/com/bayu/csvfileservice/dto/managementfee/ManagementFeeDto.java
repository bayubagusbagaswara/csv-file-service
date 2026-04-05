package com.bayu.csvfileservice.dto.managementfee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ManagementFeeDto {

    private Long id;
    private String month;
    private Integer year;

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
