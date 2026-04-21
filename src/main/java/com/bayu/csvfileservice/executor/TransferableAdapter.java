package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.enumerator.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Data
@Builder
public class TransferableAdapter implements Transferable {

    private Long id;
    private TransferMethod transferMethod;
    private TransferScope transferScope;
    private ProcessType processType;
    private FeatureType featureType;
    private MappingStatus mappingStatus;

    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String description;

    private String biCode;
    private String branchCode;

}
