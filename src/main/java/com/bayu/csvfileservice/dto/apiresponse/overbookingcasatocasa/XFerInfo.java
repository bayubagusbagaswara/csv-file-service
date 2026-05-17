package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XFerInfo {

    private String xFerAmt;
    private String xFerDesc1;
    private String xFerDesc2;

}
