package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tax_broker_fee")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaxBrokerFee extends BaseApproval {

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

    @Column(name = "reference_combination")
    private String referenceCombination; // unique

}
