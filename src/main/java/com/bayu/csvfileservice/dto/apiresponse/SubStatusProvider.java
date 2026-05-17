package com.bayu.csvfileservice.dto.apiresponse;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubStatusProvider {

    private String providerSystem;
    private String statusCode;
    private String statusDesc;

}
