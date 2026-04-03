package com.bayu.csvfileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    private String field;
    private Object value;
    private List<String> messages;

    public static ErrorDetail of(String field, Object value, List<String> messages) {
        return new ErrorDetail(field, value, messages);
    }

}
