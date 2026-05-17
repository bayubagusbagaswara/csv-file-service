package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeBulkRequest;
import com.bayu.csvfileservice.model.ManagementFee;
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

import java.util.List;

@RestController
@RequestMapping(path = "/api/management-fee")
@RequiredArgsConstructor
public class ManagementFeeController {

    private static final Logger log = LoggerFactory.getLogger(ManagementFeeController.class);

    private static final String BASE_URL = "/api/management-fee";
    private static final String MENU_NAME = "Management Fee";

    private final ManagementFeeService managementFeeService;
    private final DataChangeFactory dataChangeFactory;

    @PostMapping(path = "/upload")
    public ResponseEntity<ApiResponse<ProcessResult>> upload(
            @RequestBody ManagementFeeBulkRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        DataChangeDto dataChangeDto = dataChangeFactory.build(
                userId,
                clientIp,
                "POST",
                BASE_URL + "/create/approve",
                MENU_NAME
        );
        ProcessResult result = managementFeeService.createBulk(request, dataChangeDto);
        return ApiResponseBuilder.success(result);
    }

    @PostMapping(path = "/create/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> approveCreate(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();
        Long dataChangeId = request.getDataChangeId();
        ProcessResult result = managementFeeService.approveCreate(dataChangeId, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<ApiResponse<List<ManagementFee>>> getAll() {
        List<ManagementFee> list = managementFeeService.getAll();
        return ApiResponseBuilder.success(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessResult>> deleteById(
            @PathVariable("id") Long id,
            @RequestBody DeleteIdRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        DataChangeDto dataChangeDto = dataChangeFactory.build(
                userId,
                clientIp,
                "DELETE",
                BASE_URL + "/delete/approve",
                MENU_NAME
        );
        ProcessResult result = managementFeeService.deleteById(id, dataChangeDto);
        return ApiResponseBuilder.success(result);
    }

    @DeleteMapping(path = "/delete/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> deleteApprove(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();
        Long dataChangeId = request.getDataChangeId();
        ProcessResult result = managementFeeService.approveDelete(dataChangeId, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

}
