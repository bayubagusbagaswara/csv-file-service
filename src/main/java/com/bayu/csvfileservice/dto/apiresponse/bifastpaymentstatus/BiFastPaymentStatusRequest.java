package com.bayu.csvfileservice.dto.apiresponse.bifastpaymentstatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BiFastPaymentStatusRequest {

    private String originalTransactionType;
    private String payUserRefNo;
    private String originalUserRefNo;

}
