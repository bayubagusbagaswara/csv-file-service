package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.managementfee.CreateTransactionRequest;
import com.bayu.csvfileservice.model.enumerator.Month;

import java.util.List;

public interface ManagementFeeMapService {

    ProcessResult map(Month month, Integer year);

    ProcessResult createTransactions(List<CreateTransactionRequest> requests);

    ProcessResult sendTransactions(List<Long> ids);

}
