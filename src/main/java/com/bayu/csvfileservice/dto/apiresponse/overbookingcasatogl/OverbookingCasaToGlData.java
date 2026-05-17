package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverbookingCasaToGlData {

    private String refNo;
    private String trnTm;
    private String traceId;

}
