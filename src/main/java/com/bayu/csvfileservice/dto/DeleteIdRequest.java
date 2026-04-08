package com.bayu.csvfileservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteIdRequest extends InputIdentifierRequest {

    private Long id;

}
