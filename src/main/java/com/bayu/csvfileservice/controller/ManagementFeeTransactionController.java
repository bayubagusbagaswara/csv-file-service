package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.transaction.CreateSingleTransactionRequest;
import com.bayu.csvfileservice.dto.transaction.SendTransactionRequest;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
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

    private final ManagementFeeMapService service;

    public ManagementFeeTransactionController(ManagementFeeMapService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProcessResult>> createTransactions(
            @RequestBody List<CreateSingleTransactionRequest> requests,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        ProcessResult result = service.create(requests, clientIp);
        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ProcessResult>> sendTransactions(
            @RequestBody SendTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        List<Long> ids = request.getIds();
        ProcessResult result = service.send(ids, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

}
