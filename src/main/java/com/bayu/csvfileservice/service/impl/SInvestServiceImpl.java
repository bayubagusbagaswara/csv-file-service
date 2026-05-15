package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestDto;
import com.bayu.csvfileservice.service.SInvestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SInvestServiceImpl implements SInvestService {

    @Override
    public ProcessResult createBulk(SInvestBulkRequest request, DataChangeDto dataChangeDto) {
        return null;
    }

    @Override
    public ProcessResult approveCreate(Long dataChangeId, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult deleteById(Long id, DataChangeDto dataChangeDto) {
        return null;
    }

    @Override
    public ProcessResult approveDelete(Long dataChangeId, String userId, String clientIp) {
        return null;
    }

    @Override
    public SInvestDto getById(Long id) {
        return null;
    }

    @Override
    public Boolean isDeletableBySiReferenceId(String siReferenceId) {
        return null;
    }
}
