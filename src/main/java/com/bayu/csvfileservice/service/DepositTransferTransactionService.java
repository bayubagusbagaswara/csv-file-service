package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.*;

import java.time.LocalDate;
import java.util.List;

public interface DepositTransferTransactionService {

    ProcessResult createSingleTransaction(CreateSingleDepositTransferTransactionRequest request, String userId, String clientIp);

    ProcessResult createBulkTransaction(CreateBulkDepositTransferTransactionRequest request, String userId, String clientIp);

    ProcessResult sendTransaction(SendDepositTransferTransactionRequest request, String userId, String clientIp);

    ProcessResult rejectTransaction(RejectDepositTransferTransactionRequest request, String userId, String clientIp);

    List<DepositTransferTransactionDto> getAllTransactionByCurrentDate(LocalDate currentDate);

}
