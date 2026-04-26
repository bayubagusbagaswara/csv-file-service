package com.bayu.csvfileservice.dto.deposittransfer;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDepositTransferSingleRequest {

    private Long id;

    private TransferMethod transferMethod;
}
