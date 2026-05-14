package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.*;
import com.bayu.csvfileservice.service.DepositTransferTransactionService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * to handle create transaction single or bulk and send transaction to middleware
 */
@RestController
@RequestMapping("/api/deposit-transfer-transactions")
public class DepositTransferTransactionController {

    private final DepositTransferTransactionService transactionService;

    public DepositTransferTransactionController(DepositTransferTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // create single transaction
    @PostMapping("/single")
    public ResponseEntity<ApiResponse<ProcessResult>> createSingleTransaction(
            @RequestBody CreateSingleDepositTransferTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();

        ProcessResult processResult = transactionService.createSingleTransaction(
                request,
                userId,
                clientIp
        );

        return ApiResponseBuilder.success(processResult);
    }

    // create bulk transaction
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<ProcessResult>> createBulkTransaction(
            @RequestBody CreateBulkDepositTransferTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();

        ProcessResult processResult = transactionService.createBulkTransaction(
                request,
                userId,
                clientIp
        );

        return ApiResponseBuilder.success(processResult);
    }

    // send transaction
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ProcessResult>> sendTransaction(
            @RequestBody SendDepositTransferTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();

        ProcessResult processResult = transactionService.sendTransaction(
                request,
                userId,
                clientIp
        );

        return ApiResponseBuilder.success(processResult);
    }

    // reject transaction
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<ProcessResult>> rejectTransaction(
            @RequestBody RejectDepositTransferTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();

        ProcessResult processResult = transactionService.rejectTransaction(
                request,
                userId,
                clientIp
        );

        return ApiResponseBuilder.success(processResult);
    }

    // get all transaction by current date
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<DepositTransferTransactionDto>>> getAllTransactionByCurrentDate() {
        LocalDate currentDate = LocalDate.now();

        List<DepositTransferTransactionDto> list =
                transactionService.getAllTransactionByCurrentDate(currentDate);

        return ApiResponseBuilder.success(list);
    }
}
