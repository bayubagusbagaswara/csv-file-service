package com.bayu.csvfileservice.dto.sinvest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SInvestRequest {

    private String imCode;
    private String imName;
    private String fundCode;
    private String fundName;
    private String bankCode;
    private String bankName;
    private String cashAccountName;
    private String cashAccountNo;
    private String currency;
    private String principle;
    private String date;
    private String referenceNo;
    private String siReferenceId;
}