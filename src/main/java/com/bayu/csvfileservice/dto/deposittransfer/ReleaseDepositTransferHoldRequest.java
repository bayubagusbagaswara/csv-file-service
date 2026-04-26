package com.bayu.csvfileservice.dto.deposittransfer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReleaseDepositTransferHoldRequest {

    private Long id;
    private String releaseId;
}