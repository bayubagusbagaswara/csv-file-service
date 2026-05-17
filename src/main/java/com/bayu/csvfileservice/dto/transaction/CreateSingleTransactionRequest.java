package com.bayu.csvfileservice.dto.transaction;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CreateSingleTransactionRequest extends InputIdentifierRequest {

    private Long id;

    private String transferMethod;

    private String description;

}
