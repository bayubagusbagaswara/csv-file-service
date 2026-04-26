package com.bayu.csvfileservice.dto.deposittransfer;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateDepositTransferBulkRequest {

    private List<Long> ids;

    private TransferMethod transferMethod;

}
