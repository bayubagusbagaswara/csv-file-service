package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.dto.ApiResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class ApiResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> buildResponse(T payload) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .payload(payload)
                .build();
        return ResponseEntity.ok(response);
    }

}
