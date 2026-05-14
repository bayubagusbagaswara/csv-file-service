package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ini sama dengan DepositTransferMap, tetapi ada field tambahan yakni bulkSiReferenceIds dan bulkReferenceId
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DepositTransferTransaction extends BaseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bulk_reference_id")
    private String bulkReferenceId;

    @Column(name = "bulk_si_reference_ids", length = 2000)
    private String bulkSiReferenceIds;




}
