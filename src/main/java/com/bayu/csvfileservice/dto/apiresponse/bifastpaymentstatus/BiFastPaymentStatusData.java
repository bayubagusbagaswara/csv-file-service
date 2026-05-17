package com.bayu.csvfileservice.dto.apiresponse.bifastpaymentstatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BiFastPaymentStatusData {

    private String payUserRefNo;
    private String infoStatus;
    private String settlementDate;

    private String senderName;
    private String senderAccountNo;
    private String senderAccountType;
    private String senderBic;

    private String beneficiaryName;
    private String beneficiaryAccountNo;
    private String beneficiaryAccountType;
    private String beneficiaryBic;

    private String senderType;
    private String senderId;
    private String senderResidentStatus;
    private String senderCityCode;

    private String beneficiaryType;
    private String beneficiaryId;
    private String beneficiaryResidentStatus;
    private String beneficiaryCityCode;

    private String senderSettlementAccount;
    private String beneficiarySettlementAccount;
    private String suerRefNoBi;

}
