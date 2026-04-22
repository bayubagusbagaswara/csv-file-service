package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.NcbsRequest;
import com.bayu.csvfileservice.model.NcbsResponse;
import com.bayu.csvfileservice.service.NcbsRequestService;
import com.bayu.csvfileservice.service.NcbsResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTransferExecutor implements TransferExecutor {

    protected final ObjectMapper objectMapper;
    protected final NcbsRequestService ncbsRequestService;
    protected final NcbsResponseService ncbsResponseService;

    protected BaseTransferExecutor(ObjectMapper objectMapper, NcbsRequestService ncbsRequestService, NcbsResponseService ncbsResponseService) {
        this.objectMapper = objectMapper;
        this.ncbsRequestService = ncbsRequestService;
        this.ncbsResponseService = ncbsResponseService;
    }

    protected NcbsRequest createNcbsRequest() {

        return null;
    }

    protected NcbsResponse createNcbsResponse() {

        return null;
    }

    private String extractResponseCode() {

        return null;
    }

    private String extractResponseMessage() {

        return null;
    }
}
