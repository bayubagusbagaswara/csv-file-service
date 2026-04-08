package com.bayu.csvfileservice.dto.managementfee;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ManagementFeeBulkRequest extends InputIdentifierRequest {

    private List<ManagementFeeRequest> items;

}
