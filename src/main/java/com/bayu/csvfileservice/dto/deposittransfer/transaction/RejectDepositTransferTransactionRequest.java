package com.bayu.csvfileservice.dto.deposittransfer.transaction;

import com.bayu.csvfileservice.dto.ApprovalIdentifierRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class RejectDepositTransferTransactionRequest extends ApprovalIdentifierRequest {

    private List<Long> ids;

}
