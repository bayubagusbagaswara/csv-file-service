package com.bayu.csvfileservice.dto.apiresponse.inquiryaccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAccountRequest {

    private String settlementAmount;
    private String settlementCurrency;
    private String chargeBearerCode;
    private String senderBic;
    private String senderAcctNo;
    private String beneficiaryBic;
    private String beneficiaryAcctNo;
    private String purposeTransaction;

}
