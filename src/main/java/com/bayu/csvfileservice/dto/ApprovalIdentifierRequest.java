package com.bayu.csvfileservice.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class ApprovalIdentifierRequest {

    private String approveId;

}
