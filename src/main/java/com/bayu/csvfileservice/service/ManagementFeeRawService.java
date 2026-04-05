package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeBulkRequest;

public interface ManagementFeeRawService {

    ProcessResult createBulk(ManagementFeeBulkRequest request, DataChangeDto dataChangeDto);

    ProcessResult createApprove(ApproveDataChangeRequest request, String clientIp);

}
