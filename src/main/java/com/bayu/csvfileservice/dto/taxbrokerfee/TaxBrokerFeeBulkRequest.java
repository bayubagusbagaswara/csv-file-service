package com.bayu.csvfileservice.dto.taxbrokerfee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxBrokerFeeBulkRequest {

    private String userId;

    private List<TaxBrokerFeeRequest> items;
}