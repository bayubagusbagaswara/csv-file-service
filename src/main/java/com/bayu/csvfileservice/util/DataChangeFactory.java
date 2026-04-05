package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataChangeFactory {

    public DataChangeDto build(
            String inputId,
            String ip,
            String httpMethod,
            String endpoint,
            String menu
    ) {
        return DataChangeDto.builder()
                .inputId(inputId)
                .inputIpAddress(ip)
                .inputDate(LocalDateTime.now())
                .httpMethod(httpMethod)
                .endpoint(endpoint)
                .requestBody(true)
                .requestParam(false)
                .pathVariable(false)
                .menu(menu)
                .build();
    }

}