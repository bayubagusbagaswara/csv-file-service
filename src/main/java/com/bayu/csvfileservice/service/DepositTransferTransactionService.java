package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.*;
import com.bayu.csvfileservice.model.enumerator.ProcessType;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;

import java.time.LocalDate;
import java.util.List;

public interface DepositTransferTransactionService {

    ProcessResult createSingle(Long id, TransferMethod transferMethod, String userId, String clientIp);

    ProcessResult createBulk(List<Long> ids, TransferMethod transferMethod, String userId, String clientIp);

    ProcessResult send(List<Long> ids, String userId, String clientIp);

    ProcessResult reject(List<Long> ids, String userId, String clientIp);

    List<DepositTransferTransactionDto> getAllByCurrentDate(LocalDate currentDate);

    List<DepositTransferTransactionDto> getAllByProcessTypeAndCurrentDate(ProcessType processType, LocalDate currentDate);

}
