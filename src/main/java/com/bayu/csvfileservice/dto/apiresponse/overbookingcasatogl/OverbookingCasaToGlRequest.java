package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverbookingCasaToGlRequest {

    private XFerInfo xFerInfo;
    private XFerXfOn xFerXfOn;

}
