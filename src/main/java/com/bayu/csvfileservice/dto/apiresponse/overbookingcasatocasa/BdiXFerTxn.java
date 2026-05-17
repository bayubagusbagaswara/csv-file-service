package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdiXFerTxn {

    private String bdiXFerAmtFrm;
    private String bdiXFerAmtFrmLce;
    private String bdiXRateAmt;
    private String bdiStdRateAmt;
    private String bdiXFerAmtTo;
    private String bdiXFerAmtToLce;

}
