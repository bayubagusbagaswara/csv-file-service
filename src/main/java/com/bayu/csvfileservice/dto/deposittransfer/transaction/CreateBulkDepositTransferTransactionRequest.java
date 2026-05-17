package com.bayu.csvfileservice.dto.deposittransfer.transaction;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CreateBulkDepositTransferTransactionRequest extends InputIdentifierRequest {

    private List<Long> ids;

    private String transferMethod;

}
