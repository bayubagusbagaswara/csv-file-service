package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeMapDto;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;

import java.time.LocalDate;
import java.util.List;

public interface TaxBrokerFeeMapService {

    ProcessResult map(LocalDate date, String userId, String clientIp);

    List<TaxBrokerFeeMapDto> getAllByDate(LocalDate date);

    List<TaxBrokerFeeMapDto> getAllByDateAndStatus(LocalDate date, MappingStatus status);

}
