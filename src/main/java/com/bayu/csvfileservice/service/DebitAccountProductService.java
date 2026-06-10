package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductBulkRequest;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductDto;

import java.util.List;

public interface DebitAccountProductService {

    ProcessResult uploadData(
            DebitAccountProductBulkRequest request,
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

    List<DebitAccountProductDto> getAll();

}
