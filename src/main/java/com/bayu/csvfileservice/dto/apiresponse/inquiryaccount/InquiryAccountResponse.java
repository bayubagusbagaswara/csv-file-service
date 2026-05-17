package com.bayu.csvfileservice.dto.apiresponse.inquiryaccount;

import com.bayu.csvfileservice.dto.apiresponse.HeaderResponse;
import com.bayu.csvfileservice.dto.apiresponse.SubStatusProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAccountResponse {

    private String responseCode;
    private String responseMessage;

    private SubStatusProvider subStatusProvider;

    private InquiryAccountData data;

    private HeaderResponse headerResponse;

}
