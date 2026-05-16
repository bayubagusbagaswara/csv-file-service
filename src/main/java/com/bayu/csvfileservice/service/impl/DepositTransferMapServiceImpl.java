package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.dto.deposittransfer.map.ReleaseDepositTransferHoldRequest;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.mapper.DepositTransferMapper;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.repository.DebitAccountProductRepository;
import com.bayu.csvfileservice.repository.DepositTransferMapRepository;
import com.bayu.csvfileservice.repository.MasterBankRepository;
import com.bayu.csvfileservice.repository.SInvestRepository;
import com.bayu.csvfileservice.service.DepositTransferMapService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class DepositTransferMapServiceImpl implements DepositTransferMapService {

    private static final String SI_REFERENCE_ID = "siReferenceId";

    private final SInvestRepository sInvestRepository;
    private final DepositTransferMapRepository depositTransferMapRepository;
    private final DebitAccountProductRepository debitAccountProductRepository;
    private final MasterBankRepository masterBankRepository;
    private final DepositTransferMapper depositTransferMapper;
    private final TransferMethodValidator transferMethodValidator;
    private final TransferableMapper transferableMapper;
    private final TransferOrchestratorService transferOrchestratorService;
    private final ResponseCodeService responseCodeService;

    public DepositTransferMapServiceImpl(SInvestRepository sInvestRepository, DepositTransferMapRepository depositTransferMapRepository, DebitAccountProductRepository debitAccountProductRepository, MasterBankRepository masterBankRepository, DepositTransferMapper depositTransferMapper, TransferMethodValidator transferMethodValidator, TransferableMapper transferableMapper, TransferOrchestratorService transferOrchestratorService, ResponseCodeService responseCodeService) {
        this.sInvestRepository = sInvestRepository;
        this.depositTransferMapRepository = depositTransferMapRepository;
        this.debitAccountProductRepository = debitAccountProductRepository;
        this.masterBankRepository = masterBankRepository;
        this.depositTransferMapper = depositTransferMapper;
        this.transferMethodValidator = transferMethodValidator;
        this.transferableMapper = transferableMapper;
        this.transferOrchestratorService = transferOrchestratorService;
        this.responseCodeService = responseCodeService;
    }

    @Override
    public ProcessResult map(LocalDate currentDate, String userId, String clientIp) {
        return null;
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDate(LocalDate currentDate) {
        return List.of();
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDateAndMappingStatus(LocalDate currentDate, MappingStatus mappingStatusEnum) {
        return List.of();
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDateAndApprovalStatusAndTransferMethod(LocalDate currentDate, ApprovalStatus approvalStatusEnum, TransferMethod transferMethodEnum) {
        return List.of();
    }

    @Override
    public ProcessResult releaseHold(ReleaseDepositTransferHoldRequest request, String releaseBy, String clientIp) {
        return null;
    }
}
