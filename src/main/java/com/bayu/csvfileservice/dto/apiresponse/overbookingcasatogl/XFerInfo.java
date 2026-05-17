package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XFerInfo {

    private String effectiveDt;
    private String overrideCode;
    private String trnId;
    private String svcBranch;

    private AcctIdFrom acctIdFrom;
    private AcctIdTo acctIdTo;
}
