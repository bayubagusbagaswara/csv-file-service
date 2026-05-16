package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;

import java.time.LocalDate;
import java.util.List;

public interface DepositTransferMapService {

    ProcessResult map(LocalDate currentDate, String userId, String clientIp);

    List<DepositTransferMapDto> getAllByCurrentDate(LocalDate currentDate);

    List<DepositTransferMapDto> getAllByCurrentDateAndMappingStatus(LocalDate currentDate, MappingStatus mappingStatusEnum);

    ProcessResult releaseHold(List<Long> ids, String releaseBy, String clientIp);

}
