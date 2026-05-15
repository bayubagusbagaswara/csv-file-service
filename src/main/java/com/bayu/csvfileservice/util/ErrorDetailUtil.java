package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.dto.ErrorDetail;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ErrorDetailUtil {

    public static ErrorDetail buildError(String field, String value, List<String> errors) {
        return ErrorDetail.of(
                field, value, errors.stream().distinct().toList()
        );
    }

}
