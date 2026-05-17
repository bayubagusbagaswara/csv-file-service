package com.bayu.csvfileservice.dto.apiresponse.credittransfer;

import com.bayu.csvfileservice.dto.apiresponse.HeaderResponse;
import com.bayu.csvfileservice.dto.apiresponse.SubStatusProvider;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreditTransferResponse {

    private String responseCode;
    private String responseMessage;

    private CreditTransferData data;

    private SubStatusProvider subStatusProvider;

    private HeaderResponse headerResponse;
}
