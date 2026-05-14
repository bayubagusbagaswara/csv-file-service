package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.CreateBulkDepositTransferTransactionRequest;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.CreateSingleDepositTransferTransactionRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.deposittransfer.map.ReleaseDepositTransferHoldRequest;

import java.util.List;

public interface DepositTransferService {

    ProcessResult uploadRaw(SInvestBulkRequest request);

    ProcessResult map();

    ProcessResult createSingleTransaction(CreateSingleDepositTransferTransactionRequest request);

    ProcessResult createBulkTransaction(CreateBulkDepositTransferTransactionRequest request);

    ProcessResult sendTransaction(List<Long> ids);

    ProcessResult releaseHold(ReleaseDepositTransferHoldRequest request);

}
