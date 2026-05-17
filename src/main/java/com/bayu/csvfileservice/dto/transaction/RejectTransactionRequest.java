package com.bayu.csvfileservice.dto.transaction;

import com.bayu.csvfileservice.dto.ApprovalIdentifierRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class RejectTransactionRequest extends ApprovalIdentifierRequest {

    private List<Long> ids;

}
