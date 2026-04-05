package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.exception.JsonSerializeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonHelper {

    private final ObjectMapper objectMapper;

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new JsonSerializeException("Failed to parse JSON with message: " + e.getMessage(), e);
        }
    }

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new JsonSerializeException("Failed to serialize object with message: " + e.getMessage(), e);
        }
    }

}
