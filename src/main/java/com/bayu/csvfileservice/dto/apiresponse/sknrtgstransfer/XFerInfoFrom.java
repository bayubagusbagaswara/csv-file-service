package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XFerInfoFrom {

    private String acctId;
    private String acctType;
    private String acctCurrency;
    private String bdiAccountStatus;
    private String bdiAccountCitizen;

}
