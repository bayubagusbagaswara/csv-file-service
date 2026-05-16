package com.bayu.csvfileservice.dto.deposittransfer.map;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReleaseDepositTransferHoldRequest {

    private List<Long> ids;
    private String releaseBy;

}