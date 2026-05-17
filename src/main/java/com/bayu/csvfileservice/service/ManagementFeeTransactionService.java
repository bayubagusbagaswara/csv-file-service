package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;

import java.util.List;

public interface ManagementFeeTransactionService {

    ProcessResult create(Long id, TransferMethod transferMethod, String description, String userId, String clientIp);

    ProcessResult send(List<Long> ids, String userId, String clientIp);

    ProcessResult reject(List<Long> ids, String userId, String clientIp);

}
