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
@Table(name = "s_invest_instruction")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SInvestInstruction extends BaseApproval {

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

    private boolean processedToPlacementData = false; // true jika sudah proses (sudah masuk ke PlacementData), dan false jika belum masuk ke PlacementData

}
