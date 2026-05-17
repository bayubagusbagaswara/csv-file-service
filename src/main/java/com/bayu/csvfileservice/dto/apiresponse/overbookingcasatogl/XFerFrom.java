package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XFerFrom {

    private String trnAmtF;
    private String trnRateF;
    private String trnAmtLceF;
    private String memo1F;

}
