package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductBulkRequest;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductDto;
import com.bayu.csvfileservice.service.DebitAccountProductService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debit-account-products")
@RequiredArgsConstructor
public class DebitAccountProductController {

    private final DebitAccountProductService service;

    @PostMapping("/upload-data")
    public ResponseEntity<ApiResponse<ProcessResult>> uploadData(
            @RequestBody DebitAccountProductBulkRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result =
                service.uploadData(request, clientIp);

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/approve/create")
    public ResponseEntity<ApiResponse<ProcessResult>> approveCreate(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result =
                service.approveCreate(request, clientIp);

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/approve/edit")
    public ResponseEntity<ApiResponse<ProcessResult>> approveEdit(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result =
                service.approveEdit(request, clientIp);

        return ApiResponseBuilder.success(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DebitAccountProductDto>>> getAll() {
        List<DebitAccountProductDto> result =
                service.getAll();

        return ApiResponseBuilder.success(result);
    }
}