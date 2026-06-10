package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationBulkRequest;
import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationDto;

import java.util.List;

public interface UserLimitAuthorizationService {

    ProcessResult uploadData(
            UserLimitAuthorizationBulkRequest request,
            String clientIp
    );

    ProcessResult approveCreate(
            ApproveDataChangeRequest request,
            String clientIp
    );

    ProcessResult approveEdit(
            ApproveDataChangeRequest request,
            String clientIp
    );

    List<UserLimitAuthorizationDto> getAll();

}
