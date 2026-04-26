package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.CreateDepositTransferTransactionRequest;
import com.bayu.csvfileservice.dto.deposittransfer.DepositTransferBulkRequest;
import com.bayu.csvfileservice.dto.deposittransfer.ReleaseDepositTransferHoldRequest;
import com.bayu.csvfileservice.dto.deposittransfer.SInvestRequest;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.mapper.DepositTransferMapper;
import com.bayu.csvfileservice.model.*;
import com.bayu.csvfileservice.model.enumerator.ApiResponseCode;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import com.bayu.csvfileservice.repository.*;
import com.bayu.csvfileservice.service.DepositTransferService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositTransferServiceImpl implements DepositTransferService {

    private final SInvestRepository sInvestRepository;
    private final DepositTransferMapRepository depositTransferMapRepository;
    private final DebitAccountProductRepository accountDebitProductRepository;
    private final MasterBankRepository masterBankRepository;
    private final DepositTransferMapper depositTransferMapper;
    private final TransferMethodValidator transferMethodValidator;
    private final TransferableMapper transferableMapper;
    private final TransferOrchestratorService transferOrchestratorService;
    private final ResponseCodeService responseCodeService;

    @Override
    @Transactional
    public ProcessResult uploadRaw(DepositTransferBulkRequest request) {

        ProcessResult result = new ProcessResult();

        for (SInvestRequest item : request.getItems()) {
            try {
                if (sInvestRepository.existsBySiReferenceId(item.getSiReferenceId())) {
                    result.addError(ErrorDetail.of(
                            "siReferenceId",
                            item.getSiReferenceId(),
                            Collections.singletonList("SI Reference ID already exists")
                    ));
                    continue;
                }

                SInvest entity = depositTransferMapper.toSInvest(item);
                sInvestRepository.save(entity);

                result.addSuccess();

            } catch (Exception e) {
                log.error("Failed upload S-Invest {}", item.getSiReferenceId(), e);

                result.addError(ErrorDetail.of(
                        "siReferenceId",
                        item.getSiReferenceId(),
                        Collections.singletonList(e.getMessage())
                ));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult map() {

        ProcessResult result = new ProcessResult();

        List<SInvest> sInvestList = sInvestRepository.findAll();

        Map<String, Long> duplicateCounter = sInvestList.stream()
                .collect(Collectors.groupingBy(
                        SInvest::getSiReferenceId,
                        Collectors.counting()
                ));

        for (SInvest sInvest : sInvestList) {
            try {
                boolean duplicateInRaw = duplicateCounter.get(sInvest.getSiReferenceId()) != null
                        && duplicateCounter.get(sInvest.getSiReferenceId()) > 1;

                boolean alreadyExistsAsReadyOrSent =
                        depositTransferMapRepository.existsBySiReferenceIdAndMappingStatusIn(
                                sInvest.getSiReferenceId(),
                                Arrays.asList(
                                        MappingStatus.READY,
                                        MappingStatus.SENT,
                                        MappingStatus.SUCCESS,
                                        MappingStatus.RETRY
                                )
                        );

                DepositTransferMap depositTransferMap =
                        depositTransferMapper.toDepositTransferMap(sInvest);

                DebitAccountProduct accountDebitProduct = accountDebitProductRepository
                        .findByFundCode(sInvest.getFundCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "AccountDebitProduct not found for fundCode: " + sInvest.getFundCode()
                        ));

                MasterBank masterBank = masterBankRepository
                        .findByBankCode(sInvest.getBankCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "MasterBank not found for bankCode: " + sInvest.getBankCode()
                        ));

                depositTransferMap.setAccountDebitNo(accountDebitProduct.getCashAccount());
                depositTransferMap.setProductCode(accountDebitProduct.getProductCode());

                depositTransferMap.setBiCode(masterBank.getBiCode());
                depositTransferMap.setBankType(masterBank.getBankType());
                depositTransferMap.setBranchCode(masterBank.getBranchCode());

                depositTransferMap.setTransferScope(resolveTransferScope(sInvest.getBankCode()));
                depositTransferMap.setRetryCount(0);

                if (duplicateInRaw || alreadyExistsAsReadyOrSent) {
                    depositTransferMap.setMappingStatus(MappingStatus.HOLD);
                    depositTransferMap.setDescription("Possible duplicate SI Reference ID");
                } else {
                    depositTransferMap.setMappingStatus(MappingStatus.DRAFT);
                }

                depositTransferMapRepository.save(depositTransferMap);

                result.addSuccess();

            } catch (Exception e) {
                log.error("Failed mapping siReferenceId {}", sInvest.getSiReferenceId(), e);

                result.addError(ErrorDetail.of(
                        "siReferenceId",
                        sInvest.getSiReferenceId(),
                        Collections.singletonList(e.getMessage())
                ));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult createTransaction(List<CreateDepositTransferTransactionRequest> requests) {

        ProcessResult result = new ProcessResult();

        for (CreateDepositTransferTransactionRequest request : requests) {
            try {
                DepositTransferMap entity = depositTransferMapRepository.findById(request.getId())
                        .orElseThrow(() -> new IllegalArgumentException("DepositTransferMap not found"));

                if (!MappingStatus.DRAFT.equals(entity.getMappingStatus())) {
                    throw new IllegalStateException("Only DRAFT data can create transaction");
                }

                transferMethodValidator.validate(
                        entity.getTransferScope(),
                        request.getTransferMethod()
                );

                entity.setTransferMethod(request.getTransferMethod());
                entity.setMappingStatus(MappingStatus.READY);

                depositTransferMapRepository.save(entity);

                result.addSuccess();

            } catch (Exception e) {
                log.error("Failed create transaction id {}", request.getId(), e);

                result.addError(ErrorDetail.of(
                        "id",
                        String.valueOf(request.getId()),
                        Collections.singletonList(e.getMessage())
                ));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult sendTransaction(List<Long> ids) {

        ProcessResult result = new ProcessResult();

        List<DepositTransferMap> list = depositTransferMapRepository.findAllById(ids);

        for (DepositTransferMap entity : list) {
            try {
                if (!MappingStatus.READY.equals(entity.getMappingStatus())
                        && !MappingStatus.RETRY.equals(entity.getMappingStatus())) {
                    throw new IllegalStateException("Only READY or RETRY data can be sent");
                }

                entity.setMappingStatus(MappingStatus.SENT);
                entity.setLastSentDate(LocalDateTime.now());
                entity.setRetryCount(entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1);

                depositTransferMapRepository.save(entity);

                Transferable transferable = transferableMapper.fromDepositTransferMap(entity);

                NcbsResponse response = transferOrchestratorService.execute(transferable);

                entity.setReferenceId(response.getReferenceId());

                if (ApiResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                    entity.setMappingStatus(MappingStatus.SUCCESS);
                } else if (responseCodeService.isInsufficientBalance(response.getResponseCode())) {
                    entity.setMappingStatus(MappingStatus.RETRY);
                } else {
                    entity.setMappingStatus(MappingStatus.FAILED);
                }

                depositTransferMapRepository.save(entity);

                result.addSuccess();

            } catch (Exception e) {
                log.error("Failed send transaction id {}", entity.getId(), e);

                entity.setMappingStatus(MappingStatus.FAILED);
                depositTransferMapRepository.save(entity);

                result.addError(ErrorDetail.of(
                        "id",
                        String.valueOf(entity.getId()),
                        Collections.singletonList(e.getMessage())
                ));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult releaseHold(ReleaseDepositTransferHoldRequest request) {

        ProcessResult result = new ProcessResult();

        try {
            DepositTransferMap entity = depositTransferMapRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("DepositTransferMap not found"));

            if (!MappingStatus.HOLD.equals(entity.getMappingStatus())) {
                throw new IllegalStateException("Only HOLD data can be released");
            }

            entity.setReleaseId(request.getReleaseId());
            entity.setMappingStatus(MappingStatus.DRAFT);
            entity.setDescription("Released from HOLD");

            depositTransferMapRepository.save(entity);

            result.addSuccess();

        } catch (Exception e) {
            log.error("Failed release HOLD id {}", request.getId(), e);

            result.addError(ErrorDetail.of(
                    "id",
                    String.valueOf(request.getId()),
                    Collections.singletonList(e.getMessage())
            ));
        }

        return result;
    }

    private TransferScope resolveTransferScope(String bankCode) {
        return "0011".equals(bankCode)
                ? TransferScope.INTERNAL
                : TransferScope.EXTERNAL;
    }
}