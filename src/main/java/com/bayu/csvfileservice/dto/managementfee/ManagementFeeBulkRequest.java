package com.bayu.csvfileservice.dto.managementfee;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ManagementFeeBulkRequest extends InputIdentifierRequest {

    private List<ManagementFeeRequest> items;

}
