package com.bayu.csvfileservice.dto.apiresponse.credittransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransferData {

    private String trxType;
    private String payUserRefNo;
    private String infoStatus;
    private String settlementDate;
    private String beneficiaryName;
    private String beneficiaryType;
    private String beneficiaryId;
    private String beneficiaryResidentStatus;
    private String beneficiaryCityCode;
    private String cardNo;
    private String feeAmt;
    private String chargeBearerCode;
    private String userRefNoBi;

}
