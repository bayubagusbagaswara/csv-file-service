package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.CreateNcbsRequest;
import com.bayu.csvfileservice.model.NcbsRequest;

public interface NcbsRequestService {

    NcbsRequest create(CreateNcbsRequest createNcbsRequest);

}
