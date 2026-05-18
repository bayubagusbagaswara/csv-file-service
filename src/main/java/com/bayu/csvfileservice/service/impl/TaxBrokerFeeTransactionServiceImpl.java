package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferExecutionResult;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.model.TaxBrokerFeeMap;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.repository.TaxBrokerFeeMapRepository;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.service.TaxBrokerFeeTransactionService;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxBrokerFeeTransactionServiceImpl implements TaxBrokerFeeTransactionService {

    private static final String ID = "id";
    private static final String IDS = "ids";

    private final TaxBrokerFeeMapRepository taxBrokerFeeMapRepository;

    private final TransferMethodValidator transferMethodValidator;
    private final TransferableMapper transferableMapper;
    private final TransferOrchestratorService transferOrchestratorService;
    private final ResponseCodeService responseCodeService;

    @Override
    @Transactional
    public ProcessResult create(
            Long id,
            TransferMethod transferMethod,
            String description,
            String userId,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        try {
            validateCreateRequest(id, transferMethod);

            TaxBrokerFeeMap map = taxBrokerFeeMapRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException(
                            "TaxBrokerFeeMap not found with id: " + id
                    ));

            validateCreateCandidate(map);

            transferMethodValidator.validate(
                    map.getTransferScope(),
                    transferMethod
            );

            map.setTransferMethod(transferMethod);

            if (description != null && !description.trim().isEmpty()) {
                map.setDescription(description);
            }

            map.setMappingStatus(MappingStatus.READY);
            map.setApprovalStatus(ApprovalStatus.PENDING);

            map.setInputId(userId);
            map.setInputDate(now);
            map.setInputIpAddress(clientIp);

            map.setApproveId(null);
            map.setApproveDate(null);
            map.setApproveIpAddress(null);

            map.setReferenceId(null);
            map.setInquiryReferenceId(null);
            map.setRetryCount(0);
            map.setLastSentDate(null);

            taxBrokerFeeMapRepository.save(map);

            result.addSuccess();

        } catch (Exception e) {
            log.error(
                    "Failed to create Tax Broker Fee transaction. id={}, transferMethod={}, userId={}",
                    id,
                    transferMethod,
                    userId,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            ID,
                            id != null ? String.valueOf(id) : null,
                            Collections.singletonList(e.getMessage())
                    )
            );
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult send(
            List<Long> ids,
            String userId,
            String clientIp
    ) {
        validateIds(ids);

        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        Map<Long, TaxBrokerFeeMap> dataMap = getTaxBrokerFeeMapByIds(ids);

        for (Long id : ids) {
            processSendSingle(id, dataMap.get(id), userId, clientIp, now, result);
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult reject(
            List<Long> ids,
            String userId,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        if (ids == null || ids.isEmpty()) {
            result.addError(
                    ErrorDetail.of(
                            IDS,
                            null,
                            Collections.singletonList("TaxBrokerFeeMap id list cannot be empty")
                    )
            );
            return result;
        }

        List<TaxBrokerFeeMap> maps =
                taxBrokerFeeMapRepository.findAllById(ids);

        Map<Long, TaxBrokerFeeMap> dataMap =
                new HashMap<>();

        for (TaxBrokerFeeMap map : maps) {
            dataMap.put(map.getId(), map);
        }

        for (Long id : ids) {
            TaxBrokerFeeMap map = dataMap.get(id);

            try {
                if (map == null) {
                    throw new DataNotFoundException(
                            "TaxBrokerFeeMap not found with id: " + id
                    );
                }

                validateRejectCandidate(map);

                map.setApprovalStatus(ApprovalStatus.REJECTED);
                map.setApproveId(userId);
                map.setApproveDate(now);
                map.setApproveIpAddress(clientIp);

                map.setMappingStatus(MappingStatus.DRAFT);

                map.setTransferMethod(null);
                map.setReferenceId(null);
                map.setInquiryReferenceId(null);
                map.setRetryCount(0);
                map.setLastSentDate(null);

                taxBrokerFeeMapRepository.save(map);

                result.addSuccess();

            } catch (Exception e) {
                log.error(
                        "Failed to reject Tax Broker Fee transaction. id={}, userId={}",
                        id,
                        userId,
                        e
                );

                result.addError(
                        ErrorDetail.of(
                                ID,
                                String.valueOf(id),
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return result;
    }

    private void processSendSingle(
            Long id,
            TaxBrokerFeeMap map,
            String userId,
            String clientIp,
            LocalDateTime now,
            ProcessResult result
    ) {
        try {
            validateSendDataFound(id, map);

            validateSendCandidate(map);

            markAsSent(map, now);

            TransferExecutionResult executionResult = executeTransfer(map);

            updateTransactionResult(map, executionResult, userId, clientIp, now);

            result.addSuccess();

        } catch (Exception e) {
            handleSendError(id, map, userId, result, e);
        }
    }

    private void validateSendDataFound(Long id, TaxBrokerFeeMap map) {
        if (map == null) {
            throw new DataNotFoundException(
                    "TaxBrokerFeeMap not found with id: " + id
            );
        }
    }

    private void markAsSent(TaxBrokerFeeMap map, LocalDateTime now) {

        map.setMappingStatus(MappingStatus.SENT);
        map.setLastSentDate(now);

        map.setRetryCount(
                map.getRetryCount() == null
                        ? 1
                        : map.getRetryCount() + 1
        );

        taxBrokerFeeMapRepository.save(map);
    }

    private TransferExecutionResult executeTransfer(TaxBrokerFeeMap map) {

        Transferable transferable =
                transferableMapper.fromTaxBrokerFeeMap(map);

        return transferOrchestratorService.execute(transferable);
    }

    private void updateTransactionResult(
            TaxBrokerFeeMap map,
            TransferExecutionResult executionResult,
            String userId,
            String clientIp,
            LocalDateTime now
    ) {
        map.setInquiryReferenceId(executionResult.getInquiryReferenceId());
        map.setReferenceId(executionResult.getReferenceId());

        applyResponseStatus(map, executionResult);

        map.setApprovalStatus(ApprovalStatus.APPROVED);
        map.setApproveId(userId);
        map.setApproveDate(now);
        map.setApproveIpAddress(clientIp);

        taxBrokerFeeMapRepository.save(map);
    }

    private void handleSendError(
            Long id,
            TaxBrokerFeeMap map,
            String userId,
            ProcessResult result,
            Exception e
    ) {
        log.error(
                "Failed to send Tax Broker Fee transaction. id={}, portfolioCode={}, userId={}",
                id,
                map != null ? map.getPortfolioCode() : null,
                userId,
                e
        );

        if (map != null) {
            map.setMappingStatus(MappingStatus.FAILED);
            taxBrokerFeeMapRepository.save(map);
        }

        result.addError(
                ErrorDetail.of(
                        ID,
                        String.valueOf(id),
                        Collections.singletonList(e.getMessage())
                )
        );
    }

    private Map<Long, TaxBrokerFeeMap> getTaxBrokerFeeMapByIds(List<Long> ids) {

        List<TaxBrokerFeeMap> maps =
                taxBrokerFeeMapRepository.findAllById(ids);

        Map<Long, TaxBrokerFeeMap> dataMap =
                new HashMap<Long, TaxBrokerFeeMap>();

        for (TaxBrokerFeeMap map : maps) {
            dataMap.put(map.getId(), map);
        }

        return dataMap;
    }

    private void validateCreateRequest(
            Long id,
            TransferMethod transferMethod
    ) {
        if (id == null) {
            throw new IllegalArgumentException("TaxBrokerFeeMap id is required");
        }

        if (transferMethod == null) {
            throw new IllegalArgumentException("Transfer method is required");
        }
    }

    private void validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("TaxBrokerFeeMap id list cannot be empty");
        }
    }

    private void validateCreateCandidate(TaxBrokerFeeMap map) {
        if (!MappingStatus.DRAFT.equals(map.getMappingStatus())) {
            throw new IllegalStateException(
                    "Only DRAFT data can create transaction. Current status: " + map.getMappingStatus()
            );
        }

        if (map.getTransferScope() == null) {
            throw new IllegalStateException("Transfer scope is required");
        }
    }

    private void validateSendCandidate(TaxBrokerFeeMap map) {

        boolean canSend = MappingStatus.READY.equals(map.getMappingStatus())
                || MappingStatus.RETRY.equals(map.getMappingStatus());

        if (!canSend) {
            throw new IllegalStateException(
                    "Only READY or RETRY data can be sent. Current status: " + map.getMappingStatus()
            );
        }

        if (!ApprovalStatus.PENDING.equals(map.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Only PENDING transaction can be sent. Current approvalStatus: "
                            + map.getApprovalStatus()
            );
        }

        if (map.getTransferMethod() == null) {
            throw new IllegalStateException("Transfer method is required");
        }

        if (map.getTransferScope() == null) {
            throw new IllegalStateException("Transfer scope is required");
        }
    }

    private void validateRejectCandidate(TaxBrokerFeeMap map) {
        if (!ApprovalStatus.PENDING.equals(map.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Only PENDING transaction can be rejected. Current approvalStatus: "
                            + map.getApprovalStatus()
            );
        }

        if (!MappingStatus.READY.equals(map.getMappingStatus())) {
            throw new IllegalStateException(
                    "Only READY transaction can be rejected. Current status: " + map.getMappingStatus()
            );
        }
    }

    private void applyResponseStatus(
            TaxBrokerFeeMap map,
            TransferExecutionResult executionResult
    ) {
        if (executionResult == null) {
            map.setMappingStatus(MappingStatus.FAILED);
            return;
        }

        String responseCode = executionResult.getResponseCode();

        if (responseCodeService.isSuccess(responseCode)) {
            map.setMappingStatus(MappingStatus.SUCCESS);
            return;
        }

        if (responseCodeService.isInsufficientBalance(responseCode)) {
            map.setMappingStatus(MappingStatus.RETRY);
            return;
        }

        map.setMappingStatus(MappingStatus.FAILED);
    }

}
