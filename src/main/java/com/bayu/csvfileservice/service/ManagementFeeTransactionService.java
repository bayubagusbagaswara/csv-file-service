package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.transaction.CreateSingleTransactionRequest;

import java.util.List;

public interface ManagementFeeTransactionService {

    ProcessResult create(List<CreateSingleTransactionRequest> requests, String clientIp);

    ProcessResult send(List<Long> ids, String userId, String clientIp);


    // reject transaction
}
