package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.CreateDepositTransferTransactionRequest;
import com.bayu.csvfileservice.dto.deposittransfer.DepositTransferBulkRequest;
import com.bayu.csvfileservice.dto.deposittransfer.ReleaseDepositTransferHoldRequest;
import com.bayu.csvfileservice.service.DepositTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deposit-transfer")
@RequiredArgsConstructor
public class DepositTransferController {

    private final DepositTransferService depositTransferService;

    @PostMapping("/upload-raw")
    public ResponseEntity<ApiResponse<ProcessResult>> uploadRaw(
            @RequestBody DepositTransferBulkRequest request
    ) {
        return buildResponse(depositTransferService.uploadRaw(request));
    }

    @PostMapping("/map")
    public ResponseEntity<ApiResponse<ProcessResult>> map() {
        return buildResponse(depositTransferService.map());
    }

    @PostMapping("/create-transaction")
    public ResponseEntity<ApiResponse<ProcessResult>> createTransaction(
            @RequestBody List<CreateDepositTransferTransactionRequest> request
    ) {
        return buildResponse(depositTransferService.createTransaction(request));
    }

    @PostMapping("/send-transaction")
    public ResponseEntity<ApiResponse<ProcessResult>> sendTransaction(
            @RequestBody List<Long> ids
    ) {
        return buildResponse(depositTransferService.sendTransaction(ids));
    }

    @PostMapping("/release-hold")
    public ResponseEntity<ApiResponse<ProcessResult>> releaseHold(
            @RequestBody ReleaseDepositTransferHoldRequest request
    ) {
        return buildResponse(depositTransferService.releaseHold(request));
    }

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(T payload) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .payload(payload)
                .build();

        return ResponseEntity.ok(response);
    }
}
