package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdiXFerXFon {

    private BdiXFerTxn bdiXFerTxn;
    private String bdiXFerEffNbr;
    private String bdiXFerCostCtr;
    private String bdiFeeAmt;
    private String bdiFeeAmtLce;
    private BdiXFerMemo bdiXFerMemo;
    private String lldInfo;

}
