package com.bayu.csvfileservice.dto.taxbrokerfee;

import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaxBrokerFeeMapDto {

    private Long id;

    private LocalDate date;

    private String portfolioCode;

    private String name;

    private String investmentManager;

    private String debitAccount;

    private String creditAccount;

    private String creditAccountName;

    private String biCode;

    private String beneficiaryBankName;

    private String type;

    private BigDecimal totalRvp;

    private BigDecimal totalDvp;

    private BigDecimal netAmount;

    private BigDecimal taxAmount;

    private String description;

    private TransferScope transferScope;

    private TransferMethod transferMethod;

    private MappingStatus status;

    private ApprovalStatus approvalStatus;

    private String referenceId;

    private String inquiryReferenceId;

    private Integer retryCount;

    private LocalDateTime lastSentDate;

    private String referenceCombination;

}
