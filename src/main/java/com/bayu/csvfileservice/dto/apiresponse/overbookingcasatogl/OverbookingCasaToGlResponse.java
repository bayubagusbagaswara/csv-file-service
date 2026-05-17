package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl;

import com.bayu.csvfileservice.dto.apiresponse.SubStatusProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverbookingCasaToGlResponse {

    private String responseCode;
    private String responseMessage;

    private SubStatusProvider subStatusProvider;

    private OverbookingCasaToGlData data;

}
