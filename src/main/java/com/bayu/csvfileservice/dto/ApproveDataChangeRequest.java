package com.bayu.csvfileservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApproveDataChangeRequest extends ApprovalIdentifierRequest {

    private Long dataChangeId;

}
