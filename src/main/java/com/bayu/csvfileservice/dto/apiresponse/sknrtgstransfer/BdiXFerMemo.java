package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BdiXFerMemo {

    private String bdiFrMemo1;
    private String bdiFrMemo2;
    private String bdiToMemo1;
    private String bdiToMemo2;

}
