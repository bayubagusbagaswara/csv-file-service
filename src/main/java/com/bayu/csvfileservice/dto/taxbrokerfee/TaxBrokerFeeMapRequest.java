package com.bayu.csvfileservice.dto.taxbrokerfee;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaxBrokerFeeMapRequest {

    private LocalDate date;

    private String userId;
}