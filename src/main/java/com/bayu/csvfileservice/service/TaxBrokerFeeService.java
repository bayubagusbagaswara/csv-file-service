package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeBulkRequest;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeDto;

import java.time.LocalDate;
import java.util.List;

public interface TaxBrokerFeeService {

    ProcessResult upload(
            TaxBrokerFeeBulkRequest request,
            String clientIp
    );

    ProcessResult createApprove(
            ApproveDataChangeRequest request,
            String clientIp
    );

    ProcessResult deleteById(
            DeleteIdRequest request,
            String clientIp
    );

    ProcessResult deleteApprove(
            ApproveDataChangeRequest request,
            String clientIp
    );

    List<TaxBrokerFeeDto> getAllByDate(LocalDate date);


}
