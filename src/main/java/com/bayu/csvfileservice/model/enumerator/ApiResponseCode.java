package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum ApiResponseCode {

    SUCCESS("200R000000");

    private final String code;

    ApiResponseCode(String code) {
        this.code = code;
    }

}
