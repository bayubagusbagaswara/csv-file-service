package com.bayu.csvfileservice.dto.taxbrokerfee;

import lombok.Data;

import java.util.List;

@Data
public class TaxBrokerFeeIdsRequest {

    private List<Long> ids;

    private String userId;
}