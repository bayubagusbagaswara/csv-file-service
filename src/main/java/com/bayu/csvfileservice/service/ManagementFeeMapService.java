package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.transaction.CreateSingleTransactionRequest;
import com.bayu.csvfileservice.model.enumerator.Month;

import java.util.List;

public interface ManagementFeeMapService {

    ProcessResult map(Month monthEnum, Integer year, String clientIp);

    ProcessResult create(List<CreateSingleTransactionRequest> requests, String clientIp);

    ProcessResult send(List<Long> ids, String userId, String clientIp);

}
