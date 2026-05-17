package com.bayu.csvfileservice.dto.apiresponse.bifastpaymentstatus;

import com.bayu.csvfileservice.dto.apiresponse.HeaderResponse;
import com.bayu.csvfileservice.dto.apiresponse.SubStatusProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BiFastPaymentStatusResponse {

    private String responseCode;
    private String responseMessage;

    private BiFastPaymentStatusData data;

    private SubStatusProvider subStatusProvider;

    private HeaderResponse headerResponse;
}
