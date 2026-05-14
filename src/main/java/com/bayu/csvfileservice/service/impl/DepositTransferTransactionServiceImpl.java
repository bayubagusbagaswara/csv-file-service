package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.*;
import com.bayu.csvfileservice.service.DepositTransferTransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DepositTransferTransactionServiceImpl implements DepositTransferTransactionService {

    @Override
    public ProcessResult createSingleTransaction(CreateSingleDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult createBulkTransaction(CreateBulkDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult sendTransaction(SendDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult rejectTransaction(RejectDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public List<DepositTransferTransactionDto> getAllTransactionByCurrentDate(LocalDate currentDate) {
        return List.of();
    }
}
