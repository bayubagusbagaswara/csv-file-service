package com.bayu.csvfileservice.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class InputIdentifierRequest {

    private String inputId;

}
