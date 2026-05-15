package com.bayu.csvfileservice.dto.sinvest;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SInvestBulkRequest extends InputIdentifierRequest {

    private List<SInvestRequest> items;

}