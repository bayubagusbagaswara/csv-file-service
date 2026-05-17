package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.NcbsResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferExecutionResult {

    private String inquiryReferenceId;

    private String referenceId;

    private String responseCode;

    private String responseMessage;

    private NcbsResponse finalResponse;

}
