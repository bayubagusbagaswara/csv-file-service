package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcctIdTo {

    private String acctIdT;
    private String acctTypeT;
    private String acctCurT;
    private String costCenterT;

}
