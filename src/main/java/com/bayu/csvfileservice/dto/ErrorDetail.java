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

    public static ErrorDetail of(String field, String value, List<String> messages) {
        return ErrorDetail.builder()
                .field(field)
                .value(value)
                .messages(messages.stream().distinct().toList())
                .build();
    }

}
