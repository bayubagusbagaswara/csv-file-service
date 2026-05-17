package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XFerXfOn {

    private XFerFrom from;
    private XFerTo to;
    private XFerFee fee;
}
