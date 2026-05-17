package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XFerTo {

    private String trnAmtT;
    private String trnRateT;
    private String trnAmtLceT;
    private String memo1T;

}
