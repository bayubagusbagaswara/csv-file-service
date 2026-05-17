package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import com.bayu.csvfileservice.util.EnumConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management-fee/map")
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeMapController {

    private final ManagementFeeMapService service;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ProcessResult>> map(
            @RequestParam String month,
            @RequestParam Integer year,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);
        Month monthEnum = EnumConverter.fromMonth(month);
        ProcessResult result = service.map(monthEnum, year, clientIp);
        return ApiResponseBuilder.success(result);
    }

}
