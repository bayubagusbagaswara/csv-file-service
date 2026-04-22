package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.CreateNcbsResponse;
import com.bayu.csvfileservice.model.NcbsResponse;
import com.bayu.csvfileservice.model.enumerator.MiddlewareServiceType;

import java.time.LocalDate;
import java.util.List;

public interface NcbsResponseService {

    NcbsResponse create(CreateNcbsResponse createNcbsResponse);

    List<NcbsResponse> getAllByServiceTypeAndCurrentDate(MiddlewareServiceType serviceType, LocalDate date);

}
