package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Body {

    private AcctIdFrom acctIdFrom;
    private AcctIdTo acctIdTo;
    private XFerInfo xFerInfo;
}
