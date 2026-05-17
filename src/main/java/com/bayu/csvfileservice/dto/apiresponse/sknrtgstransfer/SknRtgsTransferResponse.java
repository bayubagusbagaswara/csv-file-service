package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import com.bayu.csvfileservice.dto.apiresponse.HeaderResponse;
import com.bayu.csvfileservice.dto.apiresponse.SubStatusProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SknRtgsTransferResponse {

    private String responseCode;
    private String responseMessage;

    private SubStatusProvider subStatusProvider;

    private SknRtgsTransferData data;

    private HeaderResponse headerResponse;
}
