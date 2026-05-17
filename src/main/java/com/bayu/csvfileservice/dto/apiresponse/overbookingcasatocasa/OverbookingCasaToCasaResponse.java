package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import com.bayu.csvfileservice.dto.apiresponse.HeaderResponse;
import com.bayu.csvfileservice.dto.apiresponse.SubStatusProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverbookingCasaToCasaResponse {

    private String responseCode;
    private String responseMessage;

    private SubStatusProvider subStatusProvider;

    private OverbookingCasaToCasaData data;

    private HeaderResponse headerResponse;
}
