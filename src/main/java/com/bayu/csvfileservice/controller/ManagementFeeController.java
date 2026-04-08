package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeBulkRequest;
import com.bayu.csvfileservice.model.ManagementFeeRaw;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.service.ManagementFeeRawService;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.DataChangeFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/management-fee")
@Slf4j
@RequiredArgsConstructor
public class ManagementFeeController {

    private static final String BASE_URL_MANAGEMENT_FEE = "/api/management-fee";
    private static final String MENU_MANAGEMENT_FEE = "Management Fee";

    private final ManagementFeeRawService rawService;
    private final ManagementFeeMapService mapService;
    private final DataChangeFactory dataChangeFactory;

    @PostMapping(path = "/upload-bulk")
    public ResponseEntity<ApiResponse<ProcessResult>> createBulk(@RequestBody ManagementFeeBulkRequest request, HttpServletRequest servletRequest) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        DataChangeDto dataChangeDto = dataChangeFactory.build(
                request.getInputId(),
                clientIp,
                HttpMethod.POST.name(),
                BASE_URL_MANAGEMENT_FEE + "/create/approve",
                MENU_MANAGEMENT_FEE
        );
        ProcessResult result = rawService.createBulk(request, dataChangeDto);
        return buildResponse(result);
    }

    @PostMapping(path = "/create/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> createApprove(@RequestBody ApproveDataChangeRequest request, HttpServletRequest servletRequest) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        ProcessResult result = rawService.createApprove(request, clientIp);
        return buildResponse(result);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<ApiResponse<List<ManagementFeeRaw>>> getAll() {
        List<ManagementFeeRaw> list = rawService.getAll();
        return buildResponse(list);
    }

    @DeleteMapping(path = "/deleteById")
    public ResponseEntity<ApiResponse<ProcessResult>> deleteById(
            @RequestBody DeleteIdRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        DataChangeDto dataChangeDto = dataChangeFactory.build(
                request.getInputId(),
                clientIp,
                HttpMethod.DELETE.name(),
                BASE_URL_MANAGEMENT_FEE + "/delete/approve",
                MENU_MANAGEMENT_FEE
        );
        ProcessResult result = rawService.deleteById(request, dataChangeDto);
        return buildResponse(result);
    }

    @DeleteMapping(path = "/delete/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> deleteApprove(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        ProcessResult result = rawService.deleteApprove(request, clientIp);
        return buildResponse(result);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(T payload) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .payload(payload)
                .build();

        return ResponseEntity.ok(response);
    }

}
