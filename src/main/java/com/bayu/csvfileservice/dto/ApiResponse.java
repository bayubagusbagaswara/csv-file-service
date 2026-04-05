package com.bayu.csvfileservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {

    private int code;
    private String message;
    private T payload;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T payload) {
        this.code = code;
        this.message = message;
        this.payload = payload;
    }

    public static <T> ApiResponse<T> success(T payload) {
        return new ApiResponse<>(200, "Success", payload);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

}
