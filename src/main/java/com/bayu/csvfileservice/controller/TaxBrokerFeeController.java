package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.service.ManagementFeeService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.DataChangeFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/management-fee")
@RequiredArgsConstructor
public class TaxBrokerFeeController {

    private static final Logger log = LoggerFactory.getLogger(TaxBrokerFeeController.class);

    private static final String BASE_URL = "/api/management-fee";
    private static final String MENU_NAME = "Management Fee";

    private final ManagementFeeService managementFeeService;
    private final DataChangeFactory dataChangeFactory;

    private final TaxBrokerFeeService taxBrokerFeeService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ProcessResult>> upload(
            @RequestBody TaxBrokerFeeBulkRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeService.upload(
                request,
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/create/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> createApprove(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeService.createApprove(
                request,
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<ProcessResult>> delete(
            @RequestBody DeleteIdRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeService.deleteById(
                request,
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @PostMapping("/delete/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> deleteApprove(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeService.deleteApprove(
                request,
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxBrokerFeeDto>>> getAllByDate(
            @RequestParam LocalDate date
    ) {
        List<TaxBrokerFeeDto> result = taxBrokerFeeService.getAllByDate(date);

        return ApiResponseBuilder.success(result);
    }
}
