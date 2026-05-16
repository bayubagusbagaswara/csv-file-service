package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import com.bayu.csvfileservice.model.enumerator.*;
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

    @Column(name = "im_code")
    private String imCode;

    @Column(name = "im_name")
    private String imName;

    @Column(name = "fund_code")
    private String fundCode;

    @Column(name = "fund_name")
    private String fundName;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "cash_account_name")
    private String cashAccountName;

    @Column(name = "cash_account_no")
    private String cashAccountNo;

    @Column(name = "currency")
    private String currency;

    @Column(name = "principle")
    private BigDecimal principle;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "si_reference_id", nullable = false, length = 100)
    private String siReferenceId;

    @Column(name = "reference_no")
    private String referenceNo;

    // ============  additional field from Master Debit Account Product =====================
    @Column(name = "account_debit_no")
    private String accountDebitNo;
    @Column(name = "product_code")
    private String productCode;

    // ============ additional field from Master Bank ==============================
    @Column(name = "bi_code")
    private String biCode;
    @Column(name = "bank_type")
    private String bankType;
    @Column(name = "branch_code")
    private String branchCode;

    // ============ description field =====================================
    @Column(name = "description")
    private String description;

    // ============= helper column for bulk transactions ===================
    @Column(name = "bulk_reference_id")
    private String bulkReferenceId;
    @Column(name = "bulk_si_reference_ids", length = 2000)
    private String bulkSiReferenceIds;

    // ============ enumerator ======================
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_scope")
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_method")
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_type")
    private ProcessType processType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    private TransactionStatus transactionStatus;

    // ============= referenceId for Middleware unique id, auto generate by system =============
    @Column(name = "reference_id")
    private String referenceId;

    // ============= Helper column to count how many times a transaction was executed and when the transaction was last executed. =============
    @Column(name = "retry_count")
    private Integer retryCount;
    @Column(name = "last_sent_date")
    private LocalDateTime lastSentDate;


}
