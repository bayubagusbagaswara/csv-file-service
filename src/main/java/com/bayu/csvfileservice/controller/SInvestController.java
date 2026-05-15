package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestDto;
import com.bayu.csvfileservice.service.SInvestService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.DataChangeFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * to handle upload SInvest data
 */
@RestController
@RequestMapping("/api/s-invest")
public class SInvestController {

    private static final String BASE_URL = "/api/s-invest";
    private static final String MENU_NAME = "SInvest";

    private final SInvestService sInvestService;
    private final DataChangeFactory dataChangeFactory;

    public SInvestController(SInvestService sInvestService, DataChangeFactory dataChangeFactory) {
        this.sInvestService = sInvestService;
        this.dataChangeFactory = dataChangeFactory;
    }

    // upload data (create bulk)
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ProcessResult>> uploadData(
            @RequestBody SInvestBulkRequest request,
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
        ProcessResult processResult = sInvestService.createBulk(request, dataChangeDto);
        return ApiResponseBuilder.success(processResult);
    }

    // create approve
    @PostMapping("/create/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> approveCreate(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();
        Long dataChangeId = request.getDataChangeId();
        ProcessResult processResult = sInvestService.approveCreate(dataChangeId, userId, clientIp);
        return ApiResponseBuilder.success(processResult);
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
        ProcessResult result = sInvestService.deleteById(id, dataChangeDto);
        return ApiResponseBuilder.success(result);
    }


    @PostMapping("/delete/approve")
    public ResponseEntity<ApiResponse<ProcessResult>> approveDelete(
            @RequestBody ApproveDataChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getApproveId();
        Long dataChangeId = request.getDataChangeId();

        ProcessResult result = sInvestService.approveDelete(dataChangeId, userId, clientIp);
        return ApiResponseBuilder.success(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SInvestDto>> getById(
            @PathVariable("id") Long id
    ) {
        SInvestDto dto = sInvestService.getById(id);
        return ApiResponseBuilder.success(dto);
    }

    @GetMapping("/deletable")
    public ResponseEntity<ApiResponse<Boolean>> isDeletableBySiReferenceId(
            @RequestParam("siReferenceId") String siReferenceId
    ) {
        Boolean deletable = sInvestService.isDeletableBySiReferenceId(siReferenceId);
        return ApiResponseBuilder.success(deletable);
    }

}
