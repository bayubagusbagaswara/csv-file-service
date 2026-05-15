package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestDto;

public interface SInvestService {

    ProcessResult createBulk(SInvestBulkRequest request, DataChangeDto dataChangeDto);

    ProcessResult approveCreate(Long dataChangeId, String userId, String clientIp);


    ProcessResult deleteById(Long id, DataChangeDto dataChangeDto);

    ProcessResult approveDelete(Long dataChangeId, String userId, String clientIp);

    SInvestDto getById(Long id);

    Boolean isDeletableBySiReferenceId(String siReferenceId);

}
