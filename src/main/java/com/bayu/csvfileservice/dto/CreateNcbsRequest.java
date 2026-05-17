package com.bayu.csvfileservice.dto;

import com.bayu.csvfileservice.model.enumerator.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
