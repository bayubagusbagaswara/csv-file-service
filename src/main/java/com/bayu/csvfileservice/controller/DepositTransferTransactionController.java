package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.*;
import com.bayu.csvfileservice.dto.transaction.CreateBulkTransactionRequest;
import com.bayu.csvfileservice.dto.transaction.CreateSingleTransactionRequest;
import com.bayu.csvfileservice.dto.transaction.SendTransactionRequest;
import com.bayu.csvfileservice.model.enumerator.ProcessType;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.service.DepositTransferTransactionService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.EnumConverter;
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

    @PostMapping("/single")
    public ResponseEntity<ApiResponse<ProcessResult>> createSingle(
            @RequestBody CreateSingleTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        Long id = request.getId();
        TransferMethod transferMethod = EnumConverter.fromTransferMethod(request.getTransferMethod());

        ProcessResult processResult = transactionService.createSingle(id, transferMethod, userId, clientIp);
        return ApiResponseBuilder.success(processResult);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<ProcessResult>> createBulk(
            @RequestBody CreateBulkTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        List<Long> ids = request.getIds();
        TransferMethod transferMethod = EnumConverter.fromTransferMethod(request.getTransferMethod());

        ProcessResult processResult = transactionService.createBulk(ids, transferMethod, userId, clientIp);
        return ApiResponseBuilder.success(processResult);
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ProcessResult>> send(
            @RequestBody SendTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        List<Long> ids = request.getIds();

        ProcessResult processResult = transactionService.send(ids, userId, clientIp);
        return ApiResponseBuilder.success(processResult);
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<ProcessResult>> reject(
            @RequestBody RejectDepositTransferTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();
        List<Long> ids = request.getIds();

        ProcessResult processResult = transactionService.reject(ids, userId, clientIp);
        return ApiResponseBuilder.success(processResult);
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<DepositTransferTransactionDto>>> getAllByCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        List<DepositTransferTransactionDto> list = transactionService.getAllByCurrentDate(currentDate);
        return ApiResponseBuilder.success(list);
    }

    @GetMapping("/process-type/current")
    public ResponseEntity<ApiResponse<List<DepositTransferTransactionDto>>> getAllByProcessTypeAndCurrentDate(
            @RequestParam("type") String type
    ) {
        LocalDate currentDate = LocalDate.now();
        ProcessType processType = EnumConverter.fromProcessType(type);
        List<DepositTransferTransactionDto> list = transactionService.getAllByProcessTypeAndCurrentDate(processType, currentDate);
        return ApiResponseBuilder.success(list);
    }

}
