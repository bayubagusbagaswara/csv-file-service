package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BdiXFerXfOff {

    private String bdiXFerAmtFrom;
    private String bdiXFerAmtFromLce;
    private String bdiXFerAmtTo;
    private String bdiXFerAmtToLce;
    private String bdiXFerType;
    private String bdiXFerCurrencyCode;
    private String bdiXRateAmt;
    private String bdiStdRateAmt;
    private String bdiXRefNumber;
    private BdiXFerBeneficiary bdiXFerBeneficiary;
    private String bdiXFerCostCenter;
    private String bdiFeeAmt;
    private String bdiFeeAmtLce;
    private String bdiFeeExchangeRate;
    private String bdiFeeProcIr;
    private BdiXFerMemo bdiXFerMemo;
    private TransInfo transInfo;
    private String lldInfo;

}
