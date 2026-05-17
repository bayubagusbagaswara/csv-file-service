package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.transaction.CreateSingleTransactionRequest;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.NcbsResponse;
import com.bayu.csvfileservice.model.TransactionLimit;
import com.bayu.csvfileservice.model.enumerator.ApiResponseCode;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.repository.*;
import com.bayu.csvfileservice.service.ManagementFeeTransactionService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.BankCodeHelper;
import com.bayu.csvfileservice.util.EnumConverter;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementFeeTransactionServiceImpl implements ManagementFeeTransactionService {

    private final ManagementFeeRepository rawRepository;
    private final ManagementFeeMapRepository mapRepository;
    private final MasterBankRepository masterBankRepository;
    private final DebitAccountProductRepository debitRepository;
    private final NcbsRequestRepository ncbsRequestRepository;
    private final NcbsResponseRepository ncbsResponseRepository;
    private final BankCodeHelper bankHelper;
    private final TransferMethodValidator validator;
    private final ResponseCodeRepository responseCodeRepository;
    private final TransactionLimitRepository transactionLimitRepository;
    private final TransferableMapper transferableMapper;
    private final TransferOrchestratorService orchestratorService;
    private final ResponseCodeService responseCodeService;



    // ================= CREATE TRANSACTION =================

    @Override
    @Transactional
    public ProcessResult create(List<CreateSingleTransactionRequest> items, String clientIp) {
        ProcessResult result = new ProcessResult();
        for (CreateSingleTransactionRequest item : items) {
            TransferMethod transferMethod = EnumConverter.fromTransferMethod(item.getTransferMethod());
            try {
                ManagementFeeMap entity = mapRepository.findById(item.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Data not found"));

                if (entity.getStatus() != MappingStatus.DRAFT) {
                    throw new IllegalStateException("Only DRAFT data can be processed");
                }

                // VALIDASI METHOD
                validator.validate(
                        entity.getTransferScope(),
                        transferMethod
                );

                // =============== Check range amount ===============
                BigDecimal amount = entity.getAmount();
                TransactionLimit transactionLimit = transactionLimitRepository.findByTransferMethod(transferMethod)
                        .orElseThrow(() -> new DataNotFoundException("Transfer method " + transferMethod.getName() + " is not defined"));
                if (amount.compareTo(transactionLimit.getMinAmount()) < 0
                        || amount.compareTo(transactionLimit.getMaxAmount()) > 0) {
                    throw new IllegalArgumentException("Amount " + amount + " is out of range for transfer method " + transferMethod.getName());
                }

                // =============== Set Transfer Method to Entity ================
                entity.setTransferMethod(transferMethod);
                entity.setStatus(MappingStatus.READY);

                // =============== Save entity ==================
                mapRepository.save(entity);
                result.addSuccess();

            } catch (Exception e) {
                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of(e.getMessage())
                        )
                );
            }
        }
        return result;
    }

    @Override
    public ProcessResult send(List<Long> ids, String userId, String clientIp) {
        ProcessResult result = new ProcessResult();
        List<ManagementFeeMap> list = mapRepository.findAllById(ids);

        for (ManagementFeeMap entity : list) {
            try {
                // ============= VALIDATION ==================
                if (!MappingStatus.READY.equals(entity.getStatus())
                        && !MappingStatus.RETRY.equals(entity.getStatus())) {
                    throw new IllegalStateException("Data with id " + entity.getId() + " cannot be sent");
                }

                // ================= Set SENT =============
                entity.setStatus(MappingStatus.SENT);
                entity.setLastSentDate(LocalDateTime.now());
                entity.setRetryCount(
                        entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1
                );

                mapRepository.save(entity);

                // ============= EXECUTE ==================
                Transferable transferable = transferableMapper.fromManagementFeeMap(entity);
                log.info("ManagementFeeMapService transferableMapper fromManagementFeeMap: {}", transferable);

                NcbsResponse ncbsResponse = orchestratorService.execute(transferable);
                log.info("ManagementFeeMapService orchestratorService execute: {}", ncbsResponse);

                // =========== Set referenceId to entity ===========
                entity.setReferenceId(ncbsResponse.getReferenceId());

                if (ncbsResponse.getResponseCode().equalsIgnoreCase(ApiResponseCode.SUCCESS.getCode())) {
                    entity.setStatus(MappingStatus.SUCCESS);
                } else if (responseCodeService.isInsufficientBalance(ncbsResponse.getResponseCode())) {
                    entity.setStatus(MappingStatus.RETRY);
                } else {
                    entity.setStatus(MappingStatus.FAILED);
                }

                // ============ Save entity to update status ===============
                mapRepository.save(entity);
                result.addSuccess();

            } catch (Exception e) {
                log.error("Send failed id {}", entity.getId(), e);
                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(entity.getId()),
                                List.of("Send failed " + e.getMessage())
                        )
                );
            }
        }
        return result;
    }
}
