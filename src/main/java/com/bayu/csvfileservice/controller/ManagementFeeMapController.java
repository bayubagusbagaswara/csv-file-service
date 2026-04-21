package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.managementfee.CreateTransactionRequest;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam Integer year,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        Month monthEnum = parseMonth(month);
        ProcessResult result = service.map(monthEnum, year, clientIp);
        return buildResponse(result);
    }

    // ================= CREATE TRANSACTION =================
    @PostMapping("/map/create-transactions")
    public ResponseEntity<ApiResponse<ProcessResult>> createTransactions(
            @RequestBody List<CreateTransactionRequest> requests,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        ProcessResult result = service.createTransactions(requests, clientIp);
        return buildResponse(result);
    }

    // ======================= SEND =======================
    @PostMapping("/map/send-transactions")
    public ResponseEntity<ApiResponse<ProcessResult>> sendTransactions(
            @RequestBody List<Long> ids,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        ProcessResult result = service.sendTransactions(ids, clientIp);
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
