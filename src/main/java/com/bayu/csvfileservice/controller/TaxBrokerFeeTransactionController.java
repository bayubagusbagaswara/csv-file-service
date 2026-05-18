package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeCreateTransactionRequest;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeIdsRequest;
import com.bayu.csvfileservice.service.TaxBrokerFeeTransactionService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax-broker-fee/transaction")
@RequiredArgsConstructor
public class TaxBrokerFeeTransactionController {

    private final TaxBrokerFeeTransactionService taxBrokerFeeTransactionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProcessResult>> create(
            @RequestBody TaxBrokerFeeCreateTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeTransactionService.create(
                request.getId(),
                request.getTransferMethod(),
                request.getDescription(),
                request.getUserId(),
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ProcessResult>> send(
            @RequestBody TaxBrokerFeeIdsRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeTransactionService.send(
                request.getIds(),
                request.getUserId(),
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<ProcessResult>> reject(
            @RequestBody TaxBrokerFeeIdsRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeTransactionService.reject(
                request.getIds(),
                request.getUserId(),
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }
}
