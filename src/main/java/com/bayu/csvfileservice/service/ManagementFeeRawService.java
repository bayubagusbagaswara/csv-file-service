package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeBulkRequest;
import com.bayu.csvfileservice.model.ManagementFeeRaw;

import java.util.List;

public interface ManagementFeeRawService {

    ProcessResult createBulk(ManagementFeeBulkRequest request, DataChangeDto dataChangeDto);

    ProcessResult approveCreate(Long dataChangeId, String userId, String clientIp);

    ProcessResult deleteById(Long id, DataChangeDto dataChangeDto);

    ProcessResult approveDelete(Long dataChangeId, String userId, String clientIp);

    List<ManagementFeeRaw> getAll();

}
