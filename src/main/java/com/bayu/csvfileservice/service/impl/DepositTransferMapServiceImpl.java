package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.dto.deposittransfer.map.ReleaseDepositTransferHoldRequest;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.service.DepositTransferMapService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DepositTransferMapServiceImpl implements DepositTransferMapService {

    @Override
    public ProcessResult map(LocalDate currentDate, String userId, String clientIp) {
        return null;
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDate(LocalDate currentDate) {
        return List.of();
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDateAndMappingStatus(LocalDate currentDate, MappingStatus mappingStatusEnum) {
        return List.of();
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDateAndApprovalStatusAndTransferMethod(LocalDate currentDate, ApprovalStatus approvalStatusEnum, TransferMethod transferMethodEnum) {
        return List.of();
    }

    @Override
    public ProcessResult releaseHold(ReleaseDepositTransferHoldRequest request, String releaseBy, String clientIp) {
        return null;
    }
}
