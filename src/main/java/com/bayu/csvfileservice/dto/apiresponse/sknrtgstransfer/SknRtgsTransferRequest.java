package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SknRtgsTransferRequest {

    private String type;
    private XFerInfoFrom xFerInfoFrom;
    private BdiXFerXfOff bdiXFerXfOff;

}
