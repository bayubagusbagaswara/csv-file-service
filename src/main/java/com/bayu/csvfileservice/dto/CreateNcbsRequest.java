package com.bayu.csvfileservice.dto;

import com.bayu.csvfileservice.model.enumerator.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateNcbsRequest {

    private LocalDateTime createdDate;
    private Long entityId;
    private String siReferenceId;

    private TransferScope transferScope;
    private ProcessType processType;
    private TransferMethod transferMethod;
    private MiddlewareServiceType serviceType;

    private String requestJson;
    private String referenceId;

    private String payUserRefNo;

    private FeatureType featureType;
    private String referenceCombination;

}
