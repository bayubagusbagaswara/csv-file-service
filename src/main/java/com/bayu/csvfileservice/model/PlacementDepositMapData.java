package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
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
public class PlacementDepositMapData extends BaseApproval {

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

    private String referenceNo;

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

    private String placementApprovalId;

    private String placementApprovalStatus;

    private String releaseId;

}
