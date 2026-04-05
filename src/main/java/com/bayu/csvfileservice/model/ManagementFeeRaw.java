package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import com.bayu.csvfileservice.model.enumerator.Month;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "management_fee_raw")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ManagementFeeRaw extends BaseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Month month;

    private Integer year;

    private String mutualFundName;

    private String investmentManager;

    private String fundCode;

    private String debitAccount;

    private BigDecimal amount;

    private String creditAccount;

    private String beneficiaryName;

    private String bankName;

    private String paymentInstructions;

    private String paymentType;

    private String period;

    private String description;

    private String bankCode;

}
