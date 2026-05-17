package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransInfo {

    private String trn;
    private String fee;

}
