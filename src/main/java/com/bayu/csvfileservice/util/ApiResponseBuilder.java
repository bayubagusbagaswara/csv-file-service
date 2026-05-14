package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.dto.ApiResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class ApiResponseBuilder {

    public static <T> ResponseEntity<ApiResponse<T>> success(T payload) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .payload(payload)
                .build();
        return ResponseEntity.ok(response);
    }

    public <T> ResponseEntity<ApiResponse<T>> created(T payload) {
        return null;
    }

    public <T> ResponseEntity<ApiResponse<T>> success(String message, T payload) {
        return null;
    }

    public <T> ResponseEntity<ApiResponse<T>> build(HttpStatus status, String message, T payload) {
        return null;
    }

}
