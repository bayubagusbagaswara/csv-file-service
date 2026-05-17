package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.model.enumerator.Month;

public interface ManagementFeeMapService {

    ProcessResult map(Month monthEnum, Integer year, String clientIp);

}
