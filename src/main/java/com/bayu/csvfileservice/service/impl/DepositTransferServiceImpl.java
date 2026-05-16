package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.ReleaseDepositTransferHoldRequest;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.CreateBulkDepositTransferTransactionRequest;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.CreateSingleDepositTransferTransactionRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestRequest;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.mapper.DepositTransferMapper;
import com.bayu.csvfileservice.model.*;
import com.bayu.csvfileservice.model.enumerator.*;
import com.bayu.csvfileservice.repository.DebitAccountProductRepository;
import com.bayu.csvfileservice.repository.DepositTransferMapRepository;
import com.bayu.csvfileservice.repository.MasterBankRepository;
import com.bayu.csvfileservice.repository.SInvestRepository;
import com.bayu.csvfileservice.service.DepositTransferService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.EnumConverter;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    public ProcessResult createSingleTransaction(CreateSingleDepositTransferTransactionRequest request) {

        ProcessResult result = new ProcessResult();

        try {
            TransferMethod transferMethod = EnumConverter.fromTransferMethod(request.getTransferMethod());
            DepositTransferMap entity = depositTransferMapRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("DepositTransferMap not found"));

            if (!MappingStatus.DRAFT.equals(entity.getMappingStatus())) {
                throw new IllegalStateException("Only DRAFT data can create single transaction");
            }

            transferMethodValidator.validate(
                    entity.getTransferScope(),
                    transferMethod
            );

            entity.setProcessType(ProcessType.SINGLE);
            entity.setBulkReferenceId(null);
            entity.setMappingStatus(MappingStatus.READY);

            depositTransferMapRepository.save(entity);

            result.addSuccess();

        } catch (Exception e) {
            log.error("Failed create single transaction id {}", request.getId(), e);

            result.addError(ErrorDetail.of(
                    "id",
                    String.valueOf(request.getId()),
                    Collections.singletonList(e.getMessage())
            ));
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult createBulkTransaction(CreateBulkDepositTransferTransactionRequest request) {

        ProcessResult result = new ProcessResult();

        try {
            List<DepositTransferMap> list = depositTransferMapRepository.findAllById(request.getIds());

            if (list.size() != request.getIds().size()) {
                throw new IllegalArgumentException("Some DepositTransferMap data not found");
            }

            validateBulkCandidates(list);

            DepositTransferMap first = list.get(0);

            transferMethodValidator.validate(
                    first.getTransferScope(),
                    request.getTransferMethod()
            );

            String bulkReferenceId = UUID.randomUUID().toString();
            String joinedSiReferenceIds = buildJoinedSiReferenceIds(list);

            for (DepositTransferMap entity : list) {
                entity.setProcessType(ProcessType.BULK);
                entity.setBulkReferenceId(bulkReferenceId);
                entity.setMappingStatus(MappingStatus.READY);

                depositTransferMapRepository.save(entity);
            }

            result.addSuccess();

        } catch (Exception e) {
            log.error("Failed create bulk transaction ids {}", request.getIds(), e);

            result.addError(ErrorDetail.of(
                    "ids",
                    String.valueOf(request.getIds()),
                    Collections.singletonList(e.getMessage())
            ));
        }

        return result;
    }

    private void processSingleSend(DepositTransferMap entity, ProcessResult result) {

        try {
            validateSendStatus(entity);

            entity.setMappingStatus(MappingStatus.SENT);
//            entity.setLastSentDate(LocalDateTime.now());
//            entity.setRetryCount(entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1);

            depositTransferMapRepository.save(entity);

            Transferable transferable = transferableMapper.fromDepositTransferMap(entity);

            NcbsResponse response = transferOrchestratorService.execute(transferable);

            //entity.setReferenceId(response.getReferenceId());

            applyResponseStatus(entity, response);

            depositTransferMapRepository.save(entity);

            result.addSuccess();

        } catch (Exception e) {
            log.error("Failed send single deposit transfer id {}", entity.getId(), e);

            entity.setMappingStatus(MappingStatus.FAILED);
            depositTransferMapRepository.save(entity);

            result.addError(ErrorDetail.of(
                    "id",
                    String.valueOf(entity.getId()),
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

    private void processBulkSend(String bulkReferenceId, ProcessResult result) {

        List<DepositTransferMap> bulkItems =
                depositTransferMapRepository.findAllByBulkReferenceId(bulkReferenceId);

        try {
            validateBulkSendItems(bulkItems);

            LocalDateTime now = LocalDateTime.now();

            for (DepositTransferMap entity : bulkItems) {
                entity.setMappingStatus(MappingStatus.SENT);
                //entity.setLastSentDate(now);
                //entity.setRetryCount(entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1);

                depositTransferMapRepository.save(entity);
            }

            Transferable transferable = transferableMapper.fromDepositTransferBulk(bulkItems);

            NcbsResponse response = transferOrchestratorService.execute(transferable);

            for (DepositTransferMap entity : bulkItems) {
                //entity.setReferenceId(response.getReferenceId());
                applyResponseStatus(entity, response);

                depositTransferMapRepository.save(entity);
            }

            result.addSuccess();

        } catch (Exception e) {
            log.error("Failed send bulk deposit transfer bulkReferenceId {}", bulkReferenceId, e);

            for (DepositTransferMap entity : bulkItems) {
                entity.setMappingStatus(MappingStatus.FAILED);
                depositTransferMapRepository.save(entity);
            }

            result.addError(ErrorDetail.of(
                    "bulkReferenceId",
                    bulkReferenceId,
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

    private void validateBulkCandidates(List<DepositTransferMap> list) {

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Bulk data cannot be empty");
        }

        DepositTransferMap first = list.get(0);

        for (DepositTransferMap item : list) {

            if (!MappingStatus.DRAFT.equals(item.getMappingStatus())) {
                throw new IllegalStateException("Only DRAFT data can be bulked");
            }

            if (!Objects.equals(first.getFundCode(), item.getFundCode())) {
                throw new IllegalArgumentException("All fundCode must be same for bulk transaction");
            }

            if (!Objects.equals(first.getFundName(), item.getFundName())) {
                throw new IllegalArgumentException("All fundName must be same for bulk transaction");
            }

            if (!Objects.equals(first.getTransferScope(), item.getTransferScope())) {
                throw new IllegalArgumentException("All transferScope must be same for bulk transaction");
            }

            if (!Objects.equals(first.getBankCode(), item.getBankCode())) {
                throw new IllegalArgumentException("All bankCode must be same for bulk transaction");
            }

            if (!Objects.equals(first.getCurrency(), item.getCurrency())) {
                throw new IllegalArgumentException("All currency must be same for bulk transaction");
            }
        }
    }

    private void validateBulkSendItems(List<DepositTransferMap> bulkItems) {

        if (bulkItems == null || bulkItems.isEmpty()) {
            throw new IllegalArgumentException("Bulk data not found");
        }

        for (DepositTransferMap entity : bulkItems) {
            validateSendStatus(entity);

            if (!ProcessType.BULK.equals(entity.getProcessType())) {
                throw new IllegalStateException("Invalid process type for bulk");
            }
        }
    }

    private void validateSendStatus(DepositTransferMap entity) {

        if (!MappingStatus.READY.equals(entity.getMappingStatus())
                && !MappingStatus.RETRY.equals(entity.getMappingStatus())) {

            throw new IllegalStateException("Only READY or RETRY data can be sent");
        }
    }

    private void applyResponseStatus(DepositTransferMap entity, NcbsResponse response) {

        if (ApiResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            entity.setMappingStatus(MappingStatus.SUCCESS);
            return;
        }

        if (responseCodeService.isInsufficientBalance(response.getResponseCode())) {
            entity.setMappingStatus(MappingStatus.RETRY);
            return;
        }

        entity.setMappingStatus(MappingStatus.FAILED);
    }

    private String buildJoinedSiReferenceIds(List<DepositTransferMap> list) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(list.get(i).getSiReferenceId());
        }

        return builder.toString();
    }

    @Override
    @Transactional
    public ProcessResult uploadRaw(SInvestBulkRequest request) {

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
                        depositTransferMapper.fromSInvestToDepositTransferMap(sInvest);

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
                //depositTransferMap.setRetryCount(0);

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
                //entity.setLastSentDate(LocalDateTime.now());
                //entity.setRetryCount(entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1);

                depositTransferMapRepository.save(entity);

                Transferable transferable = transferableMapper.fromDepositTransferMap(entity);

                NcbsResponse response = transferOrchestratorService.execute(transferable);

                //entity.setReferenceId(response.getReferenceId());

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

            //entity.setReleaseId(request.getReleaseId());
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