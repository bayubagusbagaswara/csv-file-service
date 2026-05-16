package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;

import java.time.LocalDate;
import java.util.List;

public interface DepositTransferMapService {

    ProcessResult map(LocalDate currentDate, String userId, String clientIp);

    List<DepositTransferMapDto> getAllByCurrentDate(LocalDate currentDate);

    List<DepositTransferMapDto> getAllByCurrentDateAndMappingStatus(LocalDate currentDate, MappingStatus mappingStatusEnum);

    List<DepositTransferMapDto> getAllByCurrentDateAndApprovalStatusAndTransferMethod(LocalDate currentDate, ApprovalStatus approvalStatusEnum, TransferMethod transferMethodEnum);

    ProcessResult releaseHold(List<Long> ids, String releaseBy, String clientIp);

}
