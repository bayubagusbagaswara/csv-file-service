package com.bayu.csvfileservice.dto.apiresponse.inquiryaccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAccountData {

    private String settlementDate;
    private String beneficiaryAccountName;
    private String beneficiaryAccountNo;
    private String beneficiaryAccountType;
    private String beneficiaryType;
    private String beneficiaryId;
    private String beneficiaryResidentStatus;
    private String beneficiaryCityCode;
    private String userRefNoBi;

}
