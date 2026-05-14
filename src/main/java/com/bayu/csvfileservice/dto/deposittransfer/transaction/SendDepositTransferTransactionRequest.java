package com.bayu.csvfileservice.dto.deposittransfer.transaction;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class SendDepositTransferTransactionRequest extends InputIdentifierRequest {

    private List<Long> ids;

}
