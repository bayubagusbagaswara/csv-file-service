package com.bayu.csvfileservice.dto.deposittransfer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SInvestRequest {

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
    private String siReferenceId;
}