package com.bayu.csvfileservice.dto.deposittransfer;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDepositTransferTransactionRequest {

    private Long id;
    private TransferMethod transferMethod;
}
