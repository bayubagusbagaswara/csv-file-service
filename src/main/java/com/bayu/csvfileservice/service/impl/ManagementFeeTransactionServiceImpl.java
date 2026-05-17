package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferExecutionResult;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.repository.ManagementFeeMapRepository;
import com.bayu.csvfileservice.service.ManagementFeeTransactionService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.TransferMethodValidator;
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
public class ManagementFeeTransactionServiceImpl implements ManagementFeeTransactionService {

    private static final String ID = "id";
    private static final String IDS = "ids";
    private static final String REFERENCE_COMBINATION = "referenceCombination";

    private final ManagementFeeMapRepository managementFeeMapRepository;

    private final TransferMethodValidator transferMethodValidator;
    private final TransferOrchestratorService transferOrchestratorService;
    private final ResponseCodeService responseCodeService;
    private final TransferableMapper transferableMapper;

    @Override
    public ProcessResult create(Long id, TransferMethod transferMethod, String description, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        try {
            // 1. Validasi request create transaction.
            validateCreateRequest(id, transferMethod);

            // 2. Ambil data ManagementFeeMap berdasarkan id.
            ManagementFeeMap map = managementFeeMapRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException(
                            "ManagementFeeMap not found with id: " + id
                    ));

            // 3. Validasi kandidat create transaction.
            // Hanya data DRAFT yang boleh dibuat menjadi transaction.
            validateCreateCandidate(map);

            // 4. Validasi transfer method berdasarkan transfer scope.
            //
            // INTERNAL hanya boleh OVERBOOKING.
            // EXTERNAL hanya boleh BIFAST, SKN, atau RTGS.
            transferMethodValidator.validate(
                    map.getTransferScope(),
                    transferMethod
            );

            // 5. Set transfer method pilihan user.
            map.setTransferMethod(transferMethod);

            // 6. Jika user mengirim description saat create transaction,
            // description ini bisa override description hasil mapping.
            if (description != null && !description.trim().isEmpty()) {
                map.setDescription(description);
            }

            // 7. Set status menjadi READY.
            //
            // READY artinya transaction sudah dibuat dan siap dikirim ke middleware.
            map.setStatus(MappingStatus.READY);

            // 8. Set approval status menjadi PENDING.
            //
            // Create transaction belum berarti transaksi dikirim.
            // Send nanti akan dianggap sebagai approval.
            map.setApprovalStatus(ApprovalStatus.PENDING);

            // 9. Set audit input untuk proses create transaction.
            map.setInputId(userId);
            map.setInputDate(now);
            map.setInputIpAddress(clientIp);

            // 10. Reset approval field.
            //
            // Karena transaksi baru dibuat, belum ada approve/reject.
            map.setApproveId(null);
            map.setApproveDate(null);
            map.setApproveIpAddress(null);

            // 11. Reset execution field.
            //
            // Reference baru akan dibuat saat send.
            map.setReferenceId(null);
            map.setInquiryReferenceId(null);
            map.setRetryCount(0);
            map.setLastSentDate(null);

            // 12. Simpan perubahan.
            managementFeeMapRepository.save(map);

            result.addSuccess();

        } catch (Exception e) {
            log.error(
                    "Failed to create Management Fee transaction. id={}, transferMethod={}, userId={}",
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
    public ProcessResult send(List<Long> ids, String userId, String clientIp) {
        validateIds(ids);

        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        Map<Long, ManagementFeeMap> dataMap = getManagementFeeMapByIds(ids);

        for (Long id : ids) {
            processSendSingle(id, dataMap.get(id), userId, clientIp, now, result);
        }

        return result;
    }

    @Override
    public ProcessResult reject(List<Long> ids, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        if (ids == null || ids.isEmpty()) {
            result.addError(
                    ErrorDetail.of(
                            IDS,
                            null,
                            Collections.singletonList("ManagementFeeMap id list cannot be empty")
                    )
            );
            return result;
        }

        // 1. Ambil semua ManagementFeeMap berdasarkan ids.
        List<ManagementFeeMap> maps = managementFeeMapRepository.findAllById(ids);

        // 2. Convert ke Map agar bisa cek id yang tidak ditemukan.
        Map<Long, ManagementFeeMap> dataMap = new HashMap<>();

        for (ManagementFeeMap map : maps) {
            dataMap.put(map.getId(), map);
        }

        // 3. Loop berdasarkan ids request.
        for (Long id : ids) {

            ManagementFeeMap map = dataMap.get(id);

            try {
                // 4. Validasi data harus ditemukan.
                if (map == null) {
                    throw new DataNotFoundException(
                            "ManagementFeeMap not found with id: " + id
                    );
                }

                // 5. Validasi data boleh direject.
                //
                // Hanya PENDING + READY yang boleh direject.
                // Kalau sudah SENT/SUCCESS/RETRY/FAILED, berarti sudah masuk proses middleware.
                validateRejectCandidate(map);

                // 6. Set approvalStatus menjadi REJECTED.
                map.setApprovalStatus(ApprovalStatus.REJECTED);
                map.setApproveId(userId);
                map.setApproveDate(now);
                map.setApproveIpAddress(clientIp);

                // 7. Balikkan status menjadi DRAFT.
                //
                // Artinya data bisa create transaction ulang.
                map.setStatus(MappingStatus.DRAFT);

                // 8. Reset transaction field.
                map.setTransferMethod(null);
                map.setReferenceId(null);
                map.setInquiryReferenceId(null);
                map.setRetryCount(0);
                map.setLastSentDate(null);

                // 9. Simpan hasil reject.
                managementFeeMapRepository.save(map);

                result.addSuccess();

            } catch (Exception e) {
                log.error(
                        "Failed to reject Management Fee transaction. id={}, userId={}",
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

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateCreateRequest(
            Long id,
            TransferMethod transferMethod
    ) {
        if (id == null) {
            throw new IllegalArgumentException("ManagementFeeMap id is required");
        }

        if (transferMethod == null) {
            throw new IllegalArgumentException("Transfer method is required");
        }
    }

    private void validateCreateCandidate(ManagementFeeMap map) {
        if (map == null) {
            throw new IllegalArgumentException("ManagementFeeMap data is required");
        }

        if (!MappingStatus.DRAFT.equals(map.getStatus())) {
            throw new IllegalStateException(
                    "Only DRAFT data can create transaction. Current status: " + map.getStatus()
            );
        }

        if (map.getTransferScope() == null) {
            throw new IllegalStateException("Transfer scope is required");
        }
    }

    private void validateSendCandidate(ManagementFeeMap map) {
        boolean canSend = MappingStatus.READY.equals(map.getStatus())
                || MappingStatus.RETRY.equals(map.getStatus());

        if (!canSend) {
            throw new IllegalStateException(
                    "Only READY or RETRY data can be sent. Current status: " + map.getStatus()
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

    private void validateRejectCandidate(ManagementFeeMap map) {
        if (!ApprovalStatus.PENDING.equals(map.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Only PENDING transaction can be rejected. Current approvalStatus: "
                            + map.getApprovalStatus()
            );
        }

        if (!MappingStatus.READY.equals(map.getStatus())) {
            throw new IllegalStateException(
                    "Only READY transaction can be rejected. Current status: " + map.getStatus()
            );
        }
    }

    // =========================================================
    // RESPONSE HANDLER
    // =========================================================

    private void validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ManagementFeeMap id list cannot be empty");
        }
    }

    private Map<Long, ManagementFeeMap> getManagementFeeMapByIds(List<Long> ids) {
        List<ManagementFeeMap> maps = managementFeeMapRepository.findAllById(ids);
        Map<Long, ManagementFeeMap> dataMap = new HashMap<>();
        for (ManagementFeeMap map : maps) {
            dataMap.put(map.getId(), map);
        }
        return dataMap;
    }

    private void processSendSingle(
            Long id,
            ManagementFeeMap map,
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

    private void validateSendDataFound(Long id, ManagementFeeMap map) {
        if (map == null) {
            throw new DataNotFoundException(
                    "ManagementFeeMap not found with id: " + id
            );
        }
    }

    private void markAsSent(ManagementFeeMap map, LocalDateTime now) {
        map.setStatus(MappingStatus.SENT);
        map.setLastSentDate(now);
        map.setRetryCount(
                map.getRetryCount() == null
                        ? 1
                        : map.getRetryCount() + 1
        );
        managementFeeMapRepository.save(map);
    }

    private TransferExecutionResult executeTransfer(ManagementFeeMap map) {
        Transferable transferable = transferableMapper.fromManagementFeeMap(map);
        return transferOrchestratorService.execute(transferable);
    }

    private void updateTransactionResult(
            ManagementFeeMap map,
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

        managementFeeMapRepository.save(map);
    }

    private void handleSendError(
            Long id,
            ManagementFeeMap map,
            String userId,
            ProcessResult result,
            Exception e
    ) {
        log.error("Failed to send Management Fee transaction. id={}, fundCode={}, userId={}", id, map != null ? map.getFundCode() : null, userId, e);
        if (map != null) {
            map.setStatus(MappingStatus.FAILED);
            managementFeeMapRepository.save(map);
        }
        result.addError(
                ErrorDetail.of(
                        ID,
                        String.valueOf(id),
                        Collections.singletonList(e.getMessage())
                )
        );
    }

    private void applyResponseStatus(
            ManagementFeeMap map,
            TransferExecutionResult executionResult
    ) {
        if (executionResult == null) {
            map.setStatus(MappingStatus.FAILED);
            return;
        }

        String responseCode = executionResult.getResponseCode();

        if (responseCodeService.isSuccess(responseCode)) {
            map.setStatus(MappingStatus.SUCCESS);
            return;
        }

        if (responseCodeService.isInsufficientBalance(responseCode)) {
            map.setStatus(MappingStatus.RETRY);
            return;
        }

        map.setStatus(MappingStatus.FAILED);
    }

}
