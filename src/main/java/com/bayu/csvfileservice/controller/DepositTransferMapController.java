package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.dto.deposittransfer.map.ReleaseDepositTransferHoldRequest;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.service.DepositTransferMapService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.EnumConverter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * to handle map between SInvest, DebitAccountProduct, and MasterBank
 * HOLD akan dilakukan disini
 * jadi jika user melakukan relase HOLD, maka akan update table DepositTransferMap
 */
@RestController
@RequestMapping("/api/deposit-transfer-maps")
public class DepositTransferMapController {

    private final DepositTransferMapService mapService;

    public DepositTransferMapController(DepositTransferMapService mapService) {
        this.mapService = mapService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ProcessResult>> map(
            @RequestBody InputIdentifierRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String userId = request.getInputId();
        LocalDate currentDate = LocalDate.now();

        ProcessResult processResult = mapService.map(currentDate, userId, clientIp);
        return ApiResponseBuilder.success(processResult);
    }


    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<DepositTransferMapDto>>> getAllByCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        List<DepositTransferMapDto> list = mapService.getAllByCurrentDate(currentDate);
        return ApiResponseBuilder.success(list);
    }

    @GetMapping("/current/mapping-status")
    public ResponseEntity<ApiResponse<List<DepositTransferMapDto>>> getAllByCurrentDateAndMappingStatus(
            @RequestParam("mappingStatus") String mappingStatus
    ) {
        LocalDate currentDate = LocalDate.now();
        MappingStatus mappingStatusEnum = EnumConverter.fromMappingStatus(mappingStatus);

        List<DepositTransferMapDto> list =
                mapService.getAllByCurrentDateAndMappingStatus(currentDate, mappingStatusEnum);

        return ApiResponseBuilder.success(list);
    }

    @GetMapping("/current/approval-status/transfer-method")
    public ResponseEntity<ApiResponse<List<DepositTransferMapDto>>> getAllByCurrentDateAndApprovalStatusAndTransferMethod(
            @RequestParam("approvalStatus") String approvalStatus,
            @RequestParam("transferMethod") String transferMethod
    ) {
        LocalDate currentDate = LocalDate.now();
        ApprovalStatus approvalStatusEnum = EnumConverter.fromApprovalStatus(approvalStatus);
        TransferMethod transferMethodEnum = EnumConverter.fromTransferMethod(transferMethod);

        List<DepositTransferMapDto> list =
                mapService.getAllByCurrentDateAndApprovalStatusAndTransferMethod(
                        currentDate,
                        approvalStatusEnum,
                        transferMethodEnum
                );

        return ApiResponseBuilder.success(list);
    }

    public ResponseEntity<ApiResponse<ProcessResult>> releaseHold(
            @RequestBody ReleaseDepositTransferHoldRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        String releaseBy = request.getReleaseBy();

        ProcessResult processResult = mapService.releaseHold(request, releaseBy, clientIp);
        return ApiResponseBuilder.success(processResult);
    }

}
