package com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankInfo {

    private String biCode;
    private String cocCode;
    private String name;

}
