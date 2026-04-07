package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.managementfee.CreateTransactionRequest;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management-fee")
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeMapController {

    private final ManagementFeeMapService service;

    @PostMapping("/map")
    public ResponseEntity<ApiResponse<ProcessResult>> map(
            @RequestParam String month,
            @RequestParam Integer year
    ) {
        Month monthEnum = parseMonth(month);
        ProcessResult result = service.map(monthEnum, year);
        return buildResponse(result);
    }

    // ================= CREATE TRANSACTION =================

    @PostMapping("/map/create-transaction")
    public ResponseEntity<ApiResponse<ProcessResult>> createTransaction(
            @RequestBody List<CreateTransactionRequest> requests
    ) {
        ProcessResult result = service.createTransaction(requests);
        return buildResponse(result);
    }

    // ======================= SEND =======================

    @PostMapping("/map/send")
    public ResponseEntity<ApiResponse<ProcessResult>> send(
            @RequestBody List<Long> ids
    ) {
        ProcessResult result = service.send(ids);
        return buildResponse(result);
    }

    // ======================= HELPER =======================

    private Month parseMonth(String month) {
        try {
            return Month.valueOf(month.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
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
