package com.bayu.csvfileservice.dto.sinvest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SInvestBulkRequest {

    private List<SInvestRequest> items;
}