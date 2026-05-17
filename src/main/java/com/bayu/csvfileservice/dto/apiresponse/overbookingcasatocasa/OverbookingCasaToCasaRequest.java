package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverbookingCasaToCasaRequest {

    private String type;
    private XFerInfo xFerInfo;
    private BdiXFerXFon bdiXFerXFon;

}
