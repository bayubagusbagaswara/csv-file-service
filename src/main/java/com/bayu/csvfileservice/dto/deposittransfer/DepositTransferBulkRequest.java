package com.bayu.csvfileservice.dto.deposittransfer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DepositTransferBulkRequest {

    private List<SInvestRequest> items;
}