package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcctIdFrom {

    private String acctIdF;
    private String acctTypeF;
    private String acctCurF;
    private String costCenterF;

}
