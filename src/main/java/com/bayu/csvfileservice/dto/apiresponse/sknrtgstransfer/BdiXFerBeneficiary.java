package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BdiXFerBeneficiary {

    private String bdiBeneficiaryId;
    private String bdiBeneficiaryAccount;
    private String bdiBeneficiaryName;
    private String bdiBeneficiaryAddress;
    private String bdiBeneficiaryStatus;
    private String beneficiaryCitizen;
    private BankInfo bankInfo;

}
