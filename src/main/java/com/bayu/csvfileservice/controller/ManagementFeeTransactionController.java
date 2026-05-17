package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.transaction.CreateSingleTransactionRequest;
import com.bayu.csvfileservice.dto.transaction.RejectTransactionRequest;
import com.bayu.csvfileservice.dto.transaction.SendTransactionRequest;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.service.ManagementFeeTransactionService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.EnumConverter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/management-fee/transactions")
public class ManagementFeeTransactionController {

    private final ManagementFeeTransactionService managementFeeTransactionService;

    public ManagementFeeTransactionController(ManagementFeeTransactionService managementFeeTransactionService) {
        this.managementFeeTransactionService = managementFeeTransactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProcessResult>> create(
            @RequestBody CreateSingleTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        Long id = request.getId();
        TransferMethod transferMethod = EnumConverter.fromTransferMethod(request.getTransferMethod());
        String description = request.getDescription();
        ProcessResult result = managementFeeTransactionService.create(id, transferMethod, description, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ProcessResult>> send(
            @RequestBody SendTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        List<Long> ids = request.getIds();
        ProcessResult result = managementFeeTransactionService.send(ids, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<ProcessResult>> reject(
            @RequestBody RejectTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();
        List<Long> ids = request.getIds();
        ProcessResult result = managementFeeTransactionService.reject(ids, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

}
