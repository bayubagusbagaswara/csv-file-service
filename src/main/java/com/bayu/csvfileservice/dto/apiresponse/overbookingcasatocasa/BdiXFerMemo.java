package com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdiXFerMemo {

    private String bdiFrMemo1;
    private String bdiFrMemo2;
    private String bdiToMemo1;
    private String bdiToMemo2;

}
