package com.bayu.csvfileservice.dto.apiresponse;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HeaderResponse {

    private String correlationId;
    private String bdiExternalId;
    private String bdiTimestamp;
    private String channelId;
    private String date;
    private String providerSystem;
    private String serviceCode;
    private String serviceRequestId;
    private String via;
    private String statusCodeResponse;

}
