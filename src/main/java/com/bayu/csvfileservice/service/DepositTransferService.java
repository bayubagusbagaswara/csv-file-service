package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.CreateDepositTransferBulkRequest;
import com.bayu.csvfileservice.dto.deposittransfer.CreateDepositTransferSingleRequest;
import com.bayu.csvfileservice.dto.deposittransfer.DepositTransferBulkRequest;
import com.bayu.csvfileservice.dto.deposittransfer.ReleaseDepositTransferHoldRequest;

import java.util.List;

public interface DepositTransferService {

    ProcessResult uploadRaw(DepositTransferBulkRequest request);

    ProcessResult map();

    ProcessResult createSingleTransaction(CreateDepositTransferSingleRequest request);

    ProcessResult createBulkTransaction(CreateDepositTransferBulkRequest request);

    ProcessResult sendTransaction(List<Long> ids);

    ProcessResult releaseHold(ReleaseDepositTransferHoldRequest request);

}
