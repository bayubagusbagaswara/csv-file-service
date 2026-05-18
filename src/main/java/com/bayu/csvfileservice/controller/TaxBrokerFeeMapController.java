package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeMapDto;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeMapRequest;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.service.TaxBrokerFeeMapService;
import com.bayu.csvfileservice.util.ApiResponseBuilder;
import com.bayu.csvfileservice.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tax-broker-fee/map")
@RequiredArgsConstructor
public class TaxBrokerFeeMapController {

    private final TaxBrokerFeeMapService taxBrokerFeeMapService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProcessResult>> map(
            @RequestBody TaxBrokerFeeMapRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = ClientIpUtil.getClientIp(servletRequest);

        ProcessResult result = taxBrokerFeeMapService.map(
                request.getDate(),
                request.getUserId(),
                clientIp
        );

        return ApiResponseBuilder.success(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxBrokerFeeMapDto>>> getAllByDate(
            @RequestParam LocalDate date
    ) {
        List<TaxBrokerFeeMapDto> result =
                taxBrokerFeeMapService.getAllByDate(date);

        return ApiResponseBuilder.success(result);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<TaxBrokerFeeMapDto>>> getAllByDateAndStatus(
            @RequestParam LocalDate date,
            @RequestParam MappingStatus status
    ) {
        List<TaxBrokerFeeMapDto> result =
                taxBrokerFeeMapService.getAllByDateAndStatus(date, status);

        return ApiResponseBuilder.success(result);
    }
}
