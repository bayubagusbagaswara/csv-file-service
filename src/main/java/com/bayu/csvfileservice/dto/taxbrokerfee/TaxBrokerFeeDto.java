package com.bayu.csvfileservice.dto.taxbrokerfee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxBrokerFeeDto {

    private Long id;

    private LocalDate date;

    private String portfolioCode;

    private String name;

    private String investmentManager;

    private String debitAccount;

    private String creditAccount;

    private String creditAccountName;

    private String biCode;

    private String beneficiaryBankName;

    private String type;

    private BigDecimal totalRvp;

    private BigDecimal totalDvp;

    private BigDecimal netAmount;

    private BigDecimal taxAmount;

    private String description;

    private String referenceCombination;
}