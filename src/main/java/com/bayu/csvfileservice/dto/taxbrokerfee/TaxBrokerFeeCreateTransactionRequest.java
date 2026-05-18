package com.bayu.csvfileservice.dto.taxbrokerfee;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import lombok.Data;

@Data
public class TaxBrokerFeeCreateTransactionRequest {

    private Long id;

    private TransferMethod transferMethod;

    private String description;

    private String userId;
}