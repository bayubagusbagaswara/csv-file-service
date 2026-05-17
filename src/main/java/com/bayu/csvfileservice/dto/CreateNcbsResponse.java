package com.bayu.csvfileservice.dto;

import com.bayu.csvfileservice.model.enumerator.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNcbsResponse {

    private LocalDateTime createdDate;

    private Long entityId;

    private LocalDate placementDate;

    private String siReferenceId;

    private String referenceCombination;

    private TransferScope transferScope;

    private ProcessType processType;

    private TransferMethod transferMethod;

    private String responseCode;

    private String responseMessage;

    private String providerSystem;

    private String statusCode;

    private String statusDesc;

    private String responseJson;

    private String ncbsStatus;

    private String referenceId;

    private String payUserRefNo;

    private MiddlewareServiceType serviceType;

    private FeatureType featureType;

}