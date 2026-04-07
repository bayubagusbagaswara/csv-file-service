package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "management_fee_map")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ManagementFeeMap extends BaseApproval {

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
    private String branchCode;

    private String biCode;

    @Enumerated(EnumType.STRING)
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    private MappingStatus status;

    private String referenceId;

    private Integer retryCount;

    private LocalDateTime lastSentDate;

}
