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
@Table(name = "s_invest")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SInvest extends BaseApproval {

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

    private LocalDate date;

    @Column(name = "si_reference_id", nullable = false, length = 100)
    private String siReferenceId;

    private String referenceNo;
}
