package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PlacementData
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "placement_deposit_map")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DepositTransferMap extends BaseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imCode;
    private String imName;
    private String fundCode;
    private String fundName;
    private String bankCode;
    private String bankName;
    private String cashAccountName;
    private String cashAccountNo;
    private String currency;

    private BigDecimal principle;

    private LocalDate placementDate;

    @Column(name = "si_reference_id", nullable = false, length = 100)
    private String siReferenceId;

    private String accountDebitNo;
    private String productCode;

    private String biCode;
    private String bankType;
    private String branchCode;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status")
    private MappingStatus mappingStatus;

    private String referenceId;
    private Integer retryCount;
    private LocalDateTime lastSentDate;

    private String releaseId;
}
