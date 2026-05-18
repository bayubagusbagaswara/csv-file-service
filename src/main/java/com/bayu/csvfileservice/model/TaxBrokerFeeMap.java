package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.model.base.BaseApproval;
import com.bayu.csvfileservice.model.enumerator.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tax_broker_fee_map")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaxBrokerFeeMap  extends BaseApproval implements Transferable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "portfolio_code")
    private String portfolioCode;

    @Column(name = "name")
    private String name;

    @Column(name = "investment_manager")
    private String investmentManager;

    @Column(name = "debit_account")
    private String debitAccount;

    @Column(name = "credit_account")
    private String creditAccount;

    @Column(name = "credit_account_name")
    private String creditAccountName;

    @Column(name = "bi_code")
    private String biCode;

    @Column(name = "beneficiary_bank_name")
    private String beneficiaryBankName;

    @Column(name = "type")
    private String type;

    @Column(name = "total_rvp")
    private BigDecimal totalRvp;

    @Column(name = "total_dvp")
    private BigDecimal totalDvp;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_scope")
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_method")
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status")
    private MappingStatus mappingStatus;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "inquiry_reference_id")
    private String inquiryReferenceId;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_sent_date")
    private LocalDateTime lastSentDate;

    @Column(name = "reference_combination")
    private String referenceCombination;

    @Override
    public BigDecimal getAmount() {
        return taxAmount;
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.SINGLE;
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.TAX_BROKER_FEE;
    }

    @Override
    public String getBranchCode() {
        return null;
    }

    @Override
    public String getBulkReferenceId() {
        return null;
    }

    @Override
    public String getSiReferenceId() {
        return referenceCombination;
    }

}
