package com.bayu.csvfileservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DeleteIdRequest extends InputIdentifierRequest{

    private Long id;

}
