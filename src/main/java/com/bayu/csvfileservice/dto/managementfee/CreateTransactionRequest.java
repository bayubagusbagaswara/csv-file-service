package com.bayu.csvfileservice.dto.managementfee;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import lombok.Data;

@Data
public class CreateTransactionRequest {

    private Long id;
    private TransferMethod transferMethod;

}
