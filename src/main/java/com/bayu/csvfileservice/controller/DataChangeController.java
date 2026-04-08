package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.dto.ApiResponse;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.service.DataChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/data-change")
@Slf4j
@RequiredArgsConstructor
public class DataChangeController {

    private final DataChangeService dataChangeService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DataChange>>> getAll() {
        List<DataChange> all = dataChangeService.getAll();
        return buildResponse(all);
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
