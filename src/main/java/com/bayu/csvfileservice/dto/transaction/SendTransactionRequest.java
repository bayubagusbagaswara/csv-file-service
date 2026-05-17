package com.bayu.csvfileservice.dto.transaction;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class SendTransactionRequest extends InputIdentifierRequest {

    private List<Long> ids;

}
