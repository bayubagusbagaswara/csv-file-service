package com.bayu.csvfileservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ApproveDataChangeRequest extends ApprovalIdentifierRequest {

    private Long dataChangeId;

}
