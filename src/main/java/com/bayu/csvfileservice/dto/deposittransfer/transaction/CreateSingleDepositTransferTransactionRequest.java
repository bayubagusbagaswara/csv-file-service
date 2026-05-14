package com.bayu.csvfileservice.dto.deposittransfer.transaction;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CreateSingleDepositTransferTransactionRequest extends InputIdentifierRequest {

    private Long id;
    private String transferMethod;

}
